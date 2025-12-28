<template>
  <div class="reputation">
    <div class="page-header">
      <div class="header-left">
        <h1>声誉管理</h1>
        <p class="page-subtitle">客户反馈分析与部门绩效管理</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="showFeedbackDialog = true">
          <el-icon><Plus /></el-icon>
          提交反馈
        </el-button>
        <el-button @click="loadFeedbackList">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
              <el-icon :size="24"><ChatDotRound /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalFeedback }}</div>
              <div class="stat-label">总反馈数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
              <el-icon :size="24"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.averageScore }}</div>
              <div class="stat-label">平均情感得分</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
              <el-icon :size="24"><Star /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.positiveCount }}</div>
              <div class="stat-label">正面反馈</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)">
              <el-icon :size="24"><DataAnalysis /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.departments }}</div>
              <div class="stat-label">涉及部门</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 标签页：反馈列表和绩效分析 -->
    <el-tabs v-model="activeTab" class="main-tabs">
      <!-- 反馈列表 -->
      <el-tab-pane label="客户反馈" name="feedback">
        <el-card class="feedback-card">
          <template #header>
            <div class="card-header">
              <span>反馈列表</span>
              <el-button link type="primary" @click="loadFeedbackList">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
            </div>
          </template>

          <el-table :data="feedbackList" v-loading="loading" stripe>
            <el-table-column prop="feedbackId" label="反馈ID" width="100" />
            <el-table-column prop="customerName" label="客户姓名" width="120" />
            <el-table-column prop="feedbackContent" label="反馈内容" min-width="250" show-overflow-tooltip />
            <el-table-column prop="sentimentScore" label="情感得分" width="120">
              <template #default="{ row }">
                <el-tag :type="getScoreType(row.sentimentScore)" size="small">
                  {{ formatScore(row.sentimentScore) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="department" label="责任部门" width="150">
              <template #default="{ row }">
                <el-tag>{{ row.department?.deptName || '-' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="feedbackTime" label="反馈时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.feedbackTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="reviewStatus" label="审核状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getReviewStatusType(row.reviewStatus)">
                  {{ getReviewStatusText(row.reviewStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="viewFeedbackDetail(row)">
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 绩效分析 -->
      <el-tab-pane label="部门绩效" name="performance">
        <el-card class="performance-card">
          <template #header>
            <div class="card-header">
              <span>部门绩效分析</span>
              <div class="header-actions">
                <el-date-picker
                  v-model="performanceDateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  @change="loadPerformanceData"
                  style="margin-right: 10px"
                />
                <el-button type="primary" @click="calculatePerformance">
                  <el-icon><DataAnalysis /></el-icon>
                  计算绩效
                </el-button>
              </div>
            </div>
          </template>

          <el-table :data="performanceList" v-loading="performanceLoading" stripe>
            <el-table-column prop="department.deptName" label="部门名称" width="150" />
            <el-table-column prop="statisticsDate" label="统计日期" width="120">
              <template #default="{ row }">
                {{ formatDate(row.statisticsDate) }}
              </template>
            </el-table-column>
            <el-table-column prop="totalFeedbacks" label="反馈总数" width="100" />
            <el-table-column prop="averageScore" label="平均得分" width="120">
              <template #default="{ row }">
                <el-tag :type="getScoreType(row.averageScore)">
                  {{ formatScore(row.averageScore) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="positiveCount" label="正面反馈" width="100" />
            <el-table-column prop="negativeCount" label="负面反馈" width="100" />
            <el-table-column prop="aiSuggestion" label="AI改进建议" min-width="200" show-overflow-tooltip />
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="viewPerformanceDetail(row)">
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 提交反馈对话框 -->
    <el-dialog v-model="showFeedbackDialog" title="提交客户反馈" width="600px">
      <el-form :model="feedbackForm" :rules="feedbackRules" ref="feedbackFormRef" label-width="120px">
        <el-form-item label="客户姓名" prop="customerName">
          <el-input v-model="feedbackForm.customerName" placeholder="请输入客户姓名" />
        </el-form-item>
        <el-form-item label="反馈内容" prop="feedbackContent">
          <el-input
            v-model="feedbackForm.feedbackContent"
            type="textarea"
            :rows="5"
            placeholder="请输入客户反馈内容，系统将自动进行情感分析和部门归因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showFeedbackDialog = false">取消</el-button>
        <el-button type="primary" @click="submitFeedback" :loading="submitting">
          提交反馈
        </el-button>
      </template>
    </el-dialog>

    <!-- 反馈详情对话框 -->
    <el-dialog v-model="showFeedbackDetailDialog" title="反馈详情" width="700px">
      <div v-if="currentFeedback" class="feedback-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="反馈ID">{{ currentFeedback.feedbackId }}</el-descriptions-item>
          <el-descriptions-item label="客户姓名">{{ currentFeedback.customerName }}</el-descriptions-item>
          <el-descriptions-item label="情感得分" :span="2">
            <el-tag :type="getScoreType(currentFeedback.sentimentScore)" size="large">
              {{ formatScore(currentFeedback.sentimentScore) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="责任部门">
            {{ currentFeedback.department?.deptName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="反馈时间">
            {{ formatDateTime(currentFeedback.feedbackTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="审核状态">
            <el-tag :type="getReviewStatusType(currentFeedback.reviewStatus)">
              {{ getReviewStatusText(currentFeedback.reviewStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="反馈内容" :span="2">
            {{ currentFeedback.feedbackContent }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import {
  Plus, Refresh, ChatDotRound, TrendCharts, Star, DataAnalysis
} from '@element-plus/icons-vue'
import api from '../api/index'

// 状态
const loading = ref(false)
const performanceLoading = ref(false)
const submitting = ref(false)
const activeTab = ref('feedback')
const showFeedbackDialog = ref(false)
const showFeedbackDetailDialog = ref(false)
const feedbackList = ref<any[]>([])
const performanceList = ref<any[]>([])
const currentFeedback = ref<any>(null)
const feedbackFormRef = ref<FormInstance>()
const performanceDateRange = ref<[string, string] | null>(null)

// 统计数据
const stats = reactive({
  totalFeedback: 0,
  averageScore: 0,
  positiveCount: 0,
  departments: 0
})

// 反馈表单
const feedbackForm = reactive({
  customerName: '',
  feedbackContent: ''
})

const feedbackRules: FormRules = {
  customerName: [{ required: true, message: '请输入客户姓名', trigger: 'blur' }],
  feedbackContent: [{ required: true, message: '请输入反馈内容', trigger: 'blur' }]
}

// 获取得分类型
const getScoreType = (score: number | string) => {
  const numScore = typeof score === 'string' ? parseFloat(score) : score
  if (numScore >= 0.5) return 'success'
  if (numScore >= 0) return 'warning'
  return 'danger'
}

// 格式化得分
const formatScore = (score: number | string) => {
  const numScore = typeof score === 'string' ? parseFloat(score) : score
  return numScore.toFixed(2)
}

// 获取审核状态类型
const getReviewStatusType = (status: string) => {
  const map: Record<string, string> = {
    APPROVED: 'success',
    PENDING: 'warning',
    REJECTED: 'danger'
  }
  return map[status] || 'info'
}

// 获取审核状态文本
const getReviewStatusText = (status: string) => {
  const map: Record<string, string> = {
    APPROVED: '已通过',
    PENDING: '待审核',
    REJECTED: '已拒绝'
  }
  return map[status] || status
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return '-'
  return date
}

// 加载反馈列表
const loadFeedbackList = async () => {
  loading.value = true
  try {
    // 注意：后端FeedbackController没有GET列表接口，这里需要调用其他接口
    // 暂时使用模拟数据或调用其他可用的接口
    ElMessage.info('反馈列表功能待后端接口完善')
    // TODO: 实现反馈列表加载
  } catch (error: any) {
    console.error('加载反馈列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 提交反馈
const submitFeedback = async () => {
  if (!feedbackFormRef.value) return

  try {
    await feedbackFormRef.value.validate()
    submitting.value = true

    const response = await api.post('/api/v1/feedback', feedbackForm)

    if (response.data) {
      ElMessage.success('反馈提交成功，AI分析完成')
      showFeedbackDialog.value = false
      feedbackFormRef.value.resetFields()
      loadFeedbackList()
    }
  } catch (error: any) {
    console.error('提交反馈失败:', error)
  } finally {
    submitting.value = false
  }
}

// 查看反馈详情
const viewFeedbackDetail = (feedback: any) => {
  currentFeedback.value = feedback
  showFeedbackDetailDialog.value = true
}

// 加载绩效数据
const loadPerformanceData = async () => {
  performanceLoading.value = true
  try {
    const hotelId = '1'
    let startDate = performanceDateRange.value?.[0] || getDefaultStartDate()
    let endDate = performanceDateRange.value?.[1] || getDefaultEndDate()

    const response = await api.get('/api/v1/performance/history', {
      params: {
        hotelId,
        startDate,
        endDate
      }
    })

    if (response.data) {
      performanceList.value = Array.isArray(response.data) ? response.data : []
    }
  } catch (error: any) {
    console.error('加载绩效数据失败:', error)
  } finally {
    performanceLoading.value = false
  }
}

// 计算绩效
const calculatePerformance = async () => {
  try {
    await api.post('/api/v1/performance/calculate', null, {
      params: {
        hotelId: '1'
      }
    })
    ElMessage.success('绩效计算完成')
    await loadPerformanceData()
  } catch (error: any) {
    console.error('计算绩效失败:', error)
  }
}

// 查看绩效详情
const viewPerformanceDetail = (performance: any) => {
  ElMessage.info('绩效详情功能开发中...')
  // TODO: 实现绩效详情查看
}

// 获取默认开始日期（7天前）
const getDefaultStartDate = (): string => {
  const date = new Date()
  date.setDate(date.getDate() - 7)
  return date.toISOString().split('T')[0] || ''
}

// 获取默认结束日期（今天）
const getDefaultEndDate = (): string => {
  return new Date().toISOString().split('T')[0] || ''
}

// 初始化
onMounted(() => {
  // 设置默认日期范围
  performanceDateRange.value = [getDefaultStartDate(), getDefaultEndDate()]
  loadPerformanceData()
})
</script>

<style scoped>
.reputation {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-left h1 {
  margin: 0 0 4px 0;
  color: #303133;
  font-size: 24px;
}

.page-subtitle {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
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
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
  margin-top: 4px;
}

.main-tabs {
  margin-top: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.feedback-card,
.performance-card {
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.feedback-detail {
  padding: 16px 0;
}

:deep(.el-card__header) {
  border-bottom: 1px solid #e5e7eb;
  padding: 20px 24px;
}

:deep(.el-card__body) {
  padding: 24px;
}

:deep(.el-tabs__header) {
  margin-bottom: 20px;
}
</style>
