<template>
  <div class="screen" :class="{ fullscreen: isFullscreen }" ref="rootRef">
    <header class="top">
      <div class="title">智慧农业冷链物流 · 指挥大屏</div>
      <div class="meta">
        <span>{{ clock }}</span>
        <span>数据刷新 {{ refreshSec }}s</span>
        <el-button size="small" @click="toggleFullscreen">{{ isFullscreen ? '退出全屏' : '全屏展示' }}</el-button>
        <el-button size="small" @click="loadAll">刷新</el-button>
      </div>
    </header>

    <section class="kpi-row">
      <div v-for="item in kpiCards" :key="item.key" class="kpi-card">
        <div class="kpi-label">{{ item.label }}</div>
        <div class="kpi-value">
          {{ formatValue(item.data?.value) }}
          <small>{{ item.data?.unit }}</small>
        </div>
        <div class="kpi-trend" :class="item.data?.trend">
          {{ item.data?.trend === 'up' ? '↑' : '↓' }} {{ item.data?.change }}
        </div>
      </div>
    </section>

    <section class="charts">
      <div class="panel"><div ref="tempRef" class="chart" /></div>
      <div class="panel"><div ref="alarmRef" class="chart" /></div>
      <div class="panel"><div ref="transportRef" class="chart" /></div>
    </section>

    <section class="alarms panel">
      <div class="panel-title">实时告警</div>
      <div class="alarm-scroll">
        <div v-for="a in alarms" :key="a.alarmId" class="alarm-item" :class="a.level.toLowerCase()">
          <el-tag size="small" :type="levelType(a.level)">{{ dictLabel('alarm_level', a.level) }}</el-tag>
          <span class="src">{{ a.sourceName }}</span>
          <span class="content">{{ a.content }}</span>
          <span class="time">{{ formatTime(a.createTime) }}</span>
        </div>
        <el-empty v-if="!alarms.length" description="暂无告警" :image-size="60" />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import * as echarts from 'echarts'
import { fetchDashboardOverview, fetchRealtimeAlarms } from '@/api/dashboard'
import type { DashboardOverview, KpiItem, RealtimeAlarm } from '@/types/dashboard'
import { dictLabel, ensureDicts } from '@/utils/dict'

const rootRef = ref<HTMLElement>()
const tempRef = ref<HTMLElement>()
const alarmRef = ref<HTMLElement>()
const transportRef = ref<HTMLElement>()
const overview = ref<DashboardOverview | null>(null)
const alarms = ref<RealtimeAlarm[]>([])
const clock = ref('')
const isFullscreen = ref(false)
const refreshSec = 15

let tempChart: echarts.ECharts | null = null
let alarmChart: echarts.ECharts | null = null
let transportChart: echarts.ECharts | null = null
let clockTimer: number | undefined
let dataTimer: number | undefined
let alarmTimer: number | undefined

const kpiCards = computed(() => {
  const k = overview.value?.kpi || {}
  return [
    { key: 'tempComplianceRate', label: '温度达标率', data: k.tempComplianceRate as KpiItem | undefined },
    { key: 'deviceOnlineRate', label: '设备在线率', data: k.deviceOnlineRate as KpiItem | undefined },
    { key: 'transportingOrders', label: '运输中订单', data: k.transportingOrders as KpiItem | undefined },
    { key: 'totalAlarms', label: '预警总数', data: k.totalAlarms as KpiItem | undefined },
    { key: 'avgQualityScore', label: '今日预测品质', data: k.avgQualityScore as KpiItem | undefined },
    { key: 'carbonTotal', label: '碳排放累计', data: k.carbonTotal as KpiItem | undefined },
  ]
})

function formatValue(v?: number) {
  if (v == null) return '-'
  return Number.isInteger(v) ? String(v) : v.toFixed(1)
}

function formatTime(t?: string) {
  return t ? t.replace('T', ' ').slice(11, 19) : ''
}

function levelType(level: string) {
  if (level === 'CRITICAL') return 'danger'
  if (level === 'WARNING') return 'warning'
  return 'info'
}

function darkOptionBase() {
  return {
    backgroundColor: 'transparent',
    textStyle: { color: '#cbd5e1' },
    grid: { left: 40, right: 20, top: 40, bottom: 30 },
  }
}

function renderCharts() {
  const charts = overview.value?.charts
  if (!charts) return
  if (tempRef.value) {
    tempChart ||= echarts.init(tempRef.value, 'dark')
    tempChart.setOption({
      ...darkOptionBase(),
      title: { text: '24h 温度趋势', left: 8, textStyle: { color: '#e2e8f0', fontSize: 14 } },
      xAxis: { type: 'category', data: charts.tempTrend.map((i) => i.name), axisLabel: { color: '#94a3b8' } },
      yAxis: { type: 'value', name: '℃', axisLabel: { color: '#94a3b8' }, splitLine: { lineStyle: { color: '#1e293b' } } },
      series: [{ type: 'line', smooth: true, data: charts.tempTrend.map((i) => i.value), areaStyle: { opacity: 0.15 }, itemStyle: { color: '#38bdf8' } }],
      tooltip: { trigger: 'axis' },
    })
  }
  if (alarmRef.value) {
    alarmChart ||= echarts.init(alarmRef.value, 'dark')
    alarmChart.setOption({
      ...darkOptionBase(),
      title: { text: '告警类型分布', left: 8, textStyle: { color: '#e2e8f0', fontSize: 14 } },
      series: [{
        type: 'pie',
        radius: ['35%', '65%'],
        data: charts.alarmDistribution.map((i) => ({ name: i.name, value: i.value })),
        label: { color: '#cbd5e1' },
      }],
      tooltip: { trigger: 'item' },
    })
  }
  if (transportRef.value) {
    transportChart ||= echarts.init(transportRef.value, 'dark')
    transportChart.setOption({
      ...darkOptionBase(),
      title: { text: '运输状态统计', left: 8, textStyle: { color: '#e2e8f0', fontSize: 14 } },
      xAxis: { type: 'category', data: charts.transportStats.map((i) => i.name), axisLabel: { color: '#94a3b8' } },
      yAxis: { type: 'value', axisLabel: { color: '#94a3b8' }, splitLine: { lineStyle: { color: '#1e293b' } } },
      series: [{ type: 'bar', data: charts.transportStats.map((i) => i.value), itemStyle: { color: '#34d399', borderRadius: [4, 4, 0, 0] } }],
      tooltip: { trigger: 'axis' },
    })
  }
}

async function loadOverview() {
  overview.value = await fetchDashboardOverview()
  await nextTick()
  renderCharts()
}

async function loadAlarms() {
  alarms.value = await fetchRealtimeAlarms(12)
}

async function loadAll() {
  await Promise.all([loadOverview(), loadAlarms()])
}

async function toggleFullscreen() {
  if (!rootRef.value) return
  if (!document.fullscreenElement) {
    await rootRef.value.requestFullscreen()
    isFullscreen.value = true
  } else {
    await document.exitFullscreen()
    isFullscreen.value = false
  }
  await nextTick()
  tempChart?.resize()
  alarmChart?.resize()
  transportChart?.resize()
}

function tickClock() {
  const d = new Date()
  clock.value = d.toLocaleString('zh-CN', { hour12: false })
}

function onResize() {
  tempChart?.resize()
  alarmChart?.resize()
  transportChart?.resize()
}

onMounted(async () => {
  await ensureDicts()
  tickClock()
  clockTimer = window.setInterval(tickClock, 1000)
  await loadAll()
  dataTimer = window.setInterval(loadOverview, refreshSec * 1000)
  alarmTimer = window.setInterval(loadAlarms, 10000)
  window.addEventListener('resize', onResize)
  document.addEventListener('fullscreenchange', () => {
    isFullscreen.value = !!document.fullscreenElement
  })
})

onUnmounted(() => {
  if (clockTimer) clearInterval(clockTimer)
  if (dataTimer) clearInterval(dataTimer)
  if (alarmTimer) clearInterval(alarmTimer)
  window.removeEventListener('resize', onResize)
  tempChart?.dispose()
  alarmChart?.dispose()
  transportChart?.dispose()
})
</script>

<style scoped>
.screen {
  --bg: #0a1628;
  background: var(--bg);
  color: #e2e8f0;
  min-height: calc(100vh - 120px);
  padding: 16px;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.screen.fullscreen {
  min-height: 100vh;
  border-radius: 0;
  padding: 20px 24px;
}
.top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.title {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 1px;
  background: linear-gradient(90deg, #38bdf8, #34d399);
  -webkit-background-clip: text;
  color: transparent;
}
.meta { display: flex; align-items: center; gap: 12px; color: #94a3b8; font-size: 13px; }
.kpi-row {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 10px;
}
.kpi-card {
  background: linear-gradient(180deg, #12233d, #0f1c30);
  border: 1px solid #1e3a5f;
  border-radius: 10px;
  padding: 12px;
}
.kpi-label { color: #94a3b8; font-size: 12px; }
.kpi-value { font-size: 26px; font-weight: 700; margin-top: 6px; }
.kpi-value small { font-size: 12px; margin-left: 4px; color: #94a3b8; }
.kpi-trend { margin-top: 4px; font-size: 12px; }
.kpi-trend.up { color: #34d399; }
.kpi-trend.down { color: #f87171; }
.charts {
  display: grid;
  grid-template-columns: 1.4fr 1fr 1fr;
  gap: 10px;
  min-height: 280px;
}
.panel {
  background: #0f1c30;
  border: 1px solid #1e3a5f;
  border-radius: 10px;
  padding: 8px;
}
.chart { width: 100%; height: 280px; }
.panel-title { padding: 6px 8px; color: #cbd5e1; font-weight: 600; }
.alarms.panel { flex: 0 0 auto; }
.alarm-scroll {
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-height: min(36vh, 320px);
  overflow-y: auto;
  height: auto;
}
.alarm-item {
  display: grid;
  grid-template-columns: minmax(72px, 90px) minmax(100px, 160px) minmax(0, 1fr) minmax(70px, 90px);
  gap: 8px;
  align-items: center;
  padding: 8px 10px;
  border-radius: 8px;
  background: #12233d;
  font-size: 13px;
}
.alarm-item.critical { border-left: 3px solid #f56c6c; }
.alarm-item.warning { border-left: 3px solid #e6a23c; }
.alarm-item.info { border-left: 3px solid #409eff; }
.src { color: #93c5fd; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.content { color: #e2e8f0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; min-width: 0; }
.time { color: #64748b; text-align: right; }
@media (max-width: 1200px) {
  .kpi-row { grid-template-columns: repeat(3, 1fr); }
  .charts { grid-template-columns: 1fr; }
  .alarm-item { grid-template-columns: 70px minmax(0, 1fr) 70px; }
  .src { display: none; }
}
@media (max-width: 768px) {
  .kpi-row { grid-template-columns: repeat(2, 1fr); }
  .alarm-item { grid-template-columns: 64px minmax(0, 1fr); }
  .time { display: none; }
}
</style>
