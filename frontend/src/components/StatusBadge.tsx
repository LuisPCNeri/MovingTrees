import { CalendarClock, Check } from 'lucide-react'
import type { Status } from '../api/users'

export function StatusBadge({ status }: { status: Status }) {
  const active = status === 'ACTIVE'
  const Icon = active ? Check : CalendarClock
  return (
    <span
      className={`inline-flex items-center gap-1.5 rounded-md px-2 py-1 text-xs ${
        active ? 'bg-green-50 text-green-800' : 'bg-amber-50 text-amber-800 ring-1 ring-amber-200'
      }`}
    >
      <Icon className="size-3" aria-hidden />
      {active ? 'Active' : 'Inactive'}
    </span>
  )
}
