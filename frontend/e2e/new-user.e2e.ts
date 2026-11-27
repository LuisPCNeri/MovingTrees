import { expect, test } from '@playwright/test'

test('creates a user with the right payload and goes back to the list', async ({ page }) => {
  let payload: unknown
  await page.route('**/api/users', (route) => {
    payload = route.request().postDataJSON()
    return route.fulfill({
      status: 201,
      json: { id: 42, name: 'Paulo Reis', username: 'paulo@cm-aveiro.pt', role: 'FIELD_TECHNICIAN', status: 'INACTIVE', municipalityId: 1, teamId: 3 },
    })
  })
  await page.route('**/api/users?*', (route) =>
    route.fulfill({ json: { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 } }),
  )

  await page.goto('/admin/users/new')
  await page.getByLabel(/Full name/).fill('  Paulo Reis ')
  await page.getByLabel(/Work email/).fill('Paulo@CM-Aveiro.pt')
  await page.getByLabel(/Role/).selectOption('FIELD_TECHNICIAN')
  await page.getByLabel(/Team ID/).fill('3')
  await page.getByLabel(/Temporary password/).fill('password123')
  await page.getByRole('button', { name: 'Create access' }).click()

  await expect(page).toHaveURL(/\/admin\/users$/)
  expect(payload).toEqual({
    name: 'Paulo Reis',
    username: 'paulo@cm-aveiro.pt',
    password: 'password123',
    role: 'FIELD_TECHNICIAN',
    teamId: 3,
  })
})
