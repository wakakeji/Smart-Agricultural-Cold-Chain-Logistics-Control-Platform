<template>
  <div class="mock-map" :class="layerClass">
    <div class="map-hint">
      模拟地图（广西区域投影）· 百度 Key 未配置 · 中心 {{ centerLng.toFixed(2) }}, {{ centerLat.toFixed(2) }} · 缩放 {{ zoom }}
    </div>
    <svg class="canvas" viewBox="0 0 1000 640" preserveAspectRatio="xMidYMid meet" @wheel.prevent="onWheel">
      <defs>
        <pattern id="grid" width="40" height="40" patternUnits="userSpaceOnUse">
          <path d="M 40 0 L 0 0 0 40" fill="none" stroke="rgba(255,255,255,.08)" stroke-width="1" />
        </pattern>
      </defs>
      <rect width="1000" height="640" fill="url(#grid)" />
      <!-- 轨迹线 -->
      <polyline
        v-if="trackPath"
        :points="trackPath"
        fill="none"
        stroke="#67C23A"
        stroke-width="3"
        stroke-dasharray="8 4"
        opacity="0.85"
      />
      <!-- 设施点 -->
      <g
        v-for="m in projected"
        :key="m.id"
        class="marker"
        @click.stop="emit('select', m.raw)"
      >
        <circle
          :cx="m.x"
          :cy="m.y"
          :r="m.type === 'REFRIGERATED_VEHICLE' ? 8 : 7"
          :fill="markerColor(m.status, m.type)"
          stroke="#fff"
          stroke-width="1.5"
        />
        <text v-if="zoom >= 11" :x="m.x + 10" :y="m.y + 4" class="label">{{ shortName(m.name) }}</text>
      </g>
      <!-- 回放当前位置 -->
      <circle
        v-if="playback"
        :cx="playback.x"
        :cy="playback.y"
        r="10"
        fill="#E6A23C"
        stroke="#fff"
        stroke-width="2"
      />
    </svg>
    <div class="legend">
      <span><i class="dot storage" />冷库</span>
      <span><i class="dot vehicle" />车辆</span>
      <span><i class="dot alarm" />告警</span>
      <span><i class="dot offline" />离线</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { FacilityItem, TrackPoint } from '@/types/facility'

const props = withDefaults(defineProps<{
  items: FacilityItem[]
  track?: TrackPoint[]
  playbackIndex?: number
  layer?: 'normal' | 'satellite' | 'heat'
  zoom?: number
}>(), {
  track: () => [],
  playbackIndex: -1,
  layer: 'normal',
  zoom: 12,
})

const emit = defineEmits<{
  select: [item: FacilityItem]
  'update:zoom': [z: number]
}>()

const zoom = computed({
  get: () => props.zoom,
  set: (z) => emit('update:zoom', z),
})

// 广西投影边界（GCJ-02 近似）
const BOUNDS = { minLng: 104.2, maxLng: 112.3, minLat: 20.8, maxLat: 26.8 }
const centerLng = computed(() => (BOUNDS.minLng + BOUNDS.maxLng) / 2)
const centerLat = computed(() => (BOUNDS.minLat + BOUNDS.maxLat) / 2)

const layerClass = computed(() => `layer-${props.layer}`)

function project(lng: number, lat: number) {
  const scale = 0.7 + (zoom.value - 10) * 0.08
  const x0 = ((lng - BOUNDS.minLng) / (BOUNDS.maxLng - BOUNDS.minLng)) * 1000
  const y0 = (1 - (lat - BOUNDS.minLat) / (BOUNDS.maxLat - BOUNDS.minLat)) * 640
  const cx = 500
  const cy = 320
  return {
    x: cx + (x0 - cx) * scale,
    y: cy + (y0 - cy) * scale,
  }
}

const projected = computed(() =>
  props.items.map((raw) => {
    const p = project(raw.lng, raw.lat)
    return { ...p, id: raw.id, name: raw.name, type: raw.type, status: raw.status, raw }
  }),
)

const trackPath = computed(() => {
  if (!props.track?.length) return ''
  return props.track.map((t) => {
    const p = project(t.lng, t.lat)
    return `${p.x},${p.y}`
  }).join(' ')
})

const playback = computed(() => {
  if (!props.track?.length || props.playbackIndex < 0) return null
  const t = props.track[Math.min(props.playbackIndex, props.track.length - 1)]
  return project(t.lng, t.lat)
})

function markerColor(status: string, type: string) {
  if (status === 'ALARM') return '#F56C6C'
  if (status === 'OFFLINE') return '#909399'
  return type === 'REFRIGERATED_VEHICLE' ? '#67C23A' : '#409EFF'
}

function shortName(name: string) {
  return name.length > 8 ? name.slice(0, 8) + '…' : name
}

function onWheel(e: WheelEvent) {
  const next = Math.min(14, Math.max(9, zoom.value + (e.deltaY > 0 ? -1 : 1)))
  zoom.value = next
}
</script>

<style scoped>
.mock-map {
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 480px;
  border-radius: 12px;
  overflow: hidden;
  background: linear-gradient(160deg, #0b1f33, #12304a 50%, #0d3b2e);
}
.layer-satellite { background: linear-gradient(160deg, #1a2a1a, #2d4a2d 40%, #1e3a2f); }
.layer-heat { background: linear-gradient(160deg, #2a1030, #3a2048 50%, #1a2038); }
.map-hint {
  position: absolute;
  z-index: 2;
  left: 12px;
  top: 12px;
  padding: 6px 10px;
  border-radius: 6px;
  background: rgba(0, 0, 0, .45);
  color: #e5e7eb;
  font-size: 12px;
}
.canvas { width: 100%; height: 100%; display: block; cursor: grab; }
.marker { cursor: pointer; }
.label { fill: #e5e7eb; font-size: 11px; pointer-events: none; }
.legend {
  position: absolute;
  right: 12px;
  bottom: 12px;
  display: flex;
  gap: 12px;
  padding: 8px 12px;
  border-radius: 8px;
  background: rgba(0, 0, 0, .45);
  color: #e5e7eb;
  font-size: 12px;
}
.dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 4px;
}
.dot.storage { background: #409EFF; }
.dot.vehicle { background: #67C23A; }
.dot.alarm { background: #F56C6C; }
.dot.offline { background: #909399; }
</style>
