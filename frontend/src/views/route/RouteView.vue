<template>
  <div class="page">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" @submit.prevent="onPlan">
        <el-form-item label="起点">
          <el-input v-model="origin" clearable placeholder="南宁" style="width: 140px" />
        </el-form-item>
        <el-form-item label="终点">
          <el-input v-model="destination" clearable placeholder="广州" style="width: 140px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="onPlan">规划路线</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <template v-if="plan">
      <el-alert
        class="mb"
        type="info"
        :closable="false"
        :title="String(plan.note || '规则引擎模拟路线（更换起终点会重算）')"
      />
      <div class="route-hdr">
        {{ plan.origin }} → {{ plan.destination }}
        <el-tag type="success" size="small">推荐方案 {{ plan.recommend }}</el-tag>
      </div>
      <el-row :gutter="12">
        <el-col v-for="r in routes" :key="String(r.routeId)" :xs="24" :md="8">
          <el-card shadow="never" :class="['route-card', { recommend: r.routeId === plan.recommend }]">
            <div class="route-title">
              <span>{{ r.name }}</span>
              <el-tag v-if="r.routeId === plan.recommend" type="success" size="small">推荐</el-tag>
            </div>
            <div class="route-id">方案 {{ r.routeId }}</div>
            <div class="metrics">
              <div><b>{{ r.distanceKm }}</b><span>公里</span></div>
              <div><b>{{ r.durationHours }}</b><span>小时</span></div>
              <div><b>{{ r.qualityRetention }}%</b><span>品质保留</span></div>
            </div>
            <el-progress :percentage="Number(r.score)" :stroke-width="8" />
            <p class="meta">拥堵风险 {{ (Number(r.congestionRisk) * 100).toFixed(0) }}% · 油费 ¥{{ r.fuelCost }}</p>
            <p class="waypoints">{{ (r.waypoints as string[])?.join(' → ') }}</p>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="never" class="mt">
        <template #header><strong>路况摘要</strong></template>
        <el-table :data="traffic" stripe size="small" table-layout="auto">
          <el-table-column prop="road" label="路段" min-width="200" show-overflow-tooltip />
          <el-table-column prop="level" label="路况" min-width="120" />
          <el-table-column prop="speed" label="均速 km/h" min-width="120" />
        </el-table>
      </el-card>
    </template>
    <el-empty v-else-if="!loading" description="输入起终点规划冷链运输路线" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { planRoute } from '@/api/route'

const origin = ref('南宁')
const destination = ref('广州')
const loading = ref(false)
const plan = ref<Record<string, unknown> | null>(null)

const routes = computed(() => (plan.value?.routes as Record<string, unknown>[]) || [])
const traffic = computed(() => (plan.value?.traffic as Record<string, unknown>[]) || [])

async function onPlan() {
  loading.value = true
  try {
    plan.value = await planRoute({
      origin: origin.value.trim() || undefined,
      destination: destination.value.trim() || undefined,
    })
  } finally {
    loading.value = false
  }
}

onMounted(onPlan)
</script>

<style scoped>
.search-card { margin-bottom: 12px; }
.search-card :deep(.el-form-item) { margin-bottom: 0; }
.route-hdr { margin-bottom: 12px; font-weight: 600; display: flex; align-items: center; gap: 8px; }
.route-card { margin-bottom: 12px; }
.route-card.recommend { border: 1px solid var(--el-color-success-light-5); }
.route-title { display: flex; justify-content: space-between; font-weight: 600; font-size: 16px; }
.route-id { color: #909399; font-size: 13px; margin: 4px 0 12px; }
.metrics { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; margin-bottom: 12px; text-align: center; }
.metrics b { display: block; font-size: 18px; }
.metrics span { color: #909399; font-size: 12px; }
.meta { color: #909399; font-size: 13px; margin: 8px 0 4px; }
.waypoints { font-size: 12px; color: var(--el-text-color-secondary); }
.mt { margin-top: 12px; }
.mb { margin-bottom: 12px; }
</style>
