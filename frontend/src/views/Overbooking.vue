<template>
  <div class="overbooking-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <el-icon :size="28" class="header-icon"><TrendCharts /></el-icon>
        <div>
          <h1 class="page-title">智能超售管理</h1>
          <p class="page-subtitle">基于强化学习的超售决策系统</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="showRecommendationDialog" :loading="loading">
          <el-icon><MagicStick /></el-icon>
          获取智能推荐
        </el-button>
        <el-button @click="showTrainingDialog">
          <el-icon><Setting /></el-icon>
          模型训练
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon success">
            <el-icon :size="24"><TrendCharts /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalDecisions }}</div>
            <div class="stat-label">总决策次数</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon primary">
            <el-icon :size="24"><SuccessFilled /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.acceptedDecisions }}</div>
            <div class="stat-label">已采纳决策</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon warning">
            <el-icon :size="24"><Money /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ stats.totalRevenue }}</div>
            <div class="stat-label">总增收金额</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon info">
            <el-icon :size="24"><DataAnalysis /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.confidence }}%</div>
            <div class="stat-label">模型置信度</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 主体内容 -->
    <div class="main-content">
      <!-- 左侧：推荐决策 -->
      <el-card class="decision-card">
        <template #header>
          <div class="card-header">
            <span>智能超售推荐</span>
            <el-tag :type="confidenceType">置信度: {{ recommendation.confidence }}%</el-tag>
          </div>
        </template>

        <div v-if="recommendation.recommendedOverbook !== null" class="recommendation-content">
          <div class="recommendation-main">
            <div class="recommendation-number">
              <span class="number-label">建议超售</span>
              <div class="number-value">{{ recommendation.recommendedOverbook }}</div>
              <span class="number-unit">间</span>
            </div>
            <div class="recommendation-info">
              <div class="info-item">
                <span class="info-label">当前入住率:</span>
                <span class="info-value">{{ recommendation.currentOccupancy }}%</span>
              </div>
              <div class="info-item">
                <span class="info-label">预计No-show率:</span>
                <span class="info-value">{{ recommendation.expectedNoShow }}%</span>
              </div>
              <div class="info-item">
                <span class="info-label">预期增收:</span>
                <span class="info-value success">¥{{ recommendation.expectedRevenue }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">溢出风险:</span>
                <span class="info-value" :class="riskLevelClass">{{ recommendation.riskLevel }}</span>
              </div>
            </div>
          </div>

          <div class="recommendation-actions">
            <el-button type="success" size="large" @click="applyRecommendation">
              <el-icon><CircleCheck /></el-icon>
              采纳建议
            </el-button>
            <el-button size="large" @click="ignoreRecommendation">
              <el-icon><Close /></el-icon>
              忽略建议
            </el-button>
          </div>

          <el-divider />

          <div class="state-info">
            <h4>状态分析</h4>
            <div class="state-tags">
              <el-tag>入住率: {{ recommendation.state?.occupancyLevel || '未知' }}</el-tag>
              <el-tag>{{ recommendation.state?.dayType || '未知' }}</el-tag>
              <el-tag>提前期: {{ recommendation.state?.leadTimeCategory || '未知' }}</el-tag>
            </div>
          </div>
        </div>

        <el-empty v-else description="暂无推荐，请选择日期获取推荐" />
      </el-card>

      <!-- 右侧：决策历史 -->
      <el-card class="history-card">
        <template #header>
          <div class="card-header">
            <span>决策历史</span>
            <el-button link type="primary" @click="refreshHistory">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </template>

        <el-table :data="history" stripe>
          <el-table-column prop="decisionDate" label="日期" width="120" />
          <el-table-column prop="actionChosen" label="超售数量" width="100">
            <template #default="{ row }">
              <el-tag>{{ row.actionChosen }} 间</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="wasSuccessful" label="状态" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.wasSuccessful === null" type="info">待处理</el-tag>
              <el-tag v-else-if="row.wasSuccessful" type="success">成功</el-tag>
              <el-tag v-else type="danger">失败</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="row.wasSuccessful === null"
                link
                type="primary"
                size="small"
                @click="showRecordOutcomeDialog(row)"
              >
                记录结果
              </el-button>
              <span v-else class="text-muted">已完成</span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <!-- 记录实际结果对话框 -->
    <el-dialog v-model="recordOutcomeDialogVisible" title="记录实际结果" width="500px">
      <el-alert type="info" :closable="false" show-icon style="margin-bottom: 16px">
        请输入该日期的实际取消数和未入住数，系统将自动判断决策是否成功。
      </el-alert>

      <el-form :model="recordOutcomeForm" label-width="120px">
        <el-form-item label="决策日期">
          <el-input :value="currentDecision?.decisionDate" disabled />
        </el-form-item>
        <el-form-item label="超售数量">
          <el-input :value="currentDecision?.actionChosen + ' 间'" disabled />
        </el-form-item>
        <el-form-item label="实际取消数">
          <el-input-number
            v-model="recordOutcomeForm.cancellations"
            :min="0"
            :max="100"
            placeholder="请输入实际取消数"
          />
        </el-form-item>
        <el-form-item label="实际未入住数">
          <el-input-number
            v-model="recordOutcomeForm.noShows"
            :min="0"
            :max="100"
            placeholder="请输入实际未入住数"
          />
        </el-form-item>
        <el-form-item label="">
          <div class="outcome-preview">
            <span>取消 + 未入住 = </span>
            <span class="outcome-total">{{ recordOutcomeForm.cancellations + recordOutcomeForm.noShows }}</span>
            <span> 间</span>
            <el-tag
              v-if="recordOutcomeForm.cancellations + recordOutcomeForm.noShows >= (currentDecision?.actionChosen || 0)"
              type="success"
              style="margin-left: 12px"
            >
              成功（无溢出）
            </el-tag>
            <el-tag v-else type="danger" style="margin-left: 12px">
              失败（有溢出）
            </el-tag>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="recordOutcomeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="recordOutcome" :loading="loading">
          确认记录
        </el-button>
      </template>
    </el-dialog>

    <!-- 获取推荐对话框 -->
    <el-dialog v-model="recommendationDialogVisible" title="获取智能推荐" width="500px">
      <el-form :model="recommendationForm" label-width="100px">
        <el-form-item label="酒店">
          <el-input v-model="recommendationForm.hotelId" placeholder="请输入酒店ID" />
        </el-form-item>
        <el-form-item label="房型">
          <el-select v-model="recommendationForm.roomTypeId" placeholder="请选择房型">
            <el-option label="标准间" :value="1" />
            <el-option label="标准间(海景)" :value="2" />
            <el-option label="豪华间" :value="3" />
            <el-option label="豪华间(海景)" :value="4" />
            <el-option label="套房" :value="5" />
            <el-option label="海景套房" :value="6" />
            <el-option label="家庭套房" :value="7" />
            <el-option label="总统套房" :value="8" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标日期">
          <el-date-picker
            v-model="recommendationForm.targetDate"
            type="date"
            placeholder="选择日期"
            :disabled-date="disabledDate"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="recommendationDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="getRecommendation" :loading="loading">
          获取推荐
        </el-button>
      </template>
    </el-dialog>

    <!-- 模型训练对话框 -->
    <el-dialog v-model="trainingDialogVisible" title="模型训练" width="600px">
      <div class="training-content">
        <el-alert type="info" :closable="false" show-icon>
          <p>强化学习模型通过不断与环境交互学习最优超售策略。</p>
          <p>训练次数越多，模型决策越准确。</p>
        </el-alert>

        <el-divider />

        <el-form :model="trainingForm" label-width="100px">
          <el-form-item label="训练轮数">
            <el-input-number
              v-model="trainingForm.episodes"
              :min="1"
              :max="1000"
              :step="10"
            />
          </el-form-item>
        </el-form>

        <div v-if="trainingStatus.isTraining" class="training-progress">
          <el-progress
            :percentage="trainingStatus.progress"
            :status="trainingStatus.status"
          >
            <span class="progress-text">{{ trainingStatus.current }}/{{ trainingStatus.total }}</span>
          </el-progress>
          <p class="progress-info">当前轮次奖励: {{ trainingStatus.currentReward }}</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="trainingDialogVisible = false">关闭</el-button>
        <el-button
          type="primary"
          @click="startTraining"
          :loading="trainingStatus.isTraining"
          :disabled="trainingStatus.isTraining"
        >
          {{ trainingStatus.isTraining ? '训练中...' : '开始训练' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  TrendCharts, MagicStick, Setting, SuccessFilled, Money, DataAnalysis,
  Close, Refresh, CircleCheck
} from '@element-plus/icons-vue'
import api from '../api/index'

// 数据
const loading = ref(false)
const recommendationDialogVisible = ref(false)
const trainingDialogVisible = ref(false)
const recordOutcomeDialogVisible = ref(false)

const stats = ref({
  totalDecisions: 0,
  acceptedDecisions: 0,
  totalRevenue: 0,
  confidence: 0
})

const recommendation = ref({
  recommendedOverbook: null as number | null,
  confidence: 0,
  currentOccupancy: 0,
  expectedNoShow: 0,
  expectedRevenue: 0,
  riskLevel: '',
  state: {
    occupancyLevel: '',
    dayType: '',
    leadTimeCategory: ''
  }
})

const history = ref([])

const recommendationForm = ref({
  hotelId: 1,
  roomTypeId: 1,
  targetDate: ''
})

const trainingForm = ref({
  episodes: 50
})

const trainingStatus = ref({
  isTraining: false,
  progress: 0,
  current: 0,
  total: 0,
  currentReward: 0,
  status: ''
})

const currentDecision = ref(null)
const recordOutcomeForm = ref({
  cancellations: 0,
  noShows: 0
})

// 计算属性
const confidenceType = computed(() => {
  const c = recommendation.value.confidence
  if (c >= 80) return 'success'
  if (c >= 60) return 'warning'
  return 'info'
})

const riskLevelClass = computed(() => {
  const level = recommendation.value.riskLevel
  if (level === '低') return 'success'
  if (level === '中') return 'warning'
  return 'danger'
})

// 方法
const showRecommendationDialog = () => {
  recommendationForm.value.targetDate = new Date().toISOString().split('T')[0] || ''
  recommendationDialogVisible.value = true
}

const getRecommendation = async () => {
  loading.value = true
  try {
    const response = await api.get(`/api/overbooking/recommend`, {
      params: {
        hotelId: recommendationForm.value.hotelId,
        roomTypeId: recommendationForm.value.roomTypeId,
        targetDate: recommendationForm.value.targetDate
      }
    })
    const data = response.data
    recommendation.value = {
      recommendedOverbook: data.recommendedOverbook,
      confidence: Math.round((data.confidence || 0) * 100),
      currentOccupancy: data.currentOccupancy || 75,
      expectedNoShow: data.expectedNoShow || 12,
      expectedRevenue: data.expectedRevenue || (data.recommendedOverbook ? data.recommendedOverbook * 500 : 0),
      riskLevel: data.riskLevel || (data.recommendedOverbook && data.recommendedOverbook > 2 ? '高' : '低'),
      state: data.state || {
        occupancyLevel: '未知',
        dayType: '未知',
        leadTimeCategory: '未知'
      }
    }
    ElMessage.success('获取推荐成功')
    recommendationDialogVisible.value = false
  } catch (error) {
    ElMessage.error('获取推荐失败')
    // 重置recommendation为默认状态
    recommendation.value = {
      recommendedOverbook: null,
      confidence: 0,
      currentOccupancy: 0,
      expectedNoShow: 0,
      expectedRevenue: 0,
      riskLevel: '',
      state: {
        occupancyLevel: '',
        dayType: '',
        leadTimeCategory: ''
      }
    }
  } finally {
    loading.value = false
  }
}

const applyRecommendation = async () => {
  loading.value = true
  try {
    const response = await api.post(`/api/overbooking/apply`, {
      hotelId: recommendationForm.value.hotelId,
      roomTypeId: recommendationForm.value.roomTypeId,
      decisionDate: recommendationForm.value.targetDate,
      overbookAmount: recommendation.value.recommendedOverbook
    })

    ElMessage.success('已采纳超售建议')
    refreshHistory()
    loadStats()
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    loading.value = false
  }
}

const ignoreRecommendation = () => {
  ElMessage.info('已忽略建议')
  recommendation.value.recommendedOverbook = null
}

const showRecordOutcomeDialog = (decision: any) => {
  currentDecision.value = decision
  recordOutcomeForm.value = {
    cancellations: 0,
    noShows: 0
  }
  recordOutcomeDialogVisible.value = true
}

const recordOutcome = async () => {
  loading.value = true
  try {
    await api.patch(`/api/overbooking/${currentDecision.value.id}/outcome`, {
      cancellations: recordOutcomeForm.value.cancellations,
      noShows: recordOutcomeForm.value.noShows
    })

    ElMessage.success('记录成功')
    recordOutcomeDialogVisible.value = false
    refreshHistory()
    loadStats()
  } catch (error) {
    ElMessage.error('记录失败')
  } finally {
    loading.value = false
  }
}

const showTrainingDialog = () => {
  trainingDialogVisible.value = true
}

const startTraining = async () => {
  trainingStatus.value.isTraining = true
  trainingStatus.value.progress = 0
  trainingStatus.value.total = trainingForm.value.episodes
  trainingStatus.value.status = ''

  try {
    const response = await api.post(`/api/rl-training/train`, {
      hotelId: 1,
      roomTypeId: 1,
      episodes: trainingForm.value.episodes
    })

    ElMessage.success('训练任务已启动，请稍后查看结果')
    loadStats()
  } catch (error) {
    ElMessage.error('训练失败')
    trainingStatus.value.status = 'exception'
  } finally {
    trainingStatus.value.isTraining = false
  }
}

const refreshHistory = async () => {
  try {
    const response = await api.get(`/api/overbooking/history`, {
      params: { hotelId: 1, page: 0, size: 10 }
    })

    history.value = response.data.content || []
  } catch (error) {
    console.error('获取历史失败', error)
  }
}

const loadStats = async () => {
  try {
    const response = await api.get(`/api/overbooking/performance`, {
      params: { hotelId: 1 }
    })

    const data = response.data
    stats.value = {
      totalDecisions: data.totalDecisions || 0,
      acceptedDecisions: data.acceptedDecisions || 0,
      totalRevenue: data.totalRevenue || 0,
      confidence: Math.round((data.confidence || 0))
    }
  } catch (error) {
    console.error('获取统计失败', error)
  }
}

const disabledDate = (time: Date) => {
  return time.getTime() < Date.now() - 24 * 60 * 60 * 1000
}

const getStatusType = (status: string) => {
  switch (status) {
    case 'APPLIED': return 'success'
    case 'PENDING': return 'warning'
    case 'IGNORED': return 'info'
    default: return ''
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'APPLIED': return '已采纳'
    case 'PENDING': return '待处理'
    case 'IGNORED': return '已忽略'
    default: return status
  }
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 生命周期
onMounted(() => {
  loadStats()
  refreshHistory()
})
</script>

<style scoped>
.overbooking-container {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  color: #3b82f6;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 4px 0;
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

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
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

.stat-icon.success {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.stat-icon.primary {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
}

.stat-icon.warning {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}

.stat-icon.info {
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #64748b;
  margin-top: 4px;
}

.main-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.decision-card,
.history-card {
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.recommendation-content {
  padding: 20px 0;
}

.recommendation-main {
  display: flex;
  gap: 40px;
  margin-bottom: 32px;
}

.recommendation-number {
  text-align: center;
  padding: 32px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-radius: 16px;
  min-width: 180px;
}

.number-label {
  font-size: 14px;
  color: #64748b;
  display: block;
  margin-bottom: 8px;
}

.number-value {
  font-size: 64px;
  font-weight: 700;
  color: #0284c7;
  line-height: 1;
}

.number-unit {
  font-size: 16px;
  color: #64748b;
  margin-top: 8px;
  display: block;
}

.recommendation-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 16px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f8fafc;
  border-radius: 8px;
}

.info-label {
  font-size: 14px;
  color: #64748b;
}

.info-value {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.info-value.success {
  color: #10b981;
}

.recommendation-actions {
  display: flex;
  gap: 12px;
}

.state-info {
  margin-top: 24px;
}

.state-info h4 {
  font-size: 14px;
  color: #64748b;
  margin: 0 0 12px 0;
}

.state-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.training-content {
  padding: 16px 0;
}

.training-progress {
  margin-top: 24px;
}

.progress-text {
  font-size: 14px;
  color: #64748b;
}

.progress-info {
  text-align: center;
  font-size: 14px;
  color: #64748b;
  margin-top: 12px;
}

.outcome-preview {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #1e293b;
}

.outcome-total {
  font-size: 20px;
  font-weight: 700;
  color: #0284c7;
  margin: 0 8px;
}

.text-muted {
  color: #94a3b8;
  font-size: 12px;
}
</style>
