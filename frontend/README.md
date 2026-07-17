# 前端 - 智慧农业冷链物流管控平台

Vue 3 + TypeScript + Vite + Element Plus + Pinia + ECharts（响应式布局）

## 启动

```bash
cd frontend
npm install
npm run dev
```

访问：http://localhost:5173

开发环境通过 Vite 代理将 `/api` 转发到 `http://localhost:8080`。

## 已实现页面

| 路由 | 说明 |
|------|------|
| `/login` | 7 角色切换登录 |
| `/user` | 用户权限管理 |
| `/service-monitor` | 系统/服务监控（中间件健康检查） |
| `/map` | 地图监控 DT-001（Mock 地图，无百度 Key） |
| `/dashboard` | 指挥大屏 VM-001（深色主题 + ECharts） |
| `/alarm` | 预警管理 VM-003 |
| `/monitor` | 实时数据 EM-001 |
| `/data-quality` | 数据质量 EM-005（KPI + 问题列表） |
| `/code` | 赋码管理 QT-002 |
| `/trace` | 追溯查询 QT-003（时间轴 + 链上验证） |
| `/blockchain` | 区块链存证（概览 + 交易 + 验证） |
| `/predict` | 品质预测 AI-001（模型 + 曲线 + 历史） |
| `/suggestion` | 决策建议（统计 + 采纳/忽略） |
| `/route` | 路线规划 RP-001（三方案对比） |
| `/transport` | 运输监控（在途列表 + 实时弹窗） |
| `/loss` | 损耗率统计（概览 + 趋势 + 明细） |
| `/carbon` | 碳排放统计（概览 + 来源 + 明细） |
| `/api-manage` | API 管理（目录 + 密钥生成） |
| `/h5/trace` | 消费者 H5 溯源（公开，支持 `?batchNo=`） |

## 演示账号

见 `../sql/README.md`，密码：`Abc@123456`
