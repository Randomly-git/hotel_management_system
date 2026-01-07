-- 添加预订相关字段到 bookings 表
-- 执行前请确保表已创建

-- 添加 actual_check_out_date 字段 (实际退房日期)
-- 注意：数据库中已经是date类型，这里不需要修改

-- 添加 cancel_reason 字段 (取消原因)
ALTER TABLE bookings
ADD COLUMN cancel_reason TEXT DEFAULT NULL COMMENT '取消原因';
