// @vitest-environment jsdom
import { afterEach, describe, expect, it, vi } from 'vitest'
import { cleanup, render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { apiCalls, inOrder, mockApi } from '../../test/mockApi'
import { NewUserPage } from './NewUserPage'

afterEach(() => {
  cleanup()
  vi.unstubAllGlobals()
})

const submit = () => userEvent.click(screen.getByRole('button', { name: 'Create access' }))

async function fillForm({ password = 'password123' } = {}) {
  await userEvent.type(screen.getByLabelText(/Full name/), 'Paulo Reis')
  await userEvent.type(screen.getByLabelText(/Work email/), 'paulo@cm-aveiro.pt')
  await userEvent.selectOptions(screen.getByLabelText(/Role/), 'FIELD_WORKER')
  await screen.findByRole('option', { name: 'Pruning · Central' })
  await userEvent.selectOptions(screen.getByLabelText(/Team/), '3')
  await userEvent.type(screen.getByLabelText(/Temporary password/), password)
}

describe('NewUserPage', () => {
  it('loads the team options from the API', async () => {
    mockApi(inOrder())
    render(<NewUserPage />)

    const team = screen.getByLabelText(/Team/)
    await within(team).findByRole('option', { name: 'Pruning · Central' })
    expect(within(team).getAllByRole('option').map((o) => [o.getAttribute('value'), o.textContent])).toEqual([
      ['', 'Select a team'],
      ['3', 'Pruning · Central'],
      ['4', 'Cleaning · Botânico'],
    ])
  })

  it('requires every field and does not submit an empty form', async () => {
    const fetch = mockApi(inOrder())
    render(<NewUserPage />)

    for (const label of [/Full name/, /Work email/, /Role/, /Team/, /Temporary password/]) {
      expect((screen.getByLabelText(label) as HTMLInputElement).required).toBe(true)
    }
    await submit()
    expect(apiCalls(fetch)).toHaveLength(0)
  })

  it('rejects a password shorter than 8 characters and caps it at 72', async () => {
    const fetch = mockApi(inOrder())
    render(<NewUserPage />)
    expect((screen.getByLabelText(/Temporary password/) as HTMLInputElement).maxLength).toBe(72)

    await fillForm({ password: 'short' })
    await submit()

    expect(screen.getByLabelText(/Temporary password/).closest('label')!.textContent).toContain('Use 8 to 72 characters.')
    expect(apiCalls(fetch)).toHaveLength(0)
  })

  it('shows a 409 from the server on the work email field', async () => {
    mockApi(inOrder([409, { message: 'Username already exists: paulo@cm-aveiro.pt', fieldErrors: {} }]))
    render(<NewUserPage />)
    await fillForm()
    await submit()

    const email = screen.getByLabelText(/Work email/)
    expect((await screen.findByText('This email already has access.')).closest('label')).toBe(email.closest('label'))
    expect(screen.queryByRole('alert')).toBeNull()
    expect((screen.getByRole('button', { name: 'Create access' }) as HTMLButtonElement).disabled).toBe(false)
  })
})
