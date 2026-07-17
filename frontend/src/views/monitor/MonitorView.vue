<template>
  <div class="page">
    <el-card shadow="never" class="toolbar-card">
      <div class="toolbar">
        <el-select v-model="facilityId" clearable filterable placeholder="区域/设施筛选" style="width: 220px" @change="reload">
          <el-option v-for="f in facilities" :key="f.facilityId" :label="f.name" :value="f.facilityId" />
        </el-select>
        <el-select v-model="type" clearable placeholder="传感器类型" style="width: 140px" @change="reload">
          <el-option v-for="t in typeOptions" :key="t.value" :label="t.label" :value="t.value" />
        </el-select>
        <el-select v-model="status" clearable placeholder="状态" style="width: 120px" @change="reload">
          <el-option label="在线" value="ONLINE" />
          <el-option label="离线" value="OFFLINE" />
          <el-option label="故障" value="ERROR" />
        </el-select>
        <el-input v-model="keyword" clearable placeholder="搜索传感器名称" style="width: 180px" />
        <el-select v-model="refreshSec" style="width: 150px" @change="resetTimer">
          <el-option :value="5" label="自动刷新 5秒" />
          <el-option :value="10" label="自动刷新 10秒" />
          <el-option :value="30" label="自动刷新 30秒" />
          <el-option :value="0" label="关闭自动刷新" />
        </el-select>
        <el-button type="primary" :loading="loading" @click="manualRefresh">刷新</el-button>
        <el-button @click="exportCsv">Excel导出</el-button>
        <span class="meta">共 {{ filtered.length }} / {{ total }} 个 · {{ refreshHint }}</span>
      </div>
    </el-card>

    <div class="layout">
      <div class="main" v-loading="loading">
        <el-row :gutter="16">
          <el-col v-for="item in paged" :key="item.sensorId" :xs="24" :sm="12" :md="12" :lg="8">
            <el-card shadow="hover" class="sensor-card" :class="item.status.toLowerCase()" @click="toggleExpand(item)">
              <div class="head">
                <div>
                  <div class="name">{{ item.name }}</div>
                  <div class="sub">{{ item.facilityName }} · {{ dictLabel('sensor_type', item.type) }}</div>
                </div>
                <span class="dot" :class="item.status.toLowerCase()" />
              </div>
              <div class="metrics">
                <div><b>{{ fmt2(item.temperature) }}</b><span>℃</span></div>
                <div><b>{{ fmt2(item.humidity) }}</b><span>%</span></div>
                <div><b>{{ Math.round(item.co2) }}</b><span>ppm</span></div>
              </div>
              <div class="foot">
                <span>电量 {{ item.battery }}%</span>
                <span>{{ formatTime(item.updateTime) }}</span>
              </div>
              <div v-if="expandedId === item.sensorId" class="trend" @click.stop>
                <div :ref="(el) => setChartRef(item.sensorId, el as HTMLElement)" class="chart" />
              </div>
            </el-card>
          </el-col>
        </el-row>
        <div class="pager">
          <el-pagination
            v-model:current-page="page"
            v-model:page-size="size"
            layout="total, prev, pager, next"
            :total="filtered.length"
            :page-sizes="[12, 24, 48]"
            @size-change="page = 1"
          />
        </div>
      </div>

      <aside class="alarm-side">
        <div class="side-title">最近报警</div>
        <el-empty v-if="!alarms.length" description="暂无报警" :image-size="56" />
        <div v-for="a in alarms" :key="a.alarmId" class="alarm-item" :class="a.level?.toLowerCase()">
          <div class="alarm-top">
            <el-tag size="small" :type="alarmLevelType(a.level)">{{ dictLabel('alarm_level', a.level) }}</el-tag>
            <span class="time">{{ formatTime(a.createTime) }}</span>
          </div>
          <div class="alarm-src">{{ a.sourceName }}</div>
          <div class="alarm-content">{{ a.content }}</div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { fetchMonitorFacilities, fetchSensorHistory, fetchSensors } from '@/api/monitor'
import { fetchRealtimeAlarms } from '@/api/dashboard'
import type { MonitorFacility, SensorCard } from '@/types/monitor'
import type { RealtimeAlarm } from '@/types/dashboard'
import { dictLabel, dictOptions, ensureDicts } from '@/utils/dict'

const loading = ref(false)
const dictReady = ref(false)
const typeOptions = computed(() => {
  if (!dictReady.value) return []
  const opts = dictOptions('sensor_type')
  return opts.length
    ? opts
    : [
        { value: 'TEMP', label: '温度' },
        { value: 'HUMIDITY', label: '湿度' },
        { value: 'CO2', label: 'CO2' },
        { value: 'VIBRATION', label: '振动' },
        { value: 'LIGHT', label: '光照' },
      ]
})
const list = ref<SensorCard[]>([])
const total = ref(0)
const facilities = ref<MonitorFacility[]>([])
const facilityId = ref<number>()
const type = ref('')
const status = ref('')
const keyword = ref('')
const page = ref(1)
const size = ref(12)
const expandedId = ref<number>()
const refreshSec = ref(5)
const refreshHint = ref('5s 自动刷新')
const alarms = ref<RealtimeAlarm[]>([])
const chartMap = new Map<number, echarts.ECharts>()
const chartEls = new Map<number, HTMLElement>()
let timer: number | undefined

const filtered = computed(() => {
  const k = keyword.value.trim()
  if (!k) return list.value
  return list.value.filter((i) => i.name.includes(k) || i.facilityName.includes(k))
})

const paged = computed(() => {
  const start = (page.value - 1) * size.value
  return filtered.value.slice(start, start + size.value)
})

function fmt2(n?: number) {
  return n == null || Number.isNaN(n) ? '-' : Number(n).toFixed(2)
}

function formatTime(t?: string) {
  return t ? t.replace('T', ' ').slice(11, 19) : ''
}

function alarmLevelType(level?: string) {
  if (level === 'CRITICAL') return 'danger'
  if (level === 'WARNING') return 'warning'
  return 'info'
}

function setChartRef(id: number, el: HTMLElement | null) {
  if (el) chartEls.set(id, el)
}

async function loadAlarms() {
  try {
    alarms.value = await fetchRealtimeAlarms(12)
  } catch {
    alarms.value = []
  }
}

async function reload() {
  loading.value = true
  try {
    const data = await fetchSensors({
      facilityId: facilityId.value,
      type: type.value || undefined,
      status: status.value || undefined,
    })
    list.value = data.records
    total.value = data.total
    await loadAlarms()
    if (expandedId.value) {
      await renderTrend(expandedId.value)
    }
  } finally {
    loading.value = false
  }
}

async function manualRefresh() {
  await reload()
  ElMessage.success(`已刷新传感器数据（${list.value.length} 条）`)
}

function resetTimer() {
  if (timer) clearInterval(timer)
  timer = undefined
  if (refreshSec.value > 0) {
    refreshHint.value = `${refreshSec.value}s 自动刷新`
    timer = window.setInterval(reload, refreshSec.value * 1000)
  } else {
    refreshHint.value = '已关闭自动刷新'
  }
}

function exportCsv() {
  if (!filtered.value.length) {
    ElMessage.warning('暂无数据可导出')
    return
  }
  const header = '传感器,设施,类型,温度℃,湿度%,CO2ppm,电量%,状态,更新时间\n'
  const body = filtered.value
    .map((i) =>
      [
        i.name,
        i.facilityName,
        i.type,
        fmt2(i.temperature),
        fmt2(i.humidity),
        Math.round(i.co2),
        i.battery,
        i.status,
        formatTime(i.updateTime),
      ].join(','),
    )
    .join('\n')
  const blob = new Blob(['\ufeff' + header + body], { type: 'text/csv;charset=utf-8' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `sensor_realtime_${Date.now()}.csv`
  a.click()
  ElMessage.success('已导出 Excel 兼容 CSV')
}

async function toggleExpand(item: SensorCard) {
  if (expandedId.value === item.sensorId) {
    expandedId.value = undefined
    return
  }
  expandedId.value = item.sensorId
  await nextTick()
  await renderTrend(item.sensorId)
}

async function renderTrend(sensorId: number) {
  const el = chartEls.get(sensorId)
  if (!el) return
  const points = await fetchSensorHistory(sensorId, 24)
  let chart = chartMap.get(sensorId)
  if (!chart) {
    chart = echarts.init(el)
    chartMap.set(sensorId, chart)
  }
  chart.setOption({
    grid: { left: 36, right: 12, top: 24, bottom: 24 },
    xAxis: { type: 'category', data: points.map((p) => p.time), axisLabel: { fontSize: 10 } },
    yAxis: { type: 'value', name: '℃', splitLine: { lineStyle: { type: 'dashed' } } },
    series: [{ type: 'line', smooth: true, data: points.map((p) => p.value), areaStyle: { opacity: 0.12 }, itemStyle: { color: '#409EFF' } }],
    tooltip: { trigger: 'axis' },
  })
  chart.resize()
}

onMounted(async () => {
  await ensureDicts()
  dictReady.value = true
  facilities.value = await fetchMonitorFacilities()
  await reload()
  resetTimer()
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  chartMap.forEach((c) => c.dispose())
  chartMap.clear()
})
</script>

<style scoped>
.toolbar-card { margin-bottom: 12px; }
.toolbar { display: flex; flex-wrap: wrap; gap: 8px; align-items: center; }
.meta { color: #909399; font-size: 13px; }
.layout { display: flex; gap: 12px; align-items: flex-start; }
.main { flex: 1; min-width: 0; }
.alarm-side {
  width: 280px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #ebeef5;
  padding: 12px;
  max-height: calc(100vh - 220px);
  overflow: auto;
}
.side-title { font-weight: 600; margin-bottom: 10px; }
.alarm-item {
  border-left: 3px solid #909399;
  padding: 8px 0 8px 10px;
  margin-bottom: 8px;
  border-bottom: 1px solid #f5f7fa;
}
.alarm-item.critical { border-left-color: #f56c6c; }
.alarm-item.warning { border-left-color: #e6a23c; }
.alarm-top { display: flex; justify-content: space-between; align-items: center; gap: 6px; }
.time { color: #909399; font-size: 12px; }
.alarm-src { font-size: 13px; font-weight: 600; margin-top: 4px; }
.alarm-content { font-size: 12px; color: #606266; margin-top: 2px; line-height: 1.4; }
.sensor-card { margin-bottom: 16px; cursor: pointer; border-left: 4px solid #409EFF; }
.sensor-card.offline { border-left-color: #909399; }
.sensor-card.error { border-left-color: #F56C6C; }
.head { display: flex; justify-content: space-between; gap: 8px; }
.name { font-weight: 600; font-size: 14px; }
.sub { color: #909399; font-size: 12px; margin-top: 4px; }
.dot {
  width: 10px; height: 10px; border-radius: 50%; background: #67C23A; margin-top: 4px; flex-shrink: 0;
}
.dot.offline { background: #909399; }
.dot.error { background: #F56C6C; }
.metrics {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px;
  margin-top: 14px; text-align: center;
}
.metrics b { display: block; font-size: 22px; line-height: 1.2; }
.metrics span { color: #909399; font-size: 12px; }
.foot {
  margin-top: 12px; display: flex; justify-content: space-between;
  color: #909399; font-size: 12px;
}
.trend { margin-top: 10px; border-top: 1px dashed #ebeef5; padding-top: 8px; }
.chart { height: 160px; width: 100%; }
.pager { margin-top: 8px; display: flex; justify-content: flex-end; }
@media (max-width: 1100px) {
  .layout { flex-direction: column; }
  .alarm-side { width: 100%; max-height: 240px; }
}
</style>
