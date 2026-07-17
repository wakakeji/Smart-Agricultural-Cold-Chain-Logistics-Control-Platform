<template>
  <div class="page">
    <el-card shadow="never">
      <template #header><strong>在途运输 ({{ list.length }})</strong></template>
      <el-table :data="list" v-loading="loading" stripe @row-click="openRealtime">
        <el-table-column prop="orderNo" label="订单号" width="140" />
        <el-table-column prop="vehicleName" label="车辆" width="120" />
        <el-table-column prop="route" label="路线" min-width="160" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag type="success" size="small">{{ dictLabel('transport_status', row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currentTemp" label="温度℃" width="90">
          <template #default="{ row }">{{ row.currentTemp ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="progress" label="进度" width="120">
          <template #default="{ row }">
            <el-progress :percentage="row.progress ?? 0" :stroke-width="6" />
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="发车时间" width="170" />
      </el-table>
      <p class="hint">点击行查看实时轨迹与温湿度</p>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="`实时运输 · ${realtime?.order?.orderNo || ''}`" width="520px">
      <template v-if="realtime">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="车辆">{{ realtime.order.vehicleName }}</el-descriptions-item>
          <el-descriptions-item label="路线">{{ realtime.order.route }}</el-descriptions-item>
          <el-descriptions-item label="温度">{{ realtime.order.currentTemp ?? '-' }}℃</el-descriptions-item>
          <el-descriptions-item label="湿度">{{ realtime.order.currentHumidity ?? '-' }}%</el-descriptions-item>
          <el-descriptions-item label="速度">{{ realtime.order.speed ?? '-' }} km/h</el-descriptions-item>
          <el-descriptions-item label="ETA">{{ realtime.eta || '-' }}</el-descriptions-item>
          <el-descriptions-item label="轨迹点数">{{ realtime.track?.length ?? 0 }}</el-descriptions-item>
          <el-descriptions-item label="更新">{{ realtime.lastUpdate || '-' }}</el-descriptions-item>
        </el-descriptions>
      </template>
      <div v-loading="realtimeLoading" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { fetchOngoingTransport, fetchTransportRealtime, type TransportOrder, type TransportRealtime } from '@/api/transport'
import { dictLabel, ensureDicts } from '@/utils/dict'

const loading = ref(false)
const realtimeLoading = ref(false)
const list = ref<TransportOrder[]>([])
const dialogVisible = ref(false)
const realtime = ref<TransportRealtime | null>(null)

async function load() {
  loading.value = true
  try {
    list.value = await fetchOngoingTransport()
  } finally {
    loading.value = false
  }
}

async function openRealtime(row: TransportOrder) {
  dialogVisible.value = true
  realtimeLoading.value = true
  realtime.value = null
  try {
    realtime.value = await fetchTransportRealtime(row.orderId)
  } finally {
    realtimeLoading.value = false
  }
}

onMounted(async () => {
  await ensureDicts()
  await load()
})
</script>

<style scoped>
.hint { margin-top: 8px; color: #909399; font-size: 13px; }
:deep(.el-table__row) { cursor: pointer; }
</style>
