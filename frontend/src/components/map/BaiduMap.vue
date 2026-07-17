<template>
  <div class="baidu-map">
    <div ref="elRef" class="canvas" />
    <div v-if="error" class="err">{{ error }}</div>
    <div class="legend">
      <span><i class="dot storage" />冷库</span>
      <span><i class="dot vehicle" />车辆</span>
      <span><i class="dot alarm" />告警</span>
      <span><i class="dot offline" />离线</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch } from 'vue'
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

const emit = defineEmits<{ select: [item: FacilityItem] }>()

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

function loadScript(ak: string): Promise<void> {
  const w = window as unknown as { BMap?: unknown }
  if (w.BMap) return Promise.resolve()
  return new Promise((resolve, reject) => {
    const cb = `__bmap_init_${Date.now()}`
    ;(window as unknown as Record<string, () => void>)[cb] = () => resolve()
    const s = document.createElement('script')
    s.src = `https://api.map.baidu.com/api?v=3.0&ak=${encodeURIComponent(ak)}&callback=${cb}`
    s.onerror = () => reject(new Error('百度地图脚本加载失败'))
    document.head.appendChild(s)
  })
}

function markerColor(status: string, type: string) {
  if (status === 'ALARM') return '#F56C6C'
  if (status === 'OFFLINE') return '#909399'
  return type === 'REFRIGERATED_VEHICLE' ? '#409EFF' : '#67C23A'
}

function renderMarkers() {
  const BMap = (window as unknown as { BMap: any }).BMap
  if (!map || !BMap) return
  markers.forEach((m) => map.removeOverlay(m))
  markers = []
  props.items.forEach((item) => {
    if (item.lng == null || item.lat == null) return
    const pt = new BMap.Point(item.lng, item.lat)
    const icon = new BMap.Icon(
      'data:image/svg+xml;utf8,' + encodeURIComponent(
        `<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18"><circle cx="9" cy="9" r="7" fill="${markerColor(item.status, item.type)}" stroke="#fff" stroke-width="2"/></svg>`,
      ),
      new BMap.Size(18, 18),
    )
    const mk = new BMap.Marker(pt, { icon })
    mk.addEventListener('click', () => emit('select', item))
    map.addOverlay(mk)
    markers.push(mk)
  })
}

function renderTrack() {
  const BMap = (window as unknown as { BMap: any }).BMap
  if (!map || !BMap) return
  if (polyline) {
    map.removeOverlay(polyline)
    polyline = null
  }
  if (playMarker) {
    map.removeOverlay(playMarker)
    playMarker = null
  }
  const pts = (props.track || []).map((p) => new BMap.Point(p.lng, p.lat))
  if (pts.length > 1) {
    polyline = new BMap.Polyline(pts, { strokeColor: '#67C23A', strokeWeight: 3, strokeOpacity: 0.85 })
    map.addOverlay(polyline)
  }
  if (props.playbackIndex != null && props.playbackIndex >= 0 && props.track?.[props.playbackIndex]) {
    const p = props.track[props.playbackIndex]
    playMarker = new BMap.Marker(new BMap.Point(p.lng, p.lat))
    map.addOverlay(playMarker)
  }
}

async function init() {
  if (!elRef.value || !props.ak) return
  try {
    await loadScript(props.ak)
    const BMap = (window as unknown as { BMap: any }).BMap
    map = new BMap.Map(elRef.value)
    map.centerAndZoom(new BMap.Point(108.37, 22.82), props.zoom || 12)
    map.enableScrollWheelZoom(true)
    map.addControl(new BMap.NavigationControl())
    map.addControl(new BMap.ScaleControl())
    renderMarkers()
    renderTrack()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '地图初始化失败'
  }
}

watch(() => props.items, renderMarkers, { deep: true })
watch(() => [props.track, props.playbackIndex], renderTrack, { deep: true })

onMounted(init)
onUnmounted(() => {
  map = null
})
</script>

<style scoped>
.baidu-map { position: relative; width: 100%; height: 100%; min-height: 420px; border-radius: 12px; overflow: hidden; }
.canvas { width: 100%; height: 100%; min-height: 420px; }
.err { position: absolute; inset: 0; display: grid; place-items: center; background: #fef0f0; color: #f56c6c; }
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
