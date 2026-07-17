import { get, put } from '@/utils/request'
import type { PageResult } from '@/types/api'
import type { AlarmRecord, AlarmStats } from '@/types/alarm'

export function fetchAlarmList(params: Record<string, unknown>) {
  return get<PageResult<AlarmRecord>>('/alarm/list', params)
}

export function fetchAlarmDetail(id: number) {
  return get<AlarmRecord>(`/alarm/${id}`)
}

export function handleAlarm(id: number, data: { status: string; handleRemark?: string }) {
  return put<void>(`/alarm/${id}/handle`, data)
}

export function batchHandleAlarm(data: { ids: number[]; status: string; handleRemark?: string }) {
  return put<void>('/alarm/batch-handle', data)
}

export function fetchAlarmStats(params?: { startTime?: string; endTime?: string }) {
  return get<AlarmStats>('/alarm/stats', params)
}
