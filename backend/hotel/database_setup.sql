-- =====================================================
-- 酒店管理系统安全数据库设置脚本
-- 分步执行，确保每个步骤成功后再继续
-- =====================================================

USE hotel_management_system;

-- =====================================================
-- 第一步：检查并创建基础表结构
-- =====================================================

SELECT '=== 开始创建表结构 ===' as step;

-- 1. 修改现有表结构（安全方式）
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE()
     AND TABLE_NAME = 'task_order'
     AND COLUMN_NAME = 'priority') > 0,
    'SELECT "task_order.priority字段已存在，跳过添加" as message;',
    'ALTER TABLE task_order ADD COLUMN priority VARCHAR(20) NOT NULL DEFAULT "NORMAL" COMMENT "任务优先级：URGENCY/NORMAL/LOW";'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE()
     AND TABLE_NAME = 'department_task'
     AND COLUMN_NAME = 'cancel_reason') > 0,
    'SELECT "department_task.cancel_reason字段已存在，跳过添加" as message;',
    'ALTER TABLE department_task ADD COLUMN cancel_reason TEXT COMMENT "任务取消原因";'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加缺失的completion_remark字段
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE()
     AND TABLE_NAME = 'department_task'
     AND COLUMN_NAME = 'completion_remark') > 0,
    'SELECT "department_task.completion_remark字段已存在，跳过添加" as message;',
    'ALTER TABLE department_task ADD COLUMN completion_remark TEXT COMMENT "完成备注";'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 修复task_order表status字段长度问题
SET @sql = (SELECT IF(
    CHARACTER_MAXIMUM_LENGTH IS NOT NULL AND CHARACTER_MAXIMUM_LENGTH >= 50,
    'SELECT "task_order.status字段长度足够，跳过修改" as message;',
    'ALTER TABLE task_order MODIFY COLUMN status VARCHAR(50) NOT NULL DEFAULT "PENDING" COMMENT "任务状态"'
)
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'task_order' AND COLUMN_NAME = 'status');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 创建部门客户评价表（如果不存在）
CREATE TABLE IF NOT EXISTS department_feedback (
    feedback_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_order_id BIGINT NOT NULL,
    department_task_id BIGINT NOT NULL,
    customer_id VARCHAR(50) NOT NULL,
    hotel_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    department_name VARCHAR(50) NOT NULL,
    service_rating INT NOT NULL CHECK (service_rating BETWEEN 1 AND 5),
    response_speed_rating INT NOT NULL CHECK (response_speed_rating BETWEEN 1 AND 5),
    service_quality_rating INT NOT NULL CHECK (service_quality_rating BETWEEN 1 AND 5),
    overall_rating DECIMAL(3,2) GENERATED ALWAYS AS ((service_rating + response_speed_rating + service_quality_rating) / 3.0) STORED,
    feedback_content TEXT,
    feedback_tags VARCHAR(200),
    is_recommended BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_task_order_id (task_order_id),
    INDEX idx_hotel_id (hotel_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门客户评价表';

-- 3. 创建任务历史记录表（如果不存在）
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
    INDEX idx_task_order_id (task_order_id),
    INDEX idx_hotel_id (hotel_id),
    INDEX idx_operation_time (operation_time),
    INDEX idx_operator_id (operator_id),
    INDEX idx_department_id (department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务历史记录表';

-- 4. 创建SLA服务等级协议表（如果不存在）
CREATE TABLE IF NOT EXISTS service_sla (
    sla_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    department_id BIGINT NOT NULL,
    department_name VARCHAR(50) NOT NULL,
    service_type VARCHAR(100) NOT NULL,
    response_time_limit INT NOT NULL COMMENT '响应时间限制（分钟）',
    completion_time_limit INT NOT NULL COMMENT '完成时间限制（分钟）',
    hotel_id BIGINT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_department_service (department_id, service_type),
    INDEX idx_hotel_id (hotel_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务等级协议表';

-- 5. 创建服务质量检查表（如果不存在）
CREATE TABLE IF NOT EXISTS quality_check (
    check_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_order_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    department_name VARCHAR(50) NOT NULL,
    check_type VARCHAR(50) NOT NULL COMMENT '检查类型：TIMELINESS/QUALITY/SATISFACTION/PROFESSIONALISM',
    check_score DECIMAL(5,2) NOT NULL COMMENT '检查分数（0-10）',
    check_result TEXT COMMENT '检查结果描述',
    check_standards TEXT COMMENT '检查标准',
    checked_by VARCHAR(100) NOT NULL,
    check_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    hotel_id BIGINT NOT NULL,
    INDEX idx_task_order_id (task_order_id),
    INDEX idx_department_check (department_id, check_type),
    INDEX idx_check_date (check_date),
    INDEX idx_hotel_id (hotel_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务质量检查表';

-- 6. 创建预测性维护表（如果不存在）
CREATE TABLE IF NOT EXISTS predictive_maintenance (
    maintenance_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_number VARCHAR(20) NOT NULL,
    equipment_type VARCHAR(100) NOT NULL COMMENT '设备类型：空调/热水器/电视/门锁/网络设备等',
    maintenance_type ENUM('PREVENTIVE', 'CORRECTIVE', 'PREDICTIVE') NOT NULL COMMENT '维护类型',
    prediction_score DECIMAL(5,2) COMMENT '预测分数（0-100）',
    maintenance_date TIMESTAMP NOT NULL COMMENT '计划维护日期',
    maintenance_description TEXT COMMENT '维护描述',
    status ENUM('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELED') DEFAULT 'SCHEDULED' COMMENT '维护状态',
    hotel_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_room_equipment (room_number, equipment_type),
    INDEX idx_maintenance_date (maintenance_date),
    INDEX idx_hotel_id (hotel_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预测性维护表';

SELECT '=== 表结构创建完成 ===' as step;

-- =====================================================
-- 第二步：验证表是否创建成功
-- =====================================================

SELECT '=== 验证表创建情况 ===' as step;
SELECT
    TABLE_NAME as table_name,
    TABLE_COMMENT as description,
    TABLE_ROWS as estimated_rows
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME IN ('department_feedback', 'task_history', 'service_sla', 'quality_check', 'predictive_maintenance')
ORDER BY TABLE_NAME;

-- =====================================================
-- 第三步：安全插入测试数据
-- =====================================================

SELECT '=== 开始插入测试数据 ===' as step;

-- 首先检查现有的task_order数据
SELECT '现有task_order数据检查:' as info;
SELECT COUNT(*) as task_order_count FROM task_order WHERE hotel_id = 1;

-- 只在有数据的情况下插入department_feedback
SET @task_count = (SELECT COUNT(*) FROM task_order WHERE hotel_id = 1);
SET @sql = IF(@task_count > 0,
    'INSERT IGNORE INTO department_feedback (task_order_id, department_task_id, customer_id, hotel_id, department_id, department_name, service_rating, response_speed_rating, service_quality_rating, feedback_content, feedback_tags, is_recommended) VALUES (1, 1, "VIP001", 1, 1, "工程部", 5, 4, 5, "服务很好", "专业,及时", TRUE)',
    'SELECT "跳过插入department_feedback数据，因为task_order表中没有数据" as message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 插入SLA配置数据（不依赖其他表）
INSERT IGNORE INTO service_sla (department_id, department_name, service_type, response_time_limit, completion_time_limit, hotel_id, is_active) VALUES
(1, '工程部', '网络故障', 15, 60, 1, TRUE),
(1, '工程部', '水管维修', 30, 90, 1, TRUE),
(1, '工程部', '电路维修', 10, 45, 1, TRUE),
(2, '服务部', '客房清洁', 20, 60, 1, TRUE),
(2, '服务部', '客房服务', 15, 45, 1, TRUE),
(3, '餐饮部', '客房送餐', 20, 45, 1, TRUE),
(3, '餐饮部', '餐厅服务', 5, 15, 1, TRUE);

-- 插入质量检查数据（如果有task_order数据）
SET @sql = IF(@task_count > 0,
    'INSERT IGNORE INTO quality_check (task_order_id, department_id, department_name, check_type, check_score, check_result, check_standards, checked_by, hotel_id) VALUES (1, 1, "工程部", "TIMELINESS", 8.5, "响应较快，但还有提升空间", "10分钟内响应，1小时内完成", "质检员001", 1)',
    'SELECT "跳过插入quality_check数据，因为task_order表中没有数据" as message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 插入预测性维护数据（不依赖其他表）
INSERT IGNORE INTO predictive_maintenance (room_number, equipment_type, maintenance_type, prediction_score, maintenance_date, maintenance_description, status, hotel_id) VALUES
('8801', '空调系统', 'PREVENTIVE', 88.5, NOW() + INTERVAL 3 DAYS, '定期检查和清洁空调滤网', 'SCHEDULED', 1),
('8802', '热水器', 'PREVENTIVE', 76.3, NOW() + INTERVAL 5 DAYS, '检查热水器温度和压力', 'SCHEDULED', 1),
('8803', '电视', 'CORRECTIVE', 92.1, NOW() - INTERVAL 2 HOURS, '电视无信号，需要检查线路', 'IN_PROGRESS', 1);

SELECT '=== 测试数据插入完成 ===' as step;

-- =====================================================
-- 第四步：最终验证
-- =====================================================

SELECT '=== 最终数据验证 ===' as step;

-- 显示各表的数据量
SELECT
    'department_feedback' as table_name,
    COUNT(*) as record_count
FROM department_feedback
UNION ALL
SELECT
    'task_history' as table_name,
    COUNT(*) as record_count
FROM task_history
UNION ALL
SELECT
    'service_sla' as table_name,
    COUNT(*) as record_count,
    COUNT(CASE WHEN is_active = TRUE THEN 1 END) as active_sla
FROM service_sla
UNION ALL
SELECT
    'quality_check' as table_name,
    COUNT(*) as record_count
FROM quality_check
UNION ALL
SELECT
    'predictive_maintenance' as table_name,
    COUNT(*) as record_count,
    COUNT(CASE WHEN status = "SCHEDULED" THEN 1 END) as scheduled_count
FROM predictive_maintenance;

-- 显示SLA配置概览
SELECT '=== SLA配置概览 ===' as section,
    department_name,
    COUNT(*) as sla_count,
    GROUP_CONCAT(service_type) as service_types
FROM service_sla
WHERE hotel_id = 1 AND is_active = TRUE
GROUP BY department_name
ORDER BY department_name;

SELECT '=== 数据库设置脚本执行完成 ===' as final_message,
    '如果所有表都已创建，请重启应用并测试API功能' as next_step;