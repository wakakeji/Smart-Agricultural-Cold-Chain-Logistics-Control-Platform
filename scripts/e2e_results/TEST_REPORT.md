# 功能测试问题清单与修复记录

生成时间：2026-07-17

## 1. 配置是否生效？

**是。** 当前后端 `application-dev.yml` / `spring.profiles.active=dev` 已连接：

| 组件 | 地址 |
|------|------|
| MySQL | `192.168.1.96:3306/app_db` |
| Redis | `192.168.1.96:6379` |
| MongoDB | `192.168.1.96:27017` |
| RabbitMQ | `192.168.1.96:5672` |
| InfluxDB | `http://192.168.1.96:8086` |
| 百度地图 AK | 已配置（需百度控制台开通 JS API） |

自动化登录校验 `admin` 的 `realName` 已能从该库读出。

## 2. API 自动化测试结果（修复前）

脚本：`scripts/api_full_test.py` → `scripts/e2e_results/api_issues.json`

| 严重度 | 页面 | 问题 |
|--------|------|------|
| major | 预警管理 | 来源/内容 `????` |
| major | 运输监控 | 路线 `??->??`，车辆 `-` |
| major | 赋码/二维码 | 相对路径，手机扫码打不开 |
| minor | 品质预测 | 特征英文 avgTemp 等 |
| info | 追溯查询 | 默认空页（需输入批次），API 本身有数据 |
| info | 地图 | 百度 AK「服务被禁用」属账号认证问题 |
| major | 侧边栏 | `overflow:hidden` 无法滚动（前端） |

## 3. 已做修复

1. **乱码**：`sql/gen_fix_utf8.py` + `sql/repair_business.py` 修复告警/路线/用户等 UTF-8
2. **运输车辆**：`TransportService` 冷藏车 ID 兜底匹配（设施种子重建后 ID 变化）
3. **菜单滚动**：`MainLayout.vue` `overflow-y: auto`
4. **二维码**：`coldchain.public-base-url=http://192.168.1.3:5173`，生成绝对链接
5. **追溯查询**：增加示例批次快捷按钮
6. **品质预测**：特征/模型名改为中文；风险显示低/中/高
7. **API 管理**：模块与状态中文化

## 4. 使用说明（验收）

- 追溯：点「20260716000001 百色芒果」或「20260716000002 武鸣沃柑」
- 手机扫码：手机与电脑同一局域网，前端用 `http://192.168.1.3:5173` 打开后再生成/扫码；勿用 localhost
- 百度地图：完成个人开发者认证并开通「浏览器端 JavaScript API」
- **必须重启后端**后 Java 改动才生效；前端 Vite 一般热更新即可
