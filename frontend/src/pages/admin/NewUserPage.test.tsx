// @vitest-environment jsdom
import { afterEach, describe, expect, it, vi } from 'vitest'
import { cleanup, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { NewUserPage } from './NewUserPage'

afterEach(() => {
  cleanup()
  vi.unstubAllGlobals()
})

async function fillForm({ password = 'password123' } = {}) {
  await userEvent.type(screen.getByLabelText(/Full name/), 'Paulo Reis')
  await userEvent.type(screen.getByLabelText(/Work email/), 'paulo@cm-aveiro.pt')
  await userEvent.selectOptions(screen.getByLabelText(/Role/), 'FIELD_WORKER')
  await userEvent.type(screen.getByLabelText(/Team ID/), '3')
  await userEvent.type(screen.getByLabelText(/Temporary password/), password)
}

describe('NewUserPage', () => {
  it('requires every field and does not submit an empty form', async () => {
    const fetch = vi.fn()
    vi.stubGlobal('fetch', fetch)
    render(<NewUserPage />)

    for (const label of [/Full name/, /Work email/, /Role/, /Team ID/, /Temporary password/]) {
      expect((screen.getByLabelText(label) as HTMLInputElement).required).toBe(true)
    }
    await userEvent.click(screen.getByRole('button', { name: 'Create access' }))
    expect(fetch).not.toHaveBeenCalled()
  })

  it('rejects a password shorter than 8 characters and caps it at 72', async () => {
    const fetch = vi.fn()
    vi.stubGlobal('fetch', fetch)
    render(<NewUserPage />)
    expect((screen.getByLabelText(/Temporary password/) as HTMLInputElement).maxLength).toBe(72)

    await fillForm({ password: 'short' })
    await userEvent.click(screen.getByRole('button', { name: 'Create access' }))

    expect(screen.getByLabelText(/Temporary password/).closest('label')!.textContent).toContain('Use 8 to 72 characters.')
    expect(fetch).not.toHaveBeenCalled()
  })

  it('shows a 409 from the server on the work email field', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(() => Promise.resolve(Response.json({ message: 'Username already exists: paulo@cm-aveiro.pt', fieldErrors: {} }, { status: 409 }))),
    )
    render(<NewUserPage />)
    await fillForm()
    await userEvent.click(screen.getByRole('button', { name: 'Create access' }))

    const email = screen.getByLabelText(/Work email/)
    expect((await screen.findByText('This email already has access.')).closest('label')).toBe(email.closest('label'))
    expect(screen.queryByRole('alert')).toBeNull()
    expect((screen.getByRole('button', { name: 'Create access' }) as HTMLButtonElement).disabled).toBe(false)
  })
})
