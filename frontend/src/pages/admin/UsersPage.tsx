import { useEffect, useState } from 'react'
import { AdminLayout } from '../../components/AdminLayout'
import { listUsers, ROLE_LABELS, ROLES, type Page, type Role, type User } from '../../api/users'

const PAGE_SIZE = 20

export function UsersPage() {
  const [name, setName] = useState('')
  const [role, setRole] = useState<Role | ''>('')
  const [page, setPage] = useState(0)
  const [data, setData] = useState<Page<User> | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [counts, setCounts] = useState<{ active: number; inactive: number } | null>(null)

  useEffect(() => {
    const ctrl = new AbortController()
    Promise.all([
      listUsers({ status: 'ACTIVE', size: 1 }, ctrl.signal),
      listUsers({ status: 'INACTIVE', size: 1 }, ctrl.signal),
    ])
      .then(([a, i]) => setCounts({ active: a.totalElements, inactive: i.totalElements }))
      .catch(() => {}) // the table below already surfaces the error
    return () => ctrl.abort()
  }, [])

  useEffect(() => {
    const ctrl = new AbortController()
    // debounce so typing in the search box doesn't fire one request per key
    const t = setTimeout(() => {
      listUsers({ page, size: PAGE_SIZE, name: name.trim(), role: role || undefined }, ctrl.signal)
        .then((res) => {
          setData(res)
          setError(null)
        })
        .catch((e: Error) => e.name !== 'AbortError' && setError(e.message))
    }, 300)
    return () => {
      clearTimeout(t)
      ctrl.abort()
    }
  }, [page, name, role])

  const first = data && data.totalElements > 0 ? data.page * data.size + 1 : 0
  const last = data ? data.page * data.size + data.content.length : 0

  return (
    <AdminLayout active="Users" breadcrumb="Municipality / Users">
      <h1 className="text-3xl font-bold">People who move the city</h1>
      <p className="mt-2 text-sm text-slate-600">Manage access, roles and teams for municipal operations.</p>

      <div className="mt-6 grid gap-4 sm:grid-cols-2">
        <StatCard label="Active users" value={counts?.active} />
        <StatCard label="Awaiting activation" value={counts?.inactive} />
      </div>

      <div className="mt-6 grid gap-4 sm:grid-cols-2">
        <label className="text-sm">
          Search people
          <input
            type="search"
            value={name}
            onChange={(e) => {
              setName(e.target.value)
              setPage(0)
            }}
            placeholder="Name…"
            className="mt-1 block w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
          />
        </label>
        <label className="text-sm">
          Role
          <select
            value={role}
            onChange={(e) => {
              setRole(e.target.value as Role | '')
              setPage(0)
            }}
            className="mt-1 block w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
          >
            <option value="">All roles</option>
            {ROLES.map((r) => (
              <option key={r} value={r}>
                {ROLE_LABELS[r]}
              </option>
            ))}
          </select>
        </label>
      </div>

      <section className="mt-6 rounded-xl border border-slate-200 bg-white">
        <div className="flex items-center justify-between px-5 py-4">
          <h2 className="text-lg font-semibold">Municipal users</h2>
          {data && <span className="text-xs text-slate-500">{data.totalElements} records</span>}
        </div>

        {error ? (
          <p role="alert" className="px-5 pb-5 text-sm text-red-700">
            {error}
          </p>
        ) : !data ? (
          <p className="px-5 pb-5 text-sm text-slate-500">Loading…</p>
        ) : data.content.length === 0 ? (
          <p className="px-5 pb-5 text-sm text-slate-500">No users found.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50 text-xs text-slate-600">
                <tr>
                  <th className="px-5 py-3 font-medium">Name</th>
                  <th className="px-5 py-3 font-medium">Username</th>
                  <th className="px-5 py-3 font-medium">Role</th>
                  <th className="px-5 py-3 font-medium">Team</th>
                  <th className="px-5 py-3 font-medium">Access</th>
                </tr>
              </thead>
              <tbody>
                {data.content.map((u) => (
                  <tr key={u.id} className="border-t border-slate-100 text-slate-700">
                    <td className="px-5 py-3">{u.name}</td>
                    <td className="px-5 py-3 break-all">{u.username}</td>
                    <td className="px-5 py-3">{ROLE_LABELS[u.role] ?? u.role}</td>
                    {/* shortcut: backend only exposes teamId, show the team name once a teams endpoint exists */}
                    <td className="px-5 py-3">Team #{u.teamId}</td>
                    <td className="px-5 py-3">
                      <span
                        className={`rounded-md px-2 py-1 text-xs ${
                          u.status === 'ACTIVE' ? 'bg-green-50 text-green-800' : 'bg-amber-50 text-amber-800'
                        }`}
                      >
                        {u.status === 'ACTIVE' ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {data && data.totalElements > 0 && (
          <div className="flex items-center justify-between px-5 py-4 text-xs text-slate-600">
            <span>
              Showing {first}–{last} of {data.totalElements}
            </span>
            <div className="flex items-center gap-3">
              <button type="button" aria-label="Previous page" disabled={page === 0} onClick={() => setPage(page - 1)} className="disabled:opacity-30">
                ‹
              </button>
              <span>{page + 1}</span>
              <button
                type="button"
                aria-label="Next page"
                disabled={page + 1 >= data.totalPages}
                onClick={() => setPage(page + 1)}
                className="disabled:opacity-30"
              >
                ›
              </button>
            </div>
          </div>
        )}
      </section>
    </AdminLayout>
  )
}

function StatCard({ label, value }: { label: string; value: number | undefined }) {
  return (
    <div className="rounded-xl border border-slate-200 bg-white p-5">
      <p className="text-sm text-slate-600">{label}</p>
      <p className="mt-3 text-3xl font-semibold">{value ?? '—'}</p>
    </div>
  )
}
