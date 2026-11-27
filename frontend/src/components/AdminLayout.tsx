import type { ReactNode } from 'react'
import { Bike, ChartColumn, LayoutGrid, Leaf, MapPin, SlidersVertical, Users, Wallet, Wrench, type LucideIcon } from 'lucide-react'

const NAV: [string, LucideIcon][] = [
  ['Overview', LayoutGrid],
  ['Vehicles', Bike],
  ['Hubs', MapPin],
  ['Maintenance', Wrench],
  ['Users', Users],
  ['Rules and rates', SlidersVertical],
  ['Payments', Wallet],
  ['Analytics', ChartColumn],
]

export function AdminLayout({ active, breadcrumb, children }: { active: string; breadcrumb: string; children: ReactNode }) {
  return (
    <div className="flex min-h-screen bg-canvas text-slate-900">
      <aside className="sticky top-0 hidden h-screen w-60 shrink-0 flex-col bg-forest px-3 py-7 text-white md:flex">
        <p className="flex items-center gap-2 px-3 text-xl font-semibold">
          <Leaf className="size-5" aria-hidden />
          MovingTrees
        </p>
        <p className="mt-10 px-3 text-xs font-medium tracking-wide uppercase">Fleet management</p>
        <nav className="mt-5 flex flex-col gap-1.5">
          {NAV.map(([item, Icon]) => {
            const current = item === active
            return (
              // shortcut: other sections are not built yet, turn into links when their routes exist
              <span
                key={item}
                aria-current={current ? 'page' : undefined}
                className={`flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm ${
                  current ? 'bg-mint font-medium text-forest' : 'text-white/90'
                }`}
              >
                <Icon className="size-4" aria-hidden />
                {item}
              </span>
            )
          })}
        </nav>
      </aside>
      <div className="flex min-w-0 flex-1 flex-col">
        <header className="flex h-16 items-center border-b border-slate-200 bg-white px-4 text-xs text-slate-600 md:px-8">
          {breadcrumb}
        </header>
        <main className="flex-1 px-4 py-6 md:px-8">{children}</main>
      </div>
    </div>
  )
}
