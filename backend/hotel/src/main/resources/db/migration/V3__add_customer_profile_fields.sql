-- 为customers表添加个性化服务相关的字段
-- 合并GuestProfile的功能到Customer表

-- 手动添加字段（简单版本）
-- 请逐个执行下面的ALTER TABLE语句，如果字段已存在会报错，可以忽略

ALTER TABLE customers ADD COLUMN member_id VARCHAR(50) UNIQUE COMMENT '会员ID，用于AI个性化服务';

ALTER TABLE customers ADD COLUMN phone_encrypted TEXT COMMENT '加密存储的手机号';

ALTER TABLE customers ADD COLUMN id_card_encrypted TEXT COMMENT '加密存储的身份证号';

ALTER TABLE customers ADD COLUMN last_check_in DATE COMMENT '最后入住日期';

ALTER TABLE customers ADD COLUMN last_stay_room VARCHAR(20) COMMENT '最后入住房间号';

ALTER TABLE customers ADD COLUMN avg_spend DECIMAL(10,2) COMMENT '平均消费金额';

ALTER TABLE customers ADD COLUMN total_spend DECIMAL(12,2) COMMENT '累计消费金额';

ALTER TABLE customers ADD COLUMN preference_tags TEXT COMMENT '偏好标签，逗号分隔';

ALTER TABLE customers ADD COLUMN tags TEXT COMMENT '客户标签，逗号分隔';

ALTER TABLE customers ADD COLUMN prediction_model_data LONGTEXT COMMENT 'AI预测模型数据';

-- 添加索引以提高查询性能
CREATE INDEX IF NOT EXISTS idx_customers_member_id ON customers(member_id);
CREATE INDEX IF NOT EXISTS idx_customers_preference_tags ON customers(preference_tags(255));
CREATE INDEX IF NOT EXISTS idx_customers_tags ON customers(tags(255));
CREATE INDEX IF NOT EXISTS idx_customers_last_check_in ON customers(last_check_in);

-- 从guest_profile表迁移现有数据（如果guest_profile表存在）
-- 注意：这个迁移是可选的，取决于是否有历史数据需要保留
-- SET @guest_profile_exists = (SELECT COUNT(*) FROM information_schema.tables
--     WHERE table_schema = DATABASE() AND table_name = 'guest_profile');
--
-- 如果需要迁移数据，可以取消注释下面的SQL：
-- INSERT IGNORE INTO customers (
--     id, member_id, hotel_id, name, phone_encrypted, id_card_encrypted,
--     last_check_in, last_stay_room, total_stays, avg_spend, total_spend,
--     vip_level, tags, created_at, updated_at
-- )
-- SELECT
--     guest_id, member_id, hotel_id,
--     COALESCE(customer_name, CONCAT('客户', guest_id)),
--     phone_encrypted, id_card_encrypted,
--     last_check_in, last_stay_room, COALESCE(total_stay, 0),
--     avg_spend, total_spend,
--     COALESCE(vip_level, 'normal'), tags,
--     created_at, updated_at
-- FROM guest_profile
-- WHERE member_id IS NOT NULL;
