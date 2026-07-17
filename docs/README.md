# 智慧农业冷链物流管控平台

## 技术栈（实际环境）

- 后端：SpringBoot 3 单体 + MyBatis-Plus + JWT
- 前端：Vue3 + TypeScript + Element Plus + ECharts
- 中间件：MySQL 8.4 / Redis / RabbitMQ / InfluxDB / MongoDB（`192.168.1.96`）
- 区块链：MySQL 模拟哈希存证
- AI：FastAPI（后续）

## 目录

```
backend/     后端
frontend/    前端
sql/         建表与 Mock
ai_service/  AI 服务（预留）
docs/        文档
```

## 快速启动

### 1. 初始化数据库

```bash
mysql -h 192.168.1.96 -uroot -p123456 < sql/01_schema.sql
mysql -h 192.168.1.96 -uroot -p123456 < sql/02_mock_data.sql
```

### 2. 启动后端（需 JDK 17 + Maven）

```bash
cd backend
mvn spring-boot:run
```

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

### 4. 登录

打开 http://localhost:5173 ，使用 `admin` / `Abc@123456`。

## InfluxDB Token 说明（给不懂的同学）

Token 相当于访问 InfluxDB 的“钥匙”。  
当前 **健康检查不需要 Token**；真正写入传感器时序数据时才需要。  
到时在服务器执行 `docker exec -it local-influxdb influx auth list` 拿到 Token，配置环境变量 `INFLUX_TOKEN` 即可。

## 百度地图

未申请 Key。后端预留 `coldchain.map.baidu-ak`，前端地图页使用占位组件，申请后填入即可。

## 开发阶段

- **已完成（验收功能全量）**：S0、DT-001、VM-001、VM-003、EM-001/005、QT-002/003/004/006、AI-001/004、RP-001/007、LR-001、CE-001、API-001
- 区块链为 MySQL 模拟哈希；地图为 Mock 投影；AI 为 Java 规则/XGBoost-stub
