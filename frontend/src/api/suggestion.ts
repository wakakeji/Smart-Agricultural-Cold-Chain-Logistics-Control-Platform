import { get, put } from '@/utils/request'

export function fetchSuggestionList(params?: { status?: string; priority?: string }) {
  return get<Record<string, unknown>[]>('/suggestion/list', params)
}

export function fetchSuggestionStats() {
  return get<Record<string, number>>('/suggestion/stats')
}

export function adoptSuggestion(id: number) {
  return put<void>(`/suggestion/${id}/adopt`)
}

export function ignoreSuggestion(id: number) {
  return put<void>(`/suggestion/${id}/ignore`)
}
