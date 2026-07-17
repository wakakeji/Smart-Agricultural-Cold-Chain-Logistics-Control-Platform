-- 批次赋码扩展字段（QT-002）— 兼容无 IF NOT EXISTS 的 MySQL
USE app_db;

SET @db := DATABASE();

SET @exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA=@db AND TABLE_NAME='product_batch' AND COLUMN_NAME='shelf_life'
);
SET @sql := IF(@exists=0,
  'ALTER TABLE product_batch ADD COLUMN shelf_life INT DEFAULT 30 COMMENT ''保质期(天)'' AFTER produce_date',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA=@db AND TABLE_NAME='product_batch' AND COLUMN_NAME='quantity'
);
SET @sql := IF(@exists=0,
  'ALTER TABLE product_batch ADD COLUMN quantity INT DEFAULT 0 COMMENT ''批次数量'' AFTER shelf_life',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA=@db AND TABLE_NAME='product_batch' AND COLUMN_NAME='unit'
);
SET @sql := IF(@exists=0,
  'ALTER TABLE product_batch ADD COLUMN unit VARCHAR(20) DEFAULT ''箱'' COMMENT ''单位'' AFTER quantity',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
