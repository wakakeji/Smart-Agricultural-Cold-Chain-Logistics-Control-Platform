<template>
  <div class="page">
    <el-card shadow="never" class="stats-card">
      <div class="stats">
        <div class="stat"><b>{{ overview?.avgLossRate ?? 0 }}%</b><span>平均损耗率</span></div>
        <div class="stat"><b>{{ overview?.totalLossQty ?? 0 }}</b><span>损耗总量</span></div>
        <div class="stat"><b>¥{{ overview?.totalCost ?? 0 }}</b><span>损耗成本</span></div>
        <div class="stat"><b>{{ overview?.recordCount ?? 0 }}</b><span>记录数</span></div>
      </div>
    </el-card>

    <el-row :gutter="12">
      <el-col :xs="24" :md="10">
        <el-card shadow="never">
          <template #header><strong>损耗趋势</strong></template>
          <div ref="chartRef" class="chart" />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="14">
        <el-card shadow="never">
          <template #header><strong>损耗明细</strong></template>
          <el-table :data="detail" v-loading="loading" stripe max-height="420" table-layout="auto">
            <el-table-column prop="batchNo" label="批次号" min-width="140" show-overflow-tooltip />
            <el-table-column prop="productName" label="产品" min-width="120" show-overflow-tooltip />
            <el-table-column prop="lossQuantity" label="损耗量" min-width="90" />
            <el-table-column prop="lossRate" label="损耗率%" min-width="100" />
            <el-table-column prop="lossType" label="类型" min-width="100">
              <template #default="{ row }">{{ dictLabel('loss_type', String(row.lossType)) }}</template>
            </el-table-column>
            <el-table-column prop="cost" label="成本(元)" min-width="100" />
            <el-table-column prop="reportDate" label="日期" min-width="120" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, ref } from 'vue'
import * as echarts from 'echarts'
import { fetchLossDetail, fetchLossOverview, fetchLossTrend } from '@/api/loss'
import { dictLabel, ensureDicts } from '@/utils/dict'

const loading = ref(false)
const overview = ref<Record<string, unknown> | null>(null)
const detail = ref<Record<string, unknown>[]>([])
const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

function renderChart(trend: Record<string, unknown>[]) {
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 16, top: 24, bottom: 28 },
    xAxis: { type: 'category', data: trend.map((p) => p.date) },
    yAxis: { type: 'value', name: '%' },
    series: [{ type: 'line', smooth: true, data: trend.map((p) => p.lossRate), areaStyle: { opacity: 0.12 } }],
  })
}

async function load() {
  loading.value = true
  try {
    const [ov, trend, det] = await Promise.all([fetchLossOverview(), fetchLossTrend(), fetchLossDetail()])
    overview.value = ov
    detail.value = det
    await nextTick()
    if (trend.length) renderChart(trend)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await ensureDicts()
  await load()
})
onUnmounted(() => chart?.dispose())
</script>

<style scoped>
.stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
.stat { background: #f5f7fa; border-radius: 10px; padding: 14px; text-align: center; }
.stat b { display: block; font-size: 22px; }
.stat span { color: #909399; font-size: 13px; }
.stats-card { margin-bottom: 12px; }
.chart { height: 280px; }
@media (max-width: 768px) { .stats { grid-template-columns: repeat(2, 1fr); } }
</style>
