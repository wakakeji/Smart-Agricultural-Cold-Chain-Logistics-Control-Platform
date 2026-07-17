# 实时环境监测模块（EM-001）

## 功能

- 传感器实时卡片数据（温湿度/CO₂/电量/状态）
- 按设施/类型/状态筛选
- 24h 历史趋势（演示数据生成，后续可接 InfluxDB）

## 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/monitor/sensors` | 传感器列表 |
| GET | `/api/monitor/sensor/{id}/history?hours=24` | 趋势数据 |
| GET | `/api/monitor/facilities` | 设施筛选器 |

## 说明

启动时自动补齐传感器至 **256** 个。前端使用轮询刷新（约 5s），WebSocket 可后续增强。
