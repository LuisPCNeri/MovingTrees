import { expect, test, type Page } from '@playwright/test'

const ROLES = ['FIELD_WORKER', 'FLEET_OPERATOR', 'FIELD_TECHNICIAN']
// 25 users so the table (20 per page) has a second page
const USERS = Array.from({ length: 25 }, (_, i) => ({
  id: i + 1,
  name: `Person ${i + 1}`,
  username: `person${i + 1}@cm-aveiro.pt`,
  role: ROLES[i % ROLES.length],
  status: i % 5 === 0 ? 'INACTIVE' : 'ACTIVE',
  municipalityId: 1,
  teamId: 1 + (i % 2),
}))

// Mimics GET /users: filters by role, status and teamId, then pages the result.
async function mockUsersApi(page: Page) {
  await page.route('**/api/teams', (route) =>
    route.fulfill({ json: [{ id: 1, name: 'Pruning · Central' }, { id: 2, name: 'Cleaning · Botânico' }] }),
  )
  await page.route('**/api/users?*', (route) => {
    const q = new URL(route.request().url()).searchParams
    const matches = USERS.filter(
      (u) =>
        (!q.get('role') || u.role === q.get('role')) &&
        (!q.get('status') || u.status === q.get('status')) &&
        (!q.get('teamId') || u.teamId === Number(q.get('teamId'))),
    )
    const size = Number(q.get('size') ?? 20)
    const number = Number(q.get('page') ?? 0)
    return route.fulfill({
      json: {
        content: matches.slice(number * size, (number + 1) * size),
        page: number,
        size,
        totalElements: matches.length,
        totalPages: Math.ceil(matches.length / size),
      },
    })
  })
}

const rows = (page: Page) => page.getByRole('table').locator('tbody tr')

test('filters the table by role and team', async ({ page }) => {
  await mockUsersApi(page)
  await page.goto('/admin/users')
  await expect(rows(page)).toHaveCount(20)

  const request = page.waitForRequest((r) => r.url().includes('role=FLEET_OPERATOR') && r.url().includes('size=20'))
  await page.getByLabel('Role').selectOption('FLEET_OPERATOR')
  await request

  await expect(page.getByText('8 records')).toBeVisible()
  await expect(rows(page)).toHaveCount(8)
  for (const role of await rows(page).locator('td:nth-child(3)').allTextContents()) expect(role).toBe('Fleet operator')

  await page.getByLabel('Team').selectOption({ label: 'Cleaning · Botânico' })
  await expect(page.getByText('4 records')).toBeVisible()
  for (const team of await rows(page).locator('td:nth-child(4)').allTextContents()) expect(team).toBe('Cleaning · Botânico')
})

test('pages through the users', async ({ page }) => {
  await mockUsersApi(page)
  await page.goto('/admin/users')
  await expect(page.getByText('Showing 1–20 of 25')).toBeVisible()
  await expect(page.getByRole('button', { name: 'Previous page' })).toBeDisabled()

  await page.getByRole('button', { name: 'Next page' }).click()
  await expect(page.getByText('Showing 21–25 of 25')).toBeVisible()
  await expect(rows(page)).toHaveCount(5)
  await expect(rows(page).first()).toContainText('Person 21')
  await expect(page.getByRole('button', { name: 'Next page' })).toBeDisabled()

  await page.getByRole('button', { name: 'Previous page' }).click()
  await expect(rows(page).first()).toContainText('Person 1')
})

test('sends a non-administrator away from the users page', async ({ page }) => {
  await page.route('**/api/users?*', (route) =>
    route.fulfill({ status: 403, json: { message: 'Only municipal administrators can access this operation', fieldErrors: {} } }),
  )
  await page.goto('/admin/users')
  await expect(page).toHaveURL(/\/forbidden$/)
  await expect(page.getByRole('heading', { name: 'Access restricted' })).toBeVisible()
})
