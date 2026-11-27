import { useState, type FormEvent, type ReactNode } from 'react'
import { ChevronDown } from 'lucide-react'
import { AdminLayout } from '../../components/AdminLayout'
import { FIELD } from '../../components/styles'
import { ApiError, createUser, ROLE_LABELS, ROLES, type Role } from '../../api/users'

export function NewUserPage() {
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<ApiError | null>(null)

  async function onSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    const form = new FormData(e.currentTarget)
    setSubmitting(true)
    setError(null)
    try {
      await createUser({
        name: String(form.get('name')).trim(),
        username: String(form.get('username')).trim(),
        password: String(form.get('password')),
        role: form.get('role') as Role,
        teamId: Number(form.get('teamId')),
      })
      location.assign('/admin/users')
    } catch (err) {
      setError(err instanceof ApiError ? err : new ApiError('Could not reach the server'))
      setSubmitting(false)
    }
  }

  const fieldError = (name: string) => error?.fieldErrors[name]

  return (
    <AdminLayout active="Users" breadcrumb="Municipality / Users">
      <h1 className="text-3xl font-bold tracking-tight">Create access</h1>
      <nav aria-label="Breadcrumb" className="mt-3 text-xs text-slate-600">
        <a href="/admin/users" className="hover:text-brand hover:underline">
          Users
        </a>{' '}
        / New user
      </nav>

      <form onSubmit={onSubmit} className="mt-6 max-w-2xl rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
        <h2 className="text-lg font-semibold">Details and permissions</h2>

        {error && (
          <p role="alert" className="mt-4 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
            {error.message}
          </p>
        )}

        <div className="mt-5 grid gap-5 sm:grid-cols-2">
          <Field label="Full name" error={fieldError('name')} wide>
            <input name="name" required maxLength={255} autoComplete="off" className={`px-3 ${FIELD}`} />
          </Field>
          <Field label="Username" error={fieldError('username')}>
            <input name="username" required maxLength={255} autoComplete="off" className={`px-3 ${FIELD}`} />
          </Field>
          <Field label="Initial password" error={fieldError('password')}>
            <input name="password" type="password" required maxLength={72} autoComplete="new-password" className={`px-3 ${FIELD}`} />
          </Field>
          <Field label="Role" error={fieldError('role')}>
            <span className="relative block">
              <select name="role" required defaultValue="" className={`appearance-none pr-9 pl-3 ${FIELD}`}>
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
        </div>

        <p className="mt-5 text-xs text-slate-600">New users start inactive until their access is activated.</p>

        <div className="mt-6 flex gap-3">
          <button
            type="submit"
            disabled={submitting}
            className="h-10 rounded-lg bg-brand px-6 text-sm font-semibold text-white hover:bg-forest disabled:opacity-60"
          >
            {submitting ? 'Creating…' : 'Create user'}
          </button>
          <a
            href="/admin/users"
            className="inline-flex h-10 items-center rounded-lg border border-slate-400 px-6 text-sm font-semibold text-slate-900 hover:bg-slate-50"
          >
            Cancel
          </a>
        </div>
      </form>

      <p className="mt-6 text-xs text-slate-600">All fields are required.</p>
    </AdminLayout>
  )
}

function Field({ label, error, wide, children }: { label: string; error?: string; wide?: boolean; children: ReactNode }) {
  return (
    <label className={`text-xs font-medium text-slate-700 ${wide ? 'sm:col-span-2' : ''}`}>
      <span className="mb-1.5 block">{label} *</span>
      {children}
      {error && <span className="mt-1 block font-normal text-red-700">{error}</span>}
    </label>
  )
}
