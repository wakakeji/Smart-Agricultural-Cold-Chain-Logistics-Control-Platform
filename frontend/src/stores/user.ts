import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo } from '@/types/api'
import { loginApi, logoutApi, userInfoApi } from '@/api/auth'

const TOKEN_KEY = 'cc_token'
const USER_KEY = 'cc_user'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const userInfo = ref<UserInfo | null>(readUser())

  const isLogin = computed(() => !!token.value)
  const role = computed(() => userInfo.value?.role || '')

  function setSession(t: string, user: UserInfo) {
    token.value = t
    userInfo.value = user
    localStorage.setItem(TOKEN_KEY, t)
    localStorage.setItem(USER_KEY, JSON.stringify(user))
  }

  function clear() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  async function login(payload: {
    username: string
    password: string
    role: string
    rememberMe?: boolean
  }) {
    const data = await loginApi(payload)
    setSession(data.token, data.userInfo)
    return data
  }

  async function fetchUserInfo() {
    const data = await userInfoApi()
    userInfo.value = data
    localStorage.setItem(USER_KEY, JSON.stringify(data))
    return data
  }

  async function logout() {
    try {
      await logoutApi()
    } finally {
      clear()
    }
  }

  return { token, userInfo, isLogin, role, login, logout, fetchUserInfo, clear }
})

function readUser(): UserInfo | null {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as UserInfo
  } catch {
    return null
  }
}
