-- 个性化服务系统基础功能所需数据库表结构
-- 执行顺序：先执行ALTER TABLE，再创建新表

-- =====================================================
-- 1. 修改现有表结构
-- =====================================================

-- 1.1 为 task_order 表添加优先级字段
ALTER TABLE task_order
ADD COLUMN priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '任务优先级：URGENCY/NORMAL/LOW';

-- 1.2 为 department_task 表添加取消原因字段
ALTER TABLE department_task
ADD COLUMN cancel_reason TEXT COMMENT '任务取消原因';

-- =====================================================
-- 2. 创建任务历史记录表
-- =====================================================

CREATE TABLE IF NOT EXISTS task_history (
    history_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '历史记录ID',
    task_order_id BIGINT NOT NULL COMMENT '关联的任务单ID',
    old_status VARCHAR(20) COMMENT '原状态',
    new_status VARCHAR(20) NOT NULL COMMENT '新状态',
    operation_type VARCHAR(20) NOT NULL COMMENT '操作类型：CREATE/UPDATE/CANCEL/COMPLETE/ASSIGN/ACCEPT/REJECT',
    operation_reason TEXT COMMENT '操作原因',
    operator_id VARCHAR(50) COMMENT '操作人ID',
    operator_name VARCHAR(100) COMMENT '操作人姓名',
    operator_type VARCHAR(20) COMMENT '操作人类型：CUSTOMER/STAFF/SYSTEM/MANAGER',
    operation_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    department_id BIGINT COMMENT '部门ID',
    department_name VARCHAR(50) COMMENT '部门名称',
    hotel_id BIGINT NOT NULL COMMENT '酒店ID（租户隔离）',

    -- 索引
    INDEX idx_task_order_id (task_order_id),
    INDEX idx_hotel_id (hotel_id),
    INDEX idx_operation_time (operation_time),
    INDEX idx_operator_id (operator_id),
    INDEX idx_department_id (department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务历史记录表';

-- =====================================================
-- 3. 为新功能创建测试数据（可选）
-- =====================================================

-- 3.1 插入一些优先级测试数据
-- UPDATE task_order SET priority = 'URGENCY' WHERE task_content LIKE '%火%' OR task_content LIKE '%紧急%' OR task_content LIKE '%救命%';
-- UPDATE task_order SET priority = 'LOW' WHERE task_content LIKE '%建议%' OR task_content LIKE '%咨询%';

-- =====================================================
-- 4. 验证表结构
-- =====================================================

-- 验证 task_order 表结构
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT, COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'task_order'
AND COLUMN_NAME IN ('priority');

-- 验证 department_task 表结构
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT, COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'department_task'
AND COLUMN_NAME IN ('cancel_reason');

-- 验证 task_history 表结构
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT, COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'task_history';

-- =====================================================
-- 5. 权限设置（如果需要）
-- =====================================================

-- 确保应用用户有适当的权限
-- GRANT SELECT, INSERT, UPDATE, DELETE ON task_history TO 'hotel_user'@'%';
-- GRANT SELECT, INSERT, UPDATE ON task_order TO 'hotel_user'@'%';
-- GRANT SELECT, INSERT, UPDATE ON department_task TO 'hotel_user'@'%';

-- 刷新权限
-- FLUSH PRIVILEGES;

-- =====================================================
-- 6. 数据一致性检查
-- =====================================================

-- 检查现有任务数据的一致性
SELECT
    COUNT(*) as total_tasks,
    COUNT(CASE WHEN priority IS NOT NULL THEN 1 END) as tasks_with_priority,
    COUNT(CASE WHEN priority = 'NORMAL' THEN 1 END) as normal_priority_tasks,
    COUNT(CASE WHEN priority = 'URGENCY' THEN 1 END) as urgent_priority_tasks,
    COUNT(CASE WHEN priority = 'LOW' THEN 1 END) as low_priority_tasks
FROM task_order
WHERE hotel_id = 1;

-- 检查部门任务数据
SELECT
    COUNT(*) as total_dept_tasks,
    COUNT(CASE WHEN cancel_reason IS NOT NULL THEN 1 END) as tasks_with_cancel_reason
FROM department_task
WHERE hotel_id = 1;