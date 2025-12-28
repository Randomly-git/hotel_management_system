<template>
  <div class="operation-report">
    <!-- 运营概览 -->
    <el-row :gutter="20" class="metrics-row">
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
              <el-icon><BarChart /></el-icon>
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
          <el-icon><Robot /></el-icon>
          <span>AI分析报告</span>
        </div>
      </template>

      <div class="ai-analysis">
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="analysis-item">
              <h4>服务质量分析</h4>
              <p>本月服务质量整体良好，AI识别客户满意度为4.2分。主要改进点：</p>
              <ul>
                <li>前台响应速度需提升5%</li>
                <li>客房清洁标准需统一</li>
                <li>餐饮服务评价稳步提升</li>
              </ul>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="analysis-item">
              <h4>运营效率分析</h4>
              <p>任务处理效率提升12%，AI建议：</p>
              <ul>
                <li>优化房务部任务分配算法</li>
                <li>增加前台高峰期人手配置</li>
                <li>改进客户服务流程</li>
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
import { ref, reactive, computed, watch } from 'vue'
import {
  List, Check, Timer, Warning, TrendCharts, PieChart, DataLine, Setting, MagicStick, Service
} from '@element-plus/icons-vue'

// Props
interface Props {
  period: string
}

const props = defineProps<Props>()

// 运营数据
const operationData = reactive({
  totalTasks: 1250,
  completedTasks: 1125,
  completionRate: 90,
  avgCompletionTime: 45,
  alertCount: 8
})

// 部门绩效数据
const departmentPerformance = ref([
  { name: '房务部', score: 92 },
  { name: '前台部', score: 88 },
  { name: '餐饮部', score: 85 },
  { name: '工程部', score: 78 },
  { name: '业务部', score: 82 }
])

// 任务统计详情
const taskStatistics = ref([
  {
    department: '房务部',
    totalTasks: 320,
    completedTasks: 308,
    completionRate: 96,
    avgResponseTime: 12,
    avgCompletionTime: 35,
    satisfaction: 4.5,
    performance: 92
  },
  {
    department: '前台部',
    totalTasks: 280,
    completedTasks: 252,
    completionRate: 90,
    avgResponseTime: 8,
    avgCompletionTime: 25,
    satisfaction: 4.3,
    performance: 88
  },
  {
    department: '餐饮部',
    totalTasks: 200,
    completedTasks: 180,
    completionRate: 90,
    avgResponseTime: 15,
    avgCompletionTime: 40,
    satisfaction: 4.2,
    performance: 85
  },
  {
    department: '工程部',
    totalTasks: 150,
    completedTasks: 130,
    completionRate: 87,
    avgResponseTime: 20,
    avgCompletionTime: 60,
    satisfaction: 4.0,
    performance: 78
  },
  {
    department: '业务部',
    totalTasks: 300,
    completedTasks: 255,
    completionRate: 85,
    avgResponseTime: 18,
    avgCompletionTime: 50,
    satisfaction: 4.1,
    performance: 82
  }
])

// 系统告警信息
const systemAlerts = ref([
  {
    time: '2025-12-25 10:30:00',
    level: '高',
    type: '服务超时',
    department: '前台部',
    description: '客户入住办理超过15分钟',
    status: '待处理'
  },
  {
    time: '2025-12-25 09:15:00',
    level: '中',
    type: '任务积压',
    department: '房务部',
    description: '清洁任务积压超过10个',
    status: '已处理'
  },
  {
    time: '2025-12-24 16:45:00',
    level: '低',
    type: '设备故障',
    department: '工程部',
    description: '电梯运行异常',
    status: '已处理'
  }
])

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
