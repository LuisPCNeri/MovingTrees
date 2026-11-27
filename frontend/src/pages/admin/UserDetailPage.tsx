import { useEffect, useState, type FormEvent } from 'react'
import { AdminLayout } from '../../components/AdminLayout'
import { Banner, Field, RoleSelect, UsersBreadcrumb } from '../../components/form'
import { StatusBadge } from '../../components/StatusBadge'
import { BUTTON_PRIMARY, BUTTON_SECONDARY, FIELD } from '../../components/styles'
import { changeStatus, getUser, updateUser, type ApiError, type Role, type Status, type User } from '../../api/users'

const CARD = 'mt-6 max-w-2xl rounded-xl border border-slate-200 bg-white p-4 shadow-sm sm:p-6'
const BUTTON_DANGER =
  'inline-flex h-10 items-center justify-center rounded-lg px-6 text-sm font-semibold transition-colors focus-visible:ring-2 focus-visible:ring-red-400 focus-visible:outline-none disabled:opacity-50'

export function UserDetailPage({ id }: { id: string }) {
  const [user, setUser] = useState<User | null>(null)
  const [loadError, setLoadError] = useState<ApiError | null>(null)
  const [role, setRole] = useState<Role | ''>('')
  const [teamId, setTeamId] = useState('')
  const [busy, setBusy] = useState<'save' | 'status' | null>(null)
  const [error, setError] = useState<ApiError | null>(null)
  const [notice, setNotice] = useState<string | null>(null)

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

  // Runs one action at a time and reports its outcome in the shared banner.
  async function run(kind: 'save' | 'status', action: () => Promise<string>) {
    setBusy(kind)
    setError(null)
    setNotice(null)
    try {
      setNotice(await action())
    } catch (err) {
      setError(err as ApiError)
    } finally {
      setBusy(null)
    }
  }

  function onSubmit(e: FormEvent) {
    e.preventDefault()
    if (!user || unchanged || invalid) return
    run('save', async () => {
      load(await updateUser(user.id, { role, teamId: team }))
      return "Changes saved. They apply at the user's next sign-in."
    })
  }

  function setStatus(status: Status) {
    if (!user) return
    run('status', async () => {
      // only the status changed, so keep any unsaved role/team edits in the form
      setUser(await changeStatus(user.id, status))
      return status === 'ACTIVE' ? `Access activated. ${user.name} can now sign in.` : 'Access deactivated.'
    })
  }

  const edit = (apply: () => void) => {
    apply()
    setNotice(null)
  }

  // 400 = id is not a number, 404 = missing or from another municipality
  const notFound = loadError?.status === 404 || loadError?.status === 400
  const title = user?.name ?? (notFound ? 'User not found' : 'User')

  return (
    <AdminLayout active="Users" breadcrumb="Municipality / Users">
      <h1 className="text-3xl font-bold tracking-tight break-words">{title}</h1>
      <UsersBreadcrumb current={user?.name ?? id} />

      {loadError ? (
        <div role="alert" className={`${CARD} text-sm`}>
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
        <>
          <div className="max-w-2xl">
            {error && <Banner tone="error">{error.message}</Banner>}
            {notice && <Banner tone="success">{notice}</Banner>}
          </div>

          <form onSubmit={onSubmit} className={CARD}>
            <h2 className="text-lg font-semibold">Role and team</h2>
            <p className="mt-2 text-sm break-all text-slate-600">{user.username}</p>

            <div className="mt-5 grid gap-x-4 gap-y-5 sm:grid-cols-2">
              <Field label="Role">
                <RoleSelect value={role} onChange={(e) => edit(() => setRole(e.target.value as Role))} />
              </Field>
              {/* shortcut: no teams endpoint yet, swap for a team select once the backend lists teams */}
              <Field label="Team ID" error={error?.fieldErrors.teamId}>
                <input
                  type="number"
                  min={1}
                  step={1}
                  inputMode="numeric"
                  value={teamId}
                  onChange={(e) => edit(() => setTeamId(e.target.value))}
                  className={`px-3 ${FIELD}`}
                />
              </Field>
            </div>

            <div className="mt-8 flex flex-wrap gap-3">
              <button type="submit" disabled={unchanged || invalid || busy !== null} className={BUTTON_PRIMARY}>
                {busy === 'save' ? 'Saving…' : 'Save changes'}
              </button>
              <a href="/admin/users" className={BUTTON_SECONDARY}>
                Back
              </a>
            </div>
          </form>

          <AccessCard user={user} busy={busy !== null} onChange={setStatus} />
        </>
      )}
    </AdminLayout>
  )
}

function AccessCard({ user, busy, onChange }: { user: User; busy: boolean; onChange: (status: Status) => void }) {
  const [confirming, setConfirming] = useState(false)
  const active = user.status === 'ACTIVE'

  return (
    <section aria-labelledby="access-heading" className={CARD}>
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h2 id="access-heading" className="text-lg font-semibold">
          Access
        </h2>
        <StatusBadge status={user.status} />
      </div>

      {confirming ? (
        <div role="alertdialog" aria-labelledby="confirm-text" className="mt-4 rounded-lg border border-red-200 bg-red-50 p-4">
          <p id="confirm-text" className="text-sm text-red-900">
            Deactivate {user.name}? They will not be able to sign in until their access is activated again.
          </p>
          <div className="mt-4 flex flex-wrap gap-3">
            <button
              type="button"
              onClick={() => {
                setConfirming(false)
                onChange('INACTIVE')
              }}
              className={`${BUTTON_DANGER} bg-red-700 text-white hover:bg-red-800`}
            >
              Yes, deactivate
            </button>
            <button type="button" onClick={() => setConfirming(false)} className={BUTTON_SECONDARY}>
              Cancel
            </button>
          </div>
        </div>
      ) : (
        <>
          <p className="mt-2 text-sm text-slate-600">
            {active ? 'This user can sign in.' : 'This user cannot sign in until their access is activated.'}
          </p>
          {active ? (
            <button
              type="button"
              disabled={busy}
              onClick={() => setConfirming(true)}
              className={`mt-5 border border-red-300 bg-white text-red-700 hover:bg-red-50 ${BUTTON_DANGER}`}
            >
              Deactivate access
            </button>
          ) : (
            <button type="button" disabled={busy} onClick={() => onChange('ACTIVE')} className={`mt-5 ${BUTTON_PRIMARY}`}>
              Activate access
            </button>
          )}
        </>
      )}
    </section>
  )
}
