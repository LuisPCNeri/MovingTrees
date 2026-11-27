import type { ComponentProps, ReactNode } from 'react'
import { ChevronDown } from 'lucide-react'
import { FIELD } from './styles'

export function Field(props: { label: string; required?: boolean; error?: string; hint?: string; wide?: boolean; children: ReactNode }) {
  return (
    <label className={`text-xs font-medium text-slate-700 ${props.wide ? 'sm:col-span-2' : ''}`}>
      <span className="mb-1.5 block">
        {props.label}
        {props.required && ' *'}
      </span>
      {props.children}
      {props.error ? (
        <span className="mt-1.5 block font-normal text-red-700">{props.error}</span>
      ) : (
        props.hint && <span className="mt-1.5 block font-normal text-slate-500">{props.hint}</span>
      )}
    </label>
  )
}

/** Dropdown with our field styling; children go before the options (e.g. an "All roles" placeholder). */
export function Select({ options, children, className = '', ...props }: ComponentProps<'select'> & { options: [string, string][] }) {
  return (
    <span className="relative block">
      <select {...props} className={`appearance-none pr-9 pl-3 ${FIELD} ${className}`}>
        {children}
        {options.map(([value, label]) => (
          <option key={value} value={value}>
            {label}
          </option>
        ))}
      </select>
      <ChevronDown className="pointer-events-none absolute top-1/2 right-3 size-4 -translate-y-1/2 text-slate-500" aria-hidden />
    </span>
  )
}

export function Banner({ tone, children }: { tone: 'error' | 'success'; children: ReactNode }) {
  return tone === 'error' ? (
    <p role="alert" className="mt-4 rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-800">
      {children}
    </p>
  ) : (
    <p role="status" className="mt-4 rounded-lg border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-800">
      {children}
    </p>
  )
}

export function UsersBreadcrumb({ current }: { current: string }) {
  return (
    <nav aria-label="Breadcrumb" className="mt-3 text-sm text-slate-600">
      <a href="/admin/users" className="hover:text-brand hover:underline">
        Users
      </a>
      <span aria-hidden> / </span>
      <span aria-current="page">{current}</span>
    </nav>
  )
}
