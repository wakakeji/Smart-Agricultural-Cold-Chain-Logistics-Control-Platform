<template>
  <div class="page">
    <div class="toolbar">
      <el-button type="primary" :loading="loading" @click="load">刷新探测</el-button>
      <span class="summary" v-if="summary">在线 {{ summary.up }} / {{ summary.total }}</span>
    </div>
    <el-row :gutter="16">
      <el-col v-for="item in items" :key="item.name" :xs="24" :sm="12" :md="8" :lg="8">
        <el-card shadow="hover" class="card" :class="{ down: !item.up }">
          <div class="name">{{ item.name }}</div>
          <div class="status">
            <el-tag :type="item.up ? 'success' : 'danger'">{{ item.up ? '正常' : '异常' }}</el-tag>
            <span class="latency">{{ item.latencyMs }} ms</span>
          </div>
          <p class="msg">{{ item.message }}</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { fetchHealth } from '@/api/system'
import type { HealthItem } from '@/types/api'

const loading = ref(false)
const items = ref<HealthItem[]>([])
const summary = ref<{ total: number; up: number; down: number } | null>(null)

async function load() {
  loading.value = true
  try {
    const data = await fetchHealth()
    const keys = ['mysql', 'redis', 'rabbitmq', 'mongodb', 'influxdb']
    items.value = keys
      .map((k) => data[k] as HealthItem)
      .filter((x) => x && typeof x === 'object' && 'name' in x)
    summary.value = (data.summary as { total: number; up: number; down: number }) || null
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.toolbar { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
.summary { color: #606266; }
.card { margin-bottom: 16px; min-height: 120px; }
.card.down { border-color: #f56c6c; }
.name { font-size: 18px; font-weight: 600; margin-bottom: 10px; }
.status { display: flex; align-items: center; gap: 10px; }
.latency { color: #909399; font-size: 13px; }
.msg { margin: 10px 0 0; color: #606266; font-size: 13px; word-break: break-all; }
</style>
