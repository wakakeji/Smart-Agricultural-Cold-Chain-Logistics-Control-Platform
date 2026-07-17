# 数据库脚本说明

## 文件

| 文件 | 说明 |
|------|------|
| `01_schema.sql` | 建库建表（含 `sys_dict` 字典表） |
| `02_mock_data.sql` | 测试账号、设施、基础告警/批次 |
| `03_alter_batch.sql` | 批次字段扩展 |
| `04_business_seed.sql` | 业务种子（运输/损耗/碳排放/字典等，**禁止写在 Java 里**） |

## 执行方式

推荐（UTF-8）：

```powershell
powershell -File sql/import.ps1
```

或手动：

```bash
mysql -h 192.168.1.96 -uroot -p123456 < 01_schema.sql
mysql -h 192.168.1.96 -uroot -p123456 < 02_mock_data.sql
mysql -h 192.168.1.96 -uroot -p123456 < 04_business_seed.sql
```

## 演示账号

| 用户名 | 角色 | 密码 |
|--------|------|------|
| admin | 管理员 | Abc@123456 |
| farmer01 | 农户 | Abc@123456 |
| logistics01 | 物流企业 | Abc@123456 |
| driver01 | 司机 | Abc@123456 |
| wholesaler01 | 批发商 | Abc@123456 |
| retailer01 | 零售商 | Abc@123456 |
| consumer01 | 消费者 | Abc@123456 |

> 若登录失败，多半是 BCrypt 哈希与当前编码器不一致，启动后端后调用重置密码接口或重新生成哈希写入即可。
