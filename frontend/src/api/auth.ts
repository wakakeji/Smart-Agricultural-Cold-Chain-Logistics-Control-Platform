import { get, post } from '@/utils/request'
import type { LoginResult, UserInfo } from '@/types/api'

export function loginApi(data: {
  username: string
  password: string
  rememberMe?: boolean
}) {
  return post<LoginResult>('/auth/login', data)
}

export function logoutApi() {
  return post<void>('/auth/logout')
}

export function userInfoApi() {
  return get<UserInfo>('/auth/user-info')
}
