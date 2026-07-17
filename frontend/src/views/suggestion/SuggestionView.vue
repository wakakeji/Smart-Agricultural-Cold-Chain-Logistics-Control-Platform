<template>
  <div class="page">
    <el-card shadow="never" class="stats-card">
      <div class="stats">
        <div class="stat" @click="filterStatus('')"><b>{{ stats?.total ?? 0 }}</b><span>全部</span></div>
        <div class="stat pending" @click="filterStatus('PENDING')"><b>{{ stats?.pending ?? 0 }}</b><span>待处理</span></div>
        <div class="stat done" @click="filterStatus('ADOPTED')"><b>{{ stats?.adopted ?? 0 }}</b><span>已采纳</span></div>
        <div class="stat ignored" @click="filterStatus('IGNORED')"><b>{{ stats?.ignored ?? 0 }}</b><span>已忽略</span></div>
      </div>
    </el-card>

    <el-card shadow="never">
      <div class="toolbar">
        <el-select v-model="query.priority" clearable placeholder="优先级" style="width: 120px" @change="load">
          <el-option
            v-for="o in priorityOptions"
            :key="o.value"
            :label="o.label"
            :value="o.value"
          />
        </el-select>
        <el-button type="primary" @click="load">刷新</el-button>
      </div>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="priority" label="优先级" width="90">
          <template #default="{ row }">
            <el-tag :type="prioType(String(row.priority))" size="small">
              {{ dictLabel('suggestion_priority', String(row.priority)) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="130">
          <template #default="{ row }">
            {{ dictLabel('suggestion_type', String(row.type)) }}
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
        <el-table-column prop="content" label="建议内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="expectedEffect" label="预期效果" min-width="150" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(String(row.status))">{{ statusText(String(row.status)) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button
              link type="success"
              :disabled="row.status !== 'PENDING'"
              @click="onAdopt(Number(row.sugId))"
            >采纳</el-button>
            <el-button
              link type="info"
              :disabled="row.status !== 'PENDING'"
              @click="onIgnore(Number(row.sugId))"
            >忽略</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  adoptSuggestion,
  fetchSuggestionList,
  fetchSuggestionStats,
  ignoreSuggestion,
} from '@/api/suggestion'
import { dictLabel, dictOptions, ensureDicts } from '@/utils/dict'

const loading = ref(false)
const list = ref<Record<string, unknown>[]>([])
const stats = ref<Record<string, number> | null>(null)
const query = reactive({ status: '', priority: '' })
const dictReady = ref(false)
const priorityOptions = computed(() => (dictReady.value ? dictOptions('suggestion_priority') : []))

function prioType(p: string) {
  if (p === 'HIGH') return 'danger'
  if (p === 'MEDIUM') return 'warning'
  return 'info'
}

function statusType(s: string) {
  if (s === 'PENDING') return 'warning'
  if (s === 'ADOPTED') return 'success'
  return 'info'
}

function statusText(s: string) {
  const map: Record<string, string> = { PENDING: '待处理', ADOPTED: '已采纳', IGNORED: '已忽略' }
  return map[s] || s
}

function filterStatus(status: string) {
  query.status = status
  load()
}

async function load() {
  loading.value = true
  try {
    const [items, st] = await Promise.all([
      fetchSuggestionList({
        status: query.status || undefined,
        priority: query.priority || undefined,
      }),
      fetchSuggestionStats(),
    ])
    list.value = items
    stats.value = st
  } finally {
    loading.value = false
  }
}

async function onAdopt(id: number) {
  await adoptSuggestion(id)
  ElMessage.success('已采纳')
  await load()
}

async function onIgnore(id: number) {
  await ignoreSuggestion(id)
  ElMessage.success('已忽略')
  await load()
}

onMounted(async () => {
  await ensureDicts()
  dictReady.value = true
  await load()
})
</script>

<style scoped>
.stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
.stat { background: #f5f7fa; border-radius: 10px; padding: 14px; text-align: center; cursor: pointer; }
.stat b { display: block; font-size: 24px; }
.stat span { color: #909399; font-size: 13px; }
.stat.pending { background: #fdf6ec; }
.stat.done { background: #f0f9eb; }
.stat.ignored { background: #f4f4f5; }
.stats-card { margin-bottom: 12px; }
.toolbar { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 12px; }
@media (max-width: 768px) { .stats { grid-template-columns: repeat(2, 1fr); } }
</style>
