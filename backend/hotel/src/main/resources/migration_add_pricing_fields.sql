-- 添加动态定价相关的字段到 pricing_record 表
-- 执行前请确保表已创建

-- 添加 base_price 字段 (基准价格)
ALTER TABLE pricing_record
ADD COLUMN base_price DECIMAL(10,2) DEFAULT NULL COMMENT '基准价格';

-- 添加 status 字段 (记录状态: PENDING/APPLIED/BASE_PRICE_FALLBACK)
ALTER TABLE pricing_record
ADD COLUMN status VARCHAR(20) DEFAULT 'PENDING' COMMENT '记录状态：PENDING-待审批，APPLIED-已生效，BASE_PRICE_FALLBACK-降级使用基准价';

-- 为现有记录设置默认值
UPDATE pricing_record SET base_price = original_price WHERE base_price IS NULL;
UPDATE pricing_record SET status = 'APPLIED' WHERE status IS NULL;






