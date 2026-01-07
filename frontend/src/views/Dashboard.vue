<template>
  <div class="dashboard">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="banner-content">
        <h1>欢迎使用同济酒店管理系统</h1>
        <p>全面掌控酒店运营数据，智能决策提升收益</p>
      </div>
      <div class="banner-actions">
        <el-button type="primary" :icon="Plus" @click="handleQuickAction">
          快速入住
        </el-button>
        <el-button :icon="Calendar">查看房态</el-button>
      </div>
    </div>

    <!-- 关键指标卡片 -->
    <el-row :gutter="20" class="metrics-row">
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <div class="metric-card available">
          <div class="metric-icon">
            <el-icon :size="32"><House /></el-icon>
          </div>
          <div class="metric-content">
            <div class="metric-value">{{ stats.availableRooms }}</div>
            <div class="metric-label">可用房间</div>
            <div class="metric-trend">
              <el-icon><TrendCharts /></el-icon>
              <span>较昨日 +5%</span>
            </div>
          </div>
        </div>
      </el-col>

      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <div class="metric-card occupied">
          <div class="metric-icon">
            <el-icon :size="32"><UserFilled /></el-icon>
          </div>
          <div class="metric-content">
            <div class="metric-value">{{ stats.occupiedRooms }}</div>
            <div class="metric-label">已入住</div>
            <div class="metric-trend">
              <el-icon><TrendCharts /></el-icon>
              <span>入住率 {{ stats.occupancyRate }}%</span>
            </div>
          </div>
        </div>
      </el-col>

      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <div class="metric-card revenue">
          <div class="metric-icon">
            <el-icon :size="32"><Money /></el-icon>
          </div>
          <div class="metric-content">
            <div class="metric-value">€{{ stats.todayRevenue }}</div>
            <div class="metric-label">今日收入</div>
            <div class="metric-trend positive">
              <el-icon><Top /></el-icon>
              <span>+12.5%</span>
            </div>
          </div>
        </div>
      </el-col>

      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <div class="metric-card bookings">
          <div class="metric-icon">
            <el-icon :size="32"><Calendar /></el-icon>
          </div>
          <div class="metric-content">
            <div class="metric-value">{{ stats.todayBookings }}</div>
            <div class="metric-label">今日预订</div>
            <div class="metric-trend positive">
              <el-icon><Top /></el-icon>
              <span>+8.3%</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 数据图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <!-- 预订趋势图 -->
      <el-col :xs="24" :sm="24" :md="16" :lg="16">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon><TrendCharts /></el-icon>
                <span>预订趋势</span>
              </div>
              <div class="header-actions">
                <el-radio-group v-model="chartPeriod" size="small">
                  <el-radio-button value="week">周</el-radio-button>
                  <el-radio-button value="month">月</el-radio-button>
                  <el-radio-button value="year">年</el-radio-button>
                </el-radio-group>
              </div>
            </div>
          </template>
          <div class="chart-placeholder">
            <div class="placeholder-content">
              <el-icon :size="64"><TrendCharts /></el-icon>
              <p>预订趋势图表</p>
              <span class="placeholder-tip">（ECharts 或 Chart.js 集成后显示）</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 房型分布 -->
      <el-col :xs="24" :sm="24" :md="8" :lg="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon><PieChart /></el-icon>
                <span>房型分布</span>
              </div>
            </div>
          </template>
          <div class="room-types">
            <div v-for="room in roomTypes" :key="room.type" class="room-type-item">
              <div class="room-type-info">
                <div class="room-type-name">{{ room.type }}</div>
                <div class="room-type-count">{{ room.count }}间</div>
              </div>
              <el-progress
                :percentage="room.percentage"
                :color="room.color"
                :show-text="false"
                :stroke-width="6"
              />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 待办事项和快速操作 -->
    <el-row :gutter="20" class="tasks-row">
      <!-- 待处理任务 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card class="task-card">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon><List /></el-icon>
                <span>待处理任务</span>
                <el-badge :value="pendingTasks.length" class="task-badge" />
              </div>
              <el-button link type="primary">查看全部</el-button>
            </div>
          </template>
          <div class="task-list">
            <div v-for="task in pendingTasks" :key="task.id" class="task-item">
              <div class="task-icon" :class="task.type">
                <el-icon>
                  <Service v-if="task.type === 'service'" />
                  <Tools v-else-if="task.type === 'maintenance'" />
                  <Clock v-else-if="task.type === 'cleaning'" />
                  <Bell v-else />
                </el-icon>
              </div>
              <div class="task-content">
                <div class="task-title">{{ task.title }}</div>
                <div class="task-meta">
                  <span class="task-room">{{ task.room }}</span>
                  <span class="task-time">{{ task.time }}</span>
                </div>
              </div>
              <div class="task-priority">
                <el-tag :type="getPriorityType(task.priority)" size="small">
                  {{ task.priority }}
                </el-tag>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 快速操作 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card class="quick-actions-card">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon><Grid /></el-icon>
                <span>快速操作</span>
              </div>
            </div>
          </template>
          <div class="quick-actions-grid">
            <div v-for="action in quickActions" :key="action.name" class="quick-action-item" @click="handleQuickActionClick(action)">
              <div class="action-icon" :style="{ background: action.color }">
                <el-icon :size="24">
                  <component :is="action.icon" />
                </el-icon>
              </div>
              <span class="action-name">{{ action.name }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最新动态 -->
    <el-row :gutter="20" class="activity-row">
      <el-col :span="24">
        <el-card class="activity-card">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon><Bell /></el-icon>
                <span>最新动态</span>
              </div>
              <el-button link>查看全部</el-button>
            </div>
          </template>
          <el-timeline>
            <el-timeline-item
              v-for="activity in activities"
              :key="activity.id"
              :timestamp="activity.time"
              :color="activity.color"
            >
              <div class="activity-content">
                <span class="activity-user">{{ activity.user }}</span>
                <span class="activity-action">{{ activity.action }}</span>
                <span class="activity-target">{{ activity.target }}</span>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api/index'
import {
  Plus, Calendar, House, UserFilled, Money, TrendCharts, Top,
  List, Service, Tools, Clock, Bell, Grid, PieChart, Search, DocumentCopy, SwitchButton
} from '@element-plus/icons-vue'

// 统计数据
const stats = reactive({
  availableRooms: 0,
  occupiedRooms: 0,
  occupancyRate: 0,
  todayRevenue: '0',
  todayBookings: 0
})

// 图表周期
const chartPeriod = ref('week')

// 房型分布
const roomTypes = [
  { type: '标准间', count: 45, percentage: 35, color: '#3b82f6' },
  { type: '大床房', count: 38, percentage: 30, color: '#8b5cf6' },
  { type: '豪华套房', count: 25, percentage: 20, color: '#f59e0b' },
  { type: '总统套房', count: 20, percentage: 15, color: '#10b981' }
]

// 待处理任务
const pendingTasks = ref([
  { id: 1, type: 'maintenance', title: 'B102空调故障维修', room: 'B102', time: '14:30', priority: '紧急' },
  { id: 2, type: 'service', title: 'VIP客户要求额外毛巾', room: 'A301', time: '15:00', priority: '高' },
  { id: 3, type: 'cleaning', title: 'A205退房清洁', room: 'A205', time: '15:30', priority: '中' },
  { id: 4, type: 'service', title: 'C102客户需要叫醒服务', room: 'C102', time: '16:00', priority: '低' },
  { id: 5, type: 'maintenance', title: '电梯B例行检查', room: '公共区域', time: '17:00', priority: '中' }
])

// 快速操作
const quickActions = [
  { name: '办理入住', icon: 'UserFilled', color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
  { name: '办理退房', icon: 'SwitchButton', color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
  { name: '预订管理', icon: 'Calendar', color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' },
  { name: '房间状态', icon: 'House', color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)' },
  { name: '客户查询', icon: 'Search', color: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)' },
  { name: '报表导出', icon: 'DocumentCopy', color: 'linear-gradient(135deg, #30cfd0 0%, #330867 100%)' }
]

// 最新动态
const activities = ref([
  { id: 1, user: '张先生', action: '成功预订', target: 'A305豪华套房 - 3天', time: '10分钟前', color: '#10b981' },
  { id: 2, user: '李女士', action: '完成入住', target: 'B102标准间', time: '25分钟前', color: '#3b82f6' },
  { id: 3, user: '王先生', action: '申请退房', target: 'C203大床房', time: '1小时前', color: '#f59e0b' },
  { id: 4, user: '系统', action: '自动分配', target: '任务 #1234 → 工程部', time: '2小时前', color: '#8b5cf6' },
  { id: 5, user: '赵女士', action: '提交反馈', target: '服务评分 ⭐⭐⭐⭐⭐', time: '3小时前', color: '#ec4899' }
])

// 获取优先级标签类型
const getPriorityType = (priority: string) => {
  const map: Record<string, string> = {
    '紧急': 'danger',
    '高': 'warning',
    '中': 'primary',
    '低': 'info'
  }
  return map[priority] || 'info'
}

// 快速操作处理
const handleQuickAction = () => {
  ElMessage.info('快速入住功能开发中...')
}

const handleQuickActionClick = (action: any) => {
  ElMessage.success(`正在打开：${action.name}`)
}

// 加载实时数据
const loadRealData = async () => {
  try {
    const hotelId = 1

    // 并行请求所有数据
    const [roomStatsResponse, bookingsResponse, dashboardStatsResponse] = await Promise.all([
      api.get(`/api/rooms/hotel/${hotelId}/statistics`),
      api.get(`/api/bookings/hotel/${hotelId}`),
      api.get(`/api/v1/dashboard/overview?hotelId=${hotelId}`)
    ])

    // 更新房间统计
    if (roomStatsResponse.data) {
      stats.availableRooms = roomStatsResponse.data.available || 0
      stats.occupiedRooms = roomStatsResponse.data.occupied || 0
      const totalRooms = stats.availableRooms + stats.occupiedRooms
      stats.occupancyRate = totalRooms > 0
        ? Number(((stats.occupiedRooms / totalRooms) * 100).toFixed(1))
        : 0
    }

    // 更新预订统计
    if (bookingsResponse.data && Array.isArray(bookingsResponse.data)) {
      const today = new Date().toISOString().split('T')[0]
      const todayBookings = bookingsResponse.data.filter((booking: any) =>
        booking.bookingDate && booking.bookingDate.startsWith(today)
      )
      stats.todayBookings = todayBookings.length

      // 计算今日收入（已确认和已入住的预订总价）
      const todayRevenue = bookingsResponse.data
        .filter((b: any) => b.status === 'booked' || b.status === 'checked_in')
        .filter((b: any) => b.bookingDate && b.bookingDate.startsWith(today))
        .reduce((sum: number, b: any) => sum + (b.totalPrice || 0), 0)

      stats.todayRevenue = todayRevenue.toLocaleString('zh-CN', {
        minimumFractionDigits: 0,
        maximumFractionDigits: 0
      })
    }

    // 更新房型分布数据
    if (dashboardStatsResponse.data && dashboardStatsResponse.data.data) {
      const overviewData = dashboardStatsResponse.data.data
      console.log('Dashboard概览数据:', overviewData)
    }

    ElMessage.success('数据加载成功')
  } catch (error: any) {
    console.error('加载Dashboard数据失败:', error)
    ElMessage.warning('部分数据加载失败，显示备用数据')
    // 如果API失败，使用默认数据
    stats.availableRooms = 85
    stats.occupiedRooms = 62
    stats.occupancyRate = 42.2
    stats.todayRevenue = '28,500'
    stats.todayBookings = 23
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadRealData()
})
</script>

<style scoped>
.dashboard {
  animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 欢迎横幅 */
.welcome-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 32px;
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.25);
  color: #ffffff;
}

.banner-content h1 {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.banner-content p {
  font-size: 14px;
  margin: 0;
  opacity: 0.9;
}

.banner-actions {
  display: flex;
  gap: 12px;
}

/* 指标卡片 */
.metrics-row {
  margin-bottom: 24px;
}

.metric-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  border: 1px solid rgba(0, 0, 0, 0.04);
  height: 100%;
}

.metric-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.metric-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.available .metric-icon {
  background: linear-gradient(135deg, #e0f2fe 0%, #bae6fd 100%);
  color: #0284c7;
}

.occupied .metric-icon {
  background: linear-gradient(135deg, #dcfce7 0%, #bbf7d0 100%);
  color: #16a34a;
}

.revenue .metric-icon {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  color: #d97706;
}

.bookings .metric-icon {
  background: linear-gradient(135deg, #f3e8ff 0%, #e9d5ff 100%);
  color: #9333ea;
}

.metric-content {
  flex: 1;
}

.metric-value {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.2;
}

.metric-label {
  font-size: 14px;
  color: #6b7280;
  margin-top: 4px;
  font-weight: 500;
}

.metric-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 8px;
  font-size: 12px;
  color: #10b981;
}

.metric-trend.positive {
  color: #10b981;
}

/* 图表卡片 */
.charts-row {
  margin-bottom: 24px;
}

.chart-card {
  height: 100%;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
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

.task-badge {
  margin-left: 8px;
}

.chart-placeholder {
  height: 280px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f9fafb 0%, #f3f4f6 100%);
  border-radius: 12px;
}

.placeholder-content {
  text-align: center;
  color: #9ca3af;
}

.placeholder-content .el-icon {
  margin-bottom: 12px;
  color: #d1d5db;
}

.placeholder-content p {
  font-size: 16px;
  margin: 0 0 4px 0;
}

.placeholder-tip {
  font-size: 12px;
  color: #d1d5db;
}

/* 房型分布 */
.room-types {
  padding: 8px 0;
}

.room-type-item {
  margin-bottom: 20px;
}

.room-type-item:last-child {
  margin-bottom: 0;
}

.room-type-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.room-type-name {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.room-type-count {
  font-size: 14px;
  color: #6b7280;
}

/* 任务列表 */
.tasks-row {
  margin-bottom: 24px;
}

.task-card,
.quick-actions-card,
.activity-card {
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  height: 100%;
}

.task-list {
  max-height: 400px;
  overflow-y: auto;
}

.task-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-radius: 12px;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.task-item:hover {
  background: #f9fafb;
  border-color: #e5e7eb;
}

.task-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.task-icon.service {
  background: #dbeafe;
  color: #2563eb;
}

.task-icon.maintenance {
  background: #fecaca;
  color: #dc2626;
}

.task-icon.cleaning {
  background: #d1fae5;
  color: #059669;
}

.task-content {
  flex: 1;
}

.task-title {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 4px;
}

.task-meta {
  font-size: 12px;
  color: #6b7280;
}

.task-room {
  margin-right: 12px;
}

/* 快速操作 */
.quick-actions-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.quick-action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 24px 16px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.quick-action-item:hover {
  background: #f9fafb;
  border-color: #e5e7eb;
  transform: translateY(-2px);
}

.action-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
}

.action-name {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

/* 最新动态 */
.activity-row {
  margin-bottom: 24px;
}

.activity-content {
  font-size: 14px;
  color: #4b5563;
}

.activity-user {
  font-weight: 600;
  color: #1f2937;
  margin-right: 8px;
}

.activity-action {
  margin-right: 8px;
}

.activity-target {
  color: #6b7280;
}

/* Element Plus卡片样式优化 */
:deep(.el-card__header) {
  border-bottom: 1px solid #e5e7eb;
  padding: 20px 24px;
  background: transparent;
}

:deep(.el-card__body) {
  padding: 24px;
}

:deep(.el-timeline-item__timestamp) {
  color: #9ca3af;
  font-size: 12px;
}

/* 响应式 */
@media (max-width: 768px) {
  .welcome-banner {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }

  .quick-actions-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .metric-value {
    font-size: 24px;
  }
}
</style>
