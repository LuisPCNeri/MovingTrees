import { useState, type FormEvent, type ReactNode } from 'react'
import { Check, ChevronDown } from 'lucide-react'
import { AdminLayout } from '../../components/AdminLayout'
import { FIELD } from '../../components/styles'
import { ApiError, createUser, ROLE_LABELS, ROLES, type Role } from '../../api/users'

// Copy from the Figma "Create access" screen; only Field agent is specified there so far.
const ROLE_PERMISSIONS: Partial<Record<Role, string[]>> = {
  FIELD_WORKER: ['View and reserve vehicles', 'Start and end trips', 'View personal charges', 'Report issues'],
}
// the backend caps passwords at 72 characters (bcrypt limit); 8 is our minimum
const PASSWORD_MIN = 8
const PASSWORD_MAX = 72

export function NewUserPage() {
  const [role, setRole] = useState<Role | ''>('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<ApiError | null>(null)

  async function onSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    const form = new FormData(e.currentTarget)
    const password = String(form.get('password'))
    if (password.length < PASSWORD_MIN || password.length > PASSWORD_MAX) {
      setError(new ApiError('Validation failed', { password: `Use ${PASSWORD_MIN} to ${PASSWORD_MAX} characters.` }))
      return
    }
    setSubmitting(true)
    setError(null)
    try {
      await createUser({
        name: String(form.get('name')).trim(),
        // the work email is the user's login identifier
        username: String(form.get('email')).trim().toLowerCase(),
        password,
        role: role as Role,
        teamId: Number(form.get('teamId')),
      })
      location.assign('/admin/users')
    } catch (err) {
      if (err instanceof ApiError && err.status === 409) {
        // 409 = the username (work email) is taken, so point at that field
        setError(new ApiError(err.message, { username: 'This email already has access.' }, 409))
      } else {
        setError(err instanceof ApiError ? err : new ApiError('Could not reach the server. Try again.'))
      }
      setSubmitting(false)
    }
  }

  const fieldError = (name: string) => error?.fieldErrors[name]
  const permissions = role ? ROLE_PERMISSIONS[role] : undefined

  return (
    <AdminLayout active="Users" breadcrumb="Municipality / Users">
      <h1 className="text-3xl font-bold tracking-tight">Create access</h1>
      <nav aria-label="Breadcrumb" className="mt-3 text-sm text-slate-600">
        <a href="/admin/users" className="hover:text-brand hover:underline">
          Users
        </a>
        <span aria-hidden> / </span>
        <span aria-current="page">New user</span>
      </nav>

      <div className="mt-6 grid max-w-5xl items-start gap-4 lg:grid-cols-[minmax(0,2fr)_minmax(0,1fr)]">
        <form onSubmit={onSubmit} className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm sm:p-6">
          <h2 className="text-lg font-semibold">Details and permissions</h2>

          {error && Object.keys(error.fieldErrors).length === 0 && (
            <p role="alert" className="mt-4 rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-800">
              {error.message}
            </p>
          )}

          <div className="mt-5 grid gap-x-4 gap-y-5 sm:grid-cols-2">
            <Field label="Full name" error={fieldError('name')} wide>
              <input name="name" required maxLength={255} autoComplete="off" className={`px-3 ${FIELD}`} />
            </Field>
            <Field label="Work email" error={fieldError('username')} wide>
              <input name="email" type="email" required maxLength={255} autoComplete="off" className={`px-3 ${FIELD}`} />
            </Field>
            <Field label="Role" error={fieldError('role')}>
              <span className="relative block">
                <select
                  name="role"
                  required
                  value={role}
                  onChange={(e) => setRole(e.target.value as Role)}
                  className={`appearance-none pr-9 pl-3 ${FIELD} ${role ? '' : 'text-slate-500'}`}
                >
                  <option value="" disabled>
                    Select a role
                  </option>
                  {ROLES.map((r) => (
                    <option key={r} value={r}>
                      {ROLE_LABELS[r]}
                    </option>
                  ))}
                </select>
                <ChevronDown className="pointer-events-none absolute top-1/2 right-3 size-4 -translate-y-1/2 text-slate-500" aria-hidden />
              </span>
            </Field>
            {/* shortcut: no teams endpoint yet, swap for a team select once the backend lists teams */}
            <Field label="Team ID" error={fieldError('teamId')}>
              <input name="teamId" type="number" required min={1} step={1} inputMode="numeric" className={`px-3 ${FIELD}`} />
            </Field>
            <Field label="Temporary password" error={fieldError('password')} hint="Share it with the user; they sign in with their work email." wide>
              <input
                name="password"
                type="password"
                required
                maxLength={PASSWORD_MAX}
                autoComplete="new-password"
                className={`px-3 ${FIELD}`}
              />
            </Field>
          </div>

          <p className="mt-6 text-xs text-slate-600">Access starts inactive until it is activated.</p>

          <div className="mt-8 flex flex-wrap gap-3">
            <button
              type="submit"
              disabled={submitting}
              className="h-10 min-w-36 rounded-lg bg-brand px-6 text-sm font-semibold text-white transition-colors hover:bg-forest focus-visible:ring-2 focus-visible:ring-brand/40 focus-visible:outline-none disabled:opacity-60"
            >
              {submitting ? 'Creating…' : 'Create access'}
            </button>
            <a
              href="/admin/users"
              className="inline-flex h-10 min-w-28 items-center justify-center rounded-lg border border-slate-500 bg-white px-6 text-sm font-semibold text-slate-900 transition-colors hover:bg-slate-50"
            >
              Cancel
            </a>
          </div>
        </form>

        <aside aria-live="polite" className="rounded-xl border border-green-100 bg-[#e8f5eb] p-5 shadow-sm lg:min-h-56">
          <h2 className="text-lg font-semibold">{role ? ROLE_LABELS[role] : 'Role permissions'}</h2>
          {permissions ? (
            <ul className="mt-5 space-y-4 text-sm text-slate-700">
              {permissions.map((p) => (
                <li key={p} className="flex items-center gap-2">
                  <Check className="size-3.5 text-slate-500" aria-hidden />
                  {p}
                </li>
              ))}
            </ul>
          ) : (
            !role && <p className="mt-5 text-sm text-slate-600">Select a role to see what this person can do.</p>
          )}
        </aside>
      </div>

      <p className="mt-8 text-xs text-slate-600">Fields marked with * are required.</p>
    </AdminLayout>
  )
}

function Field(props: { label: string; error?: string; hint?: string; wide?: boolean; children: ReactNode }) {
  return (
    <label className={`text-xs font-medium text-slate-700 ${props.wide ? 'sm:col-span-2' : ''}`}>
      <span className="mb-1.5 block">{props.label} *</span>
      {props.children}
      {props.error ? (
        <span className="mt-1.5 block font-normal text-red-700">{props.error}</span>
      ) : (
        props.hint && <span className="mt-1.5 block font-normal text-slate-500">{props.hint}</span>
      )}
    </label>
  )
}
