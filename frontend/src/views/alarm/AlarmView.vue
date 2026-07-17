<template>
  <div class="page">
    <el-card shadow="never" class="stats-card">
      <div class="stats">
        <div class="stat" @click="quickStatus('')"><b>{{ stats?.total ?? 0 }}</b><span>全部</span></div>
        <div class="stat pending" @click="quickStatus('PENDING')"><b>{{ stats?.pending ?? 0 }}</b><span>待处理</span></div>
        <div class="stat processing" @click="quickStatus('PROCESSING')"><b>{{ stats?.processing ?? 0 }}</b><span>处理中</span></div>
        <div class="stat done" @click="quickStatus('RESOLVED,IGNORED')"><b>{{ (stats?.resolved ?? 0) + (stats?.ignored ?? 0) }}</b><span>已处理</span></div>
      </div>
    </el-card>

    <el-card shadow="never">
      <div class="toolbar">
        <el-select v-model="query.level" clearable placeholder="告警级别" style="width: 120px" @change="load">
          <el-option v-for="o in levelOptions" :key="o.value" :label="o.label" :value="o.value" />
        </el-select>
        <el-select v-model="query.type" clearable placeholder="告警类型" style="width: 160px" @change="load">
          <el-option v-for="o in typeOptions" :key="o.value" :label="o.label" :value="o.value" />
        </el-select>
        <el-select v-model="query.status" clearable placeholder="状态" style="width: 140px" @change="load">
          <el-option v-for="o in statusOptions" :key="o.value" :label="o.label" :value="o.value" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="datetimerange"
          value-format="YYYY-MM-DDTHH:mm:ss"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          @change="load"
        />
        <el-button type="primary" @click="load">查询</el-button>
        <el-button type="success" :disabled="!selectedIds.length" @click="openBatch">批量处理</el-button>
        <el-button @click="exportCsv">导出</el-button>
      </div>

      <el-table :data="list" v-loading="loading" @selection-change="onSelect" stripe>
        <el-table-column type="selection" width="48" />
        <el-table-column prop="level" label="级别" width="110">
          <template #default="{ row }">
            <el-tag :type="levelType(row.level)" effect="dark">{{ dictLabel('alarm_level', row.level) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="140">
          <template #default="{ row }">{{ dictLabel('alarm_type', row.type) }}</template>
        </el-table-column>
        <el-table-column prop="sourceName" label="来源" min-width="140" show-overflow-tooltip />
        <el-table-column prop="content" label="内容" min-width="220" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showDetail(row)">详情</el-button>
            <el-button
              link
              type="warning"
              :disabled="row.status === 'RESOLVED' || row.status === 'IGNORED'"
              @click="openHandle(row)"
            >处理</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          layout="total, prev, pager, next"
          :total="total"
          @current-change="load"
        />
      </div>
    </el-card>

    <el-drawer v-model="drawer" title="告警详情" size="400px">
      <template v-if="current">
        <p><b>级别：</b>{{ dictLabel('alarm_level', current.level) }}</p>
        <p><b>类型：</b>{{ dictLabel('alarm_type', current.type) }}</p>
        <p><b>来源：</b>{{ current.sourceName }}</p>
        <p><b>内容：</b>{{ current.content }}</p>
        <p><b>当前值：</b>{{ current.currentValue ?? '-' }}</p>
        <p><b>阈值：</b>{{ current.threshold ?? '-' }}</p>
        <p><b>状态：</b>{{ statusText(current.status) }}</p>
        <p><b>产生时间：</b>{{ formatTime(current.createTime) }}</p>
        <el-divider>处理记录</el-divider>
        <p><b>处理人：</b>{{ current.handler || '-' }}</p>
        <p><b>处理时间：</b>{{ formatTime(current.handleTime) || '-' }}</p>
        <p><b>处理意见：</b>{{ current.handleRemark || '-' }}</p>
      </template>
    </el-drawer>

    <el-dialog v-model="handleVisible" :title="batchMode ? '批量处理告警' : '处理告警'" width="460px">
      <el-form label-width="90px">
        <el-form-item label="处理结果">
          <el-radio-group v-model="handleForm.status">
            <el-radio value="RESOLVED">已解决</el-radio>
            <el-radio value="IGNORED">忽略</el-radio>
            <el-radio value="PROCESSING">处理中</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理意见">
          <el-input v-model="handleForm.handleRemark" type="textarea" :rows="3" placeholder="填写处理说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitHandle">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  batchHandleAlarm,
  fetchAlarmDetail,
  fetchAlarmList,
  fetchAlarmStats,
  handleAlarm,
} from '@/api/alarm'
import type { AlarmRecord, AlarmStats } from '@/types/alarm'
import { dictLabel, dictOptions, ensureDicts } from '@/utils/dict'

const loading = ref(false)
const dictReady = ref(false)
const levelOptions = computed(() => (dictReady.value ? dictOptions('alarm_level') : []))
const typeOptions = computed(() => (dictReady.value ? dictOptions('alarm_type') : []))
const statusOptions = computed(() => (dictReady.value ? dictOptions('alarm_status') : []))
const saving = ref(false)
const list = ref<AlarmRecord[]>([])
const total = ref(0)
const stats = ref<AlarmStats | null>(null)
const selectedIds = ref<number[]>([])
const dateRange = ref<string[] | null>(null)
const drawer = ref(false)
const current = ref<AlarmRecord | null>(null)
const handleVisible = ref(false)
const batchMode = ref(false)
const handleTargetId = ref<number>()

const query = reactive({
  page: 1,
  size: 10,
  level: '',
  status: '',
  type: '',
})

const handleForm = reactive({
  status: 'RESOLVED',
  handleRemark: '',
})

function levelType(level: string) {
  if (level === 'CRITICAL') return 'danger'
  if (level === 'WARNING') return 'warning'
  return 'info'
}

function statusType(status: string) {
  if (status === 'PENDING') return 'danger'
  if (status === 'PROCESSING') return 'warning'
  if (status === 'RESOLVED') return 'success'
  return 'info'
}

function statusText(status: string) {
  return dictLabel('alarm_status', status)
}

function formatTime(t?: string) {
  return t ? t.replace('T', ' ').slice(0, 19) : ''
}

function quickStatus(status: string) {
  query.status = status
  query.page = 1
  load()
}

async function load() {
  loading.value = true
  try {
    const params: Record<string, unknown> = {
      page: query.page,
      size: query.size,
      level: query.level || undefined,
      status: query.status || undefined,
      type: query.type || undefined,
      startTime: dateRange.value?.[0],
      endTime: dateRange.value?.[1],
    }
    const [pageData, statsData] = await Promise.all([
      fetchAlarmList(params),
      fetchAlarmStats({ startTime: dateRange.value?.[0], endTime: dateRange.value?.[1] }),
    ])
    list.value = pageData.records
    total.value = pageData.total
    stats.value = statsData
  } finally {
    loading.value = false
  }
}

function onSelect(rows: AlarmRecord[]) {
  selectedIds.value = rows.map((r) => r.alarmId)
}

async function showDetail(row: AlarmRecord) {
  current.value = await fetchAlarmDetail(row.alarmId)
  drawer.value = true
}

function openHandle(row: AlarmRecord) {
  batchMode.value = false
  handleTargetId.value = row.alarmId
  handleForm.status = 'RESOLVED'
  handleForm.handleRemark = ''
  handleVisible.value = true
}

function openBatch() {
  batchMode.value = true
  handleForm.status = 'RESOLVED'
  handleForm.handleRemark = ''
  handleVisible.value = true
}

async function submitHandle() {
  saving.value = true
  try {
    if (batchMode.value) {
      await batchHandleAlarm({
        ids: selectedIds.value,
        status: handleForm.status,
        handleRemark: handleForm.handleRemark,
      })
    } else if (handleTargetId.value) {
      await handleAlarm(handleTargetId.value, {
        status: handleForm.status,
        handleRemark: handleForm.handleRemark,
      })
    }
    ElMessage.success('处理成功')
    handleVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

function exportCsv() {
  if (!list.value.length) {
    ElMessage.warning('暂无数据可导出')
    return
  }
  const header = ['告警ID', '级别', '类型', '来源', '内容', '状态', '时间']
  const rows = list.value.map((r) => [
    r.alarmId,
    r.level,
    r.type,
    r.sourceName || '',
    (r.content || '').replaceAll(',', '，'),
    r.status,
    formatTime(r.createTime),
  ])
  const csv = [header, ...rows].map((line) => line.join(',')).join('\n')
  const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `alarms_${Date.now()}.csv`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('已导出当前页 CSV')
}

onMounted(async () => {
  await ensureDicts()
  dictReady.value = true
  await load()
})
</script>

<style scoped>
.stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
.stat {
  background: #f5f7fa; border-radius: 10px; padding: 14px; text-align: center; cursor: pointer;
}
.stat b { display: block; font-size: 24px; }
.stat span { color: #909399; font-size: 13px; }
.stat.pending { background: #fef0f0; }
.stat.processing { background: #fdf6ec; }
.stat.done { background: #f0f9eb; }
.stats-card { margin-bottom: 12px; }
.toolbar { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 12px; }
.pager { margin-top: 12px; display: flex; justify-content: flex-end; }
@media (max-width: 768px) {
  .stats { grid-template-columns: repeat(2, 1fr); }
}
</style>
