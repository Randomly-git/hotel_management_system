<template>
  <div class="feedback-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">客户反馈</h1>
        <p class="page-subtitle">您的意见是我们改进的动力</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="showSubmitDialog = true">
          <el-icon><Plus /></el-icon>
          提交反馈
        </el-button>
      </div>
    </div>

    <!-- 反馈统计 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="6" :md="3">
        <div class="stat-card total">
          <div class="stat-icon">
            <el-icon :size="24"><ChatDotRound /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ feedbackStats.total }}</div>
            <div class="stat-label">总反馈</div>
          </div>
        </div>
      </el-col>

      <el-col :xs="12" :sm="6" :md="3">
        <div class="stat-card positive">
          <div class="stat-icon">
            <el-icon :size="24"><CircleCheck /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ feedbackStats.positive }}</div>
            <div class="stat-label">正面反馈</div>
          </div>
        </div>
      </el-col>

      <el-col :xs="12" :sm="6" :md="3">
        <div class="stat-card neutral">
          <div class="stat-icon">
            <el-icon :size="24"><Minus /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ feedbackStats.neutral }}</div>
            <div class="stat-label">中性反馈</div>
          </div>
        </div>
      </el-col>

      <el-col :xs="12" :sm="6" :md="3">
        <div class="stat-card negative">
          <div class="stat-icon">
            <el-icon :size="24"><CircleClose /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ feedbackStats.negative }}</div>
            <div class="stat-label">负面反馈</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 反馈列表 -->
    <el-card class="feedback-list-card">
      <template #header>
        <div class="card-header">
          <span>反馈历史</span>
          <div class="header-actions">
            <el-select v-model="filterType" placeholder="筛选类型" size="small" @change="loadFeedbacks">
              <el-option label="全部反馈" value="all" />
              <el-option label="我的反馈" value="mine" />
              <el-option label="待处理" value="pending" />
              <el-option label="已处理" value="processed" />
            </el-select>
          </div>
        </div>
      </template>

      <el-table :data="feedbacks" stripe style="width: 100%">
        <el-table-column prop="customerName" label="客户姓名" width="120" />
        <el-table-column prop="feedbackContent" label="反馈内容" min-width="200" show-overflow-tooltip />

        <el-table-column label="情感得分" width="120">
          <template #default="{ row }">
            <div class="sentiment-score">
              <el-rate
                v-model="row.sentimentScoreDisplay"
                disabled
                show-score
                :colors="['#F56C6C', '#E6A23C', '#67C23A']"
                :max="5"
              />
              <span class="score-text">{{ row.sentimentScore?.toFixed(2) }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="归属部门" width="120">
          <template #default="{ row }">
            {{ row.department?.deptName || '未知' }}
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row)">
              {{ getStatusText(row) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="feedbackTime" label="提交时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.feedbackTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewFeedbackDetail(row)">
              <el-icon><View /></el-icon>
            </el-button>
            <el-button
              v-if="row.needsReview && row.reviewStatus === 'PENDING'"
              size="small"
              type="warning"
              @click="reviewFeedback(row)"
            >
              审核
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="totalFeedbacks"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 提交反馈对话框 -->
    <el-dialog
      v-model="showSubmitDialog"
      title="提交反馈"
      width="600px"
    >
      <el-form :model="feedbackForm" :rules="feedbackRules" ref="feedbackFormRef" label-width="100px">
        <el-form-item label="客户姓名" prop="customerName">
          <el-input v-model="feedbackForm.customerName" placeholder="请输入您的姓名" />
        </el-form-item>

        <el-form-item label="反馈内容" prop="feedbackContent">
          <el-input
            v-model="feedbackForm.feedbackContent"
            type="textarea"
            :rows="4"
            placeholder="请详细描述您的体验和建议..."
            show-word-limit
            :maxlength="500"
            :minlength="5"
          />
        </el-form-item>

        <el-form-item label="联系方式">
          <el-input v-model="feedbackForm.contactInfo" placeholder="手机号或邮箱（可选）" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showSubmitDialog = false">取消</el-button>
        <el-button type="primary" @click="submitFeedback" :loading="submitting">
          提交反馈
        </el-button>
      </template>
    </el-dialog>

    <!-- 反馈详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      :title="`反馈详情 - ${selectedFeedback?.customerName}`"
      width="700px"
    >
      <div v-if="selectedFeedback" class="feedback-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="客户姓名">{{ selectedFeedback.customerName }}</el-descriptions-item>
          <el-descriptions-item label="联系方式">{{ selectedFeedback.contactInfo || '未提供' }}</el-descriptions-item>
          <el-descriptions-item label="提交时间" :span="2">
            {{ formatDateTime(selectedFeedback.feedbackTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="归属部门">{{ selectedFeedback.department?.deptName || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="情感得分">{{ selectedFeedback.sentimentScore?.toFixed(2) || '未评分' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(selectedFeedback)">
              {{ getStatusText(selectedFeedback) }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <el-divider>反馈内容</el-divider>
        <div class="feedback-content">
          <p>{{ selectedFeedback.feedbackContent }}</p>
        </div>

        <el-divider v-if="selectedFeedback.needsReview">审核信息</el-divider>
        <div v-if="selectedFeedback.needsReview" class="review-info">
          <p><strong>审核状态：</strong>{{ selectedFeedback.reviewStatus }}</p>
          <p v-if="selectedFeedback.reviewTime"><strong>审核时间：</strong>{{ formatDateTime(selectedFeedback.reviewTime) }}</p>
          <p v-if="selectedFeedback.reviewer"><strong>审核人：</strong>{{ selectedFeedback.reviewer }}</p>
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
  Plus, ChatDotRound, CircleCheck, Minus, CircleClose, View
} from '@element-plus/icons-vue'

// 状态
const loading = ref(false)
const submitting = ref(false)
const showSubmitDialog = ref(false)
const showDetailDialog = ref(false)

// 数据
const feedbacks = ref<any[]>([])
const selectedFeedback = ref<any>(null)
const currentPage = ref(1)
const pageSize = ref(10)
const totalFeedbacks = ref(0)
const filterType = ref('all')

// 统计数据
const feedbackStats = reactive({
  total: 0,
  positive: 0,
  neutral: 0,
  negative: 0
})

// 表单数据
const feedbackForm = reactive({
  customerName: '',
  feedbackContent: '',
  contactInfo: ''
})

// 表单验证规则
const feedbackRules = {
  customerName: [{ required: true, message: '请输入客户姓名', trigger: 'blur' }],
  feedbackContent: [
    { required: true, message: '请输入反馈内容', trigger: 'blur' },
    { min: 5, message: '反馈内容不能少于5个字符', trigger: 'blur' }
  ]
}

const feedbackFormRef = ref()

// 获取状态标签类型
const getStatusTagType = (feedback: any) => {
  if (feedback.needsReview) {
    if (feedback.reviewStatus === 'PENDING') return 'warning'
    if (feedback.reviewStatus === 'APPROVED') return 'success'
    if (feedback.reviewStatus === 'REJECTED') return 'danger'
  }
  return 'info'
}

// 获取状态文本
const getStatusText = (feedback: any) => {
  if (feedback.needsReview) {
    if (feedback.reviewStatus === 'PENDING') return '待审核'
    if (feedback.reviewStatus === 'APPROVED') return '已通过'
    if (feedback.reviewStatus === 'REJECTED') return '已拒绝'
  }
  return '正常'
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 加载反馈列表
const loadFeedbacks = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      status: filterType.value === 'all' ? undefined : filterType.value,
      hotelId: '1' // 默认酒店ID
    }

    const response = await api.get('/api/v1/feedback', { params })
    const data = response.data || {}

    // 处理分页数据
    feedbacks.value = data.content || []
    totalFeedbacks.value = data.totalElements || 0

    // 更新统计
    const allFeedbacks = feedbacks.value
    feedbackStats.total = allFeedbacks.length
    feedbackStats.positive = allFeedbacks.filter((f: any) => f.sentimentScore > 0.3).length
    feedbackStats.neutral = allFeedbacks.filter((f: any) => f.sentimentScore >= -0.3 && f.sentimentScore <= 0.3).length
    feedbackStats.negative = allFeedbacks.filter((f: any) => f.sentimentScore < -0.3).length

  } catch (error: any) {
    console.error('加载反馈列表失败:', error)
    ElMessage.error('加载反馈列表失败: ' + (error.response?.data?.message || error.message))

    // 清空数据，避免显示过时的信息
    feedbacks.value = []
    totalFeedbacks.value = 0
    feedbackStats.total = 0
    feedbackStats.positive = 0
    feedbackStats.neutral = 0
    feedbackStats.negative = 0
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

    const response = await api.post('/api/v1/feedback', {
      customerName: feedbackForm.customerName,
      feedbackContent: feedbackForm.feedbackContent,
      contactInfo: feedbackForm.contactInfo || ''
    })

    if (response.data) {
      ElMessage.success('反馈提交成功！感谢您的宝贵意见。')
      showSubmitDialog.value = false
      resetForm()
      loadFeedbacks()
    }
  } catch (error: any) {
    console.error('提交反馈失败:', error)
    ElMessage.error(error.response?.data?.message || '提交反馈失败')
  } finally {
    submitting.value = false
  }
}

// 查看反馈详情
const viewFeedbackDetail = (feedback: any) => {
  selectedFeedback.value = feedback
  showDetailDialog.value = true
}

// 审核反馈
const reviewFeedback = (feedback: any) => {
  ElMessageBox.prompt(
    `请审核这条反馈：\n\n客户：${feedback.customerName}\n内容：${feedback.feedbackContent}\n情感得分：${feedback.sentimentScore?.toFixed(2)}`,
    '评价审核',
    {
      confirmButtonText: '审核通过',
      cancelButtonText: '审核拒绝',
      inputPlaceholder: '请输入审核意见（可选）',
      inputType: 'textarea',
      inputValidator: (value) => {
        if (value && value.length > 200) {
          return '审核意见不能超过200个字符'
        }
        return true
      },
      confirmButtonClass: 'el-button--success',
      cancelButtonClass: 'el-button--danger'
    }
  )
  .then(async ({ value }) => {
    // 用户点击"审核通过"
    await processAudit(feedback.feedbackId, 'APPROVE', value)
  })
  .catch(async (action) => {
    if (action === 'cancel') {
      // 用户点击"审核拒绝"，获取输入框的值
      const inputValue = (action as any).inputValue || ''
      await processAudit(feedback.feedbackId, 'REJECT', inputValue)
    }
  })
}

// 处理审核操作
const processAudit = async (feedbackId: number, action: string, comment?: string) => {
  try {
    const params: any = {
      feedbackId,
      action
    }

    // 如果有审核意见，则添加到参数中
    if (comment && comment.trim()) {
      params.comment = comment.trim()
    }

    const response = await api.post('/api/v1/audit/process', null, {
      params
    })

    ElMessage.success(response.data || '审核操作成功')
    loadFeedbacks() // 重新加载数据
  } catch (error: any) {
    console.error('审核失败:', error)
    ElMessage.error(error.response?.data?.message || '审核操作失败')
  }
}

// 重置表单
const resetForm = () => {
  if (feedbackFormRef.value) {
    feedbackFormRef.value.resetFields()
  }
  feedbackForm.customerName = ''
  feedbackForm.feedbackContent = ''
  feedbackForm.contactInfo = ''
}

// 分页处理
const handleSizeChange = (val: number) => {
  pageSize.value = val
  loadFeedbacks()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  loadFeedbacks()
}

// 初始化
onMounted(() => {
  loadFeedbacks()
})
</script>

<style scoped>
.feedback-container {
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

.stat-card.positive .stat-icon {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.stat-card.neutral .stat-icon {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}

.stat-card.negative .stat-icon {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
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

.feedback-list-card {
  margin-top: 20px;
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

.sentiment-score {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.score-text {
  font-size: 12px;
  color: #64748b;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.feedback-detail {
  padding: 16px 0;
}

.feedback-content {
  background: #f8fafc;
  padding: 16px;
  border-radius: 8px;
  margin: 16px 0;
}

.feedback-content p {
  margin: 0;
  line-height: 1.6;
  color: #374151;
}

.review-info {
  background: #fef3c7;
  padding: 16px;
  border-radius: 8px;
  border-left: 4px solid #f59e0b;
}

.review-info p {
  margin: 8px 0;
  line-height: 1.6;
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
