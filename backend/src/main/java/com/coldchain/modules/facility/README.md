# 设施监控模块（DT-001 地图监控）

## 功能

- 冷库 / 冷藏车列表查询（支持类型、状态筛选）
- 设施详情（含传感器）
- 车辆轨迹（无真实轨迹时生成模拟点）
- 启动时自动补齐 **128 冷库 + 12 车辆** 演示数据

## 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/facilities/list?type=all&status=&refresh=false` | 设施列表；`refresh=true` 抖动在线车辆坐标 |
| GET | `/api/facilities/{id}/detail` | 设施详情 |
| GET | `/api/vehicle/{id}/track?start=&end=` | 车辆轨迹（ISO 时间） |

## 百度地图说明

未配置 `coldchain.map.baidu-ak` 时，前端使用 **Mock 坐标投影地图**，接口与字段保持兼容；申请到 Key 后可替换底图组件。
