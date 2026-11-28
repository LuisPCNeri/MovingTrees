import { Leaf } from 'lucide-react'

// shortcut: there is no login page yet, point non-administrators to it once it exists
export function ForbiddenPage() {
  return (
    <main className="flex min-h-screen items-center justify-center bg-canvas p-4 text-slate-900">
      <div className="w-full max-w-md rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
        <p className="flex items-center gap-2 font-semibold text-forest">
          <Leaf className="size-4" aria-hidden />
          MovingTrees
        </p>
        <h1 className="mt-6 text-2xl font-bold tracking-tight">Access restricted</h1>
        <p className="mt-3 text-sm text-slate-600">This area is only available to municipal administrators.</p>
      </div>
    </main>
  )
}
