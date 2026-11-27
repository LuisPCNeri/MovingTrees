import { vi } from 'vitest'

export const TEAMS = [
  { id: 3, name: 'Pruning · Central' },
  { id: 4, name: 'Cleaning · Botânico' },
]

type Reply = [status: number, body: unknown]
type Respond = (url: string, init?: RequestInit) => Reply | Promise<Response>

/** Stubs fetch: GET /api/teams always answers TEAMS, every other call goes to `respond`. */
export function mockApi(respond: Respond) {
  const fetch = vi.fn((url: string, init?: RequestInit) => {
    if (url === '/api/teams') return Promise.resolve(Response.json(TEAMS))
    const reply = respond(url, init)
    return Array.isArray(reply) ? Promise.resolve(Response.json(reply[1], { status: reply[0] })) : reply
  })
  vi.stubGlobal('fetch', fetch)
  return fetch
}

/** Answers calls with these replies, in order. */
export const inOrder = (...replies: Reply[]): Respond => {
  let next = 0
  return () => replies[next++]
}

/** The fetch calls the test cares about, without the teams lookup. */
export const apiCalls = (fetch: ReturnType<typeof mockApi>) => fetch.mock.calls.filter(([url]) => url !== '/api/teams')
