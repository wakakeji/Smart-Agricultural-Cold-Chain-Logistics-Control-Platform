<template>
  <div class="page">
    <el-row :gutter="16">
      <el-col :xs="24" :md="14">
        <el-card shadow="never">
          <div class="toolbar">
            <el-input v-model="keyword" clearable placeholder="搜索批次号/产品/产地" style="width: 220px" @keyup.enter="load" />
            <el-select v-model="status" clearable placeholder="状态" style="width: 120px" @change="load">
              <el-option label="待上链" :value="0" />
              <el-option label="已上链" :value="1" />
              <el-option label="已追溯" :value="2" />
            </el-select>
            <el-button type="primary" @click="load">查询</el-button>
          </div>
          <el-table :data="list" v-loading="loading" stripe @row-click="onRowClick">
            <el-table-column prop="batchNo" label="批次号" min-width="140" />
            <el-table-column prop="productName" label="产品" min-width="120" />
            <el-table-column prop="origin" label="产地" min-width="120" show-overflow-tooltip />
            <el-table-column prop="quantity" label="数量" width="90">
              <template #default="{ row }">{{ row.quantity }}{{ row.unit }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'info' : 'warning'" size="small">
                  {{ statusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click.stop="showQr(row)">二维码</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pager">
            <el-pagination
              v-model:current-page="page"
              v-model:page-size="size"
              layout="total, prev, pager, next"
              :total="total"
              @current-change="load"
            />
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="10">
        <el-card shadow="never">
          <template #header><strong>新建批次赋码</strong></template>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
            <el-form-item label="产品名称" prop="productName">
              <el-input v-model="form.productName" placeholder="如：武鸣沃柑" />
            </el-form-item>
            <el-form-item label="产地" prop="origin">
              <el-input v-model="form.origin" placeholder="如：广西南宁市武鸣区" />
            </el-form-item>
            <el-form-item label="生产日期" prop="produceDate">
              <el-date-picker v-model="form.produceDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
            <el-form-item label="保质期" prop="shelfLife">
              <el-input-number v-model="form.shelfLife" :min="1" :max="3650" />
              <span class="unit">天</span>
            </el-form-item>
            <el-form-item label="数量" prop="quantity">
              <el-input-number v-model="form.quantity" :min="1" />
            </el-form-item>
            <el-form-item label="单位" prop="unit">
              <el-select v-model="form.unit" style="width: 100%">
                <el-option label="箱" value="箱" />
                <el-option label="公斤" value="公斤" />
                <el-option label="个" value="个" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="submitCreate">创建并生成二维码</el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="qrVisible" title="批次二维码" width="420px" align-center>
      <div class="qr-box" v-if="qrInfo">
        <canvas ref="canvasRef" />
        <p><b>批次号：</b>{{ qrInfo.batchNo }}</p>
        <p class="link"><b>追溯链接：</b>{{ fullTraceUrl(qrInfo.qrContent) }}</p>
        <p class="hash" v-if="qrInfo.txHash"><b>链上哈希：</b>{{ qrInfo.txHash }}</p>
      </div>
      <template #footer>
        <el-button @click="qrVisible = false">关闭</el-button>
        <el-button type="primary" @click="downloadQr">下载二维码</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import QRCode from 'qrcode'
import { createBatch, fetchBatchList, fetchBatchQr } from '@/api/batch'
import type { ProductBatch, QrCodeInfo } from '@/types/batch'

const loading = ref(false)
const saving = ref(false)
const list = ref<ProductBatch[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')
const status = ref<number>()
const formRef = ref<FormInstance>()
const qrVisible = ref(false)
const qrInfo = ref<QrCodeInfo | null>(null)
const canvasRef = ref<HTMLCanvasElement>()

const form = reactive({
  productName: '武鸣沃柑',
  origin: '广西南宁市武鸣区',
  produceDate: new Date().toISOString().slice(0, 10),
  shelfLife: 30,
  quantity: 500,
  unit: '箱',
})

const rules: FormRules = {
  productName: [{ required: true, message: '必填', trigger: 'blur' }],
  origin: [{ required: true, message: '必填', trigger: 'blur' }],
  produceDate: [{ required: true, message: '必选', trigger: 'change' }],
  shelfLife: [{ required: true, message: '必填', trigger: 'change' }],
  quantity: [{ required: true, message: '必填', trigger: 'change' }],
  unit: [{ required: true, message: '必选', trigger: 'change' }],
}

function statusText(s: number) {
  return s === 1 ? '已上链' : s === 2 ? '已追溯' : '待上链'
}

function fullTraceUrl(path: string) {
  if (path.startsWith('http')) return path
  return `${window.location.origin}${path.startsWith('/') ? '' : '/'}${path}`
}

async function load() {
  loading.value = true
  try {
    const data = await fetchBatchList({
      page: page.value,
      size: size.value,
      status: status.value,
      keyword: keyword.value || undefined,
    })
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.productName = ''
  form.origin = ''
  form.produceDate = new Date().toISOString().slice(0, 10)
  form.shelfLife = 30
  form.quantity = 100
  form.unit = '箱'
}

async function submitCreate() {
  const ok = await formRef.value?.validate().catch(() => false)
  if (!ok) return
  saving.value = true
  try {
    const result = await createBatch({ ...form })
    ElMessage.success(`赋码成功：${result.batchNo}`)
    await load()
    qrInfo.value = {
      batchId: result.batchId,
      batchNo: result.batchNo,
      qrContent: result.qrCodeUrl,
      qrCodeUrl: result.qrCodeUrl,
      txHash: result.txHash,
    }
    qrVisible.value = true
    await nextTick()
    await drawQr()
  } finally {
    saving.value = false
  }
}

async function showQr(row: ProductBatch) {
  qrInfo.value = await fetchBatchQr(row.batchId)
  qrVisible.value = true
  await nextTick()
  await drawQr()
}

function onRowClick(row: ProductBatch) {
  showQr(row)
}

async function drawQr() {
  if (!canvasRef.value || !qrInfo.value) return
  const content = fullTraceUrl(qrInfo.value.qrContent)
  await QRCode.toCanvas(canvasRef.value, content, { width: 220, margin: 2 })
}

function downloadQr() {
  if (!canvasRef.value || !qrInfo.value) return
  const a = document.createElement('a')
  a.href = canvasRef.value.toDataURL('image/png')
  a.download = `qr_${qrInfo.value.batchNo}.png`
  a.click()
}

onMounted(load)
</script>

<style scoped>
.toolbar { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 12px; }
.pager { margin-top: 12px; display: flex; justify-content: flex-end; }
.unit { margin-left: 8px; color: #909399; }
.qr-box { text-align: center; }
.qr-box canvas { margin: 0 auto 12px; display: block; }
.qr-box p { text-align: left; font-size: 13px; word-break: break-all; margin: 6px 0; }
.hash { color: #606266; }
.link { color: #409EFF; }
</style>
