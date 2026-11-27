import type { ReactNode } from 'react'

const NAV = ['Overview', 'Vehicles', 'Hubs', 'Maintenance', 'Users', 'Rules and rates', 'Payments', 'Analytics']

export function AdminLayout({ active, breadcrumb, children }: { active: string; breadcrumb: string; children: ReactNode }) {
  return (
    <div className="flex min-h-screen bg-[#f4f6f3] text-slate-900">
      <aside className="hidden w-60 shrink-0 flex-col bg-[#1b5e34] px-4 py-6 text-white md:flex">
        <p className="px-2 text-xl font-semibold">MovingTrees</p>
        <p className="mt-8 px-2 text-xs font-medium tracking-wide uppercase">Fleet management</p>
        <nav className="mt-4 flex flex-col gap-1">
          {NAV.map((item) =>
            item === active ? (
              <span key={item} aria-current="page" className="rounded-md bg-[#8ee6a6] px-3 py-2 text-sm font-medium text-[#0f3d20]">
                {item}
              </span>
            ) : (
              // shortcut: other sections are not built yet, enable links when their routes exist
              <span key={item} className="px-3 py-2 text-sm text-white/70">
                {item}
              </span>
            ),
          )}
        </nav>
      </aside>
      <div className="flex min-w-0 flex-1 flex-col">
        <header className="border-b border-slate-200 bg-white px-6 py-4 text-sm text-slate-600">{breadcrumb}</header>
        <main className="flex-1 px-4 py-6 md:px-8">{children}</main>
      </div>
    </div>
  )
}
