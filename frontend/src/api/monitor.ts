import { get } from '@/utils/request'
import type { MonitorFacility, SensorListData, TrendPoint } from '@/types/monitor'

export function fetchSensors(params?: {
  facilityId?: number
  type?: string
  status?: string
  withTrend?: boolean
}) {
  return get<SensorListData>('/monitor/sensors', params)
}

export function fetchSensorHistory(id: number, hours = 24) {
  return get<TrendPoint[]>(`/monitor/sensor/${id}/history`, { hours })
}

export function fetchMonitorFacilities() {
  return get<MonitorFacility[]>('/monitor/facilities')
}
