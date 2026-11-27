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

export interface CreateUserRequest {
  name: string
  username: string
  password: string
  role: Role
  teamId: number
}

// Backend ApiError: { message, fieldErrors }
export class ApiError extends Error {
  readonly fieldErrors: Record<string, string>

  constructor(message: string, fieldErrors: Record<string, string> = {}) {
    super(message)
    this.fieldErrors = fieldErrors
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`/api${path}`, init)
  if (!res.ok) {
    const body = await res.json().catch(() => null)
    throw new ApiError(body?.message ?? `Request failed (${res.status})`, body?.fieldErrors)
  }
  return res.json()
}

export function listUsers(query: UserQuery, signal?: AbortSignal): Promise<Page<User>> {
  const params = new URLSearchParams()
  for (const [key, value] of Object.entries(query)) {
    if (value !== undefined && value !== '') params.set(key, String(value))
  }
  return request(`/users?${params}`, { signal })
}

export function createUser(body: CreateUserRequest): Promise<User> {
  return request('/users', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) })
}
