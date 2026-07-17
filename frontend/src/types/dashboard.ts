export interface KpiItem {
  value: number
  unit: string
  trend: string
  change: number
}

export interface ChartPoint {
  name: string
  value: number
}

export interface DashboardOverview {
  kpi: Record<string, KpiItem>
  charts: {
    tempTrend: ChartPoint[]
    alarmDistribution: ChartPoint[]
    transportStats: ChartPoint[]
  }
}

export interface RealtimeAlarm {
  alarmId: number
  type: string
  level: string
  sourceName: string
  content: string
  status: string
  createTime: string
}
