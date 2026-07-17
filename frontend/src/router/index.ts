import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { public: true },
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/service-monitor',
    children: [
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/system/UserView.vue'),
        meta: { title: '用户权限', roles: ['admin'] },
      },
      {
        path: 'service-monitor',
        name: 'ServiceMonitor',
        component: () => import('@/views/system/ServiceMonitorView.vue'),
        meta: { title: '系统/服务监控', roles: ['admin'] },
      },
      // 后续功能占位
      { path: 'map', name: 'Map', component: () => import('@/views/map/MapView.vue'), meta: { title: '地图监控' } },
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/dashboard/DashboardView.vue'), meta: { title: '指挥大屏' } },
      { path: 'alarm', name: 'Alarm', component: () => import('@/views/alarm/AlarmView.vue'), meta: { title: '预警管理' } },
      { path: 'monitor', name: 'Monitor', component: () => import('@/views/monitor/MonitorView.vue'), meta: { title: '实时数据' } },
      { path: 'data-quality', name: 'DataQuality', component: () => import('@/views/monitor/DataQualityView.vue'), meta: { title: '数据质量' } },
      { path: 'code', name: 'Code', component: () => import('@/views/trace/CodeView.vue'), meta: { title: '赋码管理' } },
      { path: 'trace', name: 'Trace', component: () => import('@/views/trace/TraceView.vue'), meta: { title: '追溯查询' } },
      { path: 'blockchain', name: 'Blockchain', component: () => import('@/views/blockchain/BlockchainView.vue'), meta: { title: '区块链存证' } },
      { path: 'predict', name: 'Predict', component: () => import('@/views/predict/PredictView.vue'), meta: { title: '品质预测' } },
      { path: 'suggestion', name: 'Suggestion', component: () => import('@/views/suggestion/SuggestionView.vue'), meta: { title: '决策建议' } },
      { path: 'route', name: 'Route', component: () => import('@/views/route/RouteView.vue'), meta: { title: '路线规划' } },
      { path: 'transport', name: 'Transport', component: () => import('@/views/transport/TransportView.vue'), meta: { title: '运输监控' } },
      { path: 'loss', name: 'Loss', component: () => import('@/views/stats/LossView.vue'), meta: { title: '损耗率' } },
      { path: 'carbon', name: 'Carbon', component: () => import('@/views/stats/CarbonView.vue'), meta: { title: '碳排放' } },
      { path: 'api-manage', name: 'ApiManage', component: () => import('@/views/system/ApiManageView.vue'), meta: { title: 'API管理' } },
    ],
  },
  {
    path: '/h5/trace',
    name: 'H5Trace',
    component: () => import('@/views/trace/H5TraceView.vue'),
    meta: { title: '消费者H5溯源', public: true },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const store = useUserStore()
  if (to.meta.public) return true
  if (!store.isLogin) return { path: '/login', query: { redirect: to.fullPath } }
  // 管理员登录后默认可进系统管理页；其他角色先进地图占位
  if (to.path === '/' || to.path === '/service-monitor') {
    if (store.role !== 'admin' && to.path === '/service-monitor') {
      return { path: '/map' }
    }
  }
  return true
})

export default router
