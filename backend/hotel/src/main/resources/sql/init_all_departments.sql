-- 完整的部门数据初始化脚本
-- 确保所有必要的部门都存在

-- 删除可能存在的重复数据
DELETE FROM sys_department WHERE dept_name IN (
    '房务部', '服务部', '餐饮部', '工程部', '业务部', '前厅部', '礼宾部'
);

-- 重新插入部门数据
INSERT INTO sys_department (dept_name, weight, create_time) VALUES
('房务部', 0.40, NOW()),
('服务部', 0.30, NOW()),
('餐饮部', 0.20, NOW()),
('工程部', 0.10, NOW()),
('业务部', 0.00, NOW()),
('前厅部', 0.00, NOW()),
('礼宾部', 0.00, NOW());

-- 验证插入结果
SELECT * FROM sys_department ORDER BY dept_name;

-- 检查task_order表结构
DESCRIBE task_order;

-- 检查外键约束
SELECT
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'hotel_management_system'
AND TABLE_NAME = 'task_order'
AND REFERENCED_TABLE_NAME IS NOT NULL;