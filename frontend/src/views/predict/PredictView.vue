<template>
  <div class="page">
    <el-row :gutter="12">
      <el-col :xs="24" :md="8">
        <el-card shadow="never">
          <template #header><strong>模型信息</strong></template>
          <el-descriptions v-if="modelInfo" :column="1" border size="small">
            <el-descriptions-item label="名称">{{ modelInfo.name }}</el-descriptions-item>
            <el-descriptions-item label="版本">{{ modelInfo.version }}</el-descriptions-item>
            <el-descriptions-item label="算法">{{ modelInfo.algorithm }}</el-descriptions-item>
            <el-descriptions-item label="MAE">{{ modelInfo.mae }}</el-descriptions-item>
            <el-descriptions-item label="R²">{{ modelInfo.r2 }}</el-descriptions-item>
            <el-descriptions-item label="训练样本">{{ modelInfo.trainSamples }}</el-descriptions-item>
            <el-descriptions-item label="特征">
              <el-tag v-for="f in (modelInfo.features as string[])" :key="f" size="small" class="feat">{{ f }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="16">
        <el-card shadow="never">
          <div class="toolbar">
            <el-input v-model="batchNo" clearable placeholder="批次号" style="width: 200px" />
            <el-button type="primary" :loading="predicting" @click="onPredict">执行预测</el-button>
          </div>
          <template v-if="result">
            <el-descriptions :column="2" border size="small" class="mb">
              <el-descriptions-item label="批次">{{ result.batchNo }}</el-descriptions-item>
              <el-descriptions-item label="产品">{{ result.productName }}</el-descriptions-item>
              <el-descriptions-item label="品质分">
                <b class="score">{{ result.qualityScore }}</b>
              </el-descriptions-item>
              <el-descriptions-item label="剩余货架期">{{ result.remainingShelfLife }}h</el-descriptions-item>
              <el-descriptions-item label="置信度">{{ result.confidence }}</el-descriptions-item>
              <el-descriptions-item label="风险">
                <el-tag :type="riskType(String(result.riskLevel))">
                  {{ (result.riskLabel as string) || riskLabel(String(result.riskLevel)) }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
            <div ref="curveRef" class="chart" />
          </template>
          <el-empty v-else description="输入批次号执行品质预测" />
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="mt">
      <template #header><strong>预测历史</strong></template>
      <el-table :data="history" v-loading="loading" stripe table-layout="auto">
        <el-table-column prop="batchId" label="批次ID" min-width="100" />
        <el-table-column prop="qualityScore" label="品质分" min-width="100" />
        <el-table-column prop="remainingShelfLife" label="剩余货架期(h)" min-width="140" />
        <el-table-column prop="confidence" label="置信度" min-width="100" />
        <el-table-column prop="modelVersion" label="模型版本" min-width="120" show-overflow-tooltip />
        <el-table-column prop="predictTime" label="预测时间" min-width="180" show-overflow-tooltip />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, ref } from 'vue'
import * as echarts from 'echarts'
import { fetchPredictHistory, fetchPredictModelInfo, runPredict } from '@/api/predict'

const modelInfo = ref<Record<string, unknown> | null>(null)
const batchNo = ref('')
const predicting = ref(false)
const loading = ref(false)
const result = ref<Record<string, unknown> | null>(null)
const history = ref<Record<string, unknown>[]>([])
const curveRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

function riskType(level: string) {
  if (level === 'LOW') return 'success'
  if (level === 'MEDIUM') return 'warning'
  return 'danger'
}

function riskLabel(level: string) {
  if (level === 'LOW') return '低'
  if (level === 'MEDIUM') return '中'
  if (level === 'HIGH') return '高'
  return level
}

function renderCurve(curve: Record<string, unknown>[]) {
  if (!curveRef.value) return
  if (!chart) chart = echarts.init(curveRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 16, top: 24, bottom: 28 },
    xAxis: { type: 'category', data: curve.map((p) => `${p.hour}h`) },
    yAxis: { type: 'value', min: 70, max: 100 },
    series: [{ type: 'line', smooth: true, data: curve.map((p) => p.score), areaStyle: { opacity: 0.15 } }],
  })
}

async function loadHistory() {
  loading.value = true
  try {
    history.value = await fetchPredictHistory()
  } finally {
    loading.value = false
  }
}

async function onPredict() {
  predicting.value = true
  try {
    result.value = await runPredict({ batchNo: batchNo.value.trim() || undefined })
    batchNo.value = String(result.value.batchNo)
    await loadHistory()
    await nextTick()
    const curve = result.value.curve as Record<string, unknown>[]
    if (curve?.length) renderCurve(curve)
  } finally {
    predicting.value = false
  }
}

onMounted(async () => {
  modelInfo.value = await fetchPredictModelInfo()
  await loadHistory()
})

onUnmounted(() => chart?.dispose())
</script>

<style scoped>
.toolbar { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 12px; }
.chart { height: 220px; }
.score { font-size: 20px; color: var(--el-color-primary); }
.feat { margin: 2px; }
.mb { margin-bottom: 12px; }
.mt { margin-top: 12px; }
</style>
