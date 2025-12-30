-- 修复部门数据，确保所有部门都有正确的 hotel_id
-- 这个脚本用于修复现有的部门数据，为没有 hotel_id 的记录添加默认值

-- 首先更新现有的部门记录，设置 hotel_id
UPDATE sys_department SET hotel_id = '1' WHERE hotel_id IS NULL OR hotel_id = '' OR hotel_id = 'DEFAULT_HOTEL';

-- 如果没有任何部门数据，则插入默认数据
INSERT IGNORE INTO sys_department (dept_name, weight, create_time, hotel_id) VALUES
('房务部', 0.40, NOW(), '1'),
('服务部', 0.30, NOW(), '1'),
('餐饮部', 0.20, NOW(), '1'),
('工程部', 0.10, NOW(), '1'),
('业务部', 0.00, NOW(), '1'),
('前厅部', 0.00, NOW(), '1'),
('礼宾部', 0.00, NOW(), '1');

-- 验证修复结果
SELECT '修复后的部门数据:' as info;
SELECT dept_id, dept_name, weight, hotel_id, create_time FROM sys_department WHERE hotel_id = '1' ORDER BY dept_id;
