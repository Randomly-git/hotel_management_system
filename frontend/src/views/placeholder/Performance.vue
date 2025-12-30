<template>
  <div class="performance-container">
    <el-card class="control-card">
      <div class="header-content">
        <div class="title-section">
          <h2 class="page-title">部门绩效管理</h2>
          <p class="page-subtitle">实时监控各部门绩效表现，AI驱动的改进建议</p>
        </div>
        <div class="action-section">
          <el-space>
            <!-- 时间段选择 -->
            <el-date-picker
                v-model="dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                :clearable="false"
                style="width: 300px"
                @change="handleDateChange"
            />
            <!-- 计算按钮 -->
            <el-button
                type="primary"
                :icon="Calculator"
                :loading="calculating"
                @click="handleCalculatePerformance"
            >
              计算绩效
            </el-button>
            <!-- 刷新按钮 -->
            <el-button
                :icon="Refresh"
                @click="loadPerformanceData"
            >
              刷新数据
            </el-button>
          </el-space>
        </div>
      </div>
    </el-card>

    <!-- 绩效概览卡片 -->
    <div class="overview-cards" v-loading="loading">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="metric-card">
            <div class="metric-content">
              <div class="metric-icon">
                <el-icon><User /></el-icon>
              </div>
              <div class="metric-data">
                <div class="metric-value">{{ overviewStats.totalDepartments }}</div>
                <div class="metric-label">活跃部门</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="metric-card">
            <div class="metric-content">
              <div class="metric-icon">
                <el-icon><Star /></el-icon>
              </div>
              <div class="metric-data">
                <div class="metric-value">{{ overviewStats.averageScore.toFixed(1) }}</div>
                <div class="metric-label">平均评分</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="metric-card">
            <div class="metric-content">
              <div class="metric-icon">
                <el-icon><TrendCharts /></el-icon>
              </div>
              <div class="metric-data">
                <div class="metric-value">{{ overviewStats.improvingCount }}</div>
                <div class="metric-label">上升趋势</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="metric-card">
            <div class="metric-content">
              <div class="metric-icon">
                <el-icon><Warning /></el-icon>
              </div>
              <div class="metric-data">
                <div class="metric-value">{{ overviewStats.warningCount }}</div>
                <div class="metric-label">需关注</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 绩效对比图表 -->
    <el-card class="chart-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span class="header-title">部门绩效对比</span>
          <el-radio-group v-model="chartView" size="small">
            <el-radio-button label="score">评分对比</el-radio-button>
            <el-radio-button label="trend">趋势变化</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <div class="chart-container">
        <!-- 暂时使用简单的表格展示替代复杂图表 -->
        <el-table :data="chartData" stripe style="width: 100%" v-if="chartView === 'score'">
          <el-table-column prop="department" label="部门" width="150" />
          <el-table-column prop="score" label="绩效评分" width="120">
            <template #default="scope">
              <el-progress
                :percentage="scope.row.score * 20"
                :format="(percentage) => `${scope.row.score}分`"
                :color="getScoreColor(scope.row.score)"
              />
            </template>
          </el-table-column>
          <el-table-column prop="level" label="等级" width="100">
            <template #default="scope">
              <el-tag :type="getScoreTagType(scope.row.score)">
                {{ scope.row.level }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>

        <el-table :data="trendData" stripe style="width: 100%" v-else>
          <el-table-column prop="date" label="日期" width="120" />
          <el-table-column v-for="dept in departmentList" :key="dept" :label="dept" width="100">
            <template #default="scope">
              <span :class="getTrendClass(scope.row[dept]?.change)">
                {{ scope.row[dept]?.score || '-' }}
              </span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 部门绩效详情表格 -->
    <el-card class="table-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span class="header-title">部门绩效详情</span>
          <el-tag type="info">{{ performanceList.length }} 个部门</el-tag>
        </div>
      </template>

      <el-table :data="performanceList" stripe style="width: 100%" border>
        <el-table-column label="部门名称" width="150" fixed>
          <template #default="scope">
            <span>{{ scope.row.department?.deptName || scope.row.departmentName }}</span>
          </template>
        </el-table-column>

        <el-table-column label="绩效评分" width="120">
          <template #default="scope">
            <div class="score-display">
              <el-rate
                  v-model="scope.row.scoreIndex"
                  disabled
                  show-score
                  :colors="['#F56C6C', '#E6A23C', '#67C23A']"
                  :max="5"
              />
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="totalReviews" label="评价总数" width="100" sortable />

        <el-table-column label="预警等级" width="120">
          <template #default="scope">
            <el-tag
                :type="getAlertLevelType(scope.row.alertLevel)"
                size="small"
            >
              {{ getAlertLevelText(scope.row.alertLevel) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="趋势状态" width="120">
          <template #default="scope">
            <div class="trend-display">
              <el-icon
                  :class="getTrendIconClass(scope.row.trendStatus)"
                  class="trend-icon"
              />
              <span>{{ getTrendText(scope.row.trendStatus) }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="statisticsDate" label="统计日期" width="120" sortable />

        <el-table-column label="改进建议" min-width="200">
          <template #default="scope">
            <el-tooltip
                :content="scope.row.improvementSuggestions"
                placement="top"
                :disabled="!scope.row.improvementSuggestions"
            >
              <div class="suggestion-text">
                {{ scope.row.improvementSuggestions || '暂无建议' }}
              </div>
            </el-tooltip>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button
                type="primary"
                size="small"
                :icon="View"
                @click="viewDepartmentDetail(scope.row)"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <template #empty>
        <el-empty
            description="暂无绩效数据，请选择时间段并点击计算绩效"
            :image-size="80"
        >
          <el-button type="primary" @click="handleCalculatePerformance">
            开始计算
          </el-button>
        </el-empty>
      </template>
    </el-card>

    <!-- 部门详情对话框 -->
    <el-dialog
        v-model="detailDialogVisible"
        :title="`${selectedDepartment?.department?.deptName || selectedDepartment?.departmentName || ''} - 绩效详情`"
        width="800px"
        :close-on-click-modal="false"
    >
      <div v-if="selectedDepartment" class="department-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="部门名称">
            {{ selectedDepartment.department?.deptName || selectedDepartment.departmentName }}
          </el-descriptions-item>
          <el-descriptions-item label="绩效评分">
            <el-rate
                v-model="selectedDepartment.scoreIndex"
                disabled
                show-score
            />
          </el-descriptions-item>
          <el-descriptions-item label="评价总数">
            {{ selectedDepartment.totalReviews }}
          </el-descriptions-item>
          <el-descriptions-item label="预警等级">
            <el-tag :type="getAlertLevelType(selectedDepartment.alertLevel)">
              {{ getAlertLevelText(selectedDepartment.alertLevel) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="趋势状态">
            <div class="trend-display">
              <el-icon :class="getTrendIconClass(selectedDepartment.trendStatus)" />
              <span>{{ getTrendText(selectedDepartment.trendStatus) }}</span>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="统计日期">
            {{ selectedDepartment.statisticsDate }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="suggestions-section" v-if="selectedDepartment.improvementSuggestions">
          <h4>AI改进建议</h4>
          <el-alert
              :title="selectedDepartment.improvementSuggestions"
              type="info"
              :closable="false"
              show-icon
          />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Calculator, Refresh, User, Star, TrendCharts, Warning,
  View, ArrowUp, ArrowDown, Minus
} from '@element-plus/icons-vue'
import { performanceApi } from '@/api'

// 类型定义
interface DepartmentPerformance {
  recordId: number
  department: {
    deptId: number
    deptName: string
  }
  departmentName?: string
  scoreIndex: number
  totalReviews: number
  alertLevel: string
  statisticsDate: string
  trendStatus: string
  improvementSuggestions: string
  hotelId: string
}

// 响应式数据
const dateRange = ref<[string, string]>([
  new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
  new Date().toISOString().split('T')[0]
])

const performanceList = ref<DepartmentPerformance[]>([])
const loading = ref(false)
const calculating = ref(false)
const chartView = ref('score')
const detailDialogVisible = ref(false)
const selectedDepartment = ref<DepartmentPerformance | null>(null)
const chartData = ref<any[]>([])
const trendData = ref<any[]>([])
const departmentList = ref<string[]>([])

// 概览统计
const overviewStats = reactive({
  totalDepartments: 0,
  averageScore: 0,
  improvingCount: 0,
  warningCount: 0
})

// 图表实例
let chartInstance: any = null

// 生命周期
onMounted(() => {
  loadPerformanceData()
  initChart()
})

// 监听图表视图变化
watch(chartView, () => {
  updateChartData()
})

// 方法
const loadPerformanceData = async () => {
  loading.value = true
  try {
    const [startDate, endDate] = dateRange.value
    const response = await performanceApi.getPerformanceHistory({
      hotelId: 'DEFAULT_HOTEL',
      startDate,
      endDate
    })

    performanceList.value = response.data
    updateOverviewStats()
    updateChartData()
  } catch (error) {
    console.error('加载绩效数据失败:', error)
    ElMessage.error('加载绩效数据失败')
  } finally {
    loading.value = false
  }
}

const handleDateChange = () => {
  loadPerformanceData()
}

const handleCalculatePerformance = async () => {
  calculating.value = true
  try {
    const [startDate, endDate] = dateRange.value

    await ElMessageBox.confirm(
      `确定要计算 ${startDate} 至 ${endDate} 的部门绩效吗？`,
      '确认计算',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // 计算每一天的绩效
    const start = new Date(startDate)
    const end = new Date(endDate)
    const days = []

    for (let d = new Date(start); d <= end; d.setDate(d.getDate() + 1)) {
      days.push(new Date(d).toISOString().split('T')[0])
    }

    for (const date of days) {
      await performanceApi.calculatePerformance({
        hotelId: 'DEFAULT_HOTEL',
        date
      })
    }

    ElMessage.success(`成功计算 ${days.length} 天的绩效数据`)
    await loadPerformanceData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('计算绩效失败:', error)
      ElMessage.error('计算绩效失败')
    }
  } finally {
    calculating.value = false
  }
}

const updateOverviewStats = () => {
  const list = performanceList.value
  overviewStats.totalDepartments = list.length
  overviewStats.averageScore = list.length > 0
    ? list.reduce((sum, item) => sum + item.scoreIndex, 0) / list.length
    : 0
  overviewStats.improvingCount = list.filter(item => item.trendStatus === 'UP').length
  overviewStats.warningCount = list.filter(item => ['高', '中'].includes(item.alertLevel)).length
}

const updateChartData = () => {
  const data = performanceList.value

  if (chartView.value === 'score') {
    // 评分对比数据
    const departments = [...new Set(data.map(item => item.department?.deptName || item.departmentName))]
    departmentList.value = departments

    chartData.value = departments.map(deptName => {
      const deptData = data.filter(item => (item.department?.deptName || item.departmentName) === deptName)
      const score = deptData.length > 0 ? deptData[0].scoreIndex : 0
      return {
        department: deptName,
        score: score,
        level: getPerformanceLevel(score)
      }
    }).sort((a, b) => b.score - a.score)
  } else {
    // 趋势变化数据
    const dates = [...new Set(data.map(item => item.statisticsDate))].sort()
    const departments = [...new Set(data.map(item => item.department?.deptName || item.departmentName))]
    departmentList.value = departments

    trendData.value = dates.map(date => {
      const row: any = { date }
      departments.forEach(deptName => {
        const record = data.find(item =>
          (item.department?.deptName || item.departmentName) === deptName && item.statisticsDate === date
        )
        if (record) {
          row[deptName] = {
            score: record.scoreIndex,
            change: record.trendStatus
          }
        }
      })
      return row
    })
  }
}

const getPerformanceLevel = (score: number) => {
  if (score >= 4.5) return '优秀'
  if (score >= 4.0) return '良好'
  if (score >= 3.5) return '一般'
  if (score >= 3.0) return '需改进'
  return '严重不足'
}

const getScoreColor = (score: number) => {
  if (score >= 4.5) return '#67C23A'
  if (score >= 4.0) return '#95D475'
  if (score >= 3.5) return '#E6A23C'
  if (score >= 3.0) return '#F5DEB3'
  return '#F56C6C'
}

const getScoreTagType = (score: number) => {
  if (score >= 4.5) return 'success'
  if (score >= 4.0) return 'info'
  if (score >= 3.5) return 'warning'
  return 'danger'
}

const getTrendClass = (change: string) => {
  switch (change) {
    case 'UP': return 'trend-up'
    case 'DOWN': return 'trend-down'
    default: return 'trend-stable'
  }
}

const getAlertLevelType = (level: string) => {
  switch (level) {
    case '高': return 'danger'
    case '中': return 'warning'
    case '低': return 'success'
    default: return 'info'
  }
}

const getAlertLevelText = (level: string) => {
  switch (level) {
    case '高': return '高风险'
    case '中': return '中风险'
    case '低': return '低风险'
    default: return level
  }
}

const getTrendIconClass = (trend: string) => {
  switch (trend) {
    case 'UP': return 'trend-up'
    case 'DOWN': return 'trend-down'
    default: return 'trend-stable'
  }
}

const getTrendText = (trend: string) => {
  switch (trend) {
    case 'UP': return '上升'
    case 'DOWN': return '下降'
    case 'STABLE': return '稳定'
    default: return trend
  }
}

const viewDepartmentDetail = (department: DepartmentPerformance) => {
  selectedDepartment.value = department
  detailDialogVisible.value = true
}

// 移除echarts相关的窗口监听器
</script>

<style scoped>
.performance-container {
  padding: 24px;
  background-color: #f8fafc;
  min-height: calc(100vh - 100px);
}

.control-card {
  margin-bottom: 24px;
  border-radius: 12px;
  border: none;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 4px 0;
}

.page-subtitle {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

.overview-cards {
  margin-bottom: 24px;
}

.metric-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.metric-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.metric-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 20px;
}

.metric-data {
  flex: 1;
}

.metric-value {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1.2;
}

.metric-label {
  font-size: 12px;
  color: #64748b;
  margin-top: 4px;
}

.chart-card, .table-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-title {
  font-weight: 600;
  color: #334155;
}

.chart-container {
  width: 100%;
  min-height: 400px;
}

.score-display {
  display: flex;
  align-items: center;
  gap: 8px;
}

.trend-display {
  display: flex;
  align-items: center;
  gap: 4px;
}

.trend-icon.trend-up {
  color: #67C23A;
}

.trend-icon.trend-down {
  color: #F56C6C;
}

.trend-icon.trend-stable {
  color: #909399;
}

.trend-up {
  color: #67C23A;
  font-weight: bold;
}

.trend-down {
  color: #F56C6C;
  font-weight: bold;
}

.trend-stable {
  color: #909399;
}

.suggestion-text {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
  color: #475569;
}

.department-detail {
  padding: 20px 0;
}

.suggestions-section {
  margin-top: 24px;
}

.suggestions-section h4 {
  margin: 0 0 12px 0;
  color: #1e293b;
  font-weight: 600;
}

/* ECharts 容器样式 */
#performanceChart {
  width: 100% !important;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .action-section {
    width: 100%;
  }

  .action-section .el-space {
    flex-wrap: wrap;
    width: 100%;
  }

  .action-section .el-date-picker {
    width: 100% !important;
    margin-bottom: 8px;
  }

  .metric-content {
    flex-direction: column;
    text-align: center;
    gap: 8px;
  }

  .card-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
}
</style>