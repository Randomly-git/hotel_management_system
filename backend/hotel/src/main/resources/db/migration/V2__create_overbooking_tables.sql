-- =====================================================
-- 智能超售系统 - 数据库表
-- =====================================================

-- 1. 超售配置表
CREATE TABLE overbooking_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    hotel_id BIGINT NOT NULL,
    room_type_id BIGINT NOT NULL,
    max_overbook INT NOT NULL DEFAULT 5 COMMENT '最大超售量限制',
    compensation_rate DECIMAL(5,2) DEFAULT 1.5 COMMENT '赔偿倍数',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (hotel_id) REFERENCES hotels(id),
    FOREIGN KEY (room_type_id) REFERENCES room_types(id),
    UNIQUE KEY uk_hotel_room (hotel_id, room_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='超售配置表';

-- 2. Q-Learning 状态表 (Q-Table)
CREATE TABLE q_learning_state (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    hotel_id BIGINT NOT NULL,
    room_type_id BIGINT NOT NULL,
    state_key VARCHAR(100) NOT NULL COMMENT '状态编码',
    action_0 DECIMAL(10,2) DEFAULT 0 COMMENT '超售0的Q值',
    action_1 DECIMAL(10,2) DEFAULT 0 COMMENT '超售1的Q值',
    action_2 DECIMAL(10,2) DEFAULT 0 COMMENT '超售2的Q值',
    action_3 DECIMAL(10,2) DEFAULT 0 COMMENT '超售3的Q值',
    action_4 DECIMAL(10,2) DEFAULT 0 COMMENT '超售4的Q值',
    action_5 DECIMAL(10,2) DEFAULT 0 COMMENT '超售5的Q值',
    visit_count INT DEFAULT 0 COMMENT '访问次数',
    last_updated DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (hotel_id) REFERENCES hotels(id),
    FOREIGN KEY (room_type_id) REFERENCES room_types(id),
    UNIQUE KEY uk_state (hotel_id, room_type_id, state_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Q-Learning状态表';

-- 3. 超售决策记录表
CREATE TABLE overbooking_decisions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    hotel_id BIGINT NOT NULL,
    room_type_id BIGINT NOT NULL,
    decision_date DATE NOT NULL,
    state_key VARCHAR(100) NOT NULL COMMENT '当时状态',
    action_chosen INT NOT NULL COMMENT '选择的超售量',
    q_value DECIMAL(10,2) COMMENT '对应Q值',
    total_rooms INT NOT NULL,
    confirmed_bookings INT NOT NULL,
    actual_cancellations INT DEFAULT 0,
    actual_no_shows INT DEFAULT 0,
    was_successful BOOLEAN COMMENT '是否成功(无溢出)',
    reward DECIMAL(12,2) COMMENT '实际奖励',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (hotel_id) REFERENCES hotels(id),
    FOREIGN KEY (room_type_id) REFERENCES room_types(id),
    INDEX idx_date (decision_date),
    INDEX idx_hotel_type (hotel_id, room_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='超售决策记录';

-- 4. RL 训练历史表
CREATE TABLE rl_training_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    hotel_id BIGINT NOT NULL,
    room_type_id BIGINT NOT NULL,
    episode INT NOT NULL COMMENT '训练回合',
    total_reward DECIMAL(12,2) COMMENT '总奖励',
    avg_reward DECIMAL(10,2) COMMENT '平均奖励',
    epsilon DECIMAL(5,4) COMMENT '探索率',
    q_table_size INT COMMENT 'Q表大小',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (hotel_id) REFERENCES hotels(id),
    FOREIGN KEY (room_type_id) REFERENCES room_types(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RL训练历史';

-- 5. 初始化配置数据
INSERT INTO overbooking_config (hotel_id, room_type_id, max_overbook, compensation_rate, enabled) VALUES
(1, 1, 5, 1.5, TRUE),  -- 标准间
(1, 2, 5, 1.5, TRUE),  -- 标准间(海景)
(1, 3, 3, 1.5, TRUE),  -- 豪华间
(1, 4, 3, 1.5, TRUE),  -- 豪华间(海景)
(1, 5, 2, 1.5, TRUE),  -- 套房
(1, 6, 2, 1.5, TRUE),  -- 海景套房
(1, 7, 1, 1.5, TRUE),  -- 家庭套房
(1, 8, 1, 1.5, TRUE),  -- 总统套房
(2, 9, 5, 1.5, TRUE),   -- 标准单人间
(2, 10, 5, 1.5, TRUE),  -- 标准双人间
(2, 11, 3, 1.5, TRUE),  -- 商务大床房
(2, 12, 3, 1.5, TRUE),  -- 豪华商务房
(2, 13, 2, 1.5, TRUE),  -- 行政套房
(2, 14, 2, 1.5, TRUE),  -- 商务套房
(2, 15, 1, 1.5, TRUE),  -- 家庭套房
(2, 16, 1, 1.5, TRUE);  -- 总统套房
