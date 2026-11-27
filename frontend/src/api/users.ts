// Mirrors backend DTOs in pt.ua.MovingTree.dtos / enums.
export const ROLES = [
  'MUNICIPAL_ADMINISTRATOR',
  'FIELD_WORKER',
  'FLEET_OPERATOR',
  'FIELD_TECHNICIAN',
  'CLIENT_MANAGER',
  'PLATFORM_ADMIN',
] as const
export type Role = (typeof ROLES)[number]
export type Status = 'ACTIVE' | 'INACTIVE'

export const ROLE_LABELS: Record<Role, string> = {
  MUNICIPAL_ADMINISTRATOR: 'Municipal admin.',
  FIELD_WORKER: 'Field agent',
  FLEET_OPERATOR: 'Fleet operator',
  FIELD_TECHNICIAN: 'Technician',
  CLIENT_MANAGER: 'Client manager',
  PLATFORM_ADMIN: 'Platform admin',
}

export interface User {
  id: number
  name: string
  username: string
  role: Role
  status: Status
  municipalityId: number
  teamId: number
}

export interface Page<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface UserQuery {
  page?: number
  size?: number
  role?: Role
  status?: Status
  name?: string
}

export async function listUsers(query: UserQuery, signal?: AbortSignal): Promise<Page<User>> {
  const params = new URLSearchParams()
  for (const [key, value] of Object.entries(query)) {
    if (value !== undefined && value !== '') params.set(key, String(value))
  }
  const res = await fetch(`/api/users?${params}`, { signal })
  if (!res.ok) {
    const body = await res.json().catch(() => null)
    throw new Error(body?.message ?? `Request failed (${res.status})`)
  }
  return res.json()
}
