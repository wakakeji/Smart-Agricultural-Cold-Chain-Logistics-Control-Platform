-- 智慧农业冷链物流管控平台 - 核心表结构
-- MySQL 8.4 / 库名: app_db

CREATE DATABASE IF NOT EXISTS app_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE app_db;

-- 1. 角色表
CREATE TABLE IF NOT EXISTS sys_role (
  role_id      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  role_code    VARCHAR(50)  NOT NULL COMMENT '角色编码',
  role_name    VARCHAR(50)  NOT NULL COMMENT '角色名称',
  permissions  JSON         DEFAULT NULL COMMENT '权限列表JSON',
  status       TINYINT      NOT NULL DEFAULT 1 COMMENT '0禁用 1启用',
  remark       VARCHAR(200) DEFAULT NULL,
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (role_id),
  UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 2. 用户表
CREATE TABLE IF NOT EXISTS sys_user (
  user_id      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  username     VARCHAR(50)  NOT NULL COMMENT '用户名',
  password     VARCHAR(100) NOT NULL COMMENT 'BCrypt密码',
  real_name    VARCHAR(50)  NOT NULL COMMENT '真实姓名',
  role_code    VARCHAR(50)  NOT NULL COMMENT '角色编码',
  phone        VARCHAR(20)  DEFAULT NULL,
  email        VARCHAR(100) DEFAULT NULL,
  avatar       VARCHAR(255) DEFAULT NULL,
  status       TINYINT      NOT NULL DEFAULT 1 COMMENT '0禁用 1启用 2锁定',
  dept_id      BIGINT       DEFAULT NULL,
  login_fail_count INT      NOT NULL DEFAULT 0 COMMENT '连续登录失败次数',
  lock_time    DATETIME     DEFAULT NULL COMMENT '锁定截止时间',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id),
  UNIQUE KEY uk_username (username),
  KEY idx_role_code (role_code),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 3. 设施表
CREATE TABLE IF NOT EXISTS facility (
  facility_id   BIGINT       NOT NULL AUTO_INCREMENT,
  name          VARCHAR(100) NOT NULL,
  type          VARCHAR(40)  NOT NULL COMMENT 'COLD_STORAGE/REFRIGERATED_VEHICLE',
  lng           DOUBLE       NOT NULL,
  lat           DOUBLE       NOT NULL,
  status        VARCHAR(20)  NOT NULL DEFAULT 'ONLINE',
  capacity      DOUBLE       DEFAULT NULL,
  used_capacity DOUBLE       DEFAULT NULL,
  address       VARCHAR(255) DEFAULT NULL,
  current_temp  DOUBLE       DEFAULT NULL,
  current_humidity DOUBLE    DEFAULT NULL,
  load_rate     DOUBLE       DEFAULT NULL,
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (facility_id),
  KEY idx_type (type),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设施表';

-- 4. 传感器设备表
CREATE TABLE IF NOT EXISTS sensor_device (
  sensor_id     BIGINT       NOT NULL AUTO_INCREMENT,
  facility_id   BIGINT       NOT NULL,
  name          VARCHAR(100) NOT NULL,
  type          VARCHAR(50)  NOT NULL COMMENT 'TEMP/HUMIDITY/CO2/VIBRATION/LIGHT',
  model         VARCHAR(100) DEFAULT NULL,
  status        TINYINT      NOT NULL DEFAULT 1 COMMENT '0离线 1在线 2故障',
  install_date  DATE         DEFAULT NULL,
  last_maintain DATETIME     DEFAULT NULL,
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (sensor_id),
  KEY idx_facility (facility_id),
  KEY idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='传感器设备表';

-- 5. 传感器数据摘要表（明细时序存 InfluxDB）
CREATE TABLE IF NOT EXISTS sensor_data (
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  sensor_id     BIGINT       NOT NULL,
  temp          DOUBLE       DEFAULT NULL,
  humidity      DOUBLE       DEFAULT NULL,
  co2           DOUBLE       DEFAULT NULL,
  vibration     DOUBLE       DEFAULT NULL,
  light         DOUBLE       DEFAULT NULL,
  collect_time  DATETIME     NOT NULL,
  quality_flag  TINYINT      NOT NULL DEFAULT 1 COMMENT '1正常 0异常',
  PRIMARY KEY (id),
  KEY idx_sensor_time (sensor_id, collect_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='传感器数据摘要表';

-- 6. 产品批次表
CREATE TABLE IF NOT EXISTS product_batch (
  batch_id      BIGINT       NOT NULL AUTO_INCREMENT,
  batch_no      VARCHAR(64)  NOT NULL,
  product_name  VARCHAR(200) NOT NULL,
  origin        VARCHAR(200) DEFAULT NULL,
  producer_id   BIGINT       NOT NULL,
  produce_date  DATETIME     NOT NULL,
  shelf_life    INT          DEFAULT 30 COMMENT '保质期(天)',
  quantity      INT          DEFAULT 0 COMMENT '批次数量',
  unit          VARCHAR(20)  DEFAULT '箱' COMMENT '单位',
  qr_code       VARCHAR(500) DEFAULT NULL,
  tx_hash       VARCHAR(128) DEFAULT NULL,
  status        TINYINT      NOT NULL DEFAULT 0 COMMENT '0待上链 1已上链 2已追溯',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (batch_id),
  UNIQUE KEY uk_batch_no (batch_no),
  KEY idx_producer (producer_id),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品批次表';

-- 7. 追溯记录表
CREATE TABLE IF NOT EXISTS trace_record (
  trace_id      BIGINT       NOT NULL AUTO_INCREMENT,
  batch_id      BIGINT       NOT NULL,
  operation     VARCHAR(50)  NOT NULL,
  operator      VARCHAR(50)  NOT NULL,
  location      VARCHAR(200) DEFAULT NULL,
  temp          DOUBLE       DEFAULT NULL,
  humidity      DOUBLE       DEFAULT NULL,
  op_time       DATETIME     NOT NULL,
  tx_hash       VARCHAR(128) DEFAULT NULL,
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (trace_id),
  KEY idx_batch (batch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='追溯记录表';

-- 8. 运输订单表
CREATE TABLE IF NOT EXISTS transport_order (
  order_id      BIGINT       NOT NULL AUTO_INCREMENT,
  order_no      VARCHAR(64)  NOT NULL,
  batch_id      BIGINT       NOT NULL,
  vehicle_id    BIGINT       DEFAULT NULL,
  driver_id     BIGINT       DEFAULT NULL,
  route         VARCHAR(500) DEFAULT NULL,
  status        VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
  start_time    DATETIME     DEFAULT NULL,
  end_time      DATETIME     DEFAULT NULL,
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (order_id),
  UNIQUE KEY uk_order_no (order_no),
  KEY idx_batch (batch_id),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运输订单表';

-- 9. 车辆轨迹表
CREATE TABLE IF NOT EXISTS vehicle_track (
  track_id      BIGINT       NOT NULL AUTO_INCREMENT,
  vehicle_id    BIGINT       NOT NULL,
  lng           DOUBLE       NOT NULL,
  lat           DOUBLE       NOT NULL,
  speed         DOUBLE       DEFAULT NULL,
  direction     DOUBLE       DEFAULT NULL,
  report_time   DATETIME     NOT NULL,
  PRIMARY KEY (track_id),
  KEY idx_vehicle_time (vehicle_id, report_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆轨迹表';

-- 10. 告警记录表
CREATE TABLE IF NOT EXISTS alarm_record (
  alarm_id      BIGINT       NOT NULL AUTO_INCREMENT,
  type          VARCHAR(40)  NOT NULL,
  level         VARCHAR(20)  NOT NULL,
  source_id     BIGINT       DEFAULT NULL,
  source_name   VARCHAR(100) DEFAULT NULL,
  content       VARCHAR(500) NOT NULL,
  current_value DOUBLE       DEFAULT NULL,
  threshold     DOUBLE       DEFAULT NULL,
  status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
  handler       VARCHAR(50)  DEFAULT NULL,
  handle_remark VARCHAR(500) DEFAULT NULL,
  handle_time   DATETIME     DEFAULT NULL,
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (alarm_id),
  KEY idx_status_level (status, level),
  KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警记录表';

-- 11. 品质预测表
CREATE TABLE IF NOT EXISTS quality_prediction (
  pred_id               BIGINT       NOT NULL AUTO_INCREMENT,
  batch_id              BIGINT       NOT NULL,
  predict_time          DATETIME     NOT NULL,
  quality_score         DOUBLE       NOT NULL,
  remaining_shelf_life  INT          DEFAULT NULL COMMENT '剩余货架期(小时)',
  model_version         VARCHAR(50)  DEFAULT NULL,
  confidence            DOUBLE       DEFAULT NULL,
  create_time           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (pred_id),
  KEY idx_batch (batch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='品质预测表';

-- 12. 决策建议表
CREATE TABLE IF NOT EXISTS decision_suggestion (
  sug_id            BIGINT       NOT NULL AUTO_INCREMENT,
  type              VARCHAR(40)  NOT NULL,
  title             VARCHAR(200) NOT NULL,
  content           VARCHAR(1000) NOT NULL,
  priority          VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
  status            VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
  related_batch_id  BIGINT       DEFAULT NULL,
  create_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (sug_id),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='决策建议表';

-- 13. 碳排放表
CREATE TABLE IF NOT EXISTS carbon_emission (
  carbon_id       BIGINT       NOT NULL AUTO_INCREMENT,
  source_type     VARCHAR(40)  NOT NULL,
  source_id       BIGINT       DEFAULT NULL,
  source_name     VARCHAR(100) DEFAULT NULL,
  emission_value  DOUBLE       NOT NULL,
  unit            VARCHAR(30)  NOT NULL DEFAULT 'kg CO2e',
  calc_time       DATETIME     NOT NULL,
  period          VARCHAR(30)  DEFAULT NULL,
  calc_method     VARCHAR(50)  DEFAULT 'IPCC',
  PRIMARY KEY (carbon_id),
  KEY idx_source (source_type, source_id),
  KEY idx_period (period)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='碳排放表';

-- 14. 区块链交易表（模拟存证）
CREATE TABLE IF NOT EXISTS blockchain_tx (
  tx_id         BIGINT       NOT NULL AUTO_INCREMENT,
  tx_hash       VARCHAR(128) NOT NULL COMMENT '模拟交易哈希',
  block_number  BIGINT       NOT NULL DEFAULT 0,
  data_hash     VARCHAR(128) NOT NULL COMMENT '业务数据SHA-256',
  biz_type      VARCHAR(40)  NOT NULL,
  biz_id        VARCHAR(64)  NOT NULL,
  chain_status  VARCHAR(20)  NOT NULL DEFAULT 'CONFIRMED',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (tx_id),
  UNIQUE KEY uk_tx_hash (tx_hash),
  KEY idx_biz (biz_type, biz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='区块链交易表(模拟)';

-- 15. 操作日志表（业务侧；详细日志也可写 MongoDB）
CREATE TABLE IF NOT EXISTS sys_oper_log (
  log_id        BIGINT       NOT NULL AUTO_INCREMENT,
  user_id       BIGINT       DEFAULT NULL,
  username      VARCHAR(50)  DEFAULT NULL,
  operation     VARCHAR(100) NOT NULL,
  target        VARCHAR(100) DEFAULT NULL,
  params        TEXT         DEFAULT NULL,
  ip            VARCHAR(50)  DEFAULT NULL,
  result        VARCHAR(20)  DEFAULT NULL,
  cost_time     BIGINT       DEFAULT NULL,
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (log_id),
  KEY idx_user_time (user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 数据字典（状态/类型中文标签，前端展示用）
CREATE TABLE IF NOT EXISTS sys_dict (
  dict_id      BIGINT       NOT NULL AUTO_INCREMENT,
  dict_type    VARCHAR(50)  NOT NULL COMMENT '字典类型',
  dict_code    VARCHAR(50)  NOT NULL COMMENT '编码(与业务字段一致)',
  dict_label   VARCHAR(100) NOT NULL COMMENT '中文标签',
  sort_order   INT          NOT NULL DEFAULT 0,
  status       TINYINT      NOT NULL DEFAULT 1,
  remark       VARCHAR(200) DEFAULT NULL,
  PRIMARY KEY (dict_id),
  UNIQUE KEY uk_type_code (dict_type, dict_code),
  KEY idx_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典';

-- 损耗记录表（统计分析用）
CREATE TABLE IF NOT EXISTS loss_record (
  loss_id         BIGINT       NOT NULL AUTO_INCREMENT,
  batch_no        VARCHAR(64)  NOT NULL,
  product_name    VARCHAR(200) NOT NULL,
  total_quantity  INT          NOT NULL,
  loss_quantity   INT          NOT NULL,
  loss_rate       DOUBLE       NOT NULL,
  loss_type       VARCHAR(30)  NOT NULL,
  loss_reason     VARCHAR(200) DEFAULT NULL,
  cost            DOUBLE       DEFAULT NULL,
  report_date     DATE         NOT NULL,
  create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (loss_id),
  KEY idx_batch_no (batch_no),
  KEY idx_report_date (report_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='损耗记录表';
