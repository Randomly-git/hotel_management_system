-- 创建测试任务表
USE hotel_management_system;

CREATE TABLE IF NOT EXISTS test_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id VARCHAR(50) NOT NULL COMMENT '客户ID',
    request_content TEXT NOT NULL COMMENT '原始请求内容',
    nlp_intent VARCHAR(50) COMMENT 'NLP解析意图',
    nlp_description TEXT COMMENT 'NLP解析描述',
    nlp_department VARCHAR(50) COMMENT 'NLP解析部门',
    room_number VARCHAR(20) COMMENT '房间号',
    hotel_id BIGINT DEFAULT 1 COMMENT '酒店ID',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '任务状态',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    due_time TIMESTAMP NULL COMMENT '期望解决时间',
    INDEX idx_customer (customer_id),
    INDEX idx_hotel (hotel_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试任务表';

-- 创建日志表用于调试
CREATE TABLE IF NOT EXISTS nlp_debug_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id VARCHAR(50) COMMENT '客户ID',
    original_request TEXT COMMENT '原始请求',
    ai_response TEXT COMMENT 'AI完整响应',
    json_content TEXT COMMENT '提取的JSON内容',
    parsed_intent VARCHAR(50) COMMENT '解析的意图',
    parsed_description TEXT COMMENT '解析的描述',
    parsed_department VARCHAR(50) COMMENT '解析的部门',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='NLP调试日志表';