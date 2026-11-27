import { useEffect, useState, type FormEvent } from 'react'
import { AdminLayout } from '../../components/AdminLayout'
import { Banner, Field, RoleSelect, UsersBreadcrumb } from '../../components/form'
import { StatusBadge } from '../../components/StatusBadge'
import { BUTTON_PRIMARY, BUTTON_SECONDARY, FIELD } from '../../components/styles'
import { getUser, updateUser, type ApiError, type Role, type User } from '../../api/users'

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
      .catch((e: ApiError) => e.name !== 'AbortError' && setLoadError(e))
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
      setSaveError(err as ApiError)
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
      <UsersBreadcrumb current={user?.name ?? id} />

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

          {saveError && <Banner tone="error">{saveError.message}</Banner>}
          {saved && <Banner tone="success">Changes saved. They apply at the user's next sign-in.</Banner>}

          <div className="mt-5 grid gap-x-4 gap-y-5 sm:grid-cols-2">
            <Field label="Role">
              <RoleSelect
                value={role}
                onChange={(e) => {
                  setRole(e.target.value as Role)
                  setSaved(false)
                }}
              />
            </Field>
            {/* shortcut: no teams endpoint yet, swap for a team select once the backend lists teams */}
            <Field label="Team ID" error={saveError?.fieldErrors.teamId}>
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
            </Field>
          </div>

          <div className="mt-8 flex flex-wrap gap-3">
            <button type="submit" disabled={unchanged || invalid || saving} className={BUTTON_PRIMARY}>
              {saving ? 'Saving…' : 'Save changes'}
            </button>
            <a href="/admin/users" className={BUTTON_SECONDARY}>
              Back
            </a>
          </div>
        </form>
      )}
    </AdminLayout>
  )
}
