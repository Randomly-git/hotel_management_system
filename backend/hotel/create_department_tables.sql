-- 创建部门任务分发相关表
USE hotel_management_system;

-- 部门任务表（用于存储分发给各部门的具体任务）
CREATE TABLE IF NOT EXISTS department_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_order_id BIGINT NOT NULL COMMENT '关联的原始任务单ID',
    department_id BIGINT NOT NULL COMMENT '部门ID',
    task_title VARCHAR(200) NOT NULL COMMENT '任务标题',
    task_description TEXT NOT NULL COMMENT '任务详细描述',
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM' COMMENT '优先级',
    status ENUM('ASSIGNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') DEFAULT 'ASSIGNED' COMMENT '任务状态',
    assigned_to VARCHAR(100) COMMENT '分配给的员工',
    room_number VARCHAR(20) COMMENT '房间号',
    customer_id VARCHAR(50) COMMENT '客户ID',
    expected_completion_time TIMESTAMP COMMENT '期望完成时间',
    actual_completion_time TIMESTAMP COMMENT '实际完成时间',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    hotel_id BIGINT NOT NULL DEFAULT 1 COMMENT '酒店ID',
    INDEX idx_task_order (task_order_id),
    INDEX idx_department (department_id),
    INDEX idx_status (status),
    INDEX idx_priority (priority),
    INDEX idx_hotel (hotel_id),
    INDEX idx_room (room_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门任务表';

-- 部门任务评论/备注表（用于记录任务处理过程）
CREATE TABLE IF NOT EXISTS department_task_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    department_task_id BIGINT NOT NULL COMMENT '部门任务ID',
    commenter_id VARCHAR(50) COMMENT '评论人ID',
    commenter_name VARCHAR(100) COMMENT '评论人姓名',
    comment_content TEXT NOT NULL COMMENT '评论内容',
    comment_type ENUM('NOTE', 'STATUS_UPDATE', 'CUSTOMER_FEEDBACK', 'ASSIGNMENT') DEFAULT 'NOTE' COMMENT '评论类型',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    hotel_id BIGINT NOT NULL DEFAULT 1 COMMENT '酒店ID',
    INDEX idx_department_task (department_task_id),
    INDEX idx_hotel (hotel_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门任务评论表';

-- 部门任务模板表（用于快速创建常用任务）
CREATE TABLE IF NOT EXISTS department_task_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    department_id BIGINT NOT NULL COMMENT '部门ID',
    template_name VARCHAR(200) NOT NULL COMMENT '模板名称',
    task_title VARCHAR(200) NOT NULL COMMENT '任务标题模板',
    task_description TEXT NOT NULL COMMENT '任务描述模板',
    default_priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM' COMMENT '默认优先级',
    estimated_duration_minutes INT DEFAULT 30 COMMENT '预估耗时（分钟）',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    hotel_id BIGINT NOT NULL DEFAULT 1 COMMENT '酒店ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_department (department_id),
    INDEX idx_hotel (hotel_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门任务模板表';

-- 插入一些初始的任务模板数据
INSERT INTO department_task_template (department_id, template_name, task_title, task_description, default_priority, estimated_duration_minutes) VALUES
-- 工程部模板
(1, '空调维修', '客房空调故障维修', '客房空调无法正常工作，需要工程部人员进行检修和维修', 'HIGH', 60),
(1, '水电维修', '客房水电设施维修', '客房内水电设施出现故障，需要紧急处理', 'HIGH', 45),
(1, '网络问题', '网络连接故障处理', '客房网络连接不稳定或无法连接，需要技术人员检查', 'MEDIUM', 30),
-- 服务部模板
(2, '房间清洁', '客房深度清洁服务', '客户要求额外的房间清洁服务', 'MEDIUM', 40),
(2, '布草补充', '补充客房布草用品', '客户需要额外的毛巾、床单等布草用品', 'LOW', 15),
(2, '行李服务', '客户行李协助服务', '客户需要行李搬运或寄存服务', 'MEDIUM', 20),
-- 餐饮部模板
(3, '客房送餐', '客房餐饮配送服务', '客户要求在客房内用餐，需要送餐服务', 'MEDIUM', 25),
(3, '特殊饮食', '特殊饮食需求处理', '客户有特殊的饮食要求或过敏原需要注意', 'HIGH', 35),
-- 安保部模板
(4, '安全巡查', '安全异常巡查', '客户报告可疑人员或情况，需要安保部巡查确认', 'URGENT', 30),
(4, '失物招领', '客户失物处理', '客户报告物品丢失，需要安保部协助寻找', 'HIGH', 60);