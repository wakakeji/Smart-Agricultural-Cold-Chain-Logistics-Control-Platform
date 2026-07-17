import { get, post } from '@/utils/request'

export function fetchDictMap() {
  return get<Record<string, Record<string, string>>>('/system/dicts/map')
}

export function fetchDictOptions(type: string) {
  return get<{ label: string; value: string }[]>('/system/dicts/options', { type })
}

export function refreshDictCache() {
  return post<void>('/system/dicts/refresh')
}
