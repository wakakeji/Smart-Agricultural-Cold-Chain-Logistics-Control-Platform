import { get } from '@/utils/request'

export function fetchLossOverview() {
  return get<Record<string, unknown>>('/loss/overview')
}

export function fetchLossTrend() {
  return get<Record<string, unknown>[]>('/loss/trend')
}

export function fetchLossDetail() {
  return get<Record<string, unknown>[]>('/loss/detail')
}
