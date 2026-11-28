import { defineConfig, devices } from '@playwright/test'

// Browser tests run against the Vite dev server with the API mocked per test (page.route), no backend needed.
export default defineConfig({
  testDir: 'e2e',
  testMatch: '*.e2e.ts',
  forbidOnly: !!process.env.CI,
  use: { baseURL: 'http://localhost:5199' },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
  webServer: {
    command: 'npm run dev -- --port 5199 --strictPort',
    url: 'http://localhost:5199',
    reuseExistingServer: !process.env.CI,
  },
})
