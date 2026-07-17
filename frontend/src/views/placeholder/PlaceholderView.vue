<template>
  <el-card shadow="never">
    <el-empty :description="`${title} 功能开发中（S0 占位）`">
      <p class="tip">将按需求文档与 UI 原型逐功能实现，百度地图相关接口已预留。</p>
    </el-empty>
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const title = computed(() => (route.meta.title as string) || '功能页')
</script>

<style scoped>
.tip { color: #909399; font-size: 13px; }
</style>
