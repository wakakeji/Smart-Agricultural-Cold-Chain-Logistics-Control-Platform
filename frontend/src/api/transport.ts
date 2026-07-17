import { get } from '@/utils/request'

export interface TransportOrder {
  orderId: number
  orderNo: string
  batchId?: number
  vehicleId?: number
  vehicleName?: string
  route?: string
  status: string
  startTime?: string
  currentTemp?: number
  currentHumidity?: number
  lng?: number
  lat?: number
  speed?: number
  progress?: number
}

export interface TransportRealtime {
  order: TransportOrder
  track: Record<string, unknown>[]
  eta?: string
  lastUpdate?: string
}

export function fetchOngoingTransport() {
  return get<TransportOrder[]>('/transport/ongoing')
}

export function fetchTransportList(status?: string) {
  return get<TransportOrder[]>('/transport/list', status ? { status } : undefined)
}

export function fetchTransportRealtime(id: number) {
  return get<TransportRealtime>(`/transport/${id}/realtime`)
}

export function fetchTransportTrack(id: number) {
  return get<Record<string, unknown>[]>(`/transport/${id}/track`)
}
