<template>
  <div class="page">
    <el-card shadow="never" class="stats-card">
      <div class="stats">
        <div class="stat"><b>{{ overview?.score ?? 0 }}</b><span>质量评分</span></div>
        <div class="stat"><b>{{ overview?.onlineRate ?? 0 }}%</b><span>在线率</span></div>
        <div class="stat"><b>{{ overview?.completeness ?? 0 }}%</b><span>完整度</span></div>
        <div class="stat"><b>{{ overview?.accuracy ?? 0 }}%</b><span>准确度</span></div>
        <div class="stat"><b>{{ overview?.timeliness ?? 0 }}%</b><span>及时性</span></div>
        <div class="stat"><b>{{ overview?.avgLatencyMs ?? 0 }}ms</b><span>平均延迟</span></div>
      </div>
    </el-card>

    <el-card shadow="never">
      <template #header><strong>质量问题</strong></template>
      <el-table :data="issues" v-loading="loading" stripe>
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="issueType(String(row.type))" size="small">
              {{ dictLabel('data_quality_issue', String(row.type)) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="count" label="数量" width="80" />
        <el-table-column prop="desc" label="描述" min-width="280" show-overflow-tooltip />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { fetchDataQualityOverview } from '@/api/dataQuality'
import { dictLabel, ensureDicts } from '@/utils/dict'

const loading = ref(false)
const overview = ref<Record<string, unknown> | null>(null)

const issues = computed(() => (overview.value?.issues as Record<string, unknown>[]) || [])

function issueType(t: string) {
  if (t === 'DELAY') return 'warning'
  if (t === 'MISSING') return 'danger'
  return 'info'
}

async function load() {
  loading.value = true
  try {
    overview.value = await fetchDataQualityOverview()
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await ensureDicts()
  await load()
})
</script>

<style scoped>
.stats { display: grid; grid-template-columns: repeat(6, 1fr); gap: 12px; }
.stat { background: #f5f7fa; border-radius: 10px; padding: 14px; text-align: center; }
.stat b { display: block; font-size: 22px; }
.stat span { color: #909399; font-size: 13px; }
.stats-card { margin-bottom: 12px; }
@media (max-width: 992px) { .stats { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 576px) { .stats { grid-template-columns: repeat(2, 1fr); } }
</style>
