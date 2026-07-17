# 后端服务 - 智慧农业冷链物流管控平台

SpringBoot 3 单体应用，MyBatis-Plus + MySQL + Redis + RabbitMQ + MongoDB + InfluxDB。

## 环境要求

- JDK 17+
- Maven 3.9+
- 中间件已部署在 `192.168.1.96`（见根目录 `docker-compose.yml`）

## 启动前准备

1. 导入数据库：

```bash
mysql -h 192.168.1.96 -uroot -p123456 < ../sql/01_schema.sql
mysql -h 192.168.1.96 -uroot -p123456 < ../sql/02_mock_data.sql
```

2. （可选）InfluxDB Token：写入时序数据时需要。可在服务器执行：

```bash
docker exec -it local-influxdb influx auth list
```

将 Token 写入环境变量 `INFLUX_TOKEN`。**健康检查不依赖 Token。**

## 启动

```bash
cd backend
mvn spring-boot:run
```

- 接口文档：http://localhost:8080/swagger-ui.html
- 健康检查：`GET /api/system/health`（需登录）

## 演示账号

用户名见 `sql/README.md`，密码统一：`Abc@123456`  
（dev 环境启动时会自动校正密码哈希）

## 测试

```bash
mvn test
```

## 模块说明（S0）

| 模块 | 路径 | 说明 |
|------|------|------|
| 认证 | `/api/auth/**` | 登录/登出/用户信息 |
| 用户 | `/api/system/users` | 用户 CRUD、启停 |
| 角色 | `/api/system/roles` | 角色列表 |
| 服务监控 | `/api/system/health` | MySQL/Redis/Rabbit/Mongo/Influx 探测 |
