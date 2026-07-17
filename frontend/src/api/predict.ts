import { get } from '@/utils/request'

export function fetchPredictModelInfo() {
  return get<Record<string, unknown>>('/predict/model-info')
}

export function runPredict(params?: { batchId?: number; batchNo?: string }) {
  return get<Record<string, unknown>>('/predict/quality', params)
}

export function fetchPredictHistory(batchId?: number) {
  return get<Record<string, unknown>[]>('/predict/history', batchId != null ? { batchId } : undefined)
}
