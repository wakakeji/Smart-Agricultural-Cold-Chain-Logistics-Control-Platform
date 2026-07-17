<template>
  <div class="page">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" @submit.prevent="onSearch">
        <el-form-item label="批次号">
          <el-input v-model="batchNo" clearable placeholder="如 20260717000001" style="width: 200px" />
        </el-form-item>
        <el-form-item label="产品名称">
          <el-input v-model="productName" clearable placeholder="模糊匹配" style="width: 160px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="onSearch">查询</el-button>
          <el-button :disabled="!result" :loading="verifying" @click="onVerify">链上验证</el-button>
        </el-form-item>
      </el-form>
      <el-alert v-if="verifyMsg" :title="verifyMsg" :type="verifyOk ? 'success' : 'error'" show-icon :closable="false" />
    </el-card>

    <template v-if="result">
      <el-row :gutter="16" class="mt">
        <el-col :xs="24" :md="10">
          <el-card shadow="never">
            <template #header><strong>产品信息</strong></template>
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="产品">{{ result.product.productName }}</el-descriptions-item>
              <el-descriptions-item label="批次号">{{ result.product.batchNo }}</el-descriptions-item>
              <el-descriptions-item label="产地">{{ result.product.origin || '-' }}</el-descriptions-item>
              <el-descriptions-item label="生产日期">{{ result.product.produceDate || '-' }}</el-descriptions-item>
              <el-descriptions-item label="保质期">
                {{ result.product.shelfLife != null ? result.product.shelfLife + ' 天' : '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="数量">
                {{ result.product.quantity ?? '-' }}{{ result.product.unit || '' }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>

          <el-card shadow="never" class="mt">
            <template #header><strong>链上存证</strong></template>
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="交易哈希">
                <span class="mono">{{ result.blockchain.txHash || '-' }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="区块高度">{{ result.blockchain.blockNumber ?? '-' }}</el-descriptions-item>
              <el-descriptions-item label="上链时间">{{ result.blockchain.chainTime || '-' }}</el-descriptions-item>
              <el-descriptions-item label="确认数">{{ result.blockchain.confirmations ?? 0 }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag size="small" :type="result.blockchain.chainStatus === 'CONFIRMED' ? 'success' : 'info'">
                  {{ chainStatusLabel }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="数据哈希">
                <span class="mono">{{ result.blockchain.dataHash || '-' }}</span>
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>

        <el-col :xs="24" :md="14">
          <el-card shadow="never">
            <template #header><strong>全链条时间轴</strong></template>
            <el-timeline v-if="result.timeline.length">
              <el-timeline-item
                v-for="node in result.timeline"
                :key="node.traceId || node.opTime + node.operation"
                :timestamp="node.opTime"
                placement="top"
                type="primary"
              >
                <div class="node">
                  <div class="node-title">{{ node.operationLabel }}</div>
                  <div class="node-meta">操作方：{{ node.operator }} · 地点：{{ node.location || '-' }}</div>
                  <div class="node-meta" v-if="node.temp != null">
                    温度 {{ node.temp }}℃ / 湿度 {{ node.humidity }}%
                  </div>
                  <div class="node-hash mono" v-if="node.txHash">{{ node.txHash }}</div>
                </div>
              </el-timeline-item>
            </el-timeline>
            <el-empty v-else description="暂无追溯节点" />
          </el-card>
        </el-col>
      </el-row>
    </template>
    <el-empty v-else-if="!loading" description="输入批次号或产品名称查询追溯信息" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { queryTrace, verifyTrace } from '@/api/trace'
import type { TraceQueryResult } from '@/types/trace'
import { dictLabel, ensureDicts } from '@/utils/dict'

const route = useRoute()
const batchNo = ref('')
const productName = ref('')
const loading = ref(false)
const verifying = ref(false)
const result = ref<TraceQueryResult | null>(null)
const verifyMsg = ref('')
const verifyOk = ref(false)
const chainStatusLabel = computed(() =>
  dictLabel('chain_status', result.value?.blockchain?.chainStatus, result.value?.blockchain?.chainStatus || '未知'),
)

async function onSearch() {
  if (!batchNo.value.trim() && !productName.value.trim()) {
    ElMessage.warning('请输入批次号或产品名称')
    return
  }
  loading.value = true
  verifyMsg.value = ''
  try {
    result.value = await queryTrace({
      batchNo: batchNo.value.trim() || undefined,
      productName: productName.value.trim() || undefined,
    })
    batchNo.value = result.value.product.batchNo
  } finally {
    loading.value = false
  }
}

async function onVerify() {
  if (!result.value) return
  verifying.value = true
  try {
    const vo = await verifyTrace({
      batchNo: result.value.product.batchNo,
      txHash: result.value.blockchain.txHash,
    })
    verifyOk.value = vo.valid
    verifyMsg.value = vo.message
  } finally {
    verifying.value = false
  }
}

onMounted(async () => {
  await ensureDicts()
  const q = route.query.batchNo
  if (typeof q === 'string' && q) {
    batchNo.value = q
    onSearch()
  }
})
</script>

<style scoped>
.page { padding: 4px; }
.search-card :deep(.el-form-item) { margin-bottom: 0; }
.mt { margin-top: 16px; }
.mono { font-family: ui-monospace, Consolas, monospace; font-size: 12px; word-break: break-all; }
.node-title { font-weight: 600; margin-bottom: 4px; }
.node-meta { color: var(--el-text-color-secondary); font-size: 13px; line-height: 1.6; }
.node-hash { margin-top: 4px; color: var(--el-text-color-placeholder); }
</style>
