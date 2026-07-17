/** 统一响应 */
export interface ApiResult<T> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

export interface UserInfo {
  userId: number
  username: string
  realName: string
  role: string
  avatar?: string
  permissions: string[]
}

export interface LoginResult {
  token: string
  tokenType: string
  expiresIn: number
  userInfo: UserInfo
}

export interface SysUser {
  userId: number
  username: string
  realName: string
  roleCode: string
  phone?: string
  email?: string
  avatar?: string
  status: number
  createTime?: string
}

export interface SysRole {
  roleId: number
  roleCode: string
  roleName: string
  permissions?: string
  status: number
  remark?: string
}

export interface HealthItem {
  name: string
  up: boolean
  latencyMs: number
  message: string
}
