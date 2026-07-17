import { get, post, put } from '@/utils/request'
import type { HealthItem, PageResult, SysRole, SysUser } from '@/types/api'

export function fetchUsers(params: {
  page?: number
  size?: number
  keyword?: string
  roleCode?: string
}) {
  return get<PageResult<SysUser>>('/system/users', params)
}

export function createUser(data: Record<string, unknown>) {
  return post<void>('/system/users', data)
}

export function updateUser(id: number, data: Record<string, unknown>) {
  return put<void>(`/system/users/${id}`, data)
}

export function updateUserStatus(id: number, status: number) {
  return put<void>(`/system/users/${id}/status`, { status })
}

export function fetchRoles() {
  return get<SysRole[]>('/system/roles')
}

export function fetchHealth() {
  return get<Record<string, HealthItem | object>>('/system/health')
}
