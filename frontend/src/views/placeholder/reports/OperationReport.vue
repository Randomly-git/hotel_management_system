<template>
  <div class="operation-report">
    <!-- 运营概览 -->
    <el-row :gutter="20" class="metrics-row" v-loading="loading">
      <el-col :xs="12" :sm="6" :md="3">
        <div class="metric-card tasks">
          <div class="metric-icon">
            <el-icon :size="24"><List /></el-icon>
          </div>
          <div class="metric-content">
            <div class="metric-value">{{ operationData.totalTasks }}</div>
            <div class="metric-label">总任务数</div>
          </div>
        </div>
      </el-col>

      <el-col :xs="12" :sm="6" :md="3">
        <div class="metric-card completed">
          <div class="metric-icon">
            <el-icon :size="24"><Check /></el-icon>
          </div>
          <div class="metric-content">
            <div class="metric-value">{{ operationData.completedTasks }}</div>
            <div class="metric-label">已完成</div>
            <div class="metric-rate">{{ operationData.completionRate }}%</div>
          </div>
        </div>
      </el-col>

      <el-col :xs="12" :sm="6" :md="3">
        <div class="metric-card efficiency">
          <div class="metric-icon">
            <el-icon :size="24"><Timer /></el-icon>
          </div>
          <div class="metric-content">
            <div class="metric-value">{{ operationData.avgCompletionTime }}</div>
            <div class="metric-label">平均完成时间</div>
            <div class="metric-unit">分钟</div>
          </div>
        </div>
      </el-col>

      <el-col :xs="12" :sm="6" :md="3">
        <div class="metric-card alerts">
          <div class="metric-icon">
            <el-icon :size="24"><Warning /></el-icon>
          </div>
          <div class="metric-content">
            <div class="metric-value">{{ operationData.alertCount }}</div>
            <div class="metric-label">告警数量</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <!-- 任务完成趋势 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <el-icon><TrendCharts /></el-icon>
              <span>任务完成趋势</span>
            </div>
          </template>
          <div class="chart-placeholder">
            <div class="chart-content">
              <el-icon :size="48" class="chart-icon"><TrendCharts /></el-icon>
              <p>任务完成趋势图表</p>
              <small>基于{{ periodText }}的数据</small>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 部门绩效对比 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <el-icon><DataLine /></el-icon>
              <span>部门绩效对比</span>
            </div>
          </template>
          <div class="department-performance">
            <div
              v-for="dept in departmentPerformance"
              :key="dept.name"
              class="performance-item"
            >
              <div class="dept-name">{{ dept.name }}</div>
              <div class="performance-bar">
                <div
                  class="bar-fill"
                  :style="{ width: dept.score + '%' }"
                  :class="getPerformanceClass(dept.score)"
                ></div>
              </div>
              <div class="score-value">{{ dept.score }}分</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 任务统计详情 -->
    <el-card class="task-stats-card">
      <template #header>
        <div class="card-header">
          <el-icon><DataLine /></el-icon>
          <span>任务统计详情</span>
        </div>
      </template>

      <el-table :data="taskStatistics" stripe style="width: 100%">
        <el-table-column prop="department" label="部门" width="120" />
        <el-table-column prop="totalTasks" label="总任务" width="100" />
        <el-table-column prop="completedTasks" label="已完成" width="100" />
        <el-table-column prop="completionRate" label="完成率" width="100">
          <template #default="{ row }">
            <span :class="getRateClass(row.completionRate)">
              {{ row.completionRate }}%
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="avgResponseTime" label="平均响应时间" width="140">
          <template #default="{ row }">
            <span>{{ row.avgResponseTime }}分钟</span>
          </template>
        </el-table-column>
        <el-table-column prop="avgCompletionTime" label="平均完成时间" width="140">
          <template #default="{ row }">
            <span>{{ row.avgCompletionTime }}分钟</span>
          </template>
        </el-table-column>
        <el-table-column prop="satisfaction" label="客户满意度" width="120">
          <template #default="{ row }">
            <el-rate
              v-model="row.satisfaction"
              disabled
              show-score
              :max="5"
              :colors="['#F56C6C', '#E6A23C', '#67C23A']"
            />
          </template>
        </el-table-column>
        <el-table-column prop="performance" label="绩效评分" width="120">
          <template #default="{ row }">
            <el-tag :type="getPerformanceTag(row.performance)">
              {{ row.performance }}分
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 系统告警信息 -->
    <el-card class="alerts-card">
      <template #header>
        <div class="card-header">
          <el-icon><Warning /></el-icon>
          <span>系统告警信息</span>
        </div>
      </template>

      <el-table :data="systemAlerts" stripe style="width: 100%">
        <el-table-column prop="time" label="告警时间" width="160" />
        <el-table-column prop="level" label="告警级别" width="100">
          <template #default="{ row }">
            <el-tag :type="getAlertTagType(row.level)">
              {{ row.level }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="告警类型" width="120" />
        <el-table-column prop="department" label="涉及部门" width="120" />
        <el-table-column prop="description" label="告警描述" min-width="200" />
        <el-table-column prop="status" label="处理状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === '已处理' ? 'success' : 'warning'">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === '待处理'"
              size="small"
              type="primary"
              @click="handleAlert(row)"
            >
              处理
            </el-button>
            <el-button size="small" @click="viewAlertDetail(row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- AI分析报告 -->
    <el-card class="ai-report-card">
      <template #header>
        <div class="card-header">
          <el-icon><Management /></el-icon>
          <span>AI分析报告</span>
        </div>
      </template>

      <div class="ai-analysis">
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="analysis-item">
              <h4>服务质量分析</h4>
              <p>{{ periodText }}服务质量整体良好，AI识别客户满意度为{{ aiAnalysis.serviceQuality.score.toFixed(1) }}分。主要改进点：</p>
              <ul v-if="aiAnalysis.serviceQuality.improvementPoints.length > 0">
                <li v-for="point in aiAnalysis.serviceQuality.improvementPoints" :key="point">{{ point }}</li>
              </ul>
              <ul v-else>
                <li>暂无改进建议</li>
              </ul>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="analysis-item">
              <h4>运营效率分析</h4>
              <p>任务处理效率提升{{ aiAnalysis.operationalEfficiency.improvementRate }}%，AI建议：</p>
              <ul v-if="aiAnalysis.operationalEfficiency.suggestions.length > 0">
                <li v-for="suggestion in aiAnalysis.operationalEfficiency.suggestions" :key="suggestion">{{ suggestion }}</li>
              </ul>
              <ul v-else>
                <li>暂无效率优化建议</li>
              </ul>
            </div>
          </el-col>
        </el-row>

        <el-divider />

        <div class="ai-recommendations">
          <h4>AI优化建议</h4>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-card class="recommendation-card" shadow="hover">
                <el-icon :size="32" class="card-icon"><MagicStick /></el-icon>
                <h5>智能排班</h5>
                <p>基于历史数据和预测需求，优化员工排班</p>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card class="recommendation-card" shadow="hover">
                <el-icon :size="32" class="card-icon"><TrendCharts /></el-icon>
                <h5>动态定价</h5>
                <p>根据需求预测和竞争态势调整房价</p>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card class="recommendation-card" shadow="hover">
                <el-icon :size="32" class="card-icon"><Service /></el-icon>
                <h5>个性化服务</h5>
                <p>基于客户画像提供定制化服务</p>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  List, Check, Timer, Warning, TrendCharts, PieChart, DataLine, Setting, MagicStick, Service
} from '@element-plus/icons-vue'
import { performanceApi } from '@/api'

// Props
interface Props {
  period: string
}

const props = defineProps<Props>()

// 运营数据
const operationData = reactive({
  totalTasks: 0,
  completedTasks: 0,
  completionRate: 0,
  avgCompletionTime: 0,
  alertCount: 0
})

// 部门绩效数据
const departmentPerformance = ref<Array<{name: string, score: number}>>([])

// 加载状态
const loading = ref(false)

// 任务统计详情
const taskStatistics = ref<Array<{
  department: string
  totalTasks: number
  completedTasks: number
  completionRate: number
  avgResponseTime: number
  avgCompletionTime: number
  satisfaction: number
  performance: number
}>>([])

// 系统告警信息
const systemAlerts = ref<Array<{
  time: string
  level: string
  type: string
  department: string
  description: string
  status: string
}>>([])

// AI分析数据
const aiAnalysis = ref({
  serviceQuality: {
    score: 0,
    improvementPoints: [] as string[]
  },
  operationalEfficiency: {
    improvementRate: 0,
    suggestions: [] as string[]
  },
  recommendations: [
    {
      icon: 'MagicStick',
      title: '智能排班',
      description: '基于历史数据和预测需求，优化员工排班'
    },
    {
      icon: 'TrendCharts',
      title: '动态定价',
      description: '根据需求预测和竞争态势调整房价'
    },
    {
      icon: 'Service',
      title: '个性化服务',
      description: '利用AI分析客户偏好，提供定制化服务'
    }
  ]
})

// 时期文本
const periodText = computed(() => {
  const periodMap: Record<string, string> = {
    today: '今日',
    week: '本周',
    month: '本月',
    year: '今年'
  }
  return periodMap[props.period] || '本月'
})

// 加载运营数据
const loadOperationData = async () => {
  loading.value = true
  try {
    // 获取运营概览数据
    const overviewResponse = await performanceApi.getOverview({
      hotelId: 1
    })

    if (overviewResponse.data.success) {
      const overviewData = overviewResponse.data.data
      Object.assign(operationData, {
        totalTasks: overviewData.totalTasks || 0,
        completedTasks: overviewData.completedTasks || 0,
        completionRate: Math.round((overviewData.completedTasks / overviewData.totalTasks) * 100) || 0,
        avgCompletionTime: overviewData.averageCompletionTime || 0,
        alertCount: overviewData.alertCount || 0
      })
    }

    // 获取部门绩效数据
    const performanceResponse = await performanceApi.getDepartmentPerformance({
      hotelId: 1,
      timeRange: props.period
    })

    if (performanceResponse.data.success) {
      const performanceData = performanceResponse.data.data || []
      departmentPerformance.value = performanceData.map((dept: any) => ({
        name: dept.departmentName || dept.name,
        score: dept.performanceScore || dept.score || 0
      }))

      // 从绩效数据生成任务统计详情
      taskStatistics.value = performanceData.map((dept: any) => ({
        department: dept.departmentName || dept.name,
        totalTasks: dept.totalTasks || 0,
        completedTasks: dept.completedTasks || 0,
        completionRate: dept.completionRate || 0,
        avgResponseTime: dept.avgResponseTime || 0,
        avgCompletionTime: dept.avgCompletionTime || 0,
        satisfaction: dept.satisfaction || 0,
        performance: dept.performanceScore || dept.score || 0
      }))

    // 获取告警信息
    const alertsResponse = await performanceApi.getAlerts({
      hotelId: 1
    })

    if (alertsResponse.data.success) {
      const alertsData = alertsResponse.data.data || []
      systemAlerts.value = alertsData.map((alert: any) => ({
        time: alert.alertTime || alert.time || '',
        level: alert.alertLevel || alert.level || '',
        type: alert.alertType || alert.type || '',
        department: alert.departmentName || alert.department || '',
        description: alert.description || '',
        status: alert.status || '待处理'
      }))
    }

    // 获取AI统计数据
    const aiStatsResponse = await performanceApi.getAIStatistics({
      hotelId: 1,
      timeRange: props.period
    })

    if (aiStatsResponse.data.success) {
      const aiData = aiStatsResponse.data.data || {}
      aiAnalysis.value.serviceQuality.score = aiData.averageSatisfaction || 0
      aiAnalysis.value.serviceQuality.improvementPoints = aiData.serviceQualityPoints || []
      aiAnalysis.value.operationalEfficiency.improvementRate = aiData.efficiencyImprovement || 0
      aiAnalysis.value.operationalEfficiency.suggestions = aiData.efficiencySuggestions || []
    }
    }

  } catch (error) {
    console.error('加载运营数据失败:', error)
    ElMessage.error('加载运营数据失败')
    // 设置默认数据
    Object.assign(operationData, {
      totalTasks: 0,
      completedTasks: 0,
      completionRate: 0,
      avgCompletionTime: 0,
      alertCount: 0
    })
    departmentPerformance.value = []
    taskStatistics.value = []
    systemAlerts.value = []
    // 重置AI分析数据
    aiAnalysis.value.serviceQuality.score = 0
    aiAnalysis.value.serviceQuality.improvementPoints = []
    aiAnalysis.value.operationalEfficiency.improvementRate = 0
    aiAnalysis.value.operationalEfficiency.suggestions = []
  } finally {
    loading.value = false
  }
}

// 监听period变化，重新加载数据
watch(() => props.period, () => {
  loadOperationData()
})

// 组件挂载时加载数据
onMounted(() => {
  loadOperationData()
})

// 获取绩效条样式
const getPerformanceClass = (score: number) => {
  if (score >= 90) return 'excellent'
  if (score >= 80) return 'good'
  if (score >= 70) return 'average'
  return 'poor'
}

// 获取完成率样式
const getRateClass = (rate: number) => {
  if (rate >= 95) return 'excellent-rate'
  if (rate >= 85) return 'good-rate'
  return 'average-rate'
}

// 获取绩效标签类型
const getPerformanceTag = (score: number) => {
  if (score >= 90) return 'success'
  if (score >= 80) return 'primary'
  if (score >= 70) return 'warning'
  return 'danger'
}

// 获取告警标签类型
const getAlertTagType = (level: string) => {
  const typeMap: Record<string, string> = {
    '高': 'danger',
    '中': 'warning',
    '低': 'info'
  }
  return typeMap[level] || 'info'
}

// 处理告警
const handleAlert = (alert: any) => {
  // 处理告警逻辑
  console.log('处理告警:', alert)
}

// 查看告警详情
const viewAlertDetail = (alert: any) => {
  // 查看告警详情逻辑
  console.log('查看告警详情:', alert)
}

// 监听period变化，更新数据
watch(() => props.period, (newPeriod) => {
  updateOperationData(newPeriod)
})

const updateOperationData = (period: string) => {
  // 根据不同时期更新数据
  const dataMap: Record<string, any> = {
    today: {
      totalTasks: 45,
      completedTasks: 42,
      completionRate: 93,
      avgCompletionTime: 38,
      alertCount: 2
    },
    week: {
      totalTasks: 320,
      completedTasks: 288,
      completionRate: 90,
      avgCompletionTime: 42,
      alertCount: 5
    },
    month: {
      totalTasks: 1250,
      completedTasks: 1125,
      completionRate: 90,
      avgCompletionTime: 45,
      alertCount: 8
    },
    year: {
      totalTasks: 15000,
      completedTasks: 13500,
      completionRate: 90,
      avgCompletionTime: 48,
      alertCount: 95
    }
  }

  const data = dataMap[period] || dataMap.month
  Object.assign(operationData, data)
}
</script>

<style scoped>
.operation-report {
  padding: 0;
}

.metrics-row {
  margin-bottom: 20px;
}

.metric-card {
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

.metric-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.metric-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
}

.metric-card.tasks .metric-icon {
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
}

.metric-card.completed .metric-icon {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.metric-card.efficiency .metric-icon {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}

.metric-card.alerts .metric-icon {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
}

.metric-value {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1;
}

.metric-label {
  font-size: 12px;
  color: #64748b;
  margin-top: 4px;
}

.metric-rate {
  font-size: 14px;
  font-weight: 600;
  color: #10b981;
  margin-top: 4px;
}

.metric-unit {
  font-size: 12px;
  color: #64748b;
  margin-top: 2px;
}

.charts-row {
  margin-bottom: 20px;
}

.chart-card {
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  height: 350px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.chart-placeholder {
  height: 280px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
  border-radius: 8px;
  margin: 20px;
}

.chart-content {
  text-align: center;
  color: #64748b;
}

.chart-icon {
  color: #cbd5e1;
  margin-bottom: 16px;
}

.department-performance {
  padding: 20px;
}

.performance-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.dept-name {
  width: 80px;
  font-size: 14px;
  color: #475569;
}

.performance-bar {
  flex: 1;
  height: 12px;
  background: #e2e8f0;
  border-radius: 6px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: 6px;
  transition: width 0.3s ease;
}

.bar-fill.excellent {
  background: linear-gradient(90deg, #10b981 0%, #059669 100%);
}

.bar-fill.good {
  background: linear-gradient(90deg, #3b82f6 0%, #2563eb 100%);
}

.bar-fill.average {
  background: linear-gradient(90deg, #f59e0b 0%, #d97706 100%);
}

.bar-fill.poor {
  background: linear-gradient(90deg, #ef4444 0%, #dc2626 100%);
}

.score-value {
  width: 60px;
  text-align: right;
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}

.task-stats-card, .alerts-card, .ai-report-card {
  margin-bottom: 20px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.excellent-rate {
  color: #10b981;
  font-weight: 600;
}

.good-rate {
  color: #3b82f6;
  font-weight: 600;
}

.average-rate {
  color: #f59e0b;
  font-weight: 600;
}

.ai-analysis {
  padding: 20px;
}

.analysis-item {
  margin-bottom: 20px;
}

.analysis-item h4 {
  color: #1e293b;
  margin-bottom: 12px;
}

.analysis-item p {
  color: #475569;
  line-height: 1.6;
  margin-bottom: 12px;
}

.analysis-item ul {
  padding-left: 20px;
}

.analysis-item li {
  color: #64748b;
  margin-bottom: 4px;
}

.ai-recommendations h4 {
  color: #1e293b;
  margin-bottom: 20px;
}

.recommendation-card {
  text-align: center;
  border: none;
  padding: 20px;
  margin-bottom: 16px;
}

.card-icon {
  color: #3b82f6;
  margin-bottom: 12px;
}

.recommendation-card h5 {
  color: #1e293b;
  margin-bottom: 8px;
}

.recommendation-card p {
  color: #64748b;
  font-size: 14px;
  line-height: 1.5;
}

/* 响应式 */
@media (max-width: 768px) {
  .metrics-row {
    grid-template-columns: repeat(2, 1fr);
  }

  .charts-row {
    grid-template-columns: 1fr;
  }

  .performance-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .dept-name {
    width: auto;
  }
}
</style>
