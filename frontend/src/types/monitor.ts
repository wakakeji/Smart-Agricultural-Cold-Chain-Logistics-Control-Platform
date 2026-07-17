export interface TrendPoint {
  time: string
  value: number
}

export interface SensorCard {
  sensorId: number
  facilityId: number
  facilityName: string
  name: string
  type: string
  temperature: number
  humidity: number
  co2: number
  battery: number
  status: string
  updateTime: string
  trendData?: TrendPoint[]
}

export interface SensorListData {
  records: SensorCard[]
  total: number
}

export interface MonitorFacility {
  facilityId: number
  name: string
  type: string
  status: string
}
