// @vitest-environment jsdom
import { afterEach, describe, expect, it, vi } from 'vitest'
import { cleanup, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { UserDetailPage } from './UserDetailPage'

const paulo = { id: 7, name: 'Paulo', username: 'paulo@cm-aveiro.pt', role: 'FIELD_WORKER', status: 'ACTIVE', municipalityId: 1, teamId: 3 }

function mockFetch(...responses: [number, unknown][]) {
  const fn = vi.fn()
  for (const [status, body] of responses) fn.mockResolvedValueOnce(new Response(JSON.stringify(body), { status }))
  vi.stubGlobal('fetch', fn)
  return fn
}

afterEach(() => {
  cleanup()
  vi.unstubAllGlobals()
})

describe('UserDetailPage', () => {
  it('disables save until the role or team changes', async () => {
    mockFetch([200, paulo])
    render(<UserDetailPage id="7" />)
    const save = await screen.findByRole('button', { name: 'Save changes' })
    expect((save as HTMLButtonElement).disabled).toBe(true)

    await userEvent.selectOptions(screen.getByLabelText('Role'), 'FIELD_TECHNICIAN')
    expect((save as HTMLButtonElement).disabled).toBe(false)

    await userEvent.selectOptions(screen.getByLabelText('Role'), 'FIELD_WORKER')
    expect((save as HTMLButtonElement).disabled).toBe(true)
  })

  it('sends the new role and shows the saved state', async () => {
    const fetch = mockFetch([200, paulo], [200, { ...paulo, role: 'FIELD_TECHNICIAN' }])
    render(<UserDetailPage id="7" />)
    await userEvent.selectOptions(await screen.findByLabelText('Role'), 'FIELD_TECHNICIAN')
    await userEvent.click(screen.getByRole('button', { name: 'Save changes' }))

    expect((await screen.findByRole('status')).textContent).toContain('Changes saved')
    expect(fetch).toHaveBeenLastCalledWith('/api/users/7', expect.objectContaining({ method: 'PUT', body: JSON.stringify({ role: 'FIELD_TECHNICIAN', teamId: 3 }) }))
    expect((screen.getByRole('button', { name: 'Save changes' }) as HTMLButtonElement).disabled).toBe(true)
  })

  it("shows not found for another municipality's user", async () => {
    mockFetch([404, { message: 'User not found', fieldErrors: {} }])
    render(<UserDetailPage id="99" />)
    expect((await screen.findByRole('heading', { level: 1 })).textContent).toContain('User not found')
  })

  it('asks for confirmation before deactivating', async () => {
    const fetch = mockFetch([200, paulo], [200, { ...paulo, status: 'INACTIVE' }])
    render(<UserDetailPage id="7" />)

    await userEvent.click(await screen.findByRole('button', { name: 'Deactivate access' }))
    expect(screen.getByRole('alertdialog').textContent).toContain('Deactivate Paulo?')
    expect(fetch).toHaveBeenCalledTimes(1) // only the initial GET

    await userEvent.click(screen.getByRole('button', { name: 'Cancel' }))
    expect(screen.queryByRole('alertdialog')).toBeNull()
    expect(fetch).toHaveBeenCalledTimes(1)

    await userEvent.click(screen.getByRole('button', { name: 'Deactivate access' }))
    await userEvent.click(screen.getByRole('button', { name: 'Yes, deactivate' }))

    expect((await screen.findByRole('status')).textContent).toBe('Access deactivated.')
    expect(fetch).toHaveBeenLastCalledWith('/api/users/7/status', expect.objectContaining({ method: 'PATCH', body: '{"status":"INACTIVE"}' }))
    expect(screen.getByRole('button', { name: 'Activate access' })).toBeTruthy()
  })

  it('activates without asking for confirmation', async () => {
    const fetch = mockFetch([200, { ...paulo, status: 'INACTIVE' }], [200, paulo])
    render(<UserDetailPage id="7" />)

    await userEvent.click(await screen.findByRole('button', { name: 'Activate access' }))

    expect((await screen.findByRole('status')).textContent).toBe('Access activated. Paulo can now sign in.')
    expect(fetch).toHaveBeenLastCalledWith('/api/users/7/status', expect.objectContaining({ method: 'PATCH', body: '{"status":"ACTIVE"}' }))
  })

  it('keeps the status and shows the error when the change fails', async () => {
    mockFetch([200, { ...paulo, status: 'INACTIVE' }], [404, { message: 'User not found', fieldErrors: {} }])
    render(<UserDetailPage id="7" />)

    await userEvent.click(await screen.findByRole('button', { name: 'Activate access' }))

    expect((await screen.findByRole('alert')).textContent).toBe('User not found')
    expect(screen.getByRole('button', { name: 'Activate access' })).toBeTruthy()
  })
})
