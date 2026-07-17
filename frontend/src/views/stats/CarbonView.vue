<template>
  <div class="page">
    <el-card shadow="never" class="stats-card">
      <div class="stats">
        <div class="stat"><b>{{ overview?.totalEmission ?? 0 }}</b><span>总排放 ({{ overview?.unit || 'kg' }})</span></div>
        <div class="stat"><b>{{ overview?.method ?? '-' }}</b><span>核算方法</span></div>
        <div class="stat"><b>{{ overview?.period ?? '-' }}</b><span>统计周期</span></div>
        <div class="stat"><b>{{ overview?.recordCount ?? 0 }}</b><span>记录数</span></div>
      </div>
    </el-card>

    <el-row :gutter="12">
      <el-col :xs="24" :md="10">
        <el-card shadow="never">
          <template #header><strong>排放来源分布</strong></template>
          <div ref="pieRef" class="chart" />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="14">
        <el-card shadow="never">
          <template #header><strong>排放明细</strong></template>
          <el-table :data="detail" v-loading="loading" stripe max-height="420" table-layout="auto">
            <el-table-column prop="sourceType" label="来源类型" min-width="110">
              <template #default="{ row }">{{ dictLabel('carbon_source', String(row.sourceType)) }}</template>
            </el-table-column>
            <el-table-column prop="sourceName" label="来源名称" min-width="140" show-overflow-tooltip />
            <el-table-column prop="emissionValue" label="排放量" min-width="100" />
            <el-table-column prop="unit" label="单位" min-width="100" show-overflow-tooltip />
            <el-table-column prop="period" label="周期" min-width="100" />
            <el-table-column prop="calcMethod" label="方法" min-width="90" />
            <el-table-column prop="calcTime" label="计算时间" min-width="160" show-overflow-tooltip />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, ref } from 'vue'
import * as echarts from 'echarts'
import { fetchCarbonDetail, fetchCarbonOverview } from '@/api/carbon'
import { dictLabel, ensureDicts } from '@/utils/dict'

const loading = ref(false)
const overview = ref<Record<string, unknown> | null>(null)
const detail = ref<Record<string, unknown>[]>([])
const pieRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

function renderPie(bySource: Record<string, unknown>[]) {
  if (!pieRef.value || !bySource.length) return
  if (!chart) chart = echarts.init(pieRef.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: ['40%', '65%'],
      data: bySource.map((s) => ({
        name: dictLabel('carbon_source', String(s.sourceType)),
        value: s.value,
      })),
    }],
  })
}

async function load() {
  loading.value = true
  try {
    const [ov, det] = await Promise.all([fetchCarbonOverview(), fetchCarbonDetail()])
    overview.value = ov
    detail.value = det
    await nextTick()
    renderPie((ov.bySource as Record<string, unknown>[]) || [])
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
.stat b { display: block; font-size: 20px; word-break: break-all; }
.stat span { color: #909399; font-size: 13px; }
.stats-card { margin-bottom: 12px; }
.chart { height: 280px; }
@media (max-width: 768px) { .stats { grid-template-columns: repeat(2, 1fr); } }
</style>
