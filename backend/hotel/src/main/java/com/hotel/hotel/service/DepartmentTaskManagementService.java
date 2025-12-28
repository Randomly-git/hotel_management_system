package com.hotel.hotel.service;

import com.hotel.hotel.entity.DepartmentTask;
import com.hotel.hotel.entity.TaskOrder;
import com.hotel.hotel.entity.Department;
import com.hotel.hotel.repository.DepartmentTaskRepository;
import com.hotel.hotel.repository.DepartmentRepository;
import com.hotel.hotel.repository.TaskOrderRepository;
import com.hotel.hotel.service.ZhipuNlpService.NlpResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class DepartmentTaskManagementService {

    @Autowired
    private DepartmentTaskRepository departmentTaskRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TaskOrderRepository taskOrderRepository;

    /**
     * 根据NLP解析结果和任务单创建部门任务
     */
    @Transactional
    public DepartmentTask createDepartmentTask(TaskOrder taskOrder, NlpResult nlpResult) {
        log.info("开始创建部门任务: taskOrderId={}, deptName={}",
            taskOrder.getTaskId(), nlpResult.getRecommendedDepartment());

        try {
            // 1. 查找部门信息（优先使用hotelId进行租户隔离查找）
            Long hotelId = taskOrder.getHotelId();
            var departmentOpt = departmentRepository.findByHotelIdAndDeptName(String.valueOf(hotelId), nlpResult.getRecommendedDepartment());
            Department department;

            if (departmentOpt.isEmpty()) {
                log.warn("找不到酒店{}的部门：{}，尝试全局查找", hotelId, nlpResult.getRecommendedDepartment());
                // 降级：全局查找部门
                var globalDeptOpt = departmentRepository.findByDeptName(nlpResult.getRecommendedDepartment());
                if (globalDeptOpt.isPresent()) {
                    department = globalDeptOpt.get();
                } else {
                    log.warn("找不到部门: {}，使用默认部门（业务部）", nlpResult.getRecommendedDepartment());
                    // 使用业务部作为默认部门
                    department = departmentRepository.findByHotelIdAndDeptName(String.valueOf(hotelId), "业务部")
                            .orElseGet(() -> getDefaultDepartment(hotelId));
                    nlpResult.setRecommendedDepartment(department.getDeptName());
                }
            } else {
                department = departmentOpt.get();
            }

            // 2. 创建部门任务
            DepartmentTask departmentTask = DepartmentTask.builder()
                .taskOrderId(taskOrder.getTaskId())
                .departmentId(department.getDeptId())
                .taskTitle(generateTaskTitle(nlpResult))
                .taskDescription(nlpResult.getDescription())
                .priority(mapNlpUrgencyToPriority(nlpResult.getUrgency()))
                .status(DepartmentTask.TaskStatus.ASSIGNED)
                .roomNumber(taskOrder.getRoomNumber())
                .customerId(taskOrder.getGuestMemberId())
                .expectedCompletionTime(taskOrder.getDueTime())
                .createdBy("SYSTEM_AUTO") // 系统自动创建
                .hotelId(taskOrder.getHotelId())
                .build();

            // 3. 保存部门任务
            DepartmentTask savedTask = departmentTaskRepository.save(departmentTask);
            log.info("部门任务保存成功: taskId={}, department={}, priority={}",
                savedTask.getId(), department.getDeptName(), savedTask.getPriority());

            return savedTask;

        } catch (Exception e) {
            log.error("创建部门任务失败", e);
            throw new RuntimeException("创建部门任务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成任务标题
     */
    private String generateTaskTitle(NlpResult nlpResult) {
        String baseTitle = nlpResult.getDescription();
        String roomInfo = nlpResult.getRoomNumber() != null ? " - " + nlpResult.getRoomNumber() + "房" : "";

        // 根据意图类型生成不同的标题
        switch (nlpResult.getIntent()) {
            case "MAINTENANCE":
                return "设备维修：" + baseTitle + roomInfo;
            case "CLEANING":
                return "清洁服务：" + baseTitle + roomInfo;
            case "FOOD":
                return "餐饮服务：" + baseTitle + roomInfo;
            case "SUPPLIES":
                return "物品补充：" + baseTitle + roomInfo;
            default:
                return "客户需求：" + baseTitle + roomInfo;
        }
    }

    /**
     * 将NLP紧急度映射到任务优先级
     */
    private DepartmentTask.TaskPriority mapNlpUrgencyToPriority(String nlpUrgency) {
        if (nlpUrgency == null) {
            return DepartmentTask.TaskPriority.MEDIUM;
        }

        switch (nlpUrgency.toUpperCase()) {
            case "LOW":
                return DepartmentTask.TaskPriority.LOW;
            case "MEDIUM":
                return DepartmentTask.TaskPriority.MEDIUM;
            case "HIGH":
                return DepartmentTask.TaskPriority.HIGH;
            case "URGENT":
                return DepartmentTask.TaskPriority.URGENT;
            default:
                return DepartmentTask.TaskPriority.MEDIUM;
        }
    }

    /**
     * 获取部门任务统计
     */
    public Map<String, Object> getDepartmentTaskStatistics(Long hotelId) {
        Map<String, Object> statistics = new HashMap<>();

        try {
            // 按状态统计
            List<Object[]> statusStats = departmentTaskRepository.countTasksByStatus(hotelId);
            Map<String, Long> statusCount = new HashMap<>();
            for (Object[] row : statusStats) {
                statusCount.put(row[0].toString(), (Long) row[1]);
            }
            statistics.put("statusCount", statusCount);

            // 统计总数
            Long totalTasks = statusCount.values().stream().mapToLong(Long::longValue).sum();
            statistics.put("totalTasks", totalTasks);

            // 查询超时任务
            List<DepartmentTask> overdueTasks = departmentTaskRepository.findOverdueTasks(
                LocalDateTime.now(), hotelId);
            statistics.put("overdueTasks", overdueTasks.size());

            return statistics;

        } catch (Exception e) {
            log.error("获取部门任务统计失败", e);
            throw new RuntimeException("获取统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取待处理任务
     */
    public List<DepartmentTask> getPendingTasks(Long hotelId) {
        return departmentTaskRepository.findPendingTasks(hotelId);
    }

    /**
     * 获取超时任务
     */
    public List<DepartmentTask> getOverdueTasks(Long hotelId) {
        return departmentTaskRepository.findOverdueTasks(LocalDateTime.now(), hotelId);
    }

    /**
     * 根据部门获取任务
     */
    public List<DepartmentTask> getTasksByDepartment(Long hotelId, String departmentName) {
        var department = departmentRepository.findByDeptName(departmentName);
        if (department.isEmpty()) {
            throw new RuntimeException("找不到部门: " + departmentName);
        }

        return departmentTaskRepository.findByHotelIdAndDepartmentId(hotelId, department.get().getDeptId());
    }

    /**
     * 更新任务状态
     */
    @Transactional
    public DepartmentTask updateTaskStatus(Long taskId, DepartmentTask.TaskStatus status) {
        DepartmentTask task = departmentTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在: " + taskId));

        task.setStatus(status);

        // 如果任务完成，记录实际完成时间
        if (status == DepartmentTask.TaskStatus.COMPLETED) {
            task.setActualCompletionTime(LocalDateTime.now());
        }

        return departmentTaskRepository.save(task);
    }

    /**
     * 分配任务给员工
     */
    @Transactional
    public DepartmentTask assignTask(Long taskId, String assignedTo) {
        DepartmentTask task = departmentTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在: " + taskId));

        task.setAssignedTo(assignedTo);
        task.setStatus(DepartmentTask.TaskStatus.IN_PROGRESS);

        return departmentTaskRepository.save(task);
    }

    /**
     * 获取默认部门（业务部）
     */
    private Department getDefaultDepartment(Long hotelId) {
        var deptOpt = departmentRepository.findByHotelIdAndDeptName(String.valueOf(hotelId), "业务部");
        if (deptOpt.isPresent()) {
            return deptOpt.get();
        }

        // 如果业务部也不存在，创建一个默认的部门实体
        log.warn("酒店{}中未找到业务部，使用虚拟部门", hotelId);
        Department defaultDept = new Department();
        defaultDept.setDeptId(1L); // 假设业务部ID为1
        defaultDept.setDeptName("业务部");
        defaultDept.setHotelId(String.valueOf(hotelId));
        return defaultDept;
    }

    /**
     * 管理端完成任务 - 同时更新task_order和department_task
     */
    @Transactional
    public void completeTaskWithTaskOrder(Long departmentTaskId, String completionRemark) {
        log.info("管理端完成任务开始: departmentTaskId={}", departmentTaskId);

        // 1. 查找部门任务
        DepartmentTask departmentTask = departmentTaskRepository.findById(departmentTaskId)
                .orElseThrow(() -> new RuntimeException("部门任务不存在: " + departmentTaskId));

        // 2. 更新部门任务状态和完成时间
        departmentTask.setStatus(DepartmentTask.TaskStatus.COMPLETED);
        departmentTask.setActualCompletionTime(LocalDateTime.now());
        if (completionRemark != null && !completionRemark.trim().isEmpty()) {
            departmentTask.setCompletionRemark(completionRemark);
        }
        departmentTaskRepository.save(departmentTask);
        log.info("部门任务更新完成: taskId={}, status={}", departmentTaskId, departmentTask.getStatus());

        // 3. 更新对应的task_order状态
        if (departmentTask.getTaskOrderId() != null) {
            Optional<TaskOrder> taskOrderOpt = taskOrderRepository.findById(departmentTask.getTaskOrderId());
            if (taskOrderOpt.isPresent()) {
                TaskOrder taskOrder = taskOrderOpt.get();
                taskOrder.setStatus("COMPLETED");
                taskOrderRepository.save(taskOrder);
                log.info("任务单状态更新完成: taskOrderId={}, status=COMPLETED", taskOrder.getTaskId());
            } else {
                log.warn("未找到对应的任务单: taskOrderId={}", departmentTask.getTaskOrderId());
            }
        }
    }
}