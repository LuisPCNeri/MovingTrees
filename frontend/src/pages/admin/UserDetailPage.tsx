import { useEffect, useState, type FormEvent } from 'react'
import { ChevronDown } from 'lucide-react'
import { AdminLayout } from '../../components/AdminLayout'
import { StatusBadge } from '../../components/StatusBadge'
import { FIELD } from '../../components/styles'
import { ApiError, getUser, ROLE_LABELS, ROLES, updateUser, type Role, type User } from '../../api/users'

export function UserDetailPage({ id }: { id: string }) {
  const [user, setUser] = useState<User | null>(null)
  const [loadError, setLoadError] = useState<ApiError | null>(null)
  const [role, setRole] = useState<Role | ''>('')
  const [teamId, setTeamId] = useState('')
  const [saving, setSaving] = useState(false)
  const [saveError, setSaveError] = useState<ApiError | null>(null)
  const [saved, setSaved] = useState(false)

  const load = (u: User) => {
    setUser(u)
    setRole(u.role)
    setTeamId(String(u.teamId))
  }

  useEffect(() => {
    const ctrl = new AbortController()
    getUser(id, ctrl.signal)
      .then(load)
      .catch((e: Error) => e.name !== 'AbortError' && setLoadError(e instanceof ApiError ? e : new ApiError(e.message)))
    return () => ctrl.abort()
  }, [id])

  const team = Number(teamId)
  const unchanged = !!user && role === user.role && team === user.teamId
  const invalid = !role || !Number.isInteger(team) || team < 1

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    if (!user || unchanged || invalid) return
    setSaving(true)
    setSaveError(null)
    setSaved(false)
    try {
      load(await updateUser(user.id, { role, teamId: team }))
      setSaved(true)
    } catch (err) {
      setSaveError(err instanceof ApiError ? err : new ApiError('Could not reach the server. Try again.'))
    } finally {
      setSaving(false)
    }
  }

  // 400 = id is not a number, 404 = missing or from another municipality
  const notFound = loadError?.status === 404 || loadError?.status === 400
  const title = user?.name ?? (notFound ? 'User not found' : 'User')

  return (
    <AdminLayout active="Users" breadcrumb="Municipality / Users">
      <h1 className="text-3xl font-bold tracking-tight break-words">{title}</h1>
      <nav aria-label="Breadcrumb" className="mt-3 text-sm text-slate-600">
        <a href="/admin/users" className="hover:text-brand hover:underline">
          Users
        </a>
        <span aria-hidden> / </span>
        <span aria-current="page">{user?.name ?? id}</span>
      </nav>

      {loadError ? (
        <div role="alert" className="mt-6 max-w-2xl rounded-xl border border-slate-200 bg-white p-6 text-sm shadow-sm">
          <p className="text-slate-700">
            {notFound ? 'This user does not exist or belongs to another municipality.' : loadError.message}
          </p>
          <a href="/admin/users" className="mt-4 inline-block font-semibold text-brand hover:underline">
            Back to users
          </a>
        </div>
      ) : !user ? (
        <p className="mt-6 text-sm text-slate-500">Loading…</p>
      ) : (
        <form onSubmit={onSubmit} className="mt-6 max-w-2xl rounded-xl border border-slate-200 bg-white p-4 shadow-sm sm:p-6">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <h2 className="text-lg font-semibold">Role and team</h2>
            <StatusBadge status={user.status} />
          </div>
          <p className="mt-2 text-sm break-all text-slate-600">{user.username}</p>

          {saveError && (
            <p role="alert" className="mt-4 rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-800">
              {saveError.message}
            </p>
          )}
          {saved && (
            <p role="status" className="mt-4 rounded-lg border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-800">
              Changes saved. They apply at the user's next sign-in.
            </p>
          )}

          <div className="mt-5 grid gap-x-4 gap-y-5 sm:grid-cols-2">
            <label className="text-xs font-medium text-slate-700">
              <span className="mb-1.5 block">Role</span>
              <span className="relative block">
                <select
                  value={role}
                  onChange={(e) => {
                    setRole(e.target.value as Role)
                    setSaved(false)
                  }}
                  className={`appearance-none pr-9 pl-3 ${FIELD}`}
                >
                  {ROLES.map((r) => (
                    <option key={r} value={r}>
                      {ROLE_LABELS[r]}
                    </option>
                  ))}
                </select>
                <ChevronDown className="pointer-events-none absolute top-1/2 right-3 size-4 -translate-y-1/2 text-slate-500" aria-hidden />
              </span>
            </label>
            {/* shortcut: no teams endpoint yet, swap for a team select once the backend lists teams */}
            <label className="text-xs font-medium text-slate-700">
              <span className="mb-1.5 block">Team ID</span>
              <input
                type="number"
                min={1}
                step={1}
                inputMode="numeric"
                value={teamId}
                onChange={(e) => {
                  setTeamId(e.target.value)
                  setSaved(false)
                }}
                className={`px-3 ${FIELD}`}
              />
              {saveError?.fieldErrors.teamId && <span className="mt-1.5 block font-normal text-red-700">{saveError.fieldErrors.teamId}</span>}
            </label>
          </div>

          <div className="mt-8 flex flex-wrap gap-3">
            <button
              type="submit"
              disabled={unchanged || invalid || saving}
              className="h-10 min-w-36 rounded-lg bg-brand px-6 text-sm font-semibold text-white transition-colors hover:bg-forest focus-visible:ring-2 focus-visible:ring-brand/40 focus-visible:outline-none disabled:cursor-not-allowed disabled:opacity-50 disabled:hover:bg-brand"
            >
              {saving ? 'Saving…' : 'Save changes'}
            </button>
            <a
              href="/admin/users"
              className="inline-flex h-10 min-w-28 items-center justify-center rounded-lg border border-slate-500 bg-white px-6 text-sm font-semibold text-slate-900 transition-colors hover:bg-slate-50"
            >
              Back
            </a>
          </div>
        </form>
      )}
    </AdminLayout>
  )
}
