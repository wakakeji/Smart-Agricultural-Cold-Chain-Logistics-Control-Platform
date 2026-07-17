import { get } from '@/utils/request'

export function fetchCarbonOverview() {
  return get<Record<string, unknown>>('/carbon/overview')
}

export function fetchCarbonTrend() {
  return get<Record<string, unknown>[]>('/carbon/trend')
}

export function fetchCarbonDetail() {
  return get<Record<string, unknown>[]>('/carbon/detail')
}
