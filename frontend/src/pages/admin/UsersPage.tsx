import { useEffect, useState, type ReactNode } from 'react'
import { CalendarClock, Check, ChevronDown, ChevronLeft, ChevronRight, UserRoundCheck, type LucideIcon } from 'lucide-react'
import { AdminLayout } from '../../components/AdminLayout'
import { listUsers, ROLE_LABELS, ROLES, type Page, type Role, type Status, type User } from '../../api/users'

const PAGE_SIZE = 20
const STATS: { status: Status; label: string; icon: LucideIcon }[] = [
  { status: 'ACTIVE', label: 'Active users', icon: UserRoundCheck },
  { status: 'INACTIVE', label: 'Awaiting activation', icon: CalendarClock },
]
const COLUMNS = ['Name', 'Username', 'Role', 'Team', 'Access']
const FIELD =
  'h-11 w-full rounded-lg border border-slate-300 bg-white text-sm font-normal text-slate-900 outline-none focus:border-brand focus:ring-2 focus:ring-brand/20'
const CELL = 'px-5 py-3.5'

interface Filters {
  name: string
  role: Role | ''
  status: Status | ''
}

export function UsersPage() {
  const [filters, setFilters] = useState<Filters>({ name: '', role: '', status: '' })
  const [page, setPage] = useState(0)
  const [data, setData] = useState<Page<User> | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [counts, setCounts] = useState<Record<Status, number> | null>(null)

  // any filter change goes back to the first page
  const updateFilters = (change: Partial<Filters>) => {
    setFilters({ ...filters, ...change })
    setPage(0)
  }

  useEffect(() => {
    const ctrl = new AbortController()
    Promise.all(STATS.map((s) => listUsers({ status: s.status, size: 1 }, ctrl.signal)))
      .then(([active, inactive]) => setCounts({ ACTIVE: active.totalElements, INACTIVE: inactive.totalElements }))
      .catch(() => {}) // the table below already surfaces the error
    return () => ctrl.abort()
  }, [])

  useEffect(() => {
    const ctrl = new AbortController()
    // debounce so typing in the search box doesn't fire one request per key
    const t = setTimeout(() => {
      const { name, role, status } = filters
      listUsers({ page, size: PAGE_SIZE, name: name.trim(), role: role || undefined, status: status || undefined }, ctrl.signal)
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
  }, [page, filters])

  return (
    <AdminLayout active="Users" breadcrumb="Municipality / Users">
      <h1 className="text-3xl font-bold tracking-tight">People who move the city</h1>
      <p className="mt-3 text-sm text-slate-600">Manage access, roles and teams for municipal operations.</p>

      <div className="mt-6 grid gap-4 sm:grid-cols-2">
        {STATS.map(({ status, label, icon: Icon }) => {
          const pressed = filters.status === status
          return (
            <button
              key={status}
              type="button"
              aria-pressed={pressed}
              onClick={() => updateFilters({ status: pressed ? '' : status })}
              className={`rounded-xl border bg-white p-5 text-left shadow-sm transition-colors hover:border-brand/50 ${
                pressed ? 'border-brand ring-2 ring-brand/15' : 'border-slate-200'
              }`}
            >
              <span className="flex items-center justify-between text-xs text-slate-700">
                {label}
                <Icon className="size-4 text-brand" aria-hidden />
              </span>
              <span className="mt-4 flex items-end justify-between">
                <span className="text-3xl font-semibold">{counts?.[status] ?? '—'}</span>
                <span className="mb-2 flex items-center text-brand" aria-hidden>
                  <span className="h-px w-12 bg-current" />
                  <ChevronRight className="-ml-2 size-3" strokeWidth={1.5} />
                </span>
              </span>
            </button>
          )
        })}
      </div>

      <div className="mt-5 grid gap-4 sm:grid-cols-[2fr_1fr]">
        <label className="text-xs font-medium text-slate-700">
          Search people
          <input
            type="search"
            value={filters.name}
            onChange={(e) => updateFilters({ name: e.target.value })}
            placeholder="Name…"
            className={`mt-1.5 block px-3 ${FIELD}`}
          />
        </label>
        <label className="text-xs font-medium text-slate-700">
          Role
          <span className="relative mt-1.5 block">
            <select
              value={filters.role}
              onChange={(e) => updateFilters({ role: e.target.value as Role | '' })}
              className={`appearance-none pr-9 pl-3 ${FIELD}`}
            >
              <option value="">All roles</option>
              {ROLES.map((r) => (
                <option key={r} value={r}>
                  {ROLE_LABELS[r]}
                </option>
              ))}
            </select>
            <ChevronDown className="pointer-events-none absolute top-1/2 right-3 size-4 -translate-y-1/2 text-slate-500" aria-hidden />
          </span>
        </label>
      </div>

      <section className="mt-7 rounded-xl border border-slate-200 bg-white shadow-sm">
        <div className="flex items-center justify-between px-5 py-5">
          <h2 className="text-lg font-semibold">Municipal users</h2>
          {data && <span className="text-xs text-slate-500">{data.totalElements} records</span>}
        </div>

        {error ? (
          <Message alert>{error}</Message>
        ) : !data ? (
          <Message>Loading…</Message>
        ) : data.content.length === 0 ? (
          <Message>No users found.</Message>
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead className="bg-slate-50 text-slate-600">
                  <tr>
                    {COLUMNS.map((c) => (
                      <th key={c} className={`${CELL} font-medium`}>
                        {c}
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody className="text-slate-700">
                  {data.content.map((u) => (
                    <tr key={u.id} className="even:bg-[#f7f9f6]">
                      <td className={CELL}>{u.name}</td>
                      <td className={`${CELL} break-all`}>{u.username}</td>
                      <td className={CELL}>{ROLE_LABELS[u.role] ?? u.role}</td>
                      {/* shortcut: backend only exposes teamId, show the team name once a teams endpoint exists */}
                      <td className={CELL}>Team #{u.teamId}</td>
                      <td className={CELL}>
                        <StatusBadge status={u.status} />
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <div className="flex items-center justify-between px-5 py-5 text-xs text-slate-600">
              <span>
                Showing {data.page * data.size + 1}–{data.page * data.size + data.content.length} of {data.totalElements}
              </span>
              <div className="flex items-center gap-2">
                <PageButton label="Previous page" icon={ChevronLeft} disabled={page === 0} onClick={() => setPage(page - 1)} />
                <span aria-current="page">{page + 1}</span>
                <PageButton label="Next page" icon={ChevronRight} disabled={page + 1 >= data.totalPages} onClick={() => setPage(page + 1)} />
              </div>
            </div>
          </>
        )}
      </section>
    </AdminLayout>
  )
}

function Message({ alert, children }: { alert?: boolean; children: ReactNode }) {
  return (
    <p role={alert ? 'alert' : undefined} className={`px-5 pb-6 text-sm ${alert ? 'text-red-700' : 'text-slate-500'}`}>
      {children}
    </p>
  )
}

function PageButton(props: { label: string; icon: LucideIcon; disabled: boolean; onClick: () => void }) {
  const Icon = props.icon
  return (
    <button
      type="button"
      aria-label={props.label}
      disabled={props.disabled}
      onClick={props.onClick}
      className="rounded p-1 hover:bg-slate-100 disabled:opacity-30 disabled:hover:bg-transparent"
    >
      <Icon className="size-3.5" />
    </button>
  )
}

function StatusBadge({ status }: { status: Status }) {
  const active = status === 'ACTIVE'
  const Icon = active ? Check : CalendarClock
  return (
    <span
      className={`inline-flex items-center gap-1.5 rounded-md px-2 py-1 ${
        active ? 'bg-green-50 text-green-800' : 'bg-amber-50 text-amber-800 ring-1 ring-amber-200'
      }`}
    >
      <Icon className="size-3" aria-hidden />
      {active ? 'Active' : 'Inactive'}
    </span>
  )
}
