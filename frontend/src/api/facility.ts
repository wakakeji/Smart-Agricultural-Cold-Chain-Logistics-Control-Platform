import { get } from '@/utils/request'
import type { FacilityDetail, FacilityListData, TrackPoint } from '@/types/facility'

export function fetchFacilities(params?: {
  type?: string
  status?: string
  refresh?: boolean
}) {
  return get<FacilityListData>('/facilities/list', params)
}

export function fetchFacilityDetail(id: number) {
  return get<FacilityDetail>(`/facilities/${id}/detail`)
}

export function fetchVehicleTrack(id: number, start?: string, end?: string) {
  return get<TrackPoint[]>(`/vehicle/${id}/track`, { start, end })
}
