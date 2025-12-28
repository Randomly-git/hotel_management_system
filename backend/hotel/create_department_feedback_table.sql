-- 创建部门客户评价表
CREATE TABLE IF NOT EXISTS department_feedback (
    feedback_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评价ID',

    -- 关联任务信息
    task_order_id BIGINT NOT NULL COMMENT '关联的任务单ID',
    department_task_id BIGINT NOT NULL COMMENT '关联的部门任务ID',

    -- 客户和酒店信息
    customer_id VARCHAR(50) NOT NULL COMMENT '客户ID',
    hotel_id BIGINT NOT NULL COMMENT '酒店ID',

    -- 部门信息
    department_id BIGINT NOT NULL COMMENT '部门ID',
    department_name VARCHAR(50) NOT NULL COMMENT '部门名称',

    -- 评分信息 (1-5分制)
    service_rating INT NOT NULL COMMENT '服务评分 1-5分',
    response_speed_rating INT NOT NULL COMMENT '响应速度评分 1-5分',
    service_quality_rating INT NOT NULL COMMENT '服务质量评分 1-5分',
    overall_rating DECIMAL(3,2) NOT NULL COMMENT '综合评分（自动计算）',

    -- 评价内容
    feedback_content TEXT COMMENT '评价内容',
    feedback_tags VARCHAR(200) COMMENT '评价标签（逗号分隔）',
    is_recommended BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否推荐服务',

    -- 时间戳
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 索引
    INDEX idx_task_order_id (task_order_id),
    INDEX idx_department_task_id (department_task_id),
    INDEX idx_customer_id (customer_id),
    INDEX idx_hotel_id (hotel_id),
    INDEX idx_department_id (department_id),
    INDEX idx_hotel_department (hotel_id, department_id),
    INDEX idx_created_at (created_at),
    INDEX idx_overall_rating (overall_rating)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门客户评价表';

-- 添加约束条件
ALTER TABLE department_feedback
ADD CONSTRAINT chk_service_rating CHECK (service_rating BETWEEN 1 AND 5),
ADD CONSTRAINT chk_response_speed_rating CHECK (response_speed_rating BETWEEN 1 AND 5),
ADD CONSTRAINT chk_service_quality_rating CHECK (service_quality_rating BETWEEN 1 AND 5),
ADD CONSTRAINT chk_overall_rating CHECK (overall_rating BETWEEN 1.0 AND 5.0);

-- 添加外键约束（可选，根据实际表结构调整）
-- ALTER TABLE department_feedback
-- ADD CONSTRAINT fk_feedback_task_order FOREIGN KEY (task_order_id) REFERENCES task_order(task_id),
-- ADD CONSTRAINT fk_feedback_department_task FOREIGN KEY (department_task_id) REFERENCES department_task(id),
-- ADD CONSTRAINT fk_feedback_department FOREIGN KEY (department_id) REFERENCES sys_department(dept_id);

-- 示例数据（用于测试）
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
(1, 1, 'VIP001', 1, 1, '工程部', 5, 4, 5, '工程师很快就来修好了空调，服务很专业', '专业,及时,满意', TRUE),
(2, 2, 'VIP002', 1, 2, '服务部', 4, 5, 4, '送餐速度很快，服务员态度很好', '及时,友好,推荐', TRUE),
(3, 3, 'VIP003', 1, 3, '餐饮部', 3, 3, 4, '菜品味道不错，就是价格有点贵', '味道好,价格贵', FALSE);