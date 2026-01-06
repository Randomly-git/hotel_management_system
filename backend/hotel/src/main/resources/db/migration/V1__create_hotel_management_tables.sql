-- =====================================================
-- 酒店管理系统 - 基础表结构
-- 基于 Kaggle Hotel Booking Demand Dataset
-- =====================================================

-- 1. 酒店信息表
CREATE TABLE hotels (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '酒店名称',
    type VARCHAR(50) NOT NULL COMMENT '酒店类型: Resort Hotel, City Hotel',
    address VARCHAR(255),
    city VARCHAR(100),
    country VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    description TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='酒店信息表';

-- 2. 房型（Room Type）表 - 基于 CSV 中的 A-H 房型代码
CREATE TABLE room_types (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    hotel_id BIGINT NOT NULL,
    type_code CHAR(1) NOT NULL COMMENT '房型代码: A, B, C, D, E, F, G, H',
    type_name VARCHAR(50) NOT NULL COMMENT '房型名称: 标准间, 豪华间, 套房, 总统套房',
    description TEXT COMMENT '房型描述',
    max_occupancy INT NOT NULL DEFAULT 2 COMMENT '最大入住人数',
    base_price DECIMAL(10,2) NOT NULL COMMENT '基础价格/晚',
    facilities JSON COMMENT '设施: ["WiFi", "空调", "电视"]',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_hotel_type (hotel_id, type_code),
    INDEX idx_hotel (hotel_id),
    INDEX idx_code (type_code),
    FOREIGN KEY (hotel_id) REFERENCES hotels(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房型配置表';

-- 3. 房间表
CREATE TABLE rooms (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    hotel_id BIGINT NOT NULL,
    room_type_id BIGINT NOT NULL,
    room_number VARCHAR(20) NOT NULL COMMENT '房间号',
    floor INT NOT NULL COMMENT '楼层',
    status ENUM('available', 'occupied', 'maintenance', 'cleaning') NOT NULL DEFAULT 'available' COMMENT '房间状态',
    has_ac BOOLEAN DEFAULT TRUE COMMENT '是否有空调',
    has_tv BOOLEAN DEFAULT TRUE COMMENT '是否有电视',
    has_wifi BOOLEAN DEFAULT TRUE COMMENT '是否有WiFi',
    has_balcony BOOLEAN DEFAULT FALSE COMMENT '是否有阳台',
    has_kitchen BOOLEAN DEFAULT FALSE COMMENT '是否有厨房',
    parking_spaces INT DEFAULT 0 COMMENT '停车位数量',
    description TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_hotel_room (hotel_id, room_number),
    INDEX idx_hotel (hotel_id),
    INDEX idx_room_type (room_type_id),
    INDEX idx_status (status),
    FOREIGN KEY (hotel_id) REFERENCES hotels(id) ON DELETE CASCADE,
    FOREIGN KEY (room_type_id) REFERENCES room_types(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房间信息表';

-- 4. 客户表
CREATE TABLE customers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    hotel_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL COMMENT '客户姓名',
    email VARCHAR(100),
    phone VARCHAR(20),
    country CHAR(2) COMMENT '国家代码: CN, US, GB 等',
    id_card_number VARCHAR(50) COMMENT '身份证/护照号',
    is_repeated_guest BOOLEAN DEFAULT FALSE COMMENT '是否重复客户',
    total_stays INT DEFAULT 0 COMMENT '累计入住次数',
    total_cancellations INT DEFAULT 0 COMMENT '累计取消次数',
    vip_level ENUM('normal', 'silver', 'gold', 'platinum') DEFAULT 'normal' COMMENT 'VIP等级',
    notes TEXT COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_hotel_id_card (hotel_id, id_card_number),
    INDEX idx_hotel (hotel_id),
    INDEX idx_name (name),
    INDEX idx_country (country),
    INDEX idx_vip (vip_level),
    FOREIGN KEY (hotel_id) REFERENCES hotels(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户信息表';

-- 5. 预订表
CREATE TABLE bookings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    hotel_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    room_type_id BIGINT NOT NULL COMMENT '预订的房型（非具体房间）',
    booking_number VARCHAR(50) NOT NULL UNIQUE COMMENT '预订编号',

    -- 预订时间信息
    lead_time INT NOT NULL COMMENT '提前预订天数',
    booking_date DATETIME NOT NULL COMMENT '预订时间',

    -- 入住时间信息
    check_in_date DATE NOT NULL COMMENT '计划入住日期',
    check_out_date DATE NOT NULL COMMENT '计划退房日期',
    stays_in_weekend_nights INT DEFAULT 0 COMMENT '周末晚数',
    stays_in_week_nights INT DEFAULT 0 COMMENT '工作日晚数',
    total_nights INT NOT NULL COMMENT '总晚数',

    -- 客人信息
    adults INT NOT NULL DEFAULT 1 COMMENT '成人数量',
    children INT DEFAULT 0 COMMENT '儿童数量',
    babies INT DEFAULT 0 COMMENT '婴儿数量',
    required_car_parking_spaces INT DEFAULT 0 COMMENT '所需停车位',

    -- 价格信息
    total_price DECIMAL(10,2) NOT NULL COMMENT '总价',
    adr DECIMAL(10,2) NOT NULL COMMENT '平均日房价(Average Daily Rate)',
    deposit_type ENUM('no_deposit', 'non_refund', 'refundable') DEFAULT 'no_deposit' COMMENT '押金类型',

    -- 餐饮
    meal_type ENUM('bb', 'hb', 'fb', 'sc') DEFAULT 'bb' COMMENT '餐型: BB=含早, HB=含早晚餐, FB=全含, SC=无餐',

    -- 渠道信息
    market_segment VARCHAR(50) COMMENT '市场细分: Online TA, Direct, Corporate, Offline TA/TO',
    distribution_channel VARCHAR(50) COMMENT '分销渠道: Direct, TA/TO, Corporate',

    -- 状态
    status ENUM('booked', 'checked_in', 'canceled', 'completed') NOT NULL DEFAULT 'booked',
    is_canceled BOOLEAN DEFAULT FALSE COMMENT '是否已取消',
    cancel_date DATETIME COMMENT '取消时间',

    -- 特殊需求
    special_requests INT DEFAULT 0 COMMENT '特殊需求数量',
    requests_text TEXT COMMENT '特殊需求内容',

    -- 变更记录
    booking_changes INT DEFAULT 0 COMMENT '预订变更次数',
    days_in_waiting_list INT DEFAULT 0 COMMENT '等待列表天数',

    -- 实际分配
    assigned_room_id BIGINT COMMENT '实际分配的房间ID',
    actual_room_type CHAR(1) COMMENT '实际房型代码',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_hotel (hotel_id),
    INDEX idx_customer (customer_id),
    INDEX idx_room_type (room_type_id),
    INDEX idx_dates (check_in_date, check_out_date),
    INDEX idx_status (status),
    INDEX idx_booking_number (booking_number),
    INDEX idx_assigned_room (assigned_room_id),
    FOREIGN KEY (hotel_id) REFERENCES hotels(id) ON DELETE CASCADE,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE RESTRICT,
    FOREIGN KEY (room_type_id) REFERENCES room_types(id) ON DELETE RESTRICT,
    FOREIGN KEY (assigned_room_id) REFERENCES rooms(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预订表';

-- 6. 入住记录表
CREATE TABLE check_in_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    hotel_id BIGINT NOT NULL,
    booking_id BIGINT NOT NULL COMMENT '关联的预订',
    room_id BIGINT NOT NULL COMMENT '实际入住房间',
    customer_id BIGINT NOT NULL,

    check_in_time DATETIME NOT NULL COMMENT '实际入住时间',
    check_out_time DATETIME COMMENT '实际退房时间',
    scheduled_check_out_date DATE NOT NULL COMMENT '计划退房日期',

    -- 价格调整
    actual_price DECIMAL(10,2) COMMENT '实际结算价格',
    price_adjustment_reason VARCHAR(255) COMMENT '价格调整原因',

    -- 延长住宿
    is_extended BOOLEAN DEFAULT FALSE COMMENT '是否延长住宿',
    original_check_out_date DATE COMMENT '原计划退房日期',

    status ENUM('active', 'completed', 'early_checkout') NOT NULL DEFAULT 'active',

    notes TEXT COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_hotel (hotel_id),
    INDEX idx_booking (booking_id),
    INDEX idx_room (room_id),
    INDEX idx_customer (customer_id),
    INDEX idx_status (status),
    INDEX idx_check_in_time (check_in_time),
    INDEX idx_check_out_time (check_out_time),
    FOREIGN KEY (hotel_id) REFERENCES hotels(id) ON DELETE CASCADE,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE RESTRICT,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入住记录表';

-- =====================================================
-- 初始化数据
-- =====================================================

-- 插入测试酒店
INSERT INTO hotels (name, type, address, city, country, phone, email) VALUES
('阳光度假酒店', 'Resort Hotel', '海滨路88号', '三亚', 'CN', '+86-898-88888888', 'contact@sunshine-resort.com'),
('城市商务酒店', 'City Hotel', '中山路123号', '上海', 'CN', '+86-21-66666666', 'info@city-business-hotel.com');

-- 插入房型配置（基于 CSV 数据）
INSERT INTO room_types (hotel_id, type_code, type_name, description, max_occupancy, base_price, facilities) VALUES
-- 阳光度假酒店房型
(1, 'A', '标准间', '舒适经济型客房，适合短期住宿', 2, 500.00, '["WiFi", "空调", "电视", "淋浴"]'),
(1, 'B', '标准间(海景)', '标准间配置，可观看海景', 2, 650.00, '["WiFi", "空调", "电视", "淋浴", "海景"]'),
(1, 'C', '豪华间', '空间更大，设施更完善', 2, 800.00, '["WiFi", "空调", "电视", "浴缸", "阳台"]'),
(1, 'D', '豪华间(海景)', '豪华间配置，绝佳海景视野', 3, 1000.00, '["WiFi", "空调", "电视", "浴缸", "阳台", "海景"]'),
(1, 'E', '套房', '独立客厅和卧室，适合商务', 3, 1500.00, '["WiFi", "空调", "电视", "浴缸", "客厅", "办公区"]'),
(1, 'F', '海景套房', '套房配置，享受无敌海景', 4, 1800.00, '["WiFi", "空调", "电视", "浴缸", "客厅", "海景"]'),
(1, 'G', '家庭套房', '两室一厅，适合家庭出游', 5, 2200.00, '["WiFi", "空调", "电视", "浴缸", "客厅", "两卧室"]'),
(1, 'H', '总统套房', '顶级奢华配置，专属服务', 6, 5000.00, '["WiFi", "空调", "电视", "浴缸", "客厅", "餐厅", "厨房", "吧台", "管家服务"]'),

-- 城市商务酒店房型
(2, 'A', '标准单人间', '经济实用，适合单人出行', 1, 400.00, '["WiFi", "空调", "电视", "淋浴"]'),
(2, 'B', '标准双人间', '标准配置，双人入住', 2, 550.00, '["WiFi", "空调", "电视", "淋浴"]'),
(2, 'C', '商务大床房', '大床配置，办公设施齐全', 2, 700.00, '["WiFi", "空调", "电视", "办公桌", "淋浴"]'),
(2, 'D', '豪华商务房', '升级配置，享受更多服务', 2, 900.00, '["WiFi", "空调", "电视", "办公桌", "浴缸"]'),
(2, 'E', '行政套房', '独立空间，行政楼层待遇', 3, 1300.00, '["WiFi", "空调", "电视", "客厅", "办公区", "行政酒廊"]'),
(2, 'F', '商务套房', '完整套房配置', 3, 1600.00, '["WiFi", "空调", "电视", "客厅", "办公区", "会议室"]'),
(2, 'G', '家庭套房', '家庭出游理想选择', 4, 2000.00, '["WiFi", "空调", "电视", "客厅", "两卧室"]'),
(2, 'H', '总统套房', '顶级商务接待', 6, 4500.00, '["WiFi", "空调", "电视", "客厅", "餐厅", "会议室", "管家服务"]');

-- 插入示例房间
INSERT INTO rooms (hotel_id, room_type_id, room_number, floor, status, parking_spaces) VALUES
-- 阳光度假酒店 - 1楼
(1, 1, 'A101', 1, 'available', 0),
(1, 1, 'A102', 1, 'available', 0),
(1, 1, 'A103', 1, 'occupied', 0),
(1, 2, 'B101', 1, 'available', 1),
(1, 2, 'B102', 1, 'occupied', 1),
-- 阳光度假酒店 - 2楼
(1, 3, 'C201', 2, 'available', 1),
(1, 3, 'C202', 2, 'cleaning', 1),
(1, 4, 'D201', 2, 'available', 1),
(1, 4, 'D202', 2, 'occupied', 1),
(1, 5, 'E201', 2, 'maintenance', 2),
-- 城市商务酒店 - 1楼
(2, 9, '101', 1, 'available', 0),
(2, 9, '102', 1, 'available', 0),
(2, 10, '103', 1, 'occupied', 1),
(2, 10, '104', 1, 'available', 1),
(2, 11, '105', 1, 'occupied', 1);
