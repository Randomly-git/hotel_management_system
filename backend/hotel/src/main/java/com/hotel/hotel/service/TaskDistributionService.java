package com.hotel.hotel.service;

import com.hotel.hotel.entity.TaskOrder;
import com.hotel.hotel.entity.Department;
import com.hotel.hotel.repository.DepartmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 任务分发服务
 * 负责将task_order表的任务分发到department_task表
 */
@Slf4j
@Service
public class TaskDistributionService {

    @Autowired
    private DepartmentTaskManagementService departmentTaskService;

    @Autowired
    private DepartmentRepository departmentRepository;

    /**
     * 自动分发任务到部门
     * 当客户请求创建任务后，自动调用此方法
     */
    public void distributeTaskToDepartment(TaskOrder taskOrder) {
        try {
            log.info("开始分发任务到部门: taskId={}, assignedDept={}",
                taskOrder.getTaskId(),
                taskOrder.getAssignedDepartment() != null ? taskOrder.getAssignedDepartment().getDeptName() : "未分配");

            // 如果任务已经分配了部门，创建部门任务
            if (taskOrder.getAssignedDepartment() != null) {
                // 创建简化的NlpResult对象
                ZhipuNlpService.NlpResult nlpResult = createNlpResultFromTask(taskOrder);

                // 调用部门任务管理服务创建部门任务
                departmentTaskService.createDepartmentTask(taskOrder, nlpResult);
                log.info("任务分发成功: taskId={}, dept={}",
                    taskOrder.getTaskId(), taskOrder.getAssignedDepartment().getDeptName());
            } else {
                log.warn("任务未分配部门，跳过分发: taskId={}", taskOrder.getTaskId());
            }

        } catch (Exception e) {
            log.error("任务分发失败: taskId={}", taskOrder.getTaskId(), e);
            // 分发失败不影响主流程，继续运行
        }
    }

    /**
     * 根据TaskOrder创建简化的NlpResult
     */
    private ZhipuNlpService.NlpResult createNlpResultFromTask(TaskOrder taskOrder) {
        ZhipuNlpService.NlpResult nlpResult = new ZhipuNlpService.NlpResult();

        // 从任务内容中解析信息
        String taskContent = taskOrder.getTaskContent();
        if (taskContent != null && taskContent.startsWith("客户请求: ")) {
            String requestContent = taskContent.substring("客户请求: ".length());
            nlpResult.setDescription(requestContent);
            nlpResult.setOriginalRequest(requestContent);
        } else {
            nlpResult.setDescription(taskContent);
            nlpResult.setOriginalRequest(taskContent);
        }

        // 设置部门信息
        if (taskOrder.getAssignedDepartment() != null) {
            nlpResult.setRecommendedDepartment(taskOrder.getAssignedDepartment().getDeptName());
        }

        // 设置房间号
        nlpResult.setRoomNumber(taskOrder.getRoomNumber());

        // 设置意图（根据部门推断）
        String deptName = taskOrder.getAssignedDepartment() != null ?
            taskOrder.getAssignedDepartment().getDeptName() : "";
        nlpResult.setIntent(inferIntentFromDepartment(deptName));

        // 设置紧急度
        nlpResult.setUrgency("MEDIUM");

        return nlpResult;
    }

    /**
     * 根据部门名称推断意图
     */
    private String inferIntentFromDepartment(String deptName) {
        if (deptName == null) return "UNKNOWN";

        switch (deptName) {
            case "工程部":
                return "MAINTENANCE";
            case "服务部":
                return "CLEANING";
            case "餐饮部":
                return "FOOD";
            case "安保部":
                return "SECURITY";
            default:
                return "UNKNOWN";
        }
    }
}