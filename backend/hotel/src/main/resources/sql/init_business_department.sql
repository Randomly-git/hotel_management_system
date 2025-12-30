-- 初始化业务部门数据脚本
-- 用于处理AI无法识别和需要人工确认的任务

INSERT INTO sys_department (dept_name, weight, create_time, hotel_id) VALUES
('房务部', 0.40, NOW(), '1'),
('服务部', 0.30, NOW(), '1'),
('餐饮部', 0.20, NOW(), '1'),
('工程部', 0.10, NOW(), '1'),
('业务部', 0.00, NOW(), '1'),
('前厅部', 0.00, NOW(), '1'),
('礼宾部', 0.00, NOW(), '1')
ON DUPLICATE KEY UPDATE
    dept_name = VALUES(dept_name),
    weight = VALUES(weight),
    create_time = VALUES(create_time),
    hotel_id = VALUES(hotel_id);

-- 验证部门数据
SELECT * FROM sys_department ORDER BY dept_id;