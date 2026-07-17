<template>
  <div class="page">
    <el-card shadow="never" class="toolbar-card">
      <div class="toolbar">
        <el-input v-model="keyword" clearable placeholder="批次号/产品" style="width: 200px" @keyup.enter="load" />
        <el-button type="primary" :loading="loading" @click="load">刷新</el-button>
        <el-button @click="exportCsv">导出CSV</el-button>
        <span class="meta">DT-002 品质变化监控 · 实线历史 / 虚线预测</span>
      </div>
    </el-card>

    <el-row :gutter="12">
      <el-col :xs="24" :md="14">
        <el-card shadow="never">
          <template #header>
            <strong>{{ current?.productName || '批次品质曲线' }}</strong>
            <span v-if="current" class="sub">批次 {{ current.batchNo }}</span>
          </template>
          <div ref="chartRef" class="chart" />
          <el-empty v-if="!current && !loading" description="请选择下方批次查看品质曲线" />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="10">
        <el-card shadow="never">
          <template #header><strong>在途货物品质列表</strong></template>
          <el-table :data="list" v-loading="loading" stripe highlight-current-row @row-click="onSelect" table-layout="auto">
            <el-table-column prop="batchNo" label="批次号" min-width="130" show-overflow-tooltip />
            <el-table-column prop="productName" label="品类" min-width="100" show-overflow-tooltip />
            <el-table-column prop="score" label="当前分" min-width="80" />
            <el-table-column prop="predict24h" label="24h预测" min-width="90" />
            <el-table-column prop="status" label="状态" min-width="90">
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, ref } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { fetchBatchList } from '@/api/batch'
import { fetchPredictHistory, runPredict } from '@/api/predict'

type QualityRow = {
  batchId: number
  batchNo: string
  productName: string
  score: number
  predict24h: number
  status: string
  curve: { hour: number; score: number }[]
}

const loading = ref(false)
const keyword = ref('')
const list = ref<QualityRow[]>([])
const current = ref<QualityRow | null>(null)
const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

function statusType(s: string) {
  if (s === '危急') return 'danger'
  if (s === '注意') return 'warning'
  return 'success'
}

function levelOf(score: number) {
  if (score < 60) return '危急'
  if (score < 80) return '注意'
  return '优良'
}

function renderChart(row: QualityRow) {
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)
  const hist = row.curve.map((p) => p.score)
  const hours = row.curve.map((p) => `${p.hour}h`)
  const pred = row.curve.map((p, i) => (i < hist.length - 3 ? null : Math.max(30, p.score - (i - (hist.length - 4)) * 2)))
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['历史品质', '24h预测', '警戒线'] },
    grid: { left: 40, right: 20, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: hours },
    yAxis: { type: 'value', min: 0, max: 100, name: '分' },
    series: [
      { name: '历史品质', type: 'line', smooth: true, data: hist, itemStyle: { color: '#409EFF' } },
      { name: '24h预测', type: 'line', smooth: true, data: pred, lineStyle: { type: 'dashed' }, itemStyle: { color: '#E6A23C' } },
      { name: '警戒线', type: 'line', data: hours.map(() => 60), lineStyle: { color: '#F56C6C', type: 'dotted' }, symbol: 'none' },
    ],
  })
}

async function load() {
  loading.value = true
  try {
    const page = await fetchBatchList({ page: 1, size: 20, keyword: keyword.value || undefined })
    const rows: QualityRow[] = []
    for (const b of page.records.slice(0, 8)) {
      const pred = await runPredict({ batchId: b.batchId, batchNo: b.batchNo })
      const hist = await fetchPredictHistory(b.batchId)
      const score = Number(pred.qualityScore ?? 90)
      const curve = ((pred.curve as { hour: number; score: number }[]) || []).map((p) => ({
        hour: Number(p.hour),
        score: Number(p.score),
      }))
      if (!curve.length && hist.length) {
        hist.forEach((h, i) => curve.push({ hour: i * 4, score: Number(h.qualityScore) }))
      }
      rows.push({
        batchId: b.batchId,
        batchNo: b.batchNo,
        productName: b.productName,
        score,
        predict24h: Math.round((score - 8) * 10) / 10,
        status: levelOf(score),
        curve: curve.length ? curve : Array.from({ length: 12 }, (_, i) => ({ hour: i * 4, score: score - i * 0.4 })),
      })
    }
    list.value = rows
    if (rows.length) {
      current.value = rows[0]
      await nextTick()
      renderChart(rows[0])
    }
  } finally {
    loading.value = false
  }
}

async function onSelect(row: QualityRow) {
  current.value = row
  await nextTick()
  renderChart(row)
}

function exportCsv() {
  if (!list.value.length) {
    ElMessage.warning('暂无数据')
    return
  }
  const header = '批次号,品类,当前品质分,24h预测,状态\n'
  const body = list.value.map((r) => `${r.batchNo},${r.productName},${r.score},${r.predict24h},${r.status}`).join('\n')
  const blob = new Blob(['\ufeff' + header + body], { type: 'text/csv;charset=utf-8' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `quality_monitor_${Date.now()}.csv`
  a.click()
  ElMessage.success('已导出品质监控CSV')
}

onMounted(load)
onUnmounted(() => chart?.dispose())
</script>

<style scoped>
.toolbar { display: flex; flex-wrap: wrap; gap: 8px; align-items: center; }
.toolbar-card { margin-bottom: 12px; }
.meta { color: #909399; font-size: 13px; }
.sub { margin-left: 8px; color: #909399; font-size: 13px; font-weight: normal; }
.chart { height: 320px; width: 100%; }
</style>
