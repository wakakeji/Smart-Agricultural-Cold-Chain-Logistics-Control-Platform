<template>
  <div class="baidu-map">
    <div ref="elRef" class="canvas" />
    <div v-if="error" class="err">
      <div>{{ error }}</div>
      <div class="hint">请检查百度控制台是否开通「浏览器端 / JavaScript API」，白名单含 localhost</div>
    </div>
    <div class="legend">
      <span><i class="dot storage" />冷库</span>
      <span><i class="dot vehicle" />车辆</span>
      <span><i class="dot alarm" />告警</span>
      <span><i class="dot offline" />离线</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import type { FacilityItem, TrackPoint } from '@/types/facility'

const props = withDefaults(defineProps<{
  ak: string
  items: FacilityItem[]
  track?: TrackPoint[]
  playbackIndex?: number
  zoom?: number
}>(), {
  track: () => [],
  playbackIndex: -1,
  zoom: 12,
})

const emit = defineEmits<{
  select: [item: FacilityItem]
  fail: [message: string]
}>()

const elRef = ref<HTMLElement>()
const error = ref('')
// eslint-disable-next-line @typescript-eslint/no-explicit-any
let map: any = null
// eslint-disable-next-line @typescript-eslint/no-explicit-any
let markers: any[] = []
// eslint-disable-next-line @typescript-eslint/no-explicit-any
let polyline: any = null
// eslint-disable-next-line @typescript-eslint/no-explicit-any
let playMarker: any = null
let destroyed = false

function getBMap(): any {
  return (window as unknown as { BMap?: any }).BMap
}

function loadScript(ak: string): Promise<void> {
  if (getBMap()) return Promise.resolve()
  return new Promise((resolve, reject) => {
    const cb = `__bmap_init_${Date.now()}`
    const timeout = window.setTimeout(() => {
      reject(new Error('百度地图脚本加载超时，请检查网络或 AK'))
    }, 15000)
    ;(window as unknown as Record<string, () => void>)[cb] = () => {
      window.clearTimeout(timeout)
      if (!getBMap()?.Map) {
        reject(new Error('百度地图 SDK 未完整加载（可能 AK 未开通 JS API）'))
        return
      }
      resolve()
    }
    const s = document.createElement('script')
    s.src = `https://api.map.baidu.com/api?v=3.0&ak=${encodeURIComponent(ak)}&callback=${cb}`
    s.onerror = () => {
      window.clearTimeout(timeout)
      reject(new Error('百度地图脚本加载失败'))
    }
    document.head.appendChild(s)
  })
}

function markerColor(status: string, type: string) {
  if (status === 'ALARM') return '#F56C6C'
  if (status === 'OFFLINE') return '#909399'
  return type === 'REFRIGERATED_VEHICLE' ? '#409EFF' : '#67C23A'
}

function safePoint(BMap: any, lng: number, lat: number) {
  const x = Number(lng)
  const y = Number(lat)
  if (!Number.isFinite(x) || !Number.isFinite(y)) return null
  try {
    return new BMap.Point(x, y)
  } catch {
    return null
  }
}

function renderMarkers() {
  const BMap = getBMap()
  if (!map || !BMap || error.value) return
  try {
    markers.forEach((m) => map.removeOverlay(m))
    markers = []
    props.items.forEach((item) => {
      if (item.lng == null || item.lat == null) return
      const pt = safePoint(BMap, item.lng, item.lat)
      if (!pt) return
      // 使用默认 Marker，避免自定义 data-URI Icon 触发百度内部 coordType 空指针
      const mk = new BMap.Marker(pt)
      const label = new BMap.Label('●', {
        offset: new BMap.Size(-6, -10),
      })
      label.setStyle({
        color: markerColor(item.status, item.type),
        border: 'none',
        background: 'transparent',
        fontSize: '16px',
        fontWeight: 'bold',
      })
      mk.setLabel(label)
      mk.addEventListener('click', () => emit('select', item))
      map.addOverlay(mk)
      markers.push(mk)
    })
  } catch (e) {
    const msg = e instanceof Error ? e.message : String(e)
    error.value = msg
    emit('fail', msg)
  }
}

function renderTrack() {
  const BMap = getBMap()
  if (!map || !BMap || error.value) return
  try {
    if (polyline) {
      map.removeOverlay(polyline)
      polyline = null
    }
    if (playMarker) {
      map.removeOverlay(playMarker)
      playMarker = null
    }
    const pts = (props.track || [])
      .map((p) => safePoint(BMap, p.lng, p.lat))
      .filter(Boolean)
    if (pts.length > 1) {
      polyline = new BMap.Polyline(pts, { strokeColor: '#67C23A', strokeWeight: 3, strokeOpacity: 0.85 })
      map.addOverlay(polyline)
    }
    if (props.playbackIndex != null && props.playbackIndex >= 0 && props.track?.[props.playbackIndex]) {
      const p = props.track[props.playbackIndex]
      const pt = safePoint(BMap, p.lng, p.lat)
      if (pt) {
        playMarker = new BMap.Marker(pt)
        map.addOverlay(playMarker)
      }
    }
  } catch (e) {
    const msg = e instanceof Error ? e.message : String(e)
    error.value = msg
    emit('fail', msg)
  }
}

async function init() {
  if (!elRef.value || !props.ak || destroyed) return
  error.value = ''
  try {
    await loadScript(props.ak)
    await nextTick()
    // 容器未完成布局时初始化易触发百度内部 null.coordType
    const el = elRef.value
    if (!el || el.clientWidth < 10 || el.clientHeight < 10) {
      await new Promise((r) => setTimeout(r, 120))
    }
    if (destroyed || !elRef.value) return
    const BMap = getBMap()
    if (!BMap?.Map) {
      throw new Error('百度地图 SDK 不可用，请确认 AK 已开通 JavaScript API')
    }
    map = new BMap.Map(elRef.value)
    const center = safePoint(BMap, 108.37, 22.82) || new BMap.Point(108.37, 22.82)
    map.centerAndZoom(center, props.zoom || 12)
    map.enableScrollWheelZoom(true)
    map.addControl(new BMap.NavigationControl())
    map.addControl(new BMap.ScaleControl())
    renderMarkers()
    renderTrack()
  } catch (e) {
    const msg = e instanceof Error ? e.message : '地图初始化失败'
    error.value = msg.includes('coordType')
      ? '百度地图初始化失败（coordType）。多为 AK 未开通浏览器端 JS API，或自定义图标异常。'
      : msg
    emit('fail', error.value)
  }
}

watch(() => props.items, renderMarkers, { deep: true })
watch(() => [props.track, props.playbackIndex], renderTrack, { deep: true })

onMounted(() => {
  void init()
})
onUnmounted(() => {
  destroyed = true
  try {
    markers.forEach((m) => map?.removeOverlay?.(m))
    if (polyline) map?.removeOverlay?.(polyline)
    if (playMarker) map?.removeOverlay?.(playMarker)
  } catch {
    /* ignore */
  }
  map = null
})
</script>

<style scoped>
.baidu-map { position: relative; width: 100%; height: 100%; min-height: 420px; border-radius: 12px; overflow: hidden; }
.canvas { width: 100%; height: 100%; min-height: 420px; }
.err {
  position: absolute; inset: 0; display: grid; place-content: center; gap: 8px;
  background: #fef0f0; color: #f56c6c; padding: 16px; text-align: center; z-index: 3;
}
.hint { color: #909399; font-size: 12px; max-width: 420px; margin: 0 auto; }
.legend {
  position: absolute; left: 12px; bottom: 12px; z-index: 2;
  display: flex; gap: 10px; background: rgba(255,255,255,.9); padding: 6px 10px; border-radius: 8px; font-size: 12px;
}
.dot { display: inline-block; width: 10px; height: 10px; border-radius: 50%; margin-right: 4px; }
.dot.storage { background: #67C23A; }
.dot.vehicle { background: #409EFF; }
.dot.alarm { background: #F56C6C; }
.dot.offline { background: #909399; }
</style>
