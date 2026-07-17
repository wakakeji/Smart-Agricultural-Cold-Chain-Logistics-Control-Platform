import { get } from '@/utils/request'
import type { DashboardOverview, RealtimeAlarm } from '@/types/dashboard'

export function fetchDashboardOverview() {
  return get<DashboardOverview>('/dashboard/overview')
}

export function fetchRealtimeAlarms(limit = 10) {
  return get<RealtimeAlarm[]>('/dashboard/realtime-alarms', { limit })
}
