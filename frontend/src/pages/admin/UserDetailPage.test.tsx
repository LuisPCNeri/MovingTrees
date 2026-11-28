// @vitest-environment jsdom
import { afterEach, describe, expect, it, vi } from 'vitest'
import { cleanup, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { apiCalls, inOrder, mockApi } from '../../test/mockApi'
import { UserDetailPage } from './UserDetailPage'

const paulo = { id: 7, name: 'Paulo', username: 'paulo@cm-aveiro.pt', role: 'FIELD_WORKER', status: 'ACTIVE', municipalityId: 1, teamId: 3 }

afterEach(() => {
  cleanup()
  vi.unstubAllGlobals()
})

const saveButton = () => screen.getByRole('button', { name: 'Save changes' }) as HTMLButtonElement

describe('UserDetailPage', () => {
  it('disables save until the role or team changes', async () => {
    mockApi(inOrder([200, paulo]))
    render(<UserDetailPage id="7" />)
    await screen.findByRole('option', { name: 'Cleaning · Botânico' })
    expect(saveButton().disabled).toBe(true)

    await userEvent.selectOptions(screen.getByLabelText('Role'), 'FIELD_TECHNICIAN')
    expect(saveButton().disabled).toBe(false)
    await userEvent.selectOptions(screen.getByLabelText('Role'), 'FIELD_WORKER')
    expect(saveButton().disabled).toBe(true)

    await userEvent.selectOptions(screen.getByLabelText('Team'), '4')
    expect(saveButton().disabled).toBe(false)
  })

  it('sends the new role and team and shows the saved state', async () => {
    const fetch = mockApi(inOrder([200, paulo], [200, { ...paulo, role: 'FIELD_TECHNICIAN', teamId: 4 }]))
    render(<UserDetailPage id="7" />)
    await screen.findByRole('option', { name: 'Cleaning · Botânico' })
    await userEvent.selectOptions(screen.getByLabelText('Role'), 'FIELD_TECHNICIAN')
    await userEvent.selectOptions(screen.getByLabelText('Team'), '4')
    await userEvent.click(saveButton())

    expect((await screen.findByRole('status')).textContent).toContain('Changes saved')
    expect(apiCalls(fetch).at(-1)).toEqual([
      '/api/users/7',
      expect.objectContaining({ method: 'PUT', body: JSON.stringify({ role: 'FIELD_TECHNICIAN', teamId: 4 }) }),
    ])
    expect(saveButton().disabled).toBe(true)
  })

  it("shows not found for another municipality's user", async () => {
    mockApi(inOrder([404, { message: 'User not found', fieldErrors: {} }]))
    render(<UserDetailPage id="99" />)
    expect((await screen.findByRole('heading', { level: 1 })).textContent).toContain('User not found')
  })

  it('asks for confirmation before deactivating', async () => {
    const fetch = mockApi(inOrder([200, paulo], [200, { ...paulo, status: 'INACTIVE' }]))
    render(<UserDetailPage id="7" />)

    await userEvent.click(await screen.findByRole('button', { name: 'Deactivate access' }))
    expect(screen.getByRole('alertdialog').textContent).toContain('Deactivate Paulo?')
    expect(apiCalls(fetch)).toHaveLength(1) // only the initial GET

    await userEvent.click(screen.getByRole('button', { name: 'Cancel' }))
    expect(screen.queryByRole('alertdialog')).toBeNull()
    expect(apiCalls(fetch)).toHaveLength(1)

    await userEvent.click(screen.getByRole('button', { name: 'Deactivate access' }))
    await userEvent.click(screen.getByRole('button', { name: 'Yes, deactivate' }))

    expect((await screen.findByRole('status')).textContent).toBe('Access deactivated.')
    expect(apiCalls(fetch).at(-1)).toEqual([
      '/api/users/7/status',
      expect.objectContaining({ method: 'PATCH', body: '{"status":"INACTIVE"}' }),
    ])
    expect(screen.getByRole('button', { name: 'Activate access' })).toBeTruthy()
  })

  it('activates without asking for confirmation', async () => {
    const fetch = mockApi(inOrder([200, { ...paulo, status: 'INACTIVE' }], [200, paulo]))
    render(<UserDetailPage id="7" />)

    await userEvent.click(await screen.findByRole('button', { name: 'Activate access' }))

    expect((await screen.findByRole('status')).textContent).toBe('Access activated. Paulo can now sign in.')
    expect(apiCalls(fetch).at(-1)).toEqual([
      '/api/users/7/status',
      expect.objectContaining({ method: 'PATCH', body: '{"status":"ACTIVE"}' }),
    ])
  })

  it('keeps the status and shows the error when the change fails', async () => {
    mockApi(inOrder([200, { ...paulo, status: 'INACTIVE' }], [404, { message: 'User not found', fieldErrors: {} }]))
    render(<UserDetailPage id="7" />)

    await userEvent.click(await screen.findByRole('button', { name: 'Activate access' }))

    expect((await screen.findByRole('alert')).textContent).toBe('User not found')
    expect(screen.getByRole('button', { name: 'Activate access' })).toBeTruthy()
  })
})
