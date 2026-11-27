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
})
