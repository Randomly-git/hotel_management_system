-- 临时修复：创建一个视图来映射表名
CREATE OR REPLACE VIEW TaskOrder AS
SELECT
    task_id AS taskId,
    hotel_id AS hotelId,
    guest_member_id AS guestMemberId,
    assigned_dept_id AS assignedDeptId,
    task_type AS taskType,
    task_content AS taskContent,
    status AS status,
    room_number AS roomNumber,
    create_time AS createTime,
    due_time AS dueTime,
    completed_time AS completedTime
FROM task_order;

-- 测试查询
SELECT * FROM TaskOrder LIMIT 5;