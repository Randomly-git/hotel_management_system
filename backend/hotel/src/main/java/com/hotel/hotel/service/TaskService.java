package com.hotel.hotel.service;

import com.hotel.hotel.entity.Department;
import com.hotel.hotel.entity.GuestProfile;
import com.hotel.hotel.entity.TaskOrder;
import com.hotel.hotel.repository.DepartmentRepository;
import com.hotel.hotel.repository.TaskOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {

    private final TaskOrderRepository taskOrderRepository;
    private final DepartmentRepository departmentRepository;
    private final GuestProfileService profileService;

    @Autowired
    public TaskService(TaskOrderRepository taskOrderRepository,
                       DepartmentRepository departmentRepository,
                       GuestProfileService profileService) {
        this.taskOrderRepository = taskOrderRepository;
        this.departmentRepository = departmentRepository;
        this.profileService = profileService;
    }

    /**
     * 核心方法：根据AI预测结果生成任务单
     * * @param memberId 客户会员ID
     * @param predictedNeed AI预测的客户潜在需求（如：需要安静房间, 需要儿童座椅）
     * @param predictedDeptName 归因的部门名称（如：房务部, 工程部）
     * @param dueMinutes 任务截止时间（分钟）
     * @return 生成的任务单实体
     */
    @Transactional
    public TaskOrder generateTaskFromPrediction(String memberId, String predictedNeed, String predictedDeptName, int dueMinutes) {

        // 1. 验证部门是否存在
        Department assignedDept = departmentRepository.findByDeptName(predictedDeptName)
                .orElseThrow(() -> new RuntimeException("任务分配部门不存在: " + predictedDeptName));

        // 2. 检查客户是否存在 (可选：可以检查 GuestProfile，这里简化为只用 MemberId)
        // profileService.getProfileByMemberId(memberId).orElseThrow(...)

        // 3. 构建任务单
        TaskOrder task = new TaskOrder();
        task.setGuestMemberId(memberId);
        task.setAssignedDepartment(assignedDept);
        task.setTaskType("PREDICTION"); // 标记为预测生成的任务
        task.setTaskContent("AI预测需求: " + predictedNeed);
        task.setStatus("PENDING");
        task.setCreateTime(LocalDateTime.now());
        task.setDueTime(LocalDateTime.now().plusMinutes(dueMinutes));

        // 4. 保存任务单
        return taskOrderRepository.save(task);
    }

    /**
     * 处理客户主动提交的请求，生成任务单（例如：APP/小程序请求）
     * @param memberId 客户会员ID
     * @param content 客户请求的内容
     * @param deptName 目标部门
     * @return 生成的任务单实体
     */
    @Transactional
    public TaskOrder generateTaskFromRequest(String memberId, String content, String deptName) {
        Department assignedDept = departmentRepository.findByDeptName(deptName)
                .orElseThrow(() -> new RuntimeException("任务分配部门不存在: " + deptName));

        TaskOrder task = new TaskOrder();
        task.setGuestMemberId(memberId);
        task.setAssignedDepartment(assignedDept);
        task.setTaskType("REQUEST");
        task.setTaskContent("客户请求: " + content);
        task.setStatus("PENDING");
        task.setCreateTime(LocalDateTime.now());
        // 客户请求一般需要尽快处理，设置一个默认截止时间
        task.setDueTime(LocalDateTime.now().plusMinutes(30));

        return taskOrderRepository.save(task);
    }

    /**
     * 更新任务单状态
     */
    @Transactional
    public TaskOrder updateTaskStatus(Long taskId, String newStatus) {
        TaskOrder task = taskOrderRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务单不存在: " + taskId));

        // 简单的状态机校验（生产环境应更复杂）
        if (List.of("PENDING", "IN_PROGRESS", "COMPLETED", "CANCELED").contains(newStatus)) {
            task.setStatus(newStatus);
            return taskOrderRepository.save(task);
        } else {
            throw new IllegalArgumentException("无效的任务状态: " + newStatus);
        }
    }

    /**
     * 查询所有待处理的任务单
     */
    public List<TaskOrder> getPendingTasks() {
        return taskOrderRepository.findByStatusOrderByCreateTimeDesc("PENDING");
    }
}