-- 简单的数据库表创建脚本
-- 先执行修改现有表，然后创建新表

-- 1. 修改现有表结构
ALTER TABLE task_order ADD COLUMN priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL';
ALTER TABLE department_task ADD COLUMN cancel_reason TEXT;

-- 2. 创建部门客户评价表
CREATE TABLE IF NOT EXISTS department_feedback (
    feedback_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_order_id BIGINT NOT NULL,
    department_task_id BIGINT NOT NULL,
    customer_id VARCHAR(50) NOT NULL,
    hotel_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    department_name VARCHAR(50) NOT NULL,
    service_rating INT NOT NULL,
    response_speed_rating INT NOT NULL,
    service_quality_rating INT NOT NULL,
    overall_rating DECIMAL(3,2) NOT NULL,
    feedback_content TEXT,
    feedback_tags VARCHAR(200),
    is_recommended BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_task_order_id (task_order_id),
    INDEX idx_hotel_id (hotel_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 创建任务历史记录表
CREATE TABLE IF NOT EXISTS task_history (
    history_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_order_id BIGINT NOT NULL,
    old_status VARCHAR(20),
    new_status VARCHAR(20) NOT NULL,
    operation_type VARCHAR(20) NOT NULL,
    operation_reason TEXT,
    operator_id VARCHAR(50),
    operator_name VARCHAR(100),
    operator_type VARCHAR(20),
    department_id BIGINT,
    department_name VARCHAR(50),
    hotel_id BIGINT NOT NULL,
    operation_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_task_order_id (task_order_id),
    INDEX idx_hotel_id (hotel_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. 插入测试数据
INSERT INTO department_feedback (
    task_order_id,
    department_task_id,
    customer_id,
    hotel_id,
    department_id,
    department_name,
    service_rating,
    response_speed_rating,
    service_quality_rating,
    feedback_content,
    feedback_tags,
    is_recommended
) VALUES
(1, 1, 'VIP001', 1, 1, '工程部', 5, 4, 5, '服务很好', '专业,及时', TRUE),
(2, 2, 'VIP002', 1, 2, '服务部', 4, 5, 4, '态度很好', '友好,推荐', TRUE);

INSERT INTO task_history (
    task_order_id,
    old_status,
    new_status,
    operation_type,
    operation_reason,
    operator_id,
    operator_name,
    operator_type,
    department_id,
    department_name,
    hotel_id
) VALUES
(1, NULL, 'PENDING', 'CREATE', '任务创建', 'VIP001', '张三', 'CUSTOMER', 1, '工程部', 1);