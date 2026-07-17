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
        <el-sub-menu v-for="g in menus" :key="g.key" :index="g.key">
          <template #title><span>{{ g.title }}</span></template>
          <el-menu-item v-for="c in g.children" :key="c.path" :index="c.path">
            {{ c.title }}
          </el-menu-item>
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
import { menusForRole } from '@/config/roleMenus'

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
  admin: '平台管理员',
}
const roleLabel = computed(() => roleMap[userStore.role] || userStore.role)
const menus = computed(() => menusForRole(userStore.role))

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
  overflow-x: hidden;
  overflow-y: auto;
  height: 100vh;
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
