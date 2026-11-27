# MovingTrees frontend

React 19 + TypeScript + Vite, styled with Tailwind CSS 4.

## Run

```bash
npm ci
npm run dev                                 # http://localhost:5173, /api is proxied to http://localhost:8080
API_URL=http://localhost:8081 npm run dev   # point the proxy at another backend port
```

## Check

```bash
npm run lint
npm test                           # component tests (Vitest + Testing Library)
npm run test:e2e:install           # first time only: downloads Chromium
npm run test:e2e                   # browser tests (Playwright), API mocked, no backend needed
npm run build                      # type-check and bundle
```

## Layout

| Path | What lives there |
|---|---|
| `src/App.tsx` | Picks the page from the URL |
| `src/pages/` | One file per screen, tests next to them (`*.test.tsx`) |
| `src/components/` | Admin layout, form pieces (`Field`, `Select`, `Banner`…), shared class names |
| `src/api/` | Backend calls and types (`users.ts`), `useTeams` hook |
| `src/test/` | Test helpers (`mockApi`) |
| `e2e/` | Playwright tests (`*.e2e.ts`) |

Brand colours (`forest`, `mint`, `brand`, `canvas`) are Tailwind tokens in `src/index.css`.
