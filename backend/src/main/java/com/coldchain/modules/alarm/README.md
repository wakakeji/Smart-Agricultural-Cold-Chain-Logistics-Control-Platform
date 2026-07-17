# 预警管理模块（VM-003）

## 功能

- 告警分页筛选（级别/状态/类型/时间）
- 单条处理、批量处理（RESOLVED / IGNORED / PROCESSING）
- 统计：按状态、级别、类型聚合

## 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/alarm/list` | 分页列表 |
| GET | `/api/alarm/{id}` | 详情 |
| PUT | `/api/alarm/{id}/handle` | 单条处理 |
| PUT | `/api/alarm/batch-handle` | 批量处理 |
| GET | `/api/alarm/stats` | 统计 |

## 状态流转

`PENDING` → `PROCESSING` / `RESOLVED` / `IGNORED`
