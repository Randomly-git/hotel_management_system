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
                :icon="Setting"
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
        <!-- 评分对比图表 -->
        <div id="scoreChart" class="chart-content" v-if="chartView === 'score'" v-loading="loading"></div>

        <!-- 趋势变化图表 -->
        <div id="trendChart" class="chart-content" v-else v-loading="loading"></div>
      </div>
    </el-card>

    <!-- 部门绩效详情表格 -->
    <el-card class="table-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span class="header-title">部门绩效详情</span>
          <el-tag type="info">{{ pagination.total }} 个部门</el-tag>
        </div>
      </template>

      <el-table :data="paginatedPerformanceList" stripe style="width: 100%" border>
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

      <!-- 分页组件 -->
      <div class="pagination-container" v-if="pagination.total > 0">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[5, 10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>

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
import { ref, reactive, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Setting, Refresh, User, Star, TrendCharts, Warning,
  View, ArrowUp, ArrowDown, Minus
} from '@element-plus/icons-vue'
import { performanceApi } from '@/api'
import * as echarts from 'echarts'

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
  new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0] || '',
  new Date().toISOString().split('T')[0] || ''
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

// 分页相关
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 计算分页数据
const paginatedPerformanceList = computed(() => {
  const start = (pagination.currentPage - 1) * pagination.pageSize
  const end = start + pagination.pageSize
  return performanceList.value.slice(start, end)
})

// 图表实例
let scoreChartInstance: any = null
let trendChartInstance: any = null

// 生命周期
onMounted(() => {
  loadPerformanceData()
  nextTick(() => {
    initCharts()
  })
})

onUnmounted(() => {
  if (scoreChartInstance) {
    scoreChartInstance.dispose()
  }
  if (trendChartInstance) {
    trendChartInstance.dispose()
  }
})

// 监听图表视图变化
watch(chartView, () => {
  updateChartData()
  nextTick(() => {
    updateCharts()
  })
})

// 更新分页信息
const updatePagination = () => {
  pagination.total = performanceList.value.length
  // 如果当前页超出范围，重置到第一页
  if (pagination.currentPage > Math.ceil(pagination.total / pagination.pageSize)) {
    pagination.currentPage = 1
  }
}

// 分页大小改变
const handleSizeChange = (newSize: number) => {
  pagination.pageSize = newSize
  pagination.currentPage = 1
}

// 当前页改变
const handleCurrentChange = (newPage: number) => {
  pagination.currentPage = newPage
}

// 方法
const loadPerformanceData = async () => {
  console.log('开始加载绩效数据，设置loading为true')
  loading.value = true
  try {
    const [startDate, endDate] = dateRange.value
    const response = await performanceApi.getPerformanceHistory({
      hotelId: '1',
      startDate,
      endDate
    })

    console.log('绩效数据响应:', response.data)
    console.log('响应状态:', response.status)
    console.log('响应数据类型:', typeof response.data)
    if (typeof response.data === 'object' && response.data !== null) {
      console.log('响应数据keys:', Object.keys(response.data))
      console.log('是否为数组:', Array.isArray(response.data))
      if (response.data.data) {
        console.log('data字段内容:', response.data.data)
        console.log('data是否为数组:', Array.isArray(response.data.data))
      }
    }

    // 检查响应是否成功（兼容不同的响应格式）
    const isSuccess = response.data &&
                     (response.data.code === 200 ||
                      response.data.success === true ||
                      (response.data.code === undefined && response.data.message === undefined))

    if (isSuccess) {
      // 处理数据：优先使用data字段，否则直接使用响应数据
      let data = response.data.data
      if (!data && Array.isArray(response.data)) {
        data = response.data // 如果响应数据直接是数组
      }
      performanceList.value = data || []
      console.log('设置绩效数据:', performanceList.value)
    } else {
      performanceList.value = []
      const errorMessage = response.data?.message || '加载绩效数据失败'
      console.log('响应错误信息:', errorMessage)
      ElMessage.error(errorMessage)
    }

    updateOverviewStats()
    updatePagination()
    updateChartData()
    nextTick(() => {
      updateCharts()
    })
  } catch (error) {
    console.error('加载绩效数据失败:', error)
    performanceList.value = []
    ElMessage.error('加载绩效数据失败')
  } finally {
    console.log('加载完成，设置loading为false')
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
        hotelId: '1',
        date: date || ''
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
  console.log('更新概览统计，数据列表:', list)

  overviewStats.totalDepartments = list.length
  overviewStats.averageScore = list.length > 0
    ? list.reduce((sum, item) => sum + item.scoreIndex, 0) / list.length
    : 0
  overviewStats.improvingCount = list.filter(item => item.trendStatus === 'UP').length
  overviewStats.warningCount = list.filter(item => ['高', '中'].includes(item.alertLevel)).length

  console.log('概览统计结果:', overviewStats)
}

const updateChartData = () => {
  const data = performanceList.value

  if (chartView.value === 'score') {
    // 评分对比数据
    const departments = [...new Set(data.map(item => item.department?.deptName || item.departmentName || '').filter(name => name !== ''))]
    departmentList.value = departments

    chartData.value = departments.map(deptName => {
      const deptData = data.filter(item => (item.department?.deptName || item.departmentName || '') === deptName)
      const score = deptData.length > 0 ? deptData[0]?.scoreIndex || 0 : 0
      return {
        department: deptName,
        score: score,
        level: getPerformanceLevel(score)
      }
    }).sort((a, b) => b.score - a.score)
  } else {
    // 趋势变化数据
    const dates = [...new Set(data.map(item => item.statisticsDate))].sort()
    const departments = [...new Set(data.map(item => item.department?.deptName || item.departmentName || '').filter(name => name !== ''))]
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
  if (score >= 90) return '优秀'
  if (score >= 80) return '良好'
  if (score >= 70) return '一般'
  if (score >= 60) return '需改进'
  return '严重不足'
}

const getScoreColor = (score: number) => {
  if (score >= 90) return '#67C23A'
  if (score >= 80) return '#95D475'
  if (score >= 70) return '#E6A23C'
  if (score >= 60) return '#F5DEB3'
  return '#F56C6C'
}

const getScoreTagType = (score: number) => {
  if (score >= 90) return 'success'
  if (score >= 80) return 'info'
  if (score >= 70) return 'warning'
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

// 图表初始化
const initCharts = () => {
  const scoreChartDom = document.getElementById('scoreChart')
  const trendChartDom = document.getElementById('trendChart')

  if (scoreChartDom) {
    scoreChartInstance = echarts.init(scoreChartDom)
  }
  if (trendChartDom) {
    trendChartInstance = echarts.init(trendChartDom)
  }

  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
}

const handleResize = () => {
  if (scoreChartInstance) {
    scoreChartInstance.resize()
  }
  if (trendChartInstance) {
    trendChartInstance.resize()
  }
}

// 更新图表数据
const updateCharts = () => {
  const data = performanceList.value

  // 评分对比图
  if (scoreChartInstance && chartView.value === 'score') {
    const departments = [...new Set(data.map(item => item.department?.deptName || item.departmentName || '').filter(name => name !== ''))]
    const chartData = departments.map(deptName => {
      const deptData = data.filter(item => (item.department?.deptName || item.departmentName || '') === deptName)
      const score = deptData.length > 0 ? deptData[0]?.scoreIndex || 0 : 0
      return {
        name: deptName,
        value: score,
        level: getPerformanceLevel(score)
      }
    }).sort((a, b) => b.value - a.value)

    const option = {
      title: {
        text: '部门绩效评分对比',
        left: 'center',
        textStyle: {
          color: '#1e293b',
          fontSize: 16,
          fontWeight: '600'
        }
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        },
        formatter: (params: any) => {
          const item = params[0]
          return `${item.name}<br/>评分: ${item.value.toFixed(1)}<br/>等级: ${getPerformanceLevel(item.value)}`
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: chartData.map(item => item.name),
        axisLabel: {
          rotate: 45,
          interval: 0
        }
      },
      yAxis: {
        type: 'value',
        min: 0,
        max: 100,
        axisLabel: {
          formatter: '{value}'
        }
      },
      series: [{
        name: '绩效评分',
        type: 'bar',
        data: chartData.map(item => ({
          value: item.value,
          itemStyle: {
            color: getScoreColor(item.value)
          }
        })),
        label: {
          show: true,
          position: 'top',
          formatter: '{c}'
        }
      }]
    }

    scoreChartInstance.setOption(option)
  }

  // 趋势变化图
  if (trendChartInstance && chartView.value === 'trend') {
    const dates = [...new Set(data.map(item => item.statisticsDate))].sort()
    const departments = [...new Set(data.map(item => item.department?.deptName || item.departmentName || '').filter(name => name !== ''))]

    const series = departments.map(deptName => ({
      name: deptName,
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      lineStyle: {
        width: 2
      },
      data: dates.map(date => {
        const record = data.find(item =>
          (item.department?.deptName || item.departmentName) === deptName && item.statisticsDate === date
        )
        return record ? record.scoreIndex : null
      })
    }))

    const option = {
      title: {
        text: '部门绩效趋势变化',
        left: 'center',
        textStyle: {
          color: '#1e293b',
          fontSize: 16,
          fontWeight: '600'
        }
      },
      tooltip: {
        trigger: 'axis',
        formatter: (params: any) => {
          let result = `${params[0].name}<br/>`
          params.forEach((item: any) => {
            if (item.value !== null) {
              result += `${item.seriesName}: ${item.value.toFixed(1)}<br/>`
            }
          })
          return result
        }
      },
      legend: {
        data: departments,
        top: '10%'
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        top: '20%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: dates
      },
      yAxis: {
        type: 'value',
        min: 0,
        max: 100,
        axisLabel: {
          formatter: '{value}'
        }
      },
      series: series
    }

    trendChartInstance.setOption(option)
  }
}
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

.chart-content {
  width: 100%;
  height: 500px;
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
  line-clamp: 2;
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

/* 分页容器样式 */
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding: 16px 0;
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