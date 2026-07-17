import axios from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResult } from '@/types/api'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

request.interceptors.request.use((config) => {
  const store = useUserStore()
  if (store.token) {
    config.headers.Authorization = `Bearer ${store.token}`
  }
  return config
})

request.interceptors.response.use(
  (res) => {
    const body = res.data as ApiResult<unknown>
    if (body.code !== 200) {
      ElMessage.error(body.message || '请求失败')
      if (body.code === 401 && !router.currentRoute.value.meta.public) {
        const store = useUserStore()
        store.clear()
        router.push('/login')
      }
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return res
  },
  (err) => {
    const msg = err.response?.data?.message || err.message || '网络异常'
    if (err.response?.status === 401 && !router.currentRoute.value.meta.public) {
      const store = useUserStore()
      store.clear()
      router.push('/login')
    }
    ElMessage.error(msg)
    return Promise.reject(err)
  },
)

export async function get<T>(url: string, params?: object): Promise<T> {
  const res = await request.get<ApiResult<T>>(url, { params })
  return res.data.data
}

export async function post<T>(url: string, data?: object): Promise<T> {
  const res = await request.post<ApiResult<T>>(url, data)
  return res.data.data
}

export async function put<T>(url: string, data?: object): Promise<T> {
  const res = await request.put<ApiResult<T>>(url, data)
  return res.data.data
}

export default request
