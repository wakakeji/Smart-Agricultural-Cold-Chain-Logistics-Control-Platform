-- 业务演示数据 + 数据字典（勿在 Java 代码中写死）
-- 依赖：01_schema.sql、02_mock_data.sql（至少有 facility/batch）
USE app_db;

-- ========== 数据字典 ==========
DELETE FROM sys_dict;
INSERT INTO sys_dict (dict_type, dict_code, dict_label, sort_order, remark) VALUES
-- 设施状态
('facility_status', 'ONLINE',  '在线', 1, NULL),
('facility_status', 'OFFLINE', '离线', 2, NULL),
('facility_status', 'ALARM',   '告警', 3, NULL),
-- 传感器类型
('sensor_type', 'TEMP',     '温度', 1, NULL),
('sensor_type', 'HUMIDITY', '湿度', 2, NULL),
('sensor_type', 'CO2',      '二氧化碳', 3, NULL),
('sensor_type', 'VIBRATION','振动', 4, NULL),
-- 告警级别
('alarm_level', 'CRITICAL', '严重', 1, NULL),
('alarm_level', 'WARNING',  '警告', 2, NULL),
('alarm_level', 'INFO',     '提示', 3, NULL),
-- 告警类型
('alarm_type', 'TEMP_OVER',      '温度过高', 1, NULL),
('alarm_type', 'HUMIDITY_OVER',  '湿度过高', 2, NULL),
('alarm_type', 'DEVICE_OFFLINE', '设备离线', 3, NULL),
('alarm_type', 'VIBRATION',      '震动异常', 4, NULL),
('alarm_type', 'CO2_OVER',       '二氧化碳过高', 5, NULL),
-- 告警状态
('alarm_status', 'PENDING',    '待处理', 1, NULL),
('alarm_status', 'PROCESSING', '处理中', 2, NULL),
('alarm_status', 'RESOLVED',   '已解决', 3, NULL),
('alarm_status', 'IGNORED',    '已忽略', 4, NULL),
-- 运输状态
('transport_status', 'PENDING',      '待发车', 1, NULL),
('transport_status', 'TRANSPORTING', '运输中', 2, NULL),
('transport_status', 'COMPLETED',    '已完成', 3, NULL),
-- 链上状态 / 网络
('chain_status', 'CONFIRMED', '已确认', 1, NULL),
('chain_status', 'PENDING',   '待确认', 2, NULL),
('chain_status', 'CHAINED',   '已上链', 3, NULL),
('network_status', 'HEALTHY', '正常', 1, NULL),
('network_status', 'DEGRADED','降级', 2, NULL),
-- 链上业务类型
('biz_type', 'BATCH',          '批次赋码', 1, NULL),
('biz_type', 'BATCH_CREATE',   '批次创建', 2, NULL),
('biz_type', 'TRACE',          '追溯节点', 3, NULL),
('biz_type', 'SENSOR_DATA',    '传感数据', 4, NULL),
('biz_type', 'QUALITY_REPORT', '品质报告', 5, NULL),
-- 碳排放来源
('carbon_source', 'VEHICLE', '运输车辆', 1, NULL),
('carbon_source', 'COLD_STORAGE', '冷库仓储', 2, NULL),
('carbon_source', 'PACKAGE', '包装环节', 3, NULL),
-- 损耗类型
('loss_type', 'TRANSPORT', '运输', 1, NULL),
('loss_type', 'STORAGE', '仓储', 2, NULL),
('loss_type', 'PACKAGE', '包装', 3, NULL),
('loss_type', 'RETAIL', '零售', 4, NULL),
('loss_type', 'SALE', '销售', 5, NULL),
-- 设施类型
('facility_type', 'COLD_STORAGE', '冷库', 1, NULL),
('facility_type', 'REFRIGERATED_VEHICLE', '冷藏车', 2, NULL),
-- 决策建议优先级
('suggestion_priority', 'HIGH', '高', 1, NULL),
('suggestion_priority', 'MEDIUM', '中', 2, NULL),
('suggestion_priority', 'LOW', '低', 3, NULL),
-- 决策建议类型
('suggestion_type', 'TEMP_CONTROL', '温控调整', 1, NULL),
('suggestion_type', 'REROUTE', '路径改道', 2, NULL),
('suggestion_type', 'PRIORITY_SALE', '优先销售', 3, NULL),
('suggestion_type', 'MAINTENANCE', '设备巡检', 4, NULL),
('suggestion_type', 'LOSS_REDUCE', '减损规范', 5, NULL),
('suggestion_type', 'CARBON', '碳排优化', 6, NULL),
-- 数据质量问题类型
('data_quality_issue', 'DELAY', '上报延迟', 1, NULL),
('data_quality_issue', 'MISSING', '数据缺失', 2, NULL),
('data_quality_issue', 'OUTLIER', '异常尖峰', 3, NULL),
-- 配置项
('sys_config', 'blockchain.display_total', '28456', 1, '存证展示下限'),
('sys_config', 'blockchain.node_count', '7', 2, '节点数');

-- ========== 补充告警（若不足） ==========
INSERT INTO alarm_record (type, level, source_id, source_name, content, current_value, threshold, status, create_time)
SELECT * FROM (
  SELECT 'TEMP_OVER' AS type, 'CRITICAL' AS level, 1 AS source_id, '南宁冷库001区' AS source_name,
         '温度超标：当前 7.2℃，阈值 4.0℃' AS content, 7.2 AS current_value, 4.0 AS threshold,
         'PENDING' AS status, NOW() AS create_time
  UNION ALL SELECT 'DEVICE_OFFLINE','WARNING',4,'冷藏车桂A·D1001','车辆设备离线超过 10 分钟',NULL,NULL,'PENDING',DATE_SUB(NOW(), INTERVAL 20 MINUTE)
  UNION ALL SELECT 'HUMIDITY_OVER','INFO',2,'柳州冷链中心B库','湿度偏高：当前 93%，阈值 90%',93.0,90.0,'PROCESSING',DATE_SUB(NOW(), INTERVAL 40 MINUTE)
  UNION ALL SELECT 'TEMP_OVER','WARNING',3,'桂林保鲜冷库C区','温度接近阈值：当前 3.8℃',3.8,4.0,'PENDING',DATE_SUB(NOW(), INTERVAL 55 MINUTE)
  UNION ALL SELECT 'VIBRATION','WARNING',5,'冷藏车桂B·E2002','运输振动异常',1.8,1.2,'PENDING',DATE_SUB(NOW(), INTERVAL 70 MINUTE)
  UNION ALL SELECT 'CO2_OVER','INFO',1,'南宁东盟冷库A区','CO2 浓度偏高',1200.0,1000.0,'RESOLVED',DATE_SUB(NOW(), INTERVAL 120 MINUTE)
  UNION ALL SELECT 'TEMP_OVER','CRITICAL',6,'冷藏车桂C·F3003','车厢温度达到 9.5℃',9.5,4.0,'PENDING',DATE_SUB(NOW(), INTERVAL 10 MINUTE)
  UNION ALL SELECT 'DEVICE_OFFLINE','INFO',2,'柳州冷链中心B库','传感器短暂离线已恢复',NULL,NULL,'RESOLVED',DATE_SUB(NOW(), INTERVAL 180 MINUTE)
) t
WHERE (SELECT COUNT(1) FROM alarm_record) < 8;

UPDATE alarm_record SET handler='赵仓管', handle_remark='已处理', handle_time=DATE_SUB(NOW(), INTERVAL 1 HOUR)
WHERE status='RESOLVED' AND (handler IS NULL OR handler='');

-- ========== 运输订单 ==========
INSERT INTO transport_order(order_no, batch_id, vehicle_id, driver_id, route, status, start_time, create_time)
SELECT * FROM (
  SELECT 'TO20260716001' AS order_no, 1 AS batch_id, 4 AS vehicle_id, 3 AS driver_id, '南宁->柳州' AS route, 'TRANSPORTING' AS status, NOW() AS start_time, NOW() AS create_time
  UNION ALL SELECT 'TO20260716002', 1, 5, 3, '南宁->桂林', 'TRANSPORTING', NOW(), NOW()
  UNION ALL SELECT 'TO20260716003', 2, 6, 3, '柳州->北海', 'PENDING', NULL, NOW()
  UNION ALL SELECT 'TO20260716004', 2, 4, 3, '百色->南宁', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()
  UNION ALL SELECT 'TO20260716005', 1, 5, 3, '南宁->玉林', 'TRANSPORTING', NOW(), NOW()
) t
WHERE (SELECT COUNT(1) FROM transport_order) = 0;

-- ========== 品质预测 ==========
INSERT INTO quality_prediction(batch_id, predict_time, quality_score, remaining_shelf_life, model_version, confidence, create_time)
SELECT * FROM (
  SELECT 1 AS batch_id, DATE_SUB(NOW(), INTERVAL 12 HOUR) AS predict_time, 95.0 AS quality_score, 80 AS remaining_shelf_life, 'xgb-v1' AS model_version, 0.92 AS confidence, DATE_SUB(NOW(), INTERVAL 12 HOUR) AS create_time
  UNION ALL SELECT 1, DATE_SUB(NOW(), INTERVAL 8 HOUR), 94.2, 76, 'xgb-v1', 0.91, DATE_SUB(NOW(), INTERVAL 8 HOUR)
  UNION ALL SELECT 1, DATE_SUB(NOW(), INTERVAL 4 HOUR), 93.8, 74, 'xgb-v1', 0.90, DATE_SUB(NOW(), INTERVAL 4 HOUR)
  UNION ALL SELECT 1, NOW(), 93.5, 72, 'xgb-v1', 0.91, NOW()
  UNION ALL SELECT 2, DATE_SUB(NOW(), INTERVAL 10 HOUR), 92.0, 55, 'xgb-v1', 0.89, DATE_SUB(NOW(), INTERVAL 10 HOUR)
  UNION ALL SELECT 2, DATE_SUB(NOW(), INTERVAL 5 HOUR), 91.5, 50, 'xgb-v1', 0.88, DATE_SUB(NOW(), INTERVAL 5 HOUR)
  UNION ALL SELECT 2, NOW(), 91.2, 48, 'xgb-v1', 0.88, NOW()
) t
WHERE (SELECT COUNT(1) FROM quality_prediction) < 5;

-- ========== 碳排放 ==========
INSERT INTO carbon_emission(source_type, source_id, source_name, emission_value, unit, calc_time, period, calc_method)
SELECT * FROM (
  SELECT 'VEHICLE' AS source_type, 4 AS source_id, '桂A·D1001' AS source_name, 420.5 AS emission_value, 'kg CO2e' AS unit, NOW() AS calc_time, '2026-Q2' AS period, 'IPCC' AS calc_method
  UNION ALL SELECT 'COLD_STORAGE', 1, '南宁东盟冷库A区', 1860.0, 'kg CO2e', NOW(), '2026-Q2', 'IPCC'
  UNION ALL SELECT 'PACKAGE', NULL, '包装环节', 566.5, 'kg CO2e', NOW(), '2026-Q2', 'IPCC'
) t
WHERE (SELECT COUNT(1) FROM carbon_emission) = 0;

-- ========== 决策建议 ==========
INSERT INTO decision_suggestion(type, title, content, priority, status, related_batch_id, create_time)
SELECT * FROM (
  SELECT 'TEMP_CONTROL' AS type, '降低车厢设定温度' AS title, '桂A·D1001 车厢温度偏高，建议将设定温度下调 1℃ 并检查冷机' AS content, 'HIGH' AS priority, 'PENDING' AS status, 1 AS related_batch_id, NOW() AS create_time
  UNION ALL SELECT 'REROUTE','绕开拥堵路段','南宁→柳州高速拥堵预计延误 40 分钟，建议改走 G7211','HIGH','PENDING',1,NOW()
  UNION ALL SELECT 'PRIORITY_SALE','优先销售临期批次','批次货架期剩余不足 48 小时，建议优先配送商超渠道','MEDIUM','PENDING',2,NOW()
  UNION ALL SELECT 'MAINTENANCE','冷库设备巡检','南宁冷库压缩机运行时长超阈值，建议安排巡检','MEDIUM','PENDING',NULL,NOW()
  UNION ALL SELECT 'LOSS_REDUCE','加强装卸规范','近7日运输损耗偏高，建议加强装卸培训与缓冲包装','LOW','ADOPTED',NULL,DATE_SUB(NOW(), INTERVAL 1 DAY)
  UNION ALL SELECT 'CARBON','优化发车班次','空载率偏高，建议合并同向订单降低碳排放','LOW','IGNORED',NULL,DATE_SUB(NOW(), INTERVAL 2 DAY)
) t
WHERE (SELECT COUNT(1) FROM decision_suggestion) = 0;

-- ========== 损耗记录 ==========
INSERT INTO loss_record(batch_no, product_name, total_quantity, loss_quantity, loss_rate, loss_type, loss_reason, cost, report_date)
SELECT * FROM (
  SELECT '20260716000001' AS batch_no, '百色芒果' AS product_name, 1000 AS total_quantity, 35 AS loss_quantity, 3.5 AS loss_rate, 'TRANSPORT' AS loss_type, '运输颠簸破损' AS loss_reason, 875 AS cost, DATE_SUB(CURDATE(), INTERVAL 6 DAY) AS report_date
  UNION ALL SELECT '20260716000002','武鸣沃柑',800,48,6.0,'STORAGE','冷库温控波动',1440,DATE_SUB(CURDATE(), INTERVAL 5 DAY)
  UNION ALL SELECT '20260716000001','百色芒果',1200,24,2.0,'PACKAGE','包装破损',600,DATE_SUB(CURDATE(), INTERVAL 4 DAY)
  UNION ALL SELECT '20260716000002','武鸣沃柑',600,18,3.0,'TRANSPORT','装卸损耗',360,DATE_SUB(CURDATE(), INTERVAL 3 DAY)
  UNION ALL SELECT '20260716000001','百色芒果',500,10,2.0,'RETAIL','货架损耗',250,DATE_SUB(CURDATE(), INTERVAL 1 DAY)
  UNION ALL SELECT '20260716000002','武鸣沃柑',900,45,5.0,'STORAGE','湿度过高',1125,DATE_SUB(CURDATE(), INTERVAL 7 DAY)
  UNION ALL SELECT '20260716000001','百色芒果',700,56,8.0,'TRANSPORT','冷链中断',1680,DATE_SUB(CURDATE(), INTERVAL 8 DAY)
  UNION ALL SELECT '20260716000002','武鸣沃柑',1100,33,3.0,'PACKAGE','包装挤压',825,DATE_SUB(CURDATE(), INTERVAL 9 DAY)
) t
WHERE (SELECT COUNT(1) FROM loss_record) = 0;

-- ========== 补充链上交易（演示列表，非写死在 Java） ==========
INSERT INTO blockchain_tx(tx_hash, block_number, data_hash, biz_type, biz_id, chain_status, create_time)
SELECT CONCAT('0xseed', LPAD(HEX(n), 56, '0')), 1784000000 + n,
       SHA2(CONCAT('seed-', n), 256),
       ELT(1 + MOD(n, 4), 'BATCH_CREATE', 'TRACE', 'SENSOR_DATA', 'QUALITY_REPORT'),
       CONCAT('SEED-', n), 'CONFIRMED', DATE_SUB(NOW(), INTERVAL n HOUR)
FROM (
  SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
  UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
  UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15
  UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION SELECT 20
  UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24 UNION SELECT 25
  UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29 UNION SELECT 30
  UNION SELECT 31 UNION SELECT 32 UNION SELECT 33 UNION SELECT 34 UNION SELECT 35
  UNION SELECT 36 UNION SELECT 37 UNION SELECT 38 UNION SELECT 39 UNION SELECT 40
) nums
WHERE (SELECT COUNT(1) FROM blockchain_tx) < 40;
