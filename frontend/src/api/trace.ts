import { get } from '@/utils/request'
import type { TraceQueryResult, TraceVerifyResult } from '@/types/trace'

export function queryTrace(params: { batchNo?: string; productName?: string }) {
  return get<TraceQueryResult>('/trace/query', params)
}

export function verifyTrace(params: { batchNo?: string; txHash?: string }) {
  return get<TraceVerifyResult>('/trace/verify', params)
}

export function fetchH5Trace(batchNo: string) {
  return get<Record<string, unknown>>('/h5/trace', { batchNo })
}
