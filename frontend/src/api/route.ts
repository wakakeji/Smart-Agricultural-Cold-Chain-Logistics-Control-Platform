import { get } from '@/utils/request'

export function planRoute(params?: { origin?: string; destination?: string }) {
  return get<Record<string, unknown>>('/route/plan', params)
}

export function fetchTraffic() {
  return get<Record<string, unknown>[]>('/route/traffic')
}
