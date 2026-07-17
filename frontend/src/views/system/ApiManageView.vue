<template>
  <div class="page">
    <el-card shadow="never" class="stats-card">
      <div class="stats">
        <div class="stat"><b>{{ stats?.apiCount ?? 0 }}</b><span>API 数量</span></div>
        <div class="stat"><b>{{ stats?.keyCount ?? 0 }}</b><span>密钥数</span></div>
        <div class="stat"><b>{{ stats?.todayCalls ?? 0 }}</b><span>今日调用</span></div>
        <div class="stat"><b>{{ stats?.successRate ?? 0 }}%</b><span>成功率</span></div>
      </div>
    </el-card>

    <el-row :gutter="12">
      <el-col :xs="24" :md="16">
        <el-card shadow="never">
          <template #header><strong>API 目录</strong></template>
          <el-table :data="catalog" v-loading="loading" stripe table-layout="auto">
            <el-table-column prop="code" label="编码" min-width="120" show-overflow-tooltip />
            <el-table-column prop="method" label="方法" min-width="80">
              <template #default="{ row }">
                <el-tag :type="row.method === 'GET' ? 'success' : 'primary'" size="small">{{ row.method }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="path" label="路径" min-width="200" show-overflow-tooltip />
            <el-table-column prop="name" label="名称" min-width="110" show-overflow-tooltip />
            <el-table-column prop="module" label="模块" min-width="90" />
            <el-table-column prop="status" label="状态" min-width="90">
              <template #default="{ row }">
                <el-tag type="success" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card shadow="never">
          <template #header><strong>API 密钥</strong></template>
          <div class="toolbar">
            <el-input v-model="keyName" placeholder="应用名称" />
            <el-button type="primary" :loading="generating" @click="onGenerate">生成密钥</el-button>
          </div>
          <el-table :data="keys" stripe size="small">
            <el-table-column prop="name" label="名称" />
            <el-table-column prop="apiKey" label="Key" min-width="140" show-overflow-tooltip>
              <template #default="{ row }"><span class="mono">{{ row.apiKey }}</span></template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="70" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchApiCatalog, fetchApiKeys, fetchApiStats, generateApiKey } from '@/api/apiManage'

const loading = ref(false)
const generating = ref(false)
const stats = ref<Record<string, unknown> | null>(null)
const catalog = ref<Record<string, unknown>[]>([])
const keys = ref<Record<string, unknown>[]>([])
const keyName = ref('')

async function load() {
  loading.value = true
  try {
    const [st, apis, ks] = await Promise.all([fetchApiStats(), fetchApiCatalog(), fetchApiKeys()])
    stats.value = st
    catalog.value = apis
    keys.value = ks
  } finally {
    loading.value = false
  }
}

async function onGenerate() {
  generating.value = true
  try {
    const item = await generateApiKey(keyName.value.trim() || undefined)
    ElMessage.success('密钥已生成')
    keys.value = [item, ...keys.value.filter((k) => k.apiKey !== item.apiKey)]
    stats.value = await fetchApiStats()
  } finally {
    generating.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
.stat { background: #f5f7fa; border-radius: 10px; padding: 14px; text-align: center; }
.stat b { display: block; font-size: 22px; }
.stat span { color: #909399; font-size: 13px; }
.stats-card { margin-bottom: 12px; }
.toolbar { display: flex; flex-direction: column; gap: 8px; margin-bottom: 12px; }
.mono { font-family: ui-monospace, Consolas, monospace; font-size: 12px; }
@media (max-width: 768px) { .stats { grid-template-columns: repeat(2, 1fr); } }
</style>
