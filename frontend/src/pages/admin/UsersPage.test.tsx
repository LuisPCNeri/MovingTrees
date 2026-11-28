// @vitest-environment jsdom
import { afterEach, describe, expect, it, vi } from 'vitest'
import { cleanup, render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { mockApi } from '../../test/mockApi'
import { UsersPage } from './UsersPage'

const ana = { id: 1, name: 'Ana Silva', username: 'ana@cm-aveiro.pt', role: 'FIELD_WORKER', status: 'ACTIVE', municipalityId: 1, teamId: 3 }
const page = (content: unknown[]) => ({ content, page: 0, size: 20, totalElements: content.length, totalPages: 1 })

// Stats cards ask with size=1; the table asks with size=20, which is the request these tests care about.
const mockFetch = (tableResponse: () => Promise<Response>) =>
  mockApi((url) => (url.includes('size=20') ? tableResponse() : [200, page([])]))
const tableUrls = (fn: ReturnType<typeof mockFetch>) =>
  fn.mock.calls.map(([url]) => url).filter((url) => url.includes('size=20'))

afterEach(() => {
  cleanup()
  vi.unstubAllGlobals()
})

describe('UsersPage', () => {
  it('shows a loading state until the users arrive', () => {
    mockFetch(() => new Promise(() => {}))
    render(<UsersPage />)
    expect(screen.getByText('Loading…')).toBeTruthy()
  })

  it('renders the user columns and a row per user', async () => {
    mockFetch(() => Promise.resolve(Response.json(page([ana]))))
    render(<UsersPage />)
    const table = await screen.findByRole('table')
    await within(table).findByText('Pruning · Central')
    expect(within(table).getAllByRole('columnheader').map((th) => th.textContent)).toEqual([
      'Name',
      'Username',
      'Role',
      'Team',
      'Access',
      'Action',
    ])
    const cells = within(table).getAllByRole('cell').map((td) => td.textContent)
    expect(cells).toEqual(['Ana Silva', 'ana@cm-aveiro.pt', 'Field agent', 'Pruning · Central', 'Active', 'Edit'])
    expect(within(table).getByRole('link', { name: 'Edit Ana Silva' }).getAttribute('href')).toBe('/admin/users/1')
  })

  it('maps every filter to its query parameter', async () => {
    const fetch = mockFetch(() => Promise.resolve(Response.json(page([ana]))))
    render(<UsersPage />)
    await screen.findByRole('table')

    await userEvent.type(screen.getByLabelText('Search people'), 'Ana')
    await userEvent.selectOptions(screen.getByLabelText('Role'), 'FIELD_TECHNICIAN')
    await screen.findByRole('option', { name: 'Cleaning · Botânico' })
    await userEvent.selectOptions(screen.getByLabelText('Team'), '4')
    await userEvent.click(screen.getByRole('button', { name: /Awaiting activation/ }))

    await waitFor(() => {
      const params = new URL(tableUrls(fetch).at(-1)!, 'http://x').searchParams
      expect(Object.fromEntries(params)).toEqual({
        page: '0',
        size: '20',
        name: 'Ana',
        role: 'FIELD_TECHNICIAN',
        status: 'INACTIVE',
        teamId: '4',
      })
    })
  })

  it('shows an empty state when no user matches', async () => {
    mockFetch(() => Promise.resolve(Response.json(page([]))))
    render(<UsersPage />)
    expect(await screen.findByText('No users found.')).toBeTruthy()
    expect(screen.queryByRole('table')).toBeNull()
  })

  it('shows the backend error message', async () => {
    mockFetch(() => Promise.resolve(Response.json({ message: 'size must be between 1 and 100', fieldErrors: {} }, { status: 400 })))
    render(<UsersPage />)
    expect((await screen.findByRole('alert')).textContent).toBe('size must be between 1 and 100')
  })
})
