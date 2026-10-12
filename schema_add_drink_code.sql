-- 给 drink 表增加 drink_code 字段
ALTER TABLE drink ADD COLUMN drink_code VARCHAR(50) DEFAULT NULL COMMENT '人工编号' AFTER id;
