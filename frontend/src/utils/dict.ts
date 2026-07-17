import { get } from '@/utils/request'

type DictMap = Record<string, Record<string, string>>

/** 本地兜底：字典接口失败或未加载时仍显示中文（专业术语保留） */
const LOCAL_FALLBACK: DictMap = {
  facility_status: { ONLINE: '在线', OFFLINE: '离线', ALARM: '告警' },
  facility_type: { COLD_STORAGE: '冷库', REFRIGERATED_VEHICLE: '冷藏车' },
  sensor_type: { TEMP: '温度', HUMIDITY: '湿度', CO2: 'CO2', VIBRATION: '振动', LIGHT: '光照' },
  alarm_level: { CRITICAL: '严重', WARNING: '警告', INFO: '提示' },
  alarm_type: {
    TEMP_OVER: '温度过高',
    HUMIDITY_OVER: '湿度过高',
    DEVICE_OFFLINE: '设备离线',
    VIBRATION: '震动异常',
    CO2_OVER: 'CO2过高',
  },
  alarm_status: { PENDING: '待处理', PROCESSING: '处理中', RESOLVED: '已解决', IGNORED: '已忽略' },
  transport_status: { PENDING: '待发车', TRANSPORTING: '运输中', COMPLETED: '已完成' },
  chain_status: { CONFIRMED: '已确认', PENDING: '待确认', CHAINED: '已上链' },
  network_status: { HEALTHY: '正常', DEGRADED: '降级' },
  biz_type: {
    BATCH: '批次赋码',
    BATCH_CREATE: '批次创建',
    TRACE: '追溯节点',
    SENSOR_DATA: '传感数据',
    QUALITY_REPORT: '品质报告',
  },
  carbon_source: { VEHICLE: '运输车辆', COLD_STORAGE: '冷库仓储', PACKAGE: '包装环节' },
  loss_type: { TRANSPORT: '运输', STORAGE: '仓储', PACKAGE: '包装', RETAIL: '零售', SALE: '销售' },
  suggestion_priority: { HIGH: '高', MEDIUM: '中', LOW: '低' },
  suggestion_type: {
    TEMP_CONTROL: '温控调整',
    REROUTE: '路径改道',
    PRIORITY_SALE: '优先销售',
    MAINTENANCE: '设备巡检',
    LOSS_REDUCE: '减损规范',
    CARBON: '碳排优化',
  },
  data_quality_issue: { DELAY: '上报延迟', MISSING: '数据缺失', OUTLIER: '异常尖峰' },
}

let cache: DictMap | null = null
let loading: Promise<DictMap> | null = null

export async function ensureDicts(): Promise<DictMap> {
  if (cache) return cache
  if (!loading) {
    loading = get<DictMap>('/system/dicts/map')
      .then((m) => {
        cache = { ...LOCAL_FALLBACK, ...(m || {}) }
        // 合并各类型，避免接口缺项时退回英文码
        for (const [type, items] of Object.entries(LOCAL_FALLBACK)) {
          cache[type] = { ...items, ...(m?.[type] || {}) }
        }
        return cache
      })
      .catch(() => {
        cache = { ...LOCAL_FALLBACK }
        return cache
      })
      .finally(() => {
        loading = null
      })
  }
  return loading
}

export function dictLabel(type: string, code?: string | number | null, fallback?: string): string {
  if (code == null || code === '') return fallback ?? ''
  const key = String(code)
  return cache?.[type]?.[key] ?? LOCAL_FALLBACK[type]?.[key] ?? fallback ?? key
}

export function dictOptions(type: string): { label: string; value: string }[] {
  const m = cache?.[type] || LOCAL_FALLBACK[type] || {}
  return Object.entries(m).map(([value, label]) => ({ value, label }))
}

export function clearDictCache() {
  cache = null
}
