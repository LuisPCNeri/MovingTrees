import { useState, type ComponentProps, type FormEvent } from 'react'
import { ArrowRight, Bike, CalendarDays, Eye, EyeOff, Leaf, Lock, Mail, MapPin, ShieldCheck, type LucideIcon } from 'lucide-react'
import { Field } from '../components/form'

const STEPS: [LucideIcon, string, string][] = [
  [CalendarDays, 'Reserve', 'Book a cargo bike or scooter before your shift.'],
  [Bike, 'Ride', 'Move tools and crews between parks without a van.'],
  [MapPin, 'Return', 'Drop it at any hub and the trip is logged for you.'],
]

// Growth rings: the brand mark behind the hero. Slightly uneven so they read as wood, not a target.
const RINGS = [
  [40, 36],
  [78, 72],
  [120, 112],
  [166, 154],
  [216, 202],
  [270, 254],
  [330, 310],
]

export function LoginPage() {
  const [submitted, setSubmitted] = useState(false)
  const [showPassword, setShowPassword] = useState(false)

  function onSubmit(e: FormEvent) {
    e.preventDefault()
    // shortcut: no sign-in endpoint yet, call it here once the backend has one
    setSubmitted(true)
  }

  return (
    <div className="grid min-h-screen bg-canvas text-slate-900 lg:grid-cols-[1.15fr_1fr]">
      <Hero />


      <main className="flex items-center justify-center px-5 py-12 sm:px-10">
        <div className="w-full max-w-md rounded-2xl border border-slate-200/80 bg-white p-6 shadow-[0_1px_2px_rgb(15_23_42/0.04),0_12px_32px_-12px_rgb(27_94_52/0.18)] sm:p-9">
          <p className="inline-flex items-center gap-2 rounded-full bg-mint/30 px-3 py-1 text-xs font-medium text-forest">
            <span className="size-1.5 rounded-full bg-brand" aria-hidden />
            Municipal access
          </p>
          <h2 className="mt-5 text-3xl font-bold tracking-tight">Sign in</h2>
          <p className="mt-2 text-sm text-slate-600">Use the work email your municipality registered for you.</p>

          <form onSubmit={onSubmit} onChange={() => setSubmitted(false)} className="mt-8 grid gap-5">
            <Field label="Work email">
              <IconInput icon={Mail} name="email" type="email" required autoComplete="username" placeholder="name@municipality.pt" />
            </Field>
            <Field label="Password">
              <IconInput icon={Lock} name="password" type={showPassword ? 'text' : 'password'} required autoComplete="current-password">
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                  aria-pressed={showPassword}
                  className="absolute top-1/2 right-2 grid size-8 -translate-y-1/2 place-items-center rounded-lg text-slate-400 transition-colors hover:bg-slate-100 hover:text-slate-700 focus-visible:ring-2 focus-visible:ring-brand/40 focus-visible:outline-none"
                >
                  {showPassword ? <EyeOff className="size-4" /> : <Eye className="size-4" />}
                </button>
              </IconInput>
            </Field>

            {submitted && (
              <p role="status" className="flex gap-2.5 rounded-xl border border-brand/20 bg-mint/20 px-4 py-3 text-sm text-forest">
                <ShieldCheck className="mt-0.5 size-4 shrink-0" aria-hidden />
                Sign-in is not available yet. Your municipality will let you know when access opens.
              </p>
            )}

            <button
              type="submit"
              className="group mt-2 inline-flex h-12 w-full items-center justify-center gap-2 rounded-xl bg-brand text-sm font-semibold text-white shadow-sm transition-colors hover:bg-forest focus-visible:ring-2 focus-visible:ring-brand/40 focus-visible:ring-offset-2 focus-visible:outline-none"
            >
              Sign in
              <ArrowRight className="size-4 transition-transform group-hover:translate-x-0.5" aria-hidden />
            </button>
          </form>

          <p className="mt-8 rounded-xl bg-slate-50 px-4 py-3 text-xs leading-relaxed text-slate-600">
            <span className="font-medium text-slate-800">No account?</span> Access is created by your municipality&apos;s
            administrators.
          </p>
        </div>
      </main>
    </div>
  )
}

function Hero() {
  return (
    <section className="relative flex flex-col overflow-hidden bg-forest px-6 py-8 text-white sm:px-10 lg:px-14 lg:py-12">
      <svg
        className="pointer-events-none absolute -right-40 -bottom-48 size-[720px] text-mint"
        viewBox="-360 -360 720 720"
        fill="none"
        aria-hidden
      >
        {RINGS.map(([rx, ry], i) => (
          <ellipse
            key={rx}
            cx={i * 3}
            cy={-i * 2}
            rx={rx}
            ry={ry}
            stroke="currentColor"
            strokeOpacity={0.08 + i * 0.025}
            strokeWidth={i % 3 === 0 ? 2 : 1}
          />
        ))}
      </svg>

      <p className="relative flex items-center gap-2 text-lg font-semibold">
        <Leaf className="size-5 text-mint" aria-hidden />
        MovingTrees
      </p>

      <div className="relative mt-14 max-w-xl lg:mt-auto">
        <h1 className="text-4xl leading-[1.05] font-bold tracking-tight sm:text-5xl lg:text-6xl">
          Move the city, <span className="text-mint">lighter.</span>
        </h1>
        <p className="mt-6 max-w-md text-base text-white/75">
          Shared electric fleets for the municipal teams who keep parks and streets alive.
        </p>
      </div>

      {/* hidden on phones so the sign-in form stays above the fold */}
      <ol className="relative mt-12 hidden gap-6 sm:grid sm:grid-cols-3 lg:mt-auto lg:pt-16">
        {STEPS.map(([Icon, title, text], i) => (
          <li key={title} className="border-t border-white/15 pt-4">
            <p className="flex items-center gap-2 text-sm font-semibold">
              <span className="text-xs text-mint tabular-nums">0{i + 1}</span>
              <Icon className="size-4 text-mint" aria-hidden />
              {title}
            </p>
            <p className="mt-2 text-sm text-white/65">{text}</p>
          </li>
        ))}
      </ol>
    </section>
  )
}

/** Tall text input with a leading icon; children render inside the field's right edge (e.g. a toggle). */
function IconInput({ icon: Icon, children, ...props }: ComponentProps<'input'> & { icon: LucideIcon }) {
  return (
    <span className="relative block">
      <Icon className="pointer-events-none absolute top-1/2 left-3.5 size-4 -translate-y-1/2 text-slate-400" aria-hidden />
      <input
        {...props}
        className={`h-12 w-full rounded-xl border border-slate-300 bg-white pl-10 text-sm font-normal text-slate-900 transition-colors outline-none placeholder:text-slate-400 hover:border-slate-400 focus:border-brand focus:ring-4 focus:ring-brand/10 ${children ? 'pr-12' : 'pr-3'}`}
      />
      {children}
    </span>
  )
}
