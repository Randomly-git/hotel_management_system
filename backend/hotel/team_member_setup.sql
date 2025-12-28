-- =====================================================
-- 团队成员数据库设置脚本
-- 执行步骤：先执行 create_all_required_tables.sql，然后执行本文件
-- =====================================================

-- =====================================================
-- 1. 更新现有任务数据的优先级
-- =====================================================

-- 为紧急任务设置高优先级（任务内容包含紧急关键词）
UPDATE task_order
SET priority = 'URGENCY'
WHERE hotel_id = 1
AND (
    task_content LIKE '%紧急%'
    OR task_content LIKE '%救命%'
    OR task_content LIKE '%着火%'
    OR task_content LIKE '%漏水%'
    OR task_content LIKE '%断电%'
);

-- 为建议咨询任务设置低优先级
UPDATE task_order
SET priority = 'LOW'
WHERE hotel_id = 1
AND (
    task_content LIKE '%建议%'
    OR task_content LIKE '%咨询%'
    OR task_content LIKE '%了解一下%'
    OR task_content LIKE '%问一下%'
);

-- 验证优先级更新
SELECT task_id, priority, task_content FROM task_order WHERE hotel_id = 1 ORDER BY task_id DESC LIMIT 10;

-- =====================================================
-- 2. 插入更多客户评价数据
-- =====================================================

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
-- 工程部评价
(4, 4, 'VIP004', 1, 1, '工程部', 5, 5, 5, '网络故障修复非常快，技术人员很专业', '网络,快速,专业,推荐', TRUE),
(5, 5, 'VIP005', 1, 1, '工程部', 4, 4, 4, '水管修复及时，但后续检查不够仔细', '水管,及时,待改进', FALSE),
(6, 6, 'VIP006', 1, 1, '工程部', 5, 3, 5, '电路安全问题处理很好', '安全,专业,及时', TRUE),

-- 服务部评价
(7, 7, 'VIP007', 1, 2, '服务部', 3, 3, 4, '客房打扫基本完成，细节有待提升', '打扫,细节,待改进', FALSE),
(8, 8, 'VIP008', 1, 2, '服务部', 4, 4, 4, '送餐服务态度很好，食物温度合适', '送餐,态度好,温度合适', TRUE),
(9, 9, 'VIP009', 1, 2, '服务部', 5, 5, 5, '行李搬运服务非常贴心，主动帮助', '行李,贴心,主动,推荐', TRUE),

-- 餐饮部评价
(10, 10, 'VIP010', 1, 3, '餐饮部', 4, 4, 4, '早餐品种丰富，味道不错', '早餐,丰富,味道好', TRUE),
(11, 11, 'VIP011', 1, 3, '餐饮部', 3, 4, 3, '菜品质量一般，需要改进', '质量一般,需改进', FALSE),

-- 前厅部评价（如果存在）
(12, 12, 'VIP012', 1, 4, '前厅部', 5, 5, 5, '办理入住快速，服务热情周到', '入住,快速,热情,推荐', TRUE),
(13, 13, 'VIP013', 1, 4, '前厅部', 4, 4, 4, '结账服务准确，解释清楚', '结账,准确,解释', TRUE);

-- 验证评价数据插入
SELECT COUNT(*) as feedback_count, AVG(overall_rating) as avg_rating
FROM department_feedback
WHERE hotel_id = 1
GROUP BY department_name;

-- =====================================================
-- 3. 插入更多SLA配置
-- =====================================================

INSERT INTO service_sla (
    department_id,
    department_name,
    service_type,
    response_time_limit,
    completion_time_limit,
    hotel_id,
    is_active
) VALUES
-- 工程部SLA
(1, '工程部', '网络故障', 15, 60, 1, TRUE),
(1, '工程部', '水管维修', 30, 90, 1, TRUE),
(1, '工程部', '电路维修', 10, 45, 1, TRUE),

-- 服务部SLA
(2, '服务部', '客房清洁', 20, 60, 1, TRUE),
(2, '服务部', '客房服务', 15, 45, 1, TRUE),
(2, '服务部', '行李服务', 10, 30, 1, TRUE),

-- 餐饮部SLA
(3, '餐饮部', '客房送餐', 20, 45, 1, TRUE),
(3, '餐饮部', '餐厅服务', 5, 15, 1, TRUE),
(3, '餐饮部', '酒吧服务', 10, 30, 1, TRUE),

-- 安保部SLA
(4, '安保部', '安全巡查', 10, 30, 1, TRUE),
(4, '安保部', '紧急处理', 5, 15, 1, TRUE),

-- 前厅部SLA
(5, '前厅部', '入住登记', 5, 10, 1, TRUE),
(5, '前厅部', '退房结账', 3, 8, 1, TRUE);

-- 验证SLA配置
SELECT COUNT(*) as sla_count FROM service_sla WHERE hotel_id = 1;

-- =====================================================
-- 4. 插入更多服务质量检查记录
-- =====================================================

INSERT INTO quality_check (
    task_order_id,
    department_id,
    department_name,
    check_type,
    check_score,
    check_result,
    check_standards,
    checked_by,
    check_date,
    hotel_id
) VALUES
-- 工程部检查记录
(1, 1, '工程部', 'TIMELINESS', 8.5, '响应较快，但还有提升空间', '10分钟内响应，1小时内完成', '质检员001', NOW() - INTERVAL 2 DAY, 1),
(4, 1, '工程部', 'QUALITY', 9.0, '技术专业，服务规范', '技术规范，服务态度良好', '质检员002', NOW() - INTERVAL 1 DAY, 1),
(5, 1, '工程部', 'SATISFACTION', 8.5, '客户基本满意，价格合理', '专业高效，价格透明', '质检员003', NOW() - INTERVAL 3 DAY, 1),

-- 服务部检查记录
(7, 2, '服务部', 'TIMELINESS', 9.2, '响应非常及时', '5分钟内响应，30分钟内完成', '质检员001', NOW() - INTERVAL 1 DAY, 1),
(8, 2, '服务部', 'QUALITY', 7.8, '服务态度良好，但细节注意不足', '态度友好，工作细致', '质检员004', NOW() - INTERVAL 2 DAY, 1),

-- 餐饮部检查记录
(10, 3, '餐饮部', 'QUALITY', 8.5, '菜品质量稳定，口味不错', '菜品质量稳定，卫生达标', '质检员005', NOW() - INTERVAL 1 DAY, 1);

-- 验证检查记录
SELECT department_name, check_type, AVG(check_score) as avg_score
FROM quality_check
WHERE hotel_id = 1
GROUP BY department_name, check_type;

-- =====================================================
-- 5. 插入更多预测维护记录
-- =====================================================

INSERT INTO predictive_maintenance (
    room_number,
    equipment_type,
    maintenance_type,
    prediction_score,
    maintenance_date,
    maintenance_description,
    status,
    hotel_id
) VALUES
-- 预防性维护
('8801', '空调系统', 'PREVENTIVE', 88.5, NOW() + INTERVAL 3 DAYS, '定期检查和清洁空调滤网', 'SCHEDULED', 1),
('8802', '热水器', 'PREVENTIVE', 76.3, NOW() + INTERVAL 5 DAYS, '检查热水器温度和压力', 'SCHEDULED', 1),
('8804', '门锁', 'PREVENTIVE', 82.1, NOW() + INTERVAL 1 WEEK, '检查门锁电池和机械结构', 'SCHEDULED', 1),
('8805', '网络设备', 'PREVENTIVE', 79.6, NOW() + INTERVAL 4 DAYS, '检查网络连接速度和稳定性', 'SCHEDULED', 1),
('8806', '电路系统', 'PREVENTIVE', 85.2, NOW() + INTERVAL 2 WEEKS, '检查电路安全性', 'SCHEDULED', 1),

-- 纠正性维护
('8803', '电视', 'CORRECTIVE', 92.1, NOW() - INTERVAL 2 HOURS, '电视无信号，需要检查线路', 'IN_PROGRESS', 1),
('8807', '电话', 'CORRECTIVE', 87.8, NOW() - INTERVAL 1 HOUR, '电话故障，需要维修', 'IN_PROGRESS', 1),
('8808', '排风系统', 'CORRECTIVE', 90.3, NOW() - INTERVAL 30 MINUTES, '排风系统异常，需要检修', 'IN_PROGRESS', 1);

-- 验证维护记录
SELECT
    equipment_type,
    maintenance_type,
    COUNT(*) as count,
    AVG(prediction_score) as avg_score
FROM predictive_maintenance
WHERE hotel_id = 1
GROUP BY equipment_type, maintenance_type;

-- =====================================================
-- 6. 插入更多任务历史记录
-- =====================================================

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
    hotel_id,
    operation_time
) VALUES
-- 工程部任务历史
(4, 'PENDING', 'IN_PROGRESS', 'ACCEPT', '部门接受任务', 'STAFF001', '王工', 'STAFF', 1, '工程部', 1, NOW() - INTERVAL 2 HOURS),
(4, 'IN_PROGRESS', 'COMPLETED', 'COMPLETE', '网络故障已修复', 'STAFF001', '王工', 'STAFF', 1, '工程部', 1, NOW() - INTERVAL 1 HOUR),
(5, 'PENDING', 'CANCELED', 'CANCEL', '客户取消需求', 'VIP005', '客户005', 'CUSTOMER', 1, '工程部', 1, NOW() - INTERVAL 30 MINUTES),

-- 服务部任务历史
(7, 'PENDING', 'IN_PROGRESS', 'ACCEPT', '部门接受任务', 'STAFF002', '李服务', 'STAFF', 2, '服务部', 1, NOW() - INTERVAL 3 HOURS),
(7, 'IN_PROGRESS', 'COMPLETED', 'COMPLETE', '房间打扫完成', 'STAFF002', '李服务', 'STAFF', 2, '服务部', 1, NOW() - INTERVAL 1.5 HOURS),

-- 餐饮部任务历史
(10, 'PENDING', 'IN_PROGRESS', 'ACCEPT', '部门接受任务', 'STAFF003', '赵厨师', 'STAFF', 3, '餐饮部', 1, NOW() - INTERVAL 1 HOUR),
(10, 'IN_PROGRESS', 'COMPLETED', 'COMPLETE', '送餐服务完成', 'STAFF003', '赵厨师', 'STAFF', 3, '餐饮部', 1, NOW() - INTERVAL 30 MINUTES);

-- 验证历史记录
SELECT
    department_name,
    operation_type,
    COUNT(*) as count
FROM task_history
WHERE hotel_id = 1
GROUP BY department_name, operation_type
ORDER BY department_name, operation_type;

-- =====================================================
-- 7. 最终数据统计和验证
-- =====================================================

-- 显示所有表的记录数量
SELECT
    '=== 数据库表统计信息 ===' as section,
    'task_order' as table_name,
    COUNT(*) as record_count,
    COUNT(CASE WHEN priority = 'URGENCY' THEN 1 END) as urgent_tasks,
    COUNT(CASE WHEN priority = 'NORMAL' THEN 1 END) as normal_tasks,
    COUNT(CASE WHEN priority = 'LOW' THEN 1 END) as low_tasks
FROM task_order WHERE hotel_id = 1
UNION ALL
SELECT
    'department_feedback' as table_name,
    COUNT(*) as record_count,
    AVG(overall_rating) as avg_rating,
    COUNT(CASE WHEN is_recommended = TRUE THEN 1 END) as recommended_count
FROM department_feedback WHERE hotel_id = 1
UNION ALL
SELECT
    'task_history' as table_name,
    COUNT(*) as record_count,
    COUNT(CASE WHEN new_status = 'COMPLETED' THEN 1 END) as completed_tasks
FROM task_history WHERE hotel_id = 1
UNION ALL
SELECT
    'service_sla' as table_name,
    COUNT(*) as record_count,
    COUNT(CASE WHEN is_active = TRUE THEN 1 END) as active_sla
FROM service_sla WHERE hotel_id = 1
UNION ALL
SELECT
    'quality_check' as table_name,
    COUNT(*) as record_count,
    AVG(check_score) as avg_score
FROM quality_check WHERE hotel_id = 1
UNION ALL
SELECT
    'predictive_maintenance' as table_name,
    COUNT(*) as record_count,
    AVG(prediction_score) as avg_score,
    COUNT(CASE WHEN status = 'SCHEDULED' THEN 1 END) as scheduled_count
FROM predictive_maintenance WHERE hotel_id = 1;

-- 显示部门绩效概览
SELECT
    department_name,
    AVG(overall_rating) as avg_rating,
    COUNT(*) as feedback_count,
    COUNT(CASE WHEN is_recommended = TRUE THEN 1 END) as recommendation_rate
FROM department_feedback
WHERE hotel_id = 1
GROUP BY department_name
ORDER BY avg_rating DESC;

-- =====================================================
-- 8. 完成消息
-- =====================================================

SELECT
    '=== 团队成员数据库设置完成 ===' as message,
    '执行日期:' as info,
    CURDATE() as current_date;

SELECT
    '下一步:' as next_step,
    '1. 重启后端应用' as step1,
    '2. 在Swagger中测试新功能' as step2,
    '3. 验证数据完整性' as step3;