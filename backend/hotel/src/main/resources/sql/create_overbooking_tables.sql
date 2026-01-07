-- 创建超售决策记录表
CREATE TABLE IF NOT EXISTS `overbooking_decisions` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `hotel_id` BIGINT NOT NULL,
  `room_type_id` BIGINT NOT NULL,
  `decision_date` DATE NOT NULL,
  `state_key` VARCHAR(100) NOT NULL,
  `action_chosen` INT NOT NULL,
  `q_value` DECIMAL(10,2),
  `total_rooms` INT NOT NULL,
  `confirmed_bookings` INT NOT NULL,
  `actual_cancellations INT DEFAULT 0,
  `actual_no_shows` INT DEFAULT 0,
  `was_successful` BOOLEAN,
  `reward` DECIMAL(12,2),
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_hotel_date` (`hotel_id`, `decision_date`),
  INDEX `idx_decision` (`hotel_id`, `decision_date`, `was_successful`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建超售配置表
CREATE TABLE IF NOT EXISTS `overbooking_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `hotel_id` BIGINT NOT NULL,
  `room_type_id` BIGINT NOT NULL,
  `max_overbook` INT NOT NULL DEFAULT 5,
  `compensation_rate` DECIMAL(5,2) NOT NULL DEFAULT 1.50,
  `enabled` BOOLEAN DEFAULT TRUE,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config` (`hotel_id`, `room_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建Q-Learning状态表
CREATE TABLE IF NOT EXISTS `q_learning_state` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `hotel_id` BIGINT NOT NULL,
  `room_type_id` BIGINT NOT NULL,
  `state_key` VARCHAR(100) NOT NULL,
  `action_0` DECIMAL(10,2) DEFAULT 0.00,
  `action_1` DECIMAL(10,2) DEFAULT 0.00,
  `action_2` DECIMAL(10,2) DEFAULT 0.00,
  `action_3` DECIMAL(10,2) DEFAULT 0.00,
  `action_4` DECIMAL(10,2) DEFAULT 0.00,
  `action_5` DECIMAL(10,2) DEFAULT 0.00,
  `visit_count` INT DEFAULT 0,
  `last_updated` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_state` (`hotel_id`, `room_type_id`, `state_key`),
  INDEX `idx_hotel_room` (`hotel_id`, `room_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
