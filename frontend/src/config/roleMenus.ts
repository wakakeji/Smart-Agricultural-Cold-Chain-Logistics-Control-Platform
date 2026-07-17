/**
 * 一级菜单对齐文档「系统一级菜单」：
 * 数字孪生 / 环境监测 / 质量追溯 / 智能预测 / 路径优化 / 损耗率监控 / 系统管理
 */
export type MenuItem = { path: string; title: string }
export type MenuGroup = { key: string; title: string; children: MenuItem[] }

export const ROLE_PATHS: Record<string, string[]> = {
  admin: [
    '/map', '/quality', '/dashboard', '/alarm',
    '/monitor', '/data-quality',
    '/code', '/trace', '/blockchain',
    '/predict', '/suggestion',
    '/route', '/transport',
    '/loss', '/carbon',
    '/user', '/service-monitor', '/api-manage',
  ],
  farmer: ['/code', '/trace', '/predict', '/quality', '/monitor', '/map'],
  logistics: ['/map', '/dashboard', '/alarm', '/monitor', '/route', '/transport', '/suggestion'],
  driver: ['/map', '/transport', '/alarm', '/route'],
  wholesaler: ['/map', '/monitor', '/data-quality', '/alarm', '/trace', '/loss', '/predict', '/quality'],
  retailer: ['/trace', '/code', '/predict', '/map', '/quality'],
  consumer: ['/trace', '/h5/trace'],
}

export const ROLE_HOME: Record<string, string> = {
  admin: '/map',
  farmer: '/code',
  logistics: '/transport',
  driver: '/transport',
  wholesaler: '/monitor',
  retailer: '/trace',
  consumer: '/h5/trace',
}

export const MENU_GROUPS: MenuGroup[] = [
  {
    key: 'digital-twin',
    title: '数字孪生',
    children: [
      { path: '/map', title: '全网地图监控' },
      { path: '/quality', title: '品质监控' },
      { path: '/dashboard', title: '指挥大屏' },
      { path: '/alarm', title: '预警管理' },
    ],
  },
  {
    key: 'env',
    title: '环境监测',
    children: [
      { path: '/monitor', title: '实时传感器数据' },
      { path: '/data-quality', title: '网络传输质量监控' },
    ],
  },
  {
    key: 'trace',
    title: '质量追溯',
    children: [
      { path: '/code', title: '赋码管理' },
      { path: '/trace', title: '追溯链查询' },
      { path: '/blockchain', title: '区块链上链管理' },
    ],
  },
  {
    key: 'ai',
    title: '智能预测',
    children: [
      { path: '/predict', title: '品质衰变预测' },
      { path: '/suggestion', title: 'AI决策建议' },
    ],
  },
  {
    key: 'transport',
    title: '路径优化',
    children: [
      { path: '/route', title: '品质约束路线规划' },
      { path: '/transport', title: '在途运输实时监控' },
    ],
  },
  {
    key: 'stats',
    title: '损耗率监控',
    children: [
      { path: '/loss', title: '损耗专项统计' },
      { path: '/carbon', title: '碳排放监测' },
    ],
  },
  {
    key: 'system',
    title: '系统管理',
    children: [
      { path: '/user', title: '用户权限' },
      { path: '/service-monitor', title: '微服务监控' },
      { path: '/api-manage', title: 'API网关管理' },
    ],
  },
]

export function pathsForRole(role: string): string[] {
  return ROLE_PATHS[role] || []
}

export function canAccess(role: string, path: string): boolean {
  const allowed = pathsForRole(role)
  if (!allowed.length) return false
  return allowed.some((p) => path === p || path.startsWith(p + '/'))
}

export function homeForRole(role: string): string {
  return ROLE_HOME[role] || '/map'
}

export function menusForRole(role: string): MenuGroup[] {
  const allowed = new Set(pathsForRole(role))
  return MENU_GROUPS
    .map((g) => ({
      ...g,
      children: g.children.filter((c) => allowed.has(c.path)),
    }))
    .filter((g) => g.children.length > 0)
}
