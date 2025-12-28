<template>
  <div class="services-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div>
        <h1>个性化服务中心</h1>
        <p class="subtitle">AI智能解析客户需求，自动分发任务</p>
      </div>
      <el-button type="primary" @click="showRequestDialog = true">
        <el-icon><Plus /></el-icon>
        新建客户请求
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#409EFF"><Clock /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalTasks || 0 }}</div>
              <div class="stat-label">总任务数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#E6A23C"><Loading /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.pendingTasks || 0 }}</div>
              <div class="stat-label">待处理</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#67C23A"><CircleCheck /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.completedTasks || 0 }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#F56C6C"><Timer /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.avgResponseTime || '0min' }}</div>
              <div class="stat-label">平均响应</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 筛选和搜索 -->
    <el-card class="filter-card">
      <el-form :inline="true">
        <el-form-item label="任务状态">
          <el-select v-model="filters.status" placeholder="全部状态" clearable @change="loadTasks">
            <el-option label="全部" value="" />
            <el-option label="待处理" value="PENDING" />
            <el-option label="处理中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELED" />
          </el-select>
        </el-form-item>
        <el-form-item label="分配部门">
          <el-select v-model="filters.department" placeholder="全部部门" clearable @change="loadTasks">
            <el-option label="全部" value="" />
            <el-option label="工程部" value="工程部" />
            <el-option label="服务部" value="服务部" />
            <el-option label="餐饮部" value="餐饮部" />
            <el-option label="前厅部" value="前厅部" />
            <el-option label="客房部" value="客房部" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadTasks">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 任务列表 -->
    <el-card class="table-card">
      <el-table :data="taskList" v-loading="loading" stripe>
        <el-table-column prop="taskId" label="任务ID" width="80" />
        <el-table-column prop="taskContent" label="任务内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="customerName" label="客户" width="120" />
        <el-table-column prop="roomNumber" label="房间号" width="100" />
        <el-table-column prop="assignedDepartment.deptName" label="分配部门" width="120">
          <template #default="{ row }">
            <el-tag :type="getDepartmentTagType(row.assignedDepartment?.deptName)">
              {{ row.assignedDepartment?.deptName || '未分配' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="{ row }">
            <el-tag :type="getPriorityTagType(row.priority)" effect="plain">
              {{ row.priority || '普通' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="viewDetail(row)">详情</el-button>
            <el-button link type="primary" size="small" @click="handleTask(row)" v-if="row.status === 'PENDING'">
              处理
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadTasks"
          @current-change="loadTasks"
        />
      </div>
    </el-card>

    <!-- 新建请求对话框 -->
    <el-dialog
      v-model="showRequestDialog"
      title="新建客户请求"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="requestForm" :rules="requestRules" ref="requestFormRef" label-width="100px">
        <el-form-item label="客户姓名" prop="customerName">
          <el-input v-model="requestForm.customerName" placeholder="请输入客户姓名" />
        </el-form-item>
        <el-form-item label="房间号" prop="roomNumber">
          <el-input v-model="requestForm.roomNumber" placeholder="请输入房间号，如：8801" />
        </el-form-item>
        <el-form-item label="请求内容" prop="content">
          <el-input
            v-model="requestForm.content"
            type="textarea"
            :rows="4"
            placeholder="请描述客户需求，例如：房间空调坏了，很热"
          />
          <div class="tip-text">
            <el-icon><InfoFilled /></el-icon>
            AI将自动解析您的请求并分配给相应部门
          </div>
        </el-form-item>
        <el-form-item label="期望时间" prop="dueTime">
          <el-date-picker
            v-model="requestForm.dueTime"
            type="datetime"
            placeholder="选择期望解决时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRequestDialog = false">取消</el-button>
        <el-button type="primary" @click="submitRequest" :loading="submitting">
          提交请求
        </el-button>
      </template>
    </el-dialog>

    <!-- 任务详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="任务详情" width="700px">
      <el-descriptions :column="2" border v-if="currentTask">
        <el-descriptions-item label="任务ID">{{ currentTask.taskId }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTagType(currentTask.status)">
            {{ getStatusLabel(currentTask.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="客户">{{ currentTask.customerName }}</el-descriptions-item>
        <el-descriptions-item label="房间号">{{ currentTask.roomNumber || '未指定' }}</el-descriptions-item>
        <el-descriptions-item label="分配部门">
          {{ currentTask.assignedDepartment?.deptName || '未分配' }}
        </el-descriptions-item>
        <el-descriptions-item label="优先级">
          <el-tag :type="getPriorityTagType(currentTask.priority)" effect="plain">
            {{ currentTask.priority || '普通' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="任务内容" :span="2">
          {{ currentTask.taskContent }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ currentTask.createdAt }}
        </el-descriptions-item>
        <el-descriptions-item label="期望时间">
          {{ currentTask.dueTime || '未指定' }}
        </el-descriptions-item>
        <el-descriptions-item label="完成时间" v-if="currentTask.completedAt">
          {{ currentTask.completedAt }}
        </el-descriptions-item>
      </el-descriptions>

      <div class="dialog-footer" style="margin-top: 20px; text-align: right">
        <el-button @click="showDetailDialog = false">关闭</el-button>
        <el-button type="primary" @click="handleTask(currentTask)" v-if="currentTask?.status === 'PENDING'">
          处理任务
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Clock, Loading, CircleCheck, Timer, InfoFilled } from '@element-plus/icons-vue'
import axios from 'axios'

// API基础地址
const API_BASE = 'http://localhost:8082/api/v1/personalization'

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const showRequestDialog = ref(false)
const showDetailDialog = ref(false)
const requestFormRef = ref<FormInstance>()
const currentTask = ref<any>(null)

// 统计数据
const statistics = reactive({
  totalTasks: 0,
  pendingTasks: 0,
  completedTasks: 0,
  avgResponseTime: '0min'
})

// 筛选条件
const filters = reactive({
  status: '',
  department: ''
})

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 任务列表
const taskList = ref<any[]>([])

// 请求表单
const requestForm = reactive({
  customerName: '',
  roomNumber: '',
  content: '',
  dueTime: '',
  hotelId: 1
})

// 表单验证规则
const requestRules: FormRules = {
  customerName: [{ required: true, message: '请输入客户姓名', trigger: 'blur' }],
  content: [{ required: true, message: '请输入请求内容', trigger: 'blur' }],
  roomNumber: [{ required: true, message: '请输入房间号', trigger: 'blur' }]
}

// 加载任务列表
const loadTasks = async () => {
  loading.value = true
  try {
    const params: any = {}
    if (filters.status) params.status = filters.status
    if (filters.department) params.department = filters.department

    // 这里使用待处理任务接口，实际应该有分页接口
    const response = await axios.get(`${API_BASE}/tasks/pending`, { params })
    if (response.data.code === 200) {
      taskList.value = response.data.data || []
      pagination.total = taskList.value.length
    }
  } catch (error: any) {
    ElMessage.error('加载任务列表失败: ' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    const response = await axios.get(`${API_BASE}/tasks/statistics`)
    if (response.data.code === 200) {
      Object.assign(statistics, response.data.data)
    }
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}

// 提交客户请求
const submitRequest = async () => {
  if (!requestFormRef.value) return

  await requestFormRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      const response = await axios.post(`${API_BASE}/request`, {
        customerId: '0', // 临时客户ID
        customerName: requestForm.customerName,
        roomNumber: requestForm.roomNumber,
        content: requestForm.content,
        dueTime: requestForm.dueTime || undefined,
        hotelId: requestForm.hotelId
      })

      if (response.data.code === 200 || response.status === 201) {
        ElMessage.success('请求提交成功！AI正在解析...')
        showRequestDialog.value = false
        resetForm()
        await loadTasks()
        await loadStatistics()
      } else {
        ElMessage.error('提交失败: ' + (response.data.message || '未知错误'))
      }
    } catch (error: any) {
      ElMessage.error('提交失败: ' + (error.response?.data?.message || error.message))
    } finally {
      submitting.value = false
    }
  })
}

// 查看详情
const viewDetail = async (task: any) => {
  try {
    const response = await axios.get(`${API_BASE}/tasks/${task.taskId}`)
    if (response.data.code === 200) {
      currentTask.value = response.data.data
      showDetailDialog.value = true
    }
  } catch (error: any) {
    ElMessage.error('获取详情失败: ' + (error.response?.data?.message || error.message))
  }
}

// 处理任务
const handleTask = async (task: any) => {
  try {
    await ElMessageBox.confirm('确认将此任务标记为处理中？', '确认操作', {
      type: 'warning'
    })

    const response = await axios.put(`${API_BASE}/tasks/${task.taskId}/status`, null, {
      params: { status: 'IN_PROGRESS' }
    })

    if (response.data.code === 200) {
      ElMessage.success('任务已开始处理')
      await loadTasks()
      await loadStatistics()
      showDetailDialog.value = false
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败: ' + (error.response?.data?.message || error.message))
    }
  }
}

// 重置表单
const resetForm = () => {
  requestFormRef.value?.resetFields()
}

// 重置筛选
const resetFilters = () => {
  filters.status = ''
  filters.department = ''
  loadTasks()
}

// 辅助函数
const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    'PENDING': '待处理',
    'IN_PROGRESS': '处理中',
    'COMPLETED': '已完成',
    'CANCELED': '已取消'
  }
  return map[status] || status
}

const getStatusTagType = (status: string) => {
  const map: Record<string, any> = {
    'PENDING': 'warning',
    'IN_PROGRESS': 'primary',
    'COMPLETED': 'success',
    'CANCELED': 'info'
  }
  return map[status] || ''
}

const getDepartmentTagType = (dept: string) => {
  const map: Record<string, any> = {
    '工程部': 'danger',
    '服务部': 'primary',
    '餐饮部': 'warning',
    '前厅部': 'success',
    '客房部': 'info'
  }
  return map[dept] || ''
}

const getPriorityTagType = (priority: string) => {
  const map: Record<string, any> = {
    '紧急': 'danger',
    '高': 'warning',
    '普通': 'info',
    '低': 'info'
  }
  return map[priority] || 'info'
}

// 初始化
onMounted(() => {
  loadTasks()
  loadStatistics()
})
</script>

<style scoped>
.services-container {
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
  font-size: 24px;
  color: #303133;
}

.subtitle {
  margin: 5px 0 0 0;
  font-size: 14px;
  color: #909399;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  font-size: 36px;
  margin-right: 15px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.filter-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-wrapper {
  margin-top: 20px;
  text-align: right;
}

.tip-text {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.tip-text .el-icon {
  margin-right: 5px;
}

.dialog-footer {
  border-top: 1px solid #ebeef5;
  padding-top: 15px;
}
</style>
