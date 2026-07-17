<template>
  <el-container class="layout">
    <el-aside :width="collapsed ? '64px' : '220px'" class="aside">
      <div class="brand">
        <span v-if="!collapsed">智慧冷链管控平台</span>
        <span v-else>冷链</span>
      </div>
      <el-menu
        :default-active="route.path"
        :collapse="collapsed"
        background-color="#1d1e1f"
        text-color="#c0c4cc"
        active-text-color="#fff"
        router
      >
        <el-sub-menu index="monitor-center">
          <template #title><span>监控中心</span></template>
          <el-menu-item index="/map">地图监控</el-menu-item>
          <el-menu-item index="/dashboard">指挥大屏</el-menu-item>
          <el-menu-item index="/alarm">预警管理</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="env">
          <template #title><span>环境监测</span></template>
          <el-menu-item index="/monitor">实时数据</el-menu-item>
          <el-menu-item index="/data-quality">数据质量</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="trace">
          <template #title><span>追溯管理</span></template>
          <el-menu-item index="/code">赋码管理</el-menu-item>
          <el-menu-item index="/trace">追溯查询</el-menu-item>
          <el-menu-item index="/blockchain">区块链存证</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="ai">
          <template #title><span>AI智能</span></template>
          <el-menu-item index="/predict">品质预测</el-menu-item>
          <el-menu-item index="/suggestion">决策建议</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="transport">
          <template #title><span>运输管理</span></template>
          <el-menu-item index="/route">路线规划</el-menu-item>
          <el-menu-item index="/transport">运输监控</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="stats">
          <template #title><span>统计分析</span></template>
          <el-menu-item index="/loss">损耗率</el-menu-item>
          <el-menu-item index="/carbon">碳排放</el-menu-item>
        </el-sub-menu>
        <el-sub-menu v-if="userStore.role === 'admin'" index="system">
          <template #title><span>系统管理</span></template>
          <el-menu-item index="/user">用户权限</el-menu-item>
          <el-menu-item index="/service-monitor">系统/服务监控</el-menu-item>
          <el-menu-item index="/api-manage">API管理</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="left">
          <el-button text @click="collapsed = !collapsed">
            <el-icon><Fold v-if="!collapsed" /><Expand v-else /></el-icon>
          </el-button>
          <span class="title">{{ (route.meta.title as string) || '首页' }}</span>
        </div>
        <div class="right">
          <span class="user">{{ userStore.userInfo?.realName }}（{{ roleLabel }}）</span>
          <el-button type="primary" link @click="onLogout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Fold, Expand } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const collapsed = ref(false)

const roleMap: Record<string, string> = {
  farmer: '农户',
  logistics: '物流企业',
  driver: '司机',
  wholesaler: '批发商',
  retailer: '零售商',
  consumer: '消费者',
  admin: '管理员',
}
const roleLabel = computed(() => roleMap[userStore.role] || userStore.role)

async function onLogout() {
  await userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout { height: 100vh; background: #f5f7fa; }
.aside {
  background: #1d1e1f;
  transition: width .2s;
  overflow: hidden;
}
.brand {
  height: 56px;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  border-bottom: 1px solid #333;
  white-space: nowrap;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
}
.left, .right { display: flex; align-items: center; gap: 8px; }
.title { font-weight: 600; color: #303133; }
.user { color: #606266; font-size: 14px; }
.main { padding: 16px; }
@media (max-width: 768px) {
  .aside { position: fixed; z-index: 20; height: 100%; }
  .user { display: none; }
}
</style>
