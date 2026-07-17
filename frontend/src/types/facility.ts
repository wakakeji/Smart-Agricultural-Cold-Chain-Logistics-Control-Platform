export interface FacilityItem {
  id: number
  name: string
  type: 'COLD_STORAGE' | 'REFRIGERATED_VEHICLE' | string
  lng: number
  lat: number
  status: 'ONLINE' | 'OFFLINE' | 'ALARM' | string
  currentTemp?: number
  currentHumidity?: number
  capacity?: number
  usedCapacity?: number
  loadRate?: number
  address?: string
  plateNo?: string
  driverName?: string
  driverPhone?: string
  speed?: number
  updateTime?: string
}

export interface FacilityListData {
  coldStorages: FacilityItem[]
  vehicles: FacilityItem[]
  total: { storages: number; vehicles: number }
}

export interface SensorDevice {
  sensorId: number
  facilityId: number
  name: string
  type: string
  model?: string
  status: number
}

export interface FacilityDetail {
  facility: FacilityItem
  sensors: SensorDevice[]
}

export interface TrackPoint {
  lng: number
  lat: number
  speed?: number
  reportTime: string
}
