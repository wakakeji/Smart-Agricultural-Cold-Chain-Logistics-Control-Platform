<template>
  <div class="map-page" :class="{ fullscreen: isFullscreen }" ref="pageRef">
    <div class="toolbar">
      <el-button type="primary" :loading="loading" @click="handleRefresh">手动刷新</el-button>
      <el-button :type="filterType === 'storage' ? 'primary' : 'default'" @click="setType('storage')">仅冷库</el-button>
      <el-button :type="filterType === 'vehicle' ? 'primary' : 'default'" @click="setType('vehicle')">仅车辆</el-button>
      <el-button :type="filterType === 'all' ? 'primary' : 'default'" @click="setType('all')">全部</el-button>
      <el-select v-model="filterStatus" clearable placeholder="状态" style="width: 120px" @change="applyFilter">
        <el-option label="在线" value="ONLINE" />
        <el-option label="离线" value="OFFLINE" />
        <el-option label="告警" value="ALARM" />
      </el-select>
      <el-input v-model="keyword" clearable placeholder="搜索定位" style="width: 180px" @keyup.enter="searchLocate" />
      <el-button @click="searchLocate">搜索定位</el-button>
      <el-button @click="switchLayer">图层: {{ layerLabel }}</el-button>
      <el-button @click="toggleFullscreen">{{ isFullscreen ? '退出全屏' : '全屏' }}</el-button>
      <el-button type="warning" plain @click="startReplay">历史回放</el-button>
      <span class="hint">提示：先点选车辆再回放；未选中时自动使用第一辆车。手动刷新会抖动车辆位置。</span>
    </div>

    <div class="body">
      <aside class="side" v-show="!sideCollapsed">
        <div class="side-head">
          <strong>筛选面板</strong>
          <el-button link @click="sideCollapsed = true">收起</el-button>
        </div>
        <el-statistic title="冷库" :value="stats.storages" />
        <el-statistic title="车辆" :value="stats.vehicles" class="mt" />
        <el-statistic title="告警" :value="alarmCount" class="mt" />
        <el-divider />
        <div class="list">
          <div
            v-for="item in displayItems.slice(0, 40)"
            :key="item.id"
            class="list-item"
            @click="showDetail(item)"
          >
            <el-tag size="small" :type="statusTag(item.status)">{{ dictLabel('facility_status', item.status) }}</el-tag>
            <span>{{ item.name }}</span>
          </div>
        </div>
      </aside>
      <el-button v-if="sideCollapsed" class="expand-btn" @click="sideCollapsed = false">展开面板</el-button>

      <div class="map-wrap">
        <BaiduMap
          v-if="useBaidu && baiduAk"
          :ak="baiduAk"
          :items="displayItems"
          :track="trackPoints"
          :playback-index="playbackIndex"
          :zoom="zoom"
          @select="showDetail"
          @fail="onBaiduFail"
        />
        <MockMap
          v-else
          v-model:zoom="zoom"
          :items="displayItems"
          :track="trackPoints"
          :playback-index="playbackIndex"
          :layer="layer"
          @select="showDetail"
        />
        <div class="stat-cards">
          <el-card shadow="hover" class="mini"><div>冷库 {{ stats.storages }}</div><small>目标 128</small></el-card>
          <el-card shadow="hover" class="mini"><div>车辆 {{ stats.vehicles }}</div><small>目标 12</small></el-card>
          <el-card shadow="hover" class="mini"><div>告警 {{ alarmCount }}</div><small>需关注</small></el-card>
        </div>
      </div>
    </div>

    <el-drawer v-model="drawer" title="设施详情" size="360px">
      <template v-if="detail">
        <p><b>名称：</b>{{ detail.facility.name }}</p>
        <p><b>类型：</b>{{ dictLabel('facility_type', detail.facility.type, detail.facility.type === 'COLD_STORAGE' ? '冷库' : '冷藏车') }}</p>
        <p><b>状态：</b>{{ dictLabel('facility_status', detail.facility.status) }}</p>
        <p><b>坐标：</b>{{ detail.facility.lng }}, {{ detail.facility.lat }}</p>
        <p><b>温度：</b>{{ detail.facility.currentTemp ?? '-' }} ℃</p>
        <p><b>湿度：</b>{{ detail.facility.currentHumidity ?? '-' }} %</p>
        <p v-if="detail.facility.loadRate != null"><b>装载率：</b>{{ (detail.facility.loadRate * 100).toFixed(0) }}%</p>
        <p v-if="detail.facility.driverName"><b>司机：</b>{{ detail.facility.driverName }} {{ detail.facility.driverPhone }}</p>
        <p><b>地址：</b>{{ detail.facility.address || '-' }}</p>
        <el-divider>传感器</el-divider>
        <el-empty v-if="!detail.sensors?.length" description="暂无传感器" />
        <el-table v-else :data="detail.sensors" size="small">
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="type" label="类型" width="90">
            <template #default="{ row }">{{ dictLabel('sensor_type', row.type) }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="70">
            <template #default="{ row }">{{ row.status === 1 ? '在线' : row.status === 2 ? '故障' : '离线' }}</template>
          </el-table-column>
        </el-table>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import MockMap from '@/components/map/MockMap.vue'
import BaiduMap from '@/components/map/BaiduMap.vue'
import { fetchFacilities, fetchFacilityDetail, fetchVehicleTrack } from '@/api/facility'
import { get } from '@/utils/request'
import type { FacilityDetail, FacilityItem, TrackPoint } from '@/types/facility'
import { dictLabel, ensureDicts } from '@/utils/dict'

const loading = ref(false)
const baiduAk = ref('')
const useBaidu = ref(false)
const allStorages = ref<FacilityItem[]>([])
const allVehicles = ref<FacilityItem[]>([])
const filterType = ref<'all' | 'storage' | 'vehicle'>('all')
const filterStatus = ref('')
const keyword = ref('')
const sideCollapsed = ref(false)
const isFullscreen = ref(false)
const pageRef = ref<HTMLElement>()
const layer = ref<'normal' | 'satellite' | 'heat'>('normal')
const zoom = ref(12)
const drawer = ref(false)
const selected = ref<FacilityItem | null>(null)
const detail = ref<FacilityDetail | null>(null)
const trackPoints = ref<TrackPoint[]>([])
const playbackIndex = ref(-1)
let timer: number | undefined
let replayTimer: number | undefined

const stats = computed(() => ({
  storages: allStorages.value.length,
  vehicles: allVehicles.value.length,
}))

const displayItems = computed(() => {
  let list: FacilityItem[] = []
  if (filterType.value === 'storage') list = [...allStorages.value]
  else if (filterType.value === 'vehicle') list = [...allVehicles.value]
  else list = [...allStorages.value, ...allVehicles.value]
  if (filterStatus.value) list = list.filter((i) => i.status === filterStatus.value)
  if (keyword.value.trim()) {
    const k = keyword.value.trim()
    list = list.filter((i) => i.name.includes(k) || (i.address || '').includes(k))
  }
  return list
})

const alarmCount = computed(() =>
  [...allStorages.value, ...allVehicles.value].filter((i) => i.status === 'ALARM').length,
)

const layerLabel = computed(() =>
  layer.value === 'normal' ? '普通' : layer.value === 'satellite' ? '卫星' : '热力',
)

async function load(refresh = false) {
  loading.value = true
  try {
    const data = await fetchFacilities({ type: 'all', refresh })
    allStorages.value = data.coldStorages || []
    allVehicles.value = data.vehicles || []
  } finally {
    loading.value = false
  }
}

async function handleRefresh() {
  await load(true)
  ElMessage.success(`已刷新：冷库 ${allStorages.value.length} · 车辆 ${allVehicles.value.length}（坐标已抖动）`)
}

function setType(t: 'all' | 'storage' | 'vehicle') {
  filterType.value = t
}

function applyFilter() {
  // computed 自动响应
}

function searchLocate() {
  if (!displayItems.value.length) {
    ElMessage.warning('未找到匹配设施')
    return
  }
  showDetail(displayItems.value[0])
  ElMessage.success(`已定位到：${displayItems.value[0].name}`)
}

function switchLayer() {
  const order: Array<'normal' | 'satellite' | 'heat'> = ['normal', 'satellite', 'heat']
  layer.value = order[(order.indexOf(layer.value) + 1) % order.length]
}

async function toggleFullscreen() {
  if (!pageRef.value) return
  if (!document.fullscreenElement) {
    await pageRef.value.requestFullscreen()
    isFullscreen.value = true
  } else {
    await document.exitFullscreen()
    isFullscreen.value = false
  }
}

async function showDetail(item: FacilityItem) {
  selected.value = item
  drawer.value = true
  detail.value = await fetchFacilityDetail(item.id)
}

async function startReplay() {
  let vehicle = selected.value?.type === 'REFRIGERATED_VEHICLE' ? selected.value : null
  if (!vehicle) {
    vehicle = allVehicles.value[0] || null
  }
  if (!vehicle) {
    ElMessage.warning('暂无车辆可回放，请先切换到「仅车辆」或等待设施加载完成')
    return
  }
  selected.value = vehicle
  filterType.value = 'vehicle'
  try {
    trackPoints.value = await fetchVehicleTrack(vehicle.id)
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '获取轨迹失败')
    return
  }
  if (!trackPoints.value.length) {
    ElMessage.warning('该车辆暂无轨迹点')
    return
  }
  playbackIndex.value = 0
  if (replayTimer) window.clearInterval(replayTimer)
  replayTimer = window.setInterval(() => {
    if (playbackIndex.value >= trackPoints.value.length - 1) {
      window.clearInterval(replayTimer)
      ElMessage.success('轨迹回放完成')
      return
    }
    playbackIndex.value += 1
  }, 400)
  ElMessage.success(`开始回放：${vehicle.name}（${trackPoints.value.length} 个点）`)
}

function statusTag(s: string) {
  if (s === 'ONLINE') return 'success'
  if (s === 'ALARM') return 'danger'
  return 'info'
}

function onBaiduFail(message: string) {
  useBaidu.value = false
  ElMessage.warning(`百度地图不可用，已切换示意地图：${message}`)
}

onMounted(async () => {
  await ensureDicts()
  try {
    const cfg = await get<{ provider: string; baiduAk: string; enabled: boolean }>('/system/map-config')
    baiduAk.value = cfg.baiduAk || ''
    useBaidu.value = !!cfg.enabled && !!cfg.baiduAk
  } catch {
    useBaidu.value = false
  }
  await load(false)
  // 文档要求：30 秒自动刷新位置
  timer = window.setInterval(() => load(true), 30000)
  document.addEventListener('fullscreenchange', () => {
    isFullscreen.value = !!document.fullscreenElement
  })
})

onUnmounted(() => {
  if (timer) window.clearInterval(timer)
  if (replayTimer) window.clearInterval(replayTimer)
})
</script>

<style scoped>
.map-page { display: flex; flex-direction: column; gap: 12px; height: calc(100vh - 120px); }
.map-page.fullscreen { height: 100vh; padding: 12px; background: #0f172a; }
.toolbar { display: flex; flex-wrap: wrap; gap: 8px; align-items: center; }
.hint { color: #909399; font-size: 12px; }
.body { flex: 1; display: flex; gap: 12px; min-height: 0; }
.side {
  width: 240px;
  background: #fff;
  border-radius: 12px;
  padding: 12px;
  overflow: auto;
  flex-shrink: 0;
}
.side-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.mt { margin-top: 8px; }
.list { max-height: 360px; overflow: auto; }
.list-item {
  display: flex; gap: 8px; align-items: center;
  padding: 6px 0; border-bottom: 1px solid #f0f0f0;
  cursor: pointer; font-size: 13px;
}
.expand-btn { align-self: flex-start; }
.map-wrap { flex: 1; position: relative; min-width: 0; }
.stat-cards {
  position: absolute; right: 12px; top: 40px;
  display: flex; flex-direction: column; gap: 8px; z-index: 3;
}
.mini { width: 120px; text-align: center; }
.mini small { color: #909399; }
@media (max-width: 900px) {
  .side { display: none; }
  .stat-cards { flex-direction: row; right: 8px; top: 8px; }
  .mini { width: 90px; }
}
</style>
