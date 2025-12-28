<template>
  <div class="task-center-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">任务中心</h1>
        <p class="page-subtitle">智能任务分配与管理中心</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="refreshTasks">
          <el-icon><Refresh /></el-icon>
          刷新任务
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="6" :md="3">
        <div class="stat-card total">
          <div class="stat-icon">
            <el-icon :size="24"><List /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalTasks }}</div>
            <div class="stat-label">总任务</div>
          </div>
        </div>
      </el-col>

      <el-col :xs="12" :sm="6" :md="3">
        <div class="stat-card pending">
          <div class="stat-icon">
            <el-icon :size="24"><Clock /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.pendingTasks }}</div>
            <div class="stat-label">待处理</div>
          </div>
        </div>
      </el-col>

      <el-col :xs="12" :sm="6" :md="3">
        <div class="stat-card completed">
          <div class="stat-icon">
            <el-icon :size="24"><Check /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.completedTasks }}</div>
            <div class="stat-label">已完成</div>
          </div>
        </div>
      </el-col>

      <el-col :xs="12" :sm="6" :md="3">
        <div class="stat-card ai">
          <div class="stat-icon">
            <el-icon :size="24"><Robot /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.aiProcessed }}</div>
            <div class="stat-label">AI处理</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 筛选和搜索 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filters">
        <el-form-item label="任务状态">
          <el-select v-model="filters.status" placeholder="全部状态" clearable @change="handleFilterChange">
            <el-option label="全部状态" value="" />
            <el-option label="待处理" value="PENDING" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>

        <el-form-item label="部门">
          <el-select v-model="filters.department" placeholder="全部部门" clearable @change="handleFilterChange">
            <el-option label="全部部门" value="" />
            <el-option label="房务部" value="房务部" />
            <el-option label="前台部" value="前台部" />
            <el-option label="业务部" value="业务部" />
            <el-option label="工程部" value="工程部" />
            <el-option label="餐饮部" value="餐饮部" />
          </el-select>
        </el-form-item>

        <el-form-item label="优先级">
          <el-select v-model="filters.priority" placeholder="全部优先级" clearable @change="handleFilterChange">
            <el-option label="全部优先级" value="" />
            <el-option label="高" value="HIGH" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="低" value="LOW" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-input
            v-model="filters.search"
            placeholder="搜索任务内容"
            clearable
            @input="handleFilterChange"
            style="width: 200px"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 任务列表 -->
    <el-card class="tasks-card">
      <template #header>
        <div class="card-header">
          <span>任务列表</span>
          <div class="header-stats">
            <el-tag size="small">共 {{ filteredTasks.length }} 个任务</el-tag>
          </div>
        </div>
      </template>

      <el-table :data="filteredTasks" stripe style="width: 100%">
        <el-table-column prop="taskId" label="任务ID" width="100" />
        <el-table-column prop="taskContent" label="任务内容" min-width="200" show-overflow-tooltip />

        <el-table-column label="优先级" width="80">
          <template #default="{ row }">
            <el-tag :type="getPriorityTagType(row.priority)">
              {{ getPriorityText(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="分配部门" width="120">
          <template #default="{ row }">
            <span>{{ row.assignedDepartment?.deptName || '未分配' }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="customerName" label="客户姓名" width="100" />
        <el-table-column prop="roomNumber" label="房间号" width="100" />

        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>

        <el-table-column prop="dueTime" label="期望完成时间" width="160">
          <template #default="{ row }">
            <span :class="{ 'overdue': isOverdue(row.dueTime) }">
              {{ formatDateTime(row.dueTime) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button-group>
              <el-button
                v-if="row.status === 'PENDING'"
                size="small"
                type="success"
                @click="acceptTask(row)"
              >
                接受
              </el-button>

              <el-button
                v-if="row.status === 'IN_PROGRESS'"
                size="small"
                type="primary"
                @click="completeTask(row)"
              >
                完成
              </el-button>

              <el-button
                v-if="row.status !== 'COMPLETED' && row.status !== 'CANCELLED'"
                size="small"
                type="danger"
                @click="cancelTask(row)"
              >
                取消
              </el-button>

              <el-button size="small" @click="viewTaskDetail(row)">
                <el-icon><View /></el-icon>
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 任务详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      :title="`任务详情 - ${selectedTask?.taskId}`"
      width="700px"
    >
      <div v-if="selectedTask" class="task-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="任务ID">{{ selectedTask.taskId }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(selectedTask.status)">
              {{ getStatusText(selectedTask.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag :type="getPriorityTagType(selectedTask.priority)">
              {{ getPriorityText(selectedTask.priority) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="分配部门">
            {{ selectedTask.assignedDepartment?.deptName || '未分配' }}
          </el-descriptions-item>
          <el-descriptions-item label="客户姓名">{{ selectedTask.customerName || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="房间号">{{ selectedTask.roomNumber || '无' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">
            {{ formatDateTime(selectedTask.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="期望完成时间" :span="2">
            {{ formatDateTime(selectedTask.dueTime) }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider>任务内容</el-divider>
        <div class="task-content">
          <p>{{ selectedTask.taskContent }}</p>
        </div>

        <el-divider v-if="selectedTask.status === 'COMPLETED'">完成信息</el-divider>
        <div v-if="selectedTask.status === 'COMPLETED'" class="completion-info">
          <p><strong>完成时间：</strong>{{ formatDateTime(selectedTask.completedAt) }}</p>
          <p><strong>处理人：</strong>{{ selectedTask.completedBy || '系统' }}</p>
        </div>

        <el-divider v-if="selectedTask.status === 'CANCELLED'">取消信息</el-divider>
        <div v-if="selectedTask.status === 'CANCELLED'" class="cancel-info">
          <p><strong>取消时间：</strong>{{ formatDateTime(selectedTask.cancelledAt) }}</p>
          <p><strong>取消原因：</strong>{{ selectedTask.cancelReason || '未说明' }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../api/index'
import {
  Refresh, List, Clock, Check, Setting, Search, View
} from '@element-plus/icons-vue'

// 状态
const loading = ref(false)
const detailVisible = ref(false)

// 数据
const tasks = ref<any[]>([])
const selectedTask = ref<any>(null)

// 统计数据
const stats = reactive({
  totalTasks: 0,
  pendingTasks: 0,
  completedTasks: 0,
  aiProcessed: 0
})

// 筛选条件
const filters = reactive({
  status: '',
  department: '',
  priority: '',
  search: ''
})

// 过滤后的任务
const filteredTasks = computed(() => {
  let result = tasks.value

  if (filters.status) {
    result = result.filter(task => task.status === filters.status)
  }

  if (filters.department) {
    result = result.filter(task =>
      task.assignedDepartment?.deptName === filters.department
    )
  }

  if (filters.priority) {
    result = result.filter(task => task.priority === filters.priority)
  }

  if (filters.search) {
    const search = filters.search.toLowerCase()
    result = result.filter(task =>
      task.taskContent?.toLowerCase().includes(search) ||
      task.customerName?.toLowerCase().includes(search) ||
      task.taskId?.toString().includes(search)
    )
  }

  return result
})

// 获取状态标签类型
const getStatusTagType = (status: string) => {
  const typeMap: Record<string, string> = {
    PENDING: 'warning',
    IN_PROGRESS: 'primary',
    COMPLETED: 'success',
    CANCELLED: 'danger'
  }
  return typeMap[status] || ''
}

// 获取状态文本
const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    PENDING: '待处理',
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return textMap[status] || status
}

// 获取优先级标签类型
const getPriorityTagType = (priority: string) => {
  const typeMap: Record<string, string> = {
    HIGH: 'danger',
    MEDIUM: 'warning',
    LOW: 'info'
  }
  return typeMap[priority] || 'info'
}

// 获取优先级文本
const getPriorityText = (priority: string) => {
  const textMap: Record<string, string> = {
    HIGH: '高',
    MEDIUM: '中',
    LOW: '低'
  }
  return textMap[priority] || priority
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 检查是否逾期
const isOverdue = (dueTime: string) => {
  if (!dueTime) return false
  return new Date(dueTime) < new Date()
}

// 加载任务列表
const loadTasks = async () => {
  loading.value = true
  try {
    const response = await api.get('/api/v1/personalization/tasks/pending')

    if (response.data && response.data.data) {
      let data = response.data.data

      // 按状态筛选
      if (filters.status) {
        data = data.filter((t: any) => t.status === filters.status)
      }

      // 按部门筛选
      if (filters.department) {
        data = data.filter((t: any) =>
          t.assignedDepartment?.deptName === filters.department
        )
      }

      tasks.value = data

      // 更新统计
      stats.totalTasks = data.length
      stats.pendingTasks = data.filter((t: any) => t.status === 'PENDING').length
      stats.completedTasks = data.filter((t: any) => t.status === 'COMPLETED').length
      stats.aiProcessed = data.length // 所有通过系统的都是AI处理的
    }
  } catch (error: any) {
    console.error('加载任务列表失败:', error)
    ElMessage.error('加载任务列表失败')
  } finally {
    loading.value = false
  }
}

// 接受任务
const acceptTask = async (task: any) => {
  try {
    await ElMessageBox.confirm(
      `确认接受任务 "${task.taskContent}" 吗？`,
      '确认接受',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await api.post(`/api/v1/department-tasks/accept/${task.taskId}`, {
      departmentId: 1 // 临时使用部门ID，后续可以改进
    })

    if (response.data && response.data.data) {
      ElMessage.success('任务接受成功')
      await loadTasks()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('接受任务失败:', error)
      ElMessage.error(error.response?.data?.message || '接受任务失败')
    }
  }
}

// 完成任务
const completeTask = async (task: any) => {
  try {
    const result = await ElMessageBox.prompt('请输入完成备注', '完成任务', {
      confirmButtonText: '完成',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '请输入完成备注'
    })

    const response = await api.post(`/api/v1/department-tasks/complete/${task.taskId}`, {
      completionNote: result.value,
      departmentId: 1
    })

    if (response.data && response.data.data) {
      ElMessage.success('任务完成成功')
      await loadTasks()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('完成任务失败:', error)
      ElMessage.error(error.response?.data?.message || '完成任务失败')
    }
  }
}

// 取消任务
const cancelTask = async (task: any) => {
  try {
    const result = await ElMessageBox.prompt('请输入取消原因', '取消任务', {
      confirmButtonText: '取消任务',
      cancelButtonText: '返回',
      inputPattern: /.+/,
      inputErrorMessage: '请输入取消原因'
    })

    const response = await api.put(`/api/v1/personalization/tasks/${task.taskId}/cancel`, null, {
      params: { cancelReason: result.value }
    })

    if (response.data && response.data.data) {
      ElMessage.success('任务取消成功')
      await loadTasks()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('取消任务失败:', error)
      ElMessage.error(error.response?.data?.message || '取消任务失败')
    }
  }
}

// 查看任务详情
const viewTaskDetail = (task: any) => {
  selectedTask.value = task
  detailVisible.value = true
}

// 筛选改变
const handleFilterChange = () => {
  // 筛选条件改变时重新过滤
}

// 刷新任务
const refreshTasks = async () => {
  await loadTasks()
  ElMessage.success('任务列表已刷新')
}

// 初始化
onMounted(() => {
  loadTasks()
})
</script>

<style scoped>
.task-center-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.page-subtitle {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 20px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
}

.stat-card.total .stat-icon {
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
}

.stat-card.pending .stat-icon {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}

.stat-card.completed .stat-icon {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.stat-card.ai .stat-icon {
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1;
}

.stat-label {
  font-size: 12px;
  color: #64748b;
  margin-top: 4px;
}

.filter-card, .tasks-card {
  margin-bottom: 20px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.header-stats {
  display: flex;
  gap: 8px;
}

.task-detail {
  padding: 16px 0;
}

.task-content {
  background: #f8fafc;
  padding: 16px;
  border-radius: 8px;
  margin: 16px 0;
}

.task-content p {
  margin: 0;
  line-height: 1.6;
  color: #374151;
}

.completion-info, .cancel-info {
  background: #f0fdf4;
  padding: 16px;
  border-radius: 8px;
  border-left: 4px solid #10b981;
}

.cancel-info {
  background: #fef2f2;
  border-left-color: #ef4444;
}

.overdue {
  color: #ef4444;
  font-weight: 600;
}

/* 响应式 */
@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
}
</style>
