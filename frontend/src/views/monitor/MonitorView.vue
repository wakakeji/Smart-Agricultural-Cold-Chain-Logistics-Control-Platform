<template>
  <div class="page">
    <el-card shadow="never" class="toolbar-card">
      <div class="toolbar">
        <el-select v-model="facilityId" clearable filterable placeholder="选择设施" style="width: 220px" @change="reload">
          <el-option v-for="f in facilities" :key="f.facilityId" :label="f.name" :value="f.facilityId" />
        </el-select>
        <el-select v-model="type" clearable placeholder="传感器类型" style="width: 140px" @change="reload">
          <el-option
            v-for="t in typeOptions"
            :key="t.value"
            :label="t.label"
            :value="t.value"
          />
        </el-select>
        <el-select v-model="status" clearable placeholder="状态" style="width: 120px" @change="reload">
          <el-option label="在线" value="ONLINE" />
          <el-option label="离线" value="OFFLINE" />
          <el-option label="故障" value="ERROR" />
        </el-select>
        <el-input v-model="keyword" clearable placeholder="搜索传感器名称" style="width: 200px" />
        <el-button type="primary" :loading="loading" @click="reload">刷新</el-button>
        <span class="meta">共 {{ filtered.length }} / {{ total }} 个 · {{ refreshHint }}</span>
      </div>
    </el-card>

    <el-row :gutter="16" v-loading="loading">
      <el-col v-for="item in paged" :key="item.sensorId" :xs="24" :sm="12" :md="8" :lg="8">
        <el-card shadow="hover" class="sensor-card" :class="item.status.toLowerCase()" @click="toggleExpand(item)">
          <div class="head">
            <div>
              <div class="name">{{ item.name }}</div>
              <div class="sub">{{ item.facilityName }} · {{ dictLabel('sensor_type', item.type) }}</div>
            </div>
            <span class="dot" :class="item.status.toLowerCase()" />
          </div>
          <div class="metrics">
            <div><b>{{ item.temperature?.toFixed(2) }}</b><span>℃</span></div>
            <div><b>{{ item.humidity?.toFixed(1) }}</b><span>%</span></div>
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
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import * as echarts from 'echarts'
import { fetchMonitorFacilities, fetchSensorHistory, fetchSensors } from '@/api/monitor'
import type { MonitorFacility, SensorCard } from '@/types/monitor'
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
const refreshHint = ref('5s 自动刷新')
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

function formatTime(t?: string) {
  return t ? t.replace('T', ' ').slice(11, 19) : ''
}

function setChartRef(id: number, el: HTMLElement | null) {
  if (el) chartEls.set(id, el)
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
    if (expandedId.value) {
      await renderTrend(expandedId.value)
    }
  } finally {
    loading.value = false
  }
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
  timer = window.setInterval(reload, 5000)
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
</style>
