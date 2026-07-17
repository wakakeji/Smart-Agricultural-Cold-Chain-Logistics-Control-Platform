<template>
  <div class="page">
    <el-card shadow="never" class="stats-card">
      <div class="stats">
        <div class="stat"><b>{{ overview?.currentBlock ?? 0 }}</b><span>当前区块</span></div>
        <div class="stat"><b>{{ overview?.totalTx ?? 0 }}</b><span>存证总数</span></div>
        <div class="stat"><b>{{ overview?.nodeCount ?? 0 }}</b><span>节点数</span></div>
        <div class="stat" :class="isHealthy ? 'ok' : ''">
          <b>{{ networkLabel }}</b><span>网络状态</span>
        </div>
      </div>
      <div v-if="nodes.length" class="nodes">
        <el-tag v-for="n in nodes" :key="n.id" :type="nodeType(n.role)" size="small">{{ n.name }}</el-tag>
      </div>
    </el-card>

    <el-row :gutter="12">
      <el-col :xs="24" :md="16">
        <el-card shadow="never">
          <div class="toolbar">
            <el-select v-model="query.bizType" clearable placeholder="业务类型" style="width: 140px" @change="loadTxs">
              <el-option v-for="o in bizOptions" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
            <el-select v-model="query.status" clearable placeholder="状态" style="width: 120px" @change="loadTxs">
              <el-option v-for="o in chainStatusOptions" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
            <el-button type="primary" @click="loadTxs">查询</el-button>
          </div>
          <el-table :data="txList" v-loading="loading" stripe>
            <el-table-column prop="txHash" label="交易哈希" min-width="180" show-overflow-tooltip>
              <template #default="{ row }"><span class="mono">{{ row.txHash }}</span></template>
            </el-table-column>
            <el-table-column prop="blockNumber" label="区块" width="90" />
            <el-table-column prop="bizType" label="类型" width="100">
              <template #default="{ row }">{{ dictLabel('biz_type', row.bizType) }}</template>
            </el-table-column>
            <el-table-column prop="bizId" label="业务ID" width="120" show-overflow-tooltip />
            <el-table-column prop="chainStatus" label="状态" width="100">
              <template #default="{ row }">
                <el-tag size="small" :type="row.chainStatus === 'CONFIRMED' ? 'success' : 'info'">
                  {{ dictLabel('chain_status', row.chainStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="时间" width="170" />
            <el-table-column prop="confirmations" label="确认数" width="80" />
          </el-table>
          <div class="pager">
            <el-pagination
              v-model:current-page="query.page"
              v-model:page-size="query.size"
              layout="total, prev, pager, next"
              :total="total"
              @current-change="loadTxs"
            />
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card shadow="never">
          <template #header><strong>哈希验证</strong></template>
          <el-form label-width="80px">
            <el-form-item label="交易哈希">
              <el-input v-model="verifyForm.txHash" placeholder="0x..." />
            </el-form-item>
            <el-form-item label="数据哈希">
              <el-input v-model="verifyForm.dataHash" placeholder="可选" />
            </el-form-item>
            <el-form-item label="原始数据">
              <el-input v-model="verifyForm.originalData" type="textarea" :rows="2" placeholder="可选 JSON" />
            </el-form-item>
            <el-button type="primary" :loading="verifying" @click="onVerify">验证</el-button>
          </el-form>
          <el-alert v-if="verifyMsg" :title="verifyMsg" :type="verifyOk ? 'success' : 'error'" show-icon :closable="false" class="mt" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import {
  fetchBlockchainOverview,
  fetchBlockchainTopology,
  fetchBlockchainTxs,
  verifyBlockchain,
  type ChainNode,
  type ChainOverview,
  type ChainTx,
} from '@/api/blockchain'
import { dictLabel, dictOptions, ensureDicts } from '@/utils/dict'

const loading = ref(false)
const dictReady = ref(false)
const bizOptions = computed(() => (dictReady.value ? dictOptions('biz_type') : []))
const chainStatusOptions = computed(() => (dictReady.value ? dictOptions('chain_status') : []))
const networkLabel = computed(() => {
  const s = overview.value?.networkStatus
  if (!s) return '-'
  // 后端可能已返回中文，或返回 HEALTHY 编码
  return s === 'HEALTHY' ? dictLabel('network_status', 'HEALTHY') : s
})
const isHealthy = computed(() => {
  const s = overview.value?.networkStatus
  return s === 'HEALTHY' || s === '正常'
})
const verifying = ref(false)
const overview = ref<ChainOverview | null>(null)
const nodes = ref<ChainNode[]>([])
const txList = ref<ChainTx[]>([])
const total = ref(0)
const verifyMsg = ref('')
const verifyOk = ref(false)

const query = reactive({ page: 1, size: 10, bizType: '', status: '' })
const verifyForm = reactive({ txHash: '', dataHash: '', originalData: '' })

function nodeType(role: string) {
  if (role === 'leader') return 'danger'
  if (role === 'peer') return 'success'
  return 'info'
}

async function loadOverview() {
  const [ov, topo] = await Promise.all([fetchBlockchainOverview(), fetchBlockchainTopology()])
  overview.value = ov
  nodes.value = topo
}

async function loadTxs() {
  loading.value = true
  try {
    const data = await fetchBlockchainTxs({
      page: query.page,
      size: query.size,
      bizType: query.bizType || undefined,
      status: query.status || undefined,
    })
    txList.value = data.records
    total.value = data.total
    if (data.overview) overview.value = data.overview
  } finally {
    loading.value = false
  }
}

async function onVerify() {
  if (!verifyForm.txHash && !verifyForm.dataHash && !verifyForm.originalData) return
  verifying.value = true
  verifyMsg.value = ''
  try {
    const vo = await verifyBlockchain({
      txHash: verifyForm.txHash || undefined,
      dataHash: verifyForm.dataHash || undefined,
      originalData: verifyForm.originalData || undefined,
    })
    verifyOk.value = vo.verified
    verifyMsg.value = vo.message
  } finally {
    verifying.value = false
  }
}

onMounted(async () => {
  await ensureDicts()
  dictReady.value = true
  await loadOverview()
  await loadTxs()
})
</script>

<style scoped>
.stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
.stat { background: #f5f7fa; border-radius: 10px; padding: 14px; text-align: center; }
.stat b { display: block; font-size: 22px; }
.stat span { color: #909399; font-size: 13px; }
.stat.ok { background: #f0f9eb; }
.stats-card { margin-bottom: 12px; }
.nodes { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 12px; }
.toolbar { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 12px; }
.pager { margin-top: 12px; display: flex; justify-content: flex-end; }
.mono { font-family: ui-monospace, Consolas, monospace; font-size: 12px; }
.mt { margin-top: 12px; }
@media (max-width: 768px) { .stats { grid-template-columns: repeat(2, 1fr); } }
</style>
