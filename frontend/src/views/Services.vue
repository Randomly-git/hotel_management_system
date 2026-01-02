<template>
  <div class="services">
    <div class="page-header">
      <h1>个性化服务</h1>
      <el-button type="primary" @click="showNewRequestDialog = true">
        <el-icon><Plus /></el-icon>
        新建服务请求
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
              <el-icon :size="24"><List /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalTasks }}</div>
              <div class="stat-label">总任务数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
              <el-icon :size="24"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pendingTasks }}</div>
              <div class="stat-label">待处理</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
              <el-icon :size="24"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.completedTasks }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)">
              <el-icon :size="24"><MagicStick /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.aiProcessed }}</div>
              <div class="stat-label">AI处理</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 任务列表 -->
    <el-card class="task-list-card">
      <template #header>
        <div class="card-header">
          <div class="header-title">
            <el-icon><List /></el-icon>
            <span>服务任务列表</span>
          </div>
          <div class="header-filters">
            <el-select v-model="taskFilters.status" placeholder="全部状态" clearable @change="loadTasks" style="width: 120px; margin-right: 10px">
              <el-option label="全部" value="" />
              <el-option label="待处理" value="PENDING" />
              <el-option label="进行中" value="IN_PROGRESS" />
              <el-option label="已完成" value="COMPLETED" />
              <el-option label="已取消" value="CANCELLED" />
            </el-select>
            <el-select v-model="taskFilters.department" placeholder="全部部门" clearable @change="loadTasks" style="width: 150px">
              <el-option label="全部" value="" />
              <el-option label="房务部" value="房务部" />
              <el-option label="餐饮部" value="餐饮部" />
              <el-option label="工程部" value="工程部" />
              <el-option label="前台部" value="前台部" />
            </el-select>
          </div>
        </div>
      </template>

      <el-table :data="tasks" v-loading="loading" stripe>
        <el-table-column prop="taskId" label="任务ID" width="80" />
        <el-table-column prop="taskContent" label="任务内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="assignedDepartment" label="分配部门" width="120">
          <template #default="{ row }">
            <el-tag>{{ row.assignedDepartment?.deptName || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="customerId" label="客户ID" width="100" />
        <el-table-column prop="roomNumber" label="房间号" width="100" />
        <el-table-column prop="priority" label="优先级" width="90">
          <template #default="{ row }">
            <el-tag :type="getPriorityType(row.priority)" size="small">
              {{ row.priority || '普通' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="viewTaskDetails(row)">详情</el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              link
              type="success"
              size="small"
              @click="updateTaskStatus(row, 'IN_PROGRESS')"
            >
              开始
            </el-button>
            <el-button
              v-if="row.status === 'IN_PROGRESS'"
              link
              type="success"
              size="small"
              @click="updateTaskStatus(row, 'COMPLETED')"
            >
              完成
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新建服务请求对话框 -->
    <el-dialog v-model="showNewRequestDialog" title="新建服务请求" width="600px">
      <el-form :model="requestForm" :rules="requestRules" ref="requestFormRef" label-width="100px">
        <el-form-item label="客户ID" prop="customerId">
          <el-input v-model="requestForm.customerId" placeholder="请输入客户ID" />
        </el-form-item>
        <el-form-item label="客户姓名">
          <el-input v-model="requestForm.customerName" placeholder="请输入客户姓名（可选）" />
        </el-form-item>
        <el-form-item label="房间号">
          <el-input v-model="requestForm.roomNumber" placeholder="请输入房间号（可选）" />
        </el-form-item>
        <el-form-item label="服务请求" prop="content">
          <el-input
            v-model="requestForm.content"
            type="textarea"
            :rows="4"
            placeholder="请用自然语言描述客户的服务需求，例如：我房间空调坏了，能派人来看一下吗？"
          />
          <div class="ai-hint">
            <el-icon><MagicStick /></el-icon>
            <span>AI将自动解析您的需求并分配给相应部门</span>
          </div>
        </el-form-item>
        <el-form-item label="期望时间">
          <el-date-picker
            v-model="requestForm.dueTime"
            type="datetime"
            placeholder="选择期望解决时间（可选）"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showNewRequestDialog = false">取消</el-button>
        <el-button type="primary" @click="submitRequest" :loading="submitting">
          <el-icon><MagicStick /></el-icon>
          AI解析并提交
        </el-button>
      </template>
    </el-dialog>

    <!-- 任务详情对话框 -->
    <el-dialog v-model="showDetailsDialog" title="任务详情" width="700px">
      <div v-if="currentTask" class="task-details">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="任务ID">
            {{ currentTask.taskId }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentTask.status)">
              {{ getStatusText(currentTask.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="分配部门">
            {{ currentTask.assignedDepartment?.deptName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag :type="getPriorityType(currentTask.priority)" size="small">
              {{ currentTask.priority || '普通' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="客户ID">
            {{ currentTask.customerId }}
          </el-descriptions-item>
          <el-descriptions-item label="房间号">
            {{ currentTask.roomNumber || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">
            {{ formatDateTime(currentTask.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="任务内容" :span="2">
            {{ currentTask.taskContent }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentTask.completedAt" label="完成时间" :span="2">
            {{ formatDateTime(currentTask.completedAt) }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentTask.notes" label="备注" :span="2">
            {{ currentTask.notes }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- AI解析结果对话框 -->
    <el-dialog v-model="showAIResultDialog" title="AI解析结果" width="500px">
      <div v-if="aiResult" class="ai-result">
        <el-alert type="success" :closable="false" show-icon>
          <template #title>
            <div style="display: flex; align-items: center; gap: 8px;">
              <el-icon><MagicStick /></el-icon>
              <span>AI智能解析成功</span>
            </div>
          </template>
        </el-alert>

        <el-descriptions :column="1" border style="margin-top: 20px">
          <el-descriptions-item label="识别意图">
            <el-tag>{{ aiResult.intent }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="分配部门">
            <el-tag type="success">{{ aiResult.recommendedDepartment }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag :type="getPriorityType(aiResult.urgency)">{{ aiResult.urgency || '普通' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="任务描述">
            {{ aiResult.description }}
          </el-descriptions-item>
          <el-descriptions-item v-if="aiResult.confidence" label="置信度">
            <el-progress :percentage="aiResult.confidence" :color="'#67c23a'" />
          </el-descriptions-item>
        </el-descriptions>

        <div class="result-actions">
          <el-button type="primary" @click="confirmAIResult">确认创建任务</el-button>
          <el-button @click="showAIResultDialog = false">取消</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import api from '../api/index'
import { Plus, List, Clock, CircleCheck, MagicStick } from '@element-plus/icons-vue'

// 状态
const loading = ref(false)
const submitting = ref(false)
const tasks = ref<any[]>([])

// 统计数据
const stats = reactive({
  totalTasks: 0,
  pendingTasks: 0,
  completedTasks: 0,
  aiProcessed: 0
})

// 筛选条件
const taskFilters = reactive({
  status: '',
  department: ''
})

// 对话框
const showNewRequestDialog = ref(false)
const showDetailsDialog = ref(false)
const showAIResultDialog = ref(false)
const currentTask = ref<any>(null)
const requestFormRef = ref<FormInstance>()
const aiResult = ref<any>(null)

// 服务请求表单
const requestForm = reactive({
  customerId: '',
  customerName: '',
  roomNumber: '',
  content: '',
  dueTime: '',
  hotelId: 1
})

const requestRules: FormRules = {
  customerId: [{ required: true, message: '请输入客户ID', trigger: 'blur' }],
  content: [{ required: true, message: '请描述服务需求', trigger: 'blur' }]
}

// 获取状态类型
const getStatusType = (status: string) => {
  const map: Record<string, any> = {
    PENDING: 'warning',
    IN_PROGRESS: 'primary',
    COMPLETED: 'success',
    CANCELLED: 'danger'
  }
  return map[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    PENDING: '待处理',
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return map[status] || status
}

// 获取优先级类型
const getPriorityType = (priority: string) => {
  const map: Record<string, any> = {
    high: 'danger',
    medium: 'warning',
    low: 'info'
  }
  return map[priority] || 'info'
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 加载任务列表
const loadTasks = async () => {
  loading.value = true
  try {
    let url = `/api/v1/personalization/tasks/pending`

    const response = await api.get(url)

    if (response.data && response.data.data) {
      let data = response.data.data

      // 映射后端字段名到前端期望的字段名
      data = data.map((t: any) => ({
        ...t,
        customerId: t.guestMemberId,  // 映射 guestMemberId -> customerId
        createdAt: t.createTime       // 映射 createTime -> createdAt
      }))

      // 按状态筛选
      if (taskFilters.status) {
        data = data.filter((t: any) => t.status === taskFilters.status)
      }

      // 按部门筛选
      if (taskFilters.department) {
        data = data.filter((t: any) =>
          t.assignedDepartment?.deptName === taskFilters.department
        )
      }

      tasks.value = data

      // 从统计API获取准确的统计数据，而不是从当前列表计算
      await loadTaskStatistics()
    }
  } catch (error: any) {
    console.error('加载任务列表失败:', error)
    ElMessage.error('加载任务列表失败')
  } finally {
    loading.value = false
  }
}

// 加载任务统计
const loadTaskStatistics = async () => {
  try {
    const response = await api.get('/api/v1/personalization/tasks/statistics')
    if (response.data && response.data.data) {
      const statistics = response.data.data
      stats.totalTasks = statistics.totalTasks || 0
      stats.pendingTasks = statistics.pendingTasks || 0
      stats.completedTasks = statistics.completedTasks || 0
      stats.aiProcessed = statistics.totalTasks || 0
    }
  } catch (error: any) {
    console.error('加载任务统计失败:', error)
    // 如果统计API失败，回退到从当前列表计算
    stats.totalTasks = tasks.value.length
    stats.pendingTasks = tasks.value.filter((t: any) => t.status === 'PENDING').length
    stats.completedTasks = tasks.value.filter((t: any) => t.status === 'COMPLETED').length
    stats.aiProcessed = tasks.value.length
  }
}

// 查看任务详情
const viewTaskDetails = async (task: any) => {
  try {
    const response = await api.get(`/api/v1/personalization/tasks/${task.taskId}`)
    if (response.data && response.data.data) {
      currentTask.value = response.data.data
      showDetailsDialog.value = true
    }
  } catch (error) {
    console.error('获取任务详情失败:', error)
    currentTask.value = task
    showDetailsDialog.value = true
  }
}

// 更新任务状态
const updateTaskStatus = async (task: any, status: string) => {
  try {
    await api.put(`/api/v1/personalization/tasks/${task.taskId}/status?status=${status}`)
    ElMessage.success('任务状态已更新')
    loadTasks()
  } catch (error: any) {
    console.error('更新任务状态失败:', error)
    ElMessage.error(error.response?.data?.message || '更新失败')
  }
}

// 提交服务请求
const submitRequest = async () => {
  if (!requestFormRef.value) return

  try {
    await requestFormRef.value.validate()
    submitting.value = true

    const response = await api.post(`/api/v1/personalization/request`, requestForm)

    if (response.data && response.data.data) {
      const result = response.data.data

      // 显示AI解析结果
      aiResult.value = {
        intent: result.nlpAnalysis?.intent || 'GENERAL',
        recommendedDepartment: result.assignedTo,
        urgency: result.nlpAnalysis?.urgency || 'medium',
        description: result.nlpAnalysis?.description || requestForm.content,
        confidence: result.nlpAnalysis?.confidence || 85,
        taskId: result.taskId
      }

      showNewRequestDialog.value = false
      showAIResultDialog.value = true

      ElMessage.success('AI解析成功，任务已创建')
      loadTasks()
    }
  } catch (error: any) {
    console.error('提交请求失败:', error)
    ElMessage.error(error.response?.data?.message || '提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 确认AI解析结果
const confirmAIResult = () => {
  showAIResultDialog.value = false

  // 重置表单
  if (requestFormRef.value) {
    requestFormRef.value.resetFields()
  }

  ElMessage.success(`任务 ${aiResult.value.taskId} 已成功创建`)
}

// 初始化
onMounted(() => {
  loadTasks()
})
</script>

<style scoped>
.services {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  color: #303133;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 12px;
  overflow: hidden;
}

.stat-card :deep(.el-card__body) {
  padding: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
  margin-top: 4px;
}

.task-list-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.header-filters {
  display: flex;
  gap: 10px;
}

.ai-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  padding: 8px 12px;
  background: linear-gradient(135deg, #e0f2fe 0%, #bae6fd 100%);
  border-radius: 6px;
  color: #0369a1;
  font-size: 12px;
}

.task-details {
  padding: 10px;
}

.ai-result {
  padding: 10px;
}

.result-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 24px;
}

/* Element Plus 样式优化 */
:deep(.el-card__body) {
  padding: 20px;
}

:deep(.el-card__header) {
  border-bottom: 1px solid #e5e7eb;
  padding: 16px 20px;
}

:deep(.el-table) {
  border-radius: 8px;
}

:deep(.el-descriptions__label) {
  font-weight: 600;
}
</style>
