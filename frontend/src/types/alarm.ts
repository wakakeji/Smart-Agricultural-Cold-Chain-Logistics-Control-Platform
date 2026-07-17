export interface AlarmRecord {
  alarmId: number
  type: string
  level: string
  sourceId?: number
  sourceName?: string
  content: string
  currentValue?: number
  threshold?: number
  status: string
  handler?: string
  handleRemark?: string
  handleTime?: string
  createTime?: string
}

export interface AlarmStats {
  total: number
  pending: number
  processing: number
  resolved: number
  ignored: number
  byLevel: Record<string, number>
  byType: Record<string, number>
}
