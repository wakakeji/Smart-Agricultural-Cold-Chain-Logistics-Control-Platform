-- 智慧农业冷链物流管控平台 - Mock 测试数据
-- 默认密码均为: Abc@123456（BCrypt）
USE app_db;

-- 清空（开发环境）
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE sys_oper_log;
TRUNCATE TABLE loss_record;
TRUNCATE TABLE blockchain_tx;
TRUNCATE TABLE carbon_emission;
TRUNCATE TABLE decision_suggestion;
TRUNCATE TABLE quality_prediction;
TRUNCATE TABLE alarm_record;
TRUNCATE TABLE vehicle_track;
TRUNCATE TABLE transport_order;
TRUNCATE TABLE trace_record;
TRUNCATE TABLE product_batch;
TRUNCATE TABLE sensor_data;
TRUNCATE TABLE sensor_device;
TRUNCATE TABLE facility;
TRUNCATE TABLE sys_user;
TRUNCATE TABLE sys_role;
SET FOREIGN_KEY_CHECKS = 1;

-- 7 个角色
INSERT INTO sys_role (role_code, role_name, permissions, status, remark) VALUES
('farmer', '农户/生产者', '["map:read","code:write","trace:read","blockchain:read"]', 1, '录入产品、赋码'),
('logistics', '物流企业', '["map:write","alarm:write","monitor:write","predict:write","route:write","transport:write"]', 1, '调度运输'),
('driver', '司机', '["map:read","alarm:read","monitor:read","trace:read","route:read","transport:write"]', 1, '接单上报'),
('wholesaler', '批发商/仓储方', '["map:read","alarm:write","monitor:write","trace:read","predict:write"]', 1, '仓储质检'),
('retailer', '零售商', '["map:read","alarm:read","monitor:read","trace:read","predict:read","blockchain:read"]', 1, '收货上架'),
('consumer', '消费者', '["h5:write","trace:read","blockchain:read"]', 1, '扫码溯源'),
('admin', '平台管理员', '["*:*:*"]', 1, '系统管理');

-- 7 个演示账号（密码: Abc@123456）
-- BCrypt: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi
INSERT INTO sys_user (username, password, real_name, role_code, phone, email, avatar, status) VALUES
('farmer01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张农户', 'farmer', '13800000001', 'farmer@example.com', NULL, 1),
('logistics01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李调度', 'logistics', '13800000002', 'logistics@example.com', NULL, 1),
('driver01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '王司机', 'driver', '13800000003', 'driver@example.com', NULL, 1),
('wholesaler01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '赵仓管', 'wholesaler', '13800000004', 'wholesaler@example.com', NULL, 1),
('retailer01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '钱零售', 'retailer', '13800000005', 'retailer@example.com', NULL, 1),
('consumer01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '孙消费者', 'consumer', '13800000006', 'consumer@example.com', NULL, 1),
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员', 'admin', '13800000000', 'admin@example.com', NULL, 1);

-- 冷库 + 车辆（示例少量，地图页可按需扩展）
INSERT INTO facility (name, type, lng, lat, status, capacity, used_capacity, address, current_temp, current_humidity, load_rate) VALUES
('南宁东盟冷库A区', 'COLD_STORAGE', 108.3669, 22.8170, 'ONLINE', 5000, 3200, '广西南宁市东盟经济区', 2.5, 85.0, NULL),
('柳州冷链中心B库', 'COLD_STORAGE', 109.4150, 24.3255, 'ONLINE', 3000, 1800, '广西柳州市柳北区', 3.1, 82.0, NULL),
('桂林保鲜冷库C区', 'COLD_STORAGE', 110.2900, 25.2736, 'ALARM', 2000, 1500, '广西桂林市七星区', 6.8, 78.0, NULL),
('冷藏车桂A·D1001', 'REFRIGERATED_VEHICLE', 108.3900, 22.8500, 'ONLINE', 20, 15, NULL, 1.8, 80.0, 0.75),
('冷藏车桂B·E2002', 'REFRIGERATED_VEHICLE', 109.4000, 24.3100, 'ONLINE', 18, 10, NULL, 2.2, 83.0, 0.55),
('冷藏车桂C·F3003', 'REFRIGERATED_VEHICLE', 110.3000, 25.2800, 'OFFLINE', 22, 0, NULL, NULL, NULL, 0.00);

INSERT INTO sensor_device (facility_id, name, type, model, status, install_date) VALUES
(1, 'A区-1号温湿度', 'TEMP', 'SHT35', 1, '2025-01-10'),
(1, 'A区-2号CO2', 'CO2', 'MH-Z19', 1, '2025-01-10'),
(2, 'B库-1号温湿度', 'TEMP', 'SHT35', 1, '2025-03-01'),
(3, 'C区-1号温湿度', 'TEMP', 'SHT35', 2, '2025-02-15'),
(4, '车载温湿度-D1001', 'TEMP', 'DS18B20', 1, '2025-06-01');

INSERT INTO alarm_record (type, level, source_id, source_name, content, current_value, threshold, status, create_time) VALUES
('TEMP_OVER', 'CRITICAL', 3, '桂林保鲜冷库C区', '温度超标：当前 6.8℃，阈值 4.0℃', 6.8, 4.0, 'PENDING', NOW()),
('DEVICE_OFFLINE', 'WARNING', 6, '冷藏车桂C·F3003', '车辆设备离线超过 10 分钟', NULL, NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
('HUMIDITY_OVER', 'INFO', 1, '南宁东盟冷库A区', '湿度偏高：当前 92%，阈值 90%', 92.0, 90.0, 'RESOLVED', DATE_SUB(NOW(), INTERVAL 2 HOUR));

UPDATE alarm_record SET handler='赵仓管', handle_remark='已调整加湿设备', handle_time=DATE_SUB(NOW(), INTERVAL 1 HOUR) WHERE status='RESOLVED';

INSERT INTO product_batch (batch_no, product_name, origin, producer_id, produce_date, qr_code, tx_hash, status) VALUES
('20260716000001', '百色芒果', '广西百色', 1, '2026-07-10 08:00:00', 'https://trace.local/h5/trace?batchNo=20260716000001', 'mock_tx_hash_001', 1),
('20260716000002', '武鸣沃柑', '广西南宁武鸣', 1, '2026-07-12 09:30:00', 'https://trace.local/h5/trace?batchNo=20260716000002', NULL, 0);

INSERT INTO blockchain_tx (tx_hash, block_number, data_hash, biz_type, biz_id, chain_status) VALUES
('mock_tx_hash_001', 10001, 'a1b2c3d4e5f6789012345678901234567890abcdef1234567890abcdef123456', 'BATCH', '20260716000001', 'CONFIRMED');
