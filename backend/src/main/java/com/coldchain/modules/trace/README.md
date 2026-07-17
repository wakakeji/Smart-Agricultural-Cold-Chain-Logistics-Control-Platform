# 质量追溯模块（QT-002 赋码 / QT-003 追溯）

## 功能

- 创建产品批次，自动生成批次号（YYYYMMDD + 6 位流水）
- 生成 H5 追溯链接作为二维码内容
- 模拟区块链上链（SHA-256 哈希写入 `blockchain_tx`）
- 全链条追溯时间轴 + 链上验证（公开接口，供 H5 使用）

## 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/batch/create` | 创建并赋码上链 |
| GET | `/api/batch/list` | 批次分页 |
| GET | `/api/batch/{id}/qr-code` | 二维码内容 |
| GET | `/api/trace/query` | 追溯查询（公开） |
| GET | `/api/trace/verify` | 存证验证（公开） |
