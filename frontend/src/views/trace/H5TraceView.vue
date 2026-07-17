<template>
  <div class="h5">
    <header class="hero">
      <div class="brand">冷链溯源</div>
      <p class="sub">扫码查看产品全链条信息</p>
    </header>

    <section class="panel">
      <el-input v-model="batchNo" clearable placeholder="输入或扫码批次号" size="large" @keyup.enter="load" />
      <el-button type="primary" class="btn" size="large" :loading="loading" @click="load">查询溯源</el-button>
    </section>

    <template v-if="result">
      <section class="card">
        <h2>{{ result.product.productName }}</h2>
        <p class="line"><span>批次号</span>{{ result.product.batchNo }}</p>
        <p class="line"><span>产地</span>{{ result.product.origin || '-' }}</p>
        <p class="line"><span>生产日期</span>{{ result.product.produceDate || '-' }}</p>
        <p class="line"><span>保质期</span>{{ result.product.shelfLife != null ? result.product.shelfLife + ' 天' : '-' }}</p>
      </section>

      <section class="card">
        <h3>流转轨迹</h3>
        <ol class="steps">
          <li v-for="(node, i) in result.timeline" :key="node.traceId || i">
            <div class="dot" />
            <div class="body">
              <div class="op">{{ node.operationLabel }}</div>
              <div class="meta">{{ node.opTime }}</div>
              <div class="meta">{{ node.operator }} · {{ node.location || '-' }}</div>
              <div class="meta" v-if="node.temp != null">温湿度 {{ node.temp }}℃ / {{ node.humidity }}%</div>
            </div>
          </li>
        </ol>
      </section>

      <section class="card chain">
        <h3>链上存证</h3>
        <p class="hash">{{ result.blockchain.txHash || '暂无' }}</p>
        <p class="meta">区块 {{ result.blockchain.blockNumber ?? '-' }} · {{ result.blockchain.chainStatus || '-' }}</p>
        <el-button class="btn" :loading="verifying" @click="onVerify">验证真伪</el-button>
        <p v-if="verifyMsg" :class="['verify', verifyOk ? 'ok' : 'bad']">{{ verifyMsg }}</p>
      </section>
    </template>

    <p v-else-if="err" class="err">{{ err }}</p>
    <footer class="foot">智慧农业冷链物流管控平台 · 模拟链存证</footer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { fetchH5Trace, queryTrace, verifyTrace } from '@/api/trace'
import type { TraceNode, TraceQueryResult } from '@/types/trace'

const route = useRoute()
const batchNo = ref('')
const loading = ref(false)
const verifying = ref(false)
const result = ref<TraceQueryResult | null>(null)
const err = ref('')
const verifyMsg = ref('')
const verifyOk = ref(false)

function fromH5(data: Record<string, unknown>): TraceQueryResult {
  return {
    product: {
      productName: String(data.productName ?? ''),
      batchNo: String(data.batchNo ?? ''),
      origin: data.origin as string | undefined,
      produceDate: data.produceDate as string | undefined,
      shelfLife: data.shelfLife as number | undefined,
    },
    blockchain: {
      txHash: data.txHash as string | undefined,
      blockNumber: undefined,
      chainTime: undefined,
      confirmations: 0,
      chainStatus: data.verified ? 'CONFIRMED' : 'UNKNOWN',
      dataHash: undefined,
    },
    timeline: (data.timeline as TraceNode[]) || [],
  }
}

async function load() {
  const no = batchNo.value.trim()
  if (!no) {
    err.value = '请输入批次号'
    return
  }
  loading.value = true
  err.value = ''
  verifyMsg.value = ''
  try {
    try {
      const h5 = await fetchH5Trace(no)
      result.value = fromH5(h5)
      if (h5.verifyMessage) {
        verifyOk.value = Boolean(h5.verified)
        verifyMsg.value = String(h5.verifyMessage)
      }
    } catch {
      result.value = await queryTrace({ batchNo: no })
    }
    batchNo.value = result.value.product.batchNo
  } catch (e: unknown) {
    result.value = null
    err.value = e instanceof Error ? e.message : '查询失败'
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

onMounted(() => {
  const q = route.query.batchNo
  if (typeof q === 'string' && q) {
    batchNo.value = q
    load()
  }
})
</script>

<style scoped>
.h5 {
  min-height: 100vh;
  background:
    radial-gradient(120% 80% at 10% 0%, #d9f2e6 0%, transparent 55%),
    linear-gradient(180deg, #eef7f2 0%, #f7faf8 40%, #e8f0ec 100%);
  color: #1a2e24;
  padding: 20px 16px 40px;
  font-family: "Source Han Sans SC", "PingFang SC", "Microsoft YaHei", sans-serif;
}
.hero { margin-bottom: 18px; }
.brand {
  font-size: 28px;
  font-weight: 700;
  letter-spacing: 0.04em;
  color: #0f5c3a;
}
.sub { margin: 6px 0 0; color: #5a7266; font-size: 14px; }
.panel { display: flex; flex-direction: column; gap: 10px; margin-bottom: 16px; }
.btn { width: 100%; margin-top: 4px; }
.card {
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(15, 92, 58, 0.12);
  border-radius: 14px;
  padding: 16px;
  margin-bottom: 12px;
  backdrop-filter: blur(6px);
}
.card h2 { margin: 0 0 10px; font-size: 20px; color: #0f5c3a; }
.card h3 { margin: 0 0 12px; font-size: 16px; }
.line { display: flex; justify-content: space-between; margin: 6px 0; font-size: 14px; }
.line span { color: #6b7f74; }
.steps { list-style: none; margin: 0; padding: 0; }
.steps li { display: flex; gap: 12px; position: relative; padding-bottom: 16px; }
.steps li:not(:last-child)::before {
  content: '';
  position: absolute;
  left: 5px;
  top: 14px;
  bottom: 0;
  width: 2px;
  background: #b7d4c4;
}
.dot {
  width: 12px;
  height: 12px;
  margin-top: 4px;
  border-radius: 50%;
  background: #1a8f5a;
  flex-shrink: 0;
}
.op { font-weight: 600; font-size: 15px; }
.meta { color: #6b7f74; font-size: 12px; line-height: 1.5; margin-top: 2px; }
.hash { word-break: break-all; font-family: ui-monospace, Consolas, monospace; font-size: 12px; }
.verify { margin-top: 10px; font-size: 14px; }
.verify.ok { color: #0f5c3a; }
.verify.bad { color: #b42318; }
.err { color: #b42318; text-align: center; }
.foot { margin-top: 24px; text-align: center; color: #8a9a91; font-size: 12px; }
</style>
