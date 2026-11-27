import { expect, test } from '@playwright/test'

test('opens on the sign-in page and does not call the API yet', async ({ page }) => {
  const apiCalls: string[] = []
  page.on('request', (r) => new URL(r.url()).pathname.startsWith('/api/') && apiCalls.push(r.url()))

  await page.goto('/')
  await expect(page.getByRole('heading', { name: 'Sign in' })).toBeVisible()

  await page.getByLabel('Work email').fill('ana.silva@cm-aveiro.pt')
  await page.getByLabel('Password', { exact: true }).fill('password123')
  await page.getByRole('button', { name: 'Sign in' }).click()

  await expect(page.getByRole('status')).toContainText('Sign-in is not available yet')
  expect(apiCalls).toEqual([])
})
