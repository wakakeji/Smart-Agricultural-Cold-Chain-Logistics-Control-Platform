<template>
  <div class="page">
    <el-card shadow="never" class="toolbar-card">
      <div class="toolbar">
        <el-button type="primary" :loading="loading" @click="manualRefresh">刷新</el-button>
        <el-button @click="exportCsv">导出报表</el-button>
        <el-button type="warning" plain @click="retransmit">模拟数据重传</el-button>
        <span class="meta">EM-005 网络传输质量监控 · 完整度/准确度/及时性/延迟</span>
      </div>
    </el-card>

    <el-card shadow="never" class="stats-card">
      <div class="stats">
        <div class="stat"><b>{{ overview?.score ?? 0 }}</b><span>质量评分</span></div>
        <div class="stat"><b>{{ overview?.onlineRate ?? 0 }}%</b><span>在线率</span></div>
        <div class="stat"><b>{{ overview?.completeness ?? 0 }}%</b><span>完整度</span></div>
        <div class="stat"><b>{{ overview?.accuracy ?? 0 }}%</b><span>准确度</span></div>
        <div class="stat"><b>{{ overview?.timeliness ?? 0 }}%</b><span>及时性</span></div>
        <div class="stat"><b>{{ overview?.avgLatencyMs ?? 0 }}ms</b><span>平均延迟</span></div>
      </div>
      <div class="extra" v-if="overview">
        <span>传感器 {{ overview.sensorOnline }}/{{ overview.sensorTotal }}</span>
        <span>P99 延迟 {{ overview.p99LatencyMs }}ms</span>
        <span v-if="lastRefresh">最近刷新 {{ lastRefresh }}</span>
      </div>
    </el-card>

    <el-row :gutter="12">
      <el-col :xs="24" :md="14">
        <el-card shadow="never">
          <template #header><strong>12小时质量趋势</strong></template>
          <div ref="chartRef" class="chart" />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="10">
        <el-card shadow="never">
          <template #header>
            <div class="head-row">
              <strong>质量问题</strong>
              <el-tag size="small" type="info">可标记重传</el-tag>
            </div>
          </template>
          <el-table :data="issues" v-loading="loading" stripe table-layout="auto">
            <el-table-column prop="type" label="类型" min-width="100">
              <template #default="{ row }">
                <el-tag :type="issueType(String(row.type))" size="small">
                  {{ dictLabel('data_quality_issue', String(row.type)) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="count" label="数量" min-width="70" />
            <el-table-column prop="desc" label="描述" min-width="180" show-overflow-tooltip />
            <el-table-column label="操作" min-width="90">
              <template #default="{ row }">
                <el-button link type="primary" @click="fixIssue(row)">重传</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-alert
            v-if="retransmitLog"
            class="mt"
            type="success"
            :closable="false"
            :title="retransmitLog"
          />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { fetchDataQualityOverview } from '@/api/dataQuality'
import { dictLabel, ensureDicts } from '@/utils/dict'

const loading = ref(false)
const overview = ref<Record<string, unknown> | null>(null)
const lastRefresh = ref('')
const retransmitLog = ref('')
const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

const issues = computed(() => (overview.value?.issues as Record<string, unknown>[]) || [])
const trend = computed(() => (overview.value?.trend as { hour: string; completeness: number; latencyMs: number }[]) || [])

function issueType(t: string) {
  if (t === 'DELAY') return 'warning'
  if (t === 'MISSING') return 'danger'
  return 'info'
}

function renderChart() {
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['完整度%', '延迟ms'] },
    grid: { left: 40, right: 40, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: trend.value.map((p) => p.hour) },
    yAxis: [
      { type: 'value', name: '完整度', min: 90, max: 100 },
      { type: 'value', name: '延迟', min: 0 },
    ],
    series: [
      { name: '完整度%', type: 'line', smooth: true, data: trend.value.map((p) => p.completeness) },
      { name: '延迟ms', type: 'bar', yAxisIndex: 1, data: trend.value.map((p) => p.latencyMs), itemStyle: { color: '#91cc75' } },
    ],
  })
}

async function load() {
  loading.value = true
  try {
    overview.value = await fetchDataQualityOverview()
    lastRefresh.value = new Date().toLocaleTimeString()
    await nextTick()
    renderChart()
  } finally {
    loading.value = false
  }
}

async function manualRefresh() {
  await load()
  ElMessage.success('数据质量指标已刷新')
}

function exportCsv() {
  if (!overview.value) {
    ElMessage.warning('暂无数据')
    return
  }
  const o = overview.value
  const header = '指标,数值\n'
  const rows = [
    `质量评分,${o.score}`,
    `在线率%,${o.onlineRate}`,
    `完整度%,${o.completeness}`,
    `准确度%,${o.accuracy}`,
    `及时性%,${o.timeliness}`,
    `平均延迟ms,${o.avgLatencyMs}`,
    `P99延迟ms,${o.p99LatencyMs}`,
  ]
  const issueLines = issues.value.map((i) => `问题-${i.type},${i.count},${String(i.desc || '').replaceAll(',', '，')}`)
  const blob = new Blob(['\ufeff' + header + rows.join('\n') + '\n\n类型,数量,描述\n' + issueLines.join('\n')], {
    type: 'text/csv;charset=utf-8',
  })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `data_quality_${Date.now()}.csv`
  a.click()
  ElMessage.success('已导出数据质量报表')
}

function retransmit() {
  retransmitLog.value = `已触发模拟重传任务 · ${new Date().toLocaleTimeString()}（演示：缺失/延迟点位补传）`
  ElMessage.success('已模拟发起数据重传')
}

function fixIssue(row: Record<string, unknown>) {
  retransmitLog.value = `已对「${dictLabel('data_quality_issue', String(row.type))}」发起重传 · ${new Date().toLocaleTimeString()}`
  ElMessage.success('问题点位重传任务已登记')
}

onMounted(async () => {
  await ensureDicts()
  await load()
})

onUnmounted(() => chart?.dispose())
</script>

<style scoped>
.toolbar { display: flex; flex-wrap: wrap; gap: 8px; align-items: center; }
.toolbar-card { margin-bottom: 12px; }
.meta { color: #909399; font-size: 13px; }
.stats { display: grid; grid-template-columns: repeat(6, 1fr); gap: 12px; }
.stat { background: #f5f7fa; border-radius: 10px; padding: 14px; text-align: center; }
.stat b { display: block; font-size: 22px; }
.stat span { color: #909399; font-size: 13px; }
.stats-card { margin-bottom: 12px; }
.extra { margin-top: 12px; display: flex; flex-wrap: wrap; gap: 16px; color: #606266; font-size: 13px; }
.chart { height: 280px; width: 100%; }
.head-row { display: flex; justify-content: space-between; align-items: center; }
.mt { margin-top: 12px; }
@media (max-width: 992px) { .stats { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 576px) { .stats { grid-template-columns: repeat(2, 1fr); } }
</style>
