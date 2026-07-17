import { get, post } from '@/utils/request'

export function fetchApiCatalog() {
  return get<Record<string, unknown>[]>('/api-manage/list')
}

export function fetchApiKeys() {
  return get<Record<string, unknown>[]>('/api-manage/keys')
}

export function fetchApiStats() {
  return get<Record<string, unknown>>('/api-manage/stats')
}

export function generateApiKey(name?: string) {
  return post<Record<string, unknown>>('/api-manage/key/generate', name ? { name } : {})
}
