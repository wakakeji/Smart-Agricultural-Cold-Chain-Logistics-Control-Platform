import { get } from '@/utils/request'

export function fetchDataQualityOverview() {
  return get<Record<string, unknown>>('/data-quality/overview')
}
