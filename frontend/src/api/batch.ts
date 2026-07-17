import { get, post } from '@/utils/request'
import type { PageResult } from '@/types/api'
import type { BatchCreateResult, ProductBatch, QrCodeInfo } from '@/types/batch'

export function createBatch(data: Record<string, unknown>) {
  return post<BatchCreateResult>('/batch/create', data)
}

export function fetchBatchList(params: {
  page?: number
  size?: number
  status?: number
  keyword?: string
}) {
  return get<PageResult<ProductBatch>>('/batch/list', params)
}

export function fetchBatchQr(id: number) {
  return get<QrCodeInfo>(`/batch/${id}/qr-code`)
}
