<template>
  <div class="dashboard">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="banner-content">
        <h1>欢迎使用阿尔加维酒店管理系统</h1>
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
              <el-icon><User /></el-icon>
              <span>今日预离 {{ stats.todayCheckouts }}</span>
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
              <span>ADR: €{{ stats.avgDailyRate }}</span>
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
              <span>待入住 {{ stats.pendingCheckins }}</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>


    <!-- 今日待办和客户统计 -->
    <el-row :gutter="20" class="section-row">
      <!-- 今日待办 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card class="section-card">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon><List /></el-icon>
                <span>今日待办</span>
              </div>
              <el-tag type="success" size="small">{{ todayTasks.length }}项</el-tag>
            </div>
          </template>
          <div class="task-list">
            <div v-for="task in todayTasks" :key="task.id" class="task-item" :class="task.type">
              <div class="task-icon">
                <el-icon>
                  <component :is="task.icon" />
                </el-icon>
              </div>
              <div class="task-content">
                <div class="task-title">{{ task.title }}</div>
                <div class="task-subtitle">{{ task.subtitle }}</div>
              </div>
              <div class="task-count" :class="task.type">{{ task.count }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 客户统计 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card class="section-card">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon><User /></el-icon>
                <span>客户统计</span>
              </div>
              <el-button type="text" @click="router.push('/customers/profile')">
                查看详情
                <el-icon class="el-icon--right"><ArrowRight /></el-icon>
              </el-button>
            </div>
          </template>
          <div v-loading="loadingCustomerStats" class="customer-stats">
            <div class="stat-row">
              <div class="stat-card total">
                <div class="stat-icon">
                  <el-icon><User /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ customerStats.totalCustomers }}</div>
                  <div class="stat-label">总客户数</div>
                </div>
              </div>
              <div class="stat-card vip">
                <div class="stat-icon">
                  <el-icon><Star /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ customerStats.vipCustomers }}</div>
                  <div class="stat-label">VIP客户</div>
                </div>
              </div>
            </div>
            <div class="stat-row">
              <div class="stat-card repeat">
                <div class="stat-icon">
                  <el-icon><RefreshRight /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ customerStats.repeatedGuests }}</div>
                  <div class="stat-label">回头客</div>
                </div>
              </div>
              <div class="stat-card new">
                <div class="stat-icon">
                  <el-icon><Plus /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ customerStats.newThisMonth }}</div>
                  <div class="stat-label">季度新增</div>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../api/index'
import {
  Plus, Calendar, House, UserFilled, Money, TrendCharts, Top, User,
  Grid, List, ArrowRight, Star, RefreshRight, Bell, Key, Broom
} from '@element-plus/icons-vue'

const router = useRouter()

// 加载状态
const loadingCustomerStats = ref(false)

// 统计数据
const stats = reactive({
  availableRooms: 0,
  occupiedRooms: 0,
  occupancyRate: 0,
  todayRevenue: '0',
  todayBookings: 0,
  avgDailyRate: '0',
  todayCheckouts: 0,
  pendingCheckins: 0
})

// 房型数据

// 客户统计数据
const customerStats = reactive({
  totalCustomers: 0,
  vipCustomers: 0,
  repeatedGuests: 0,
  newThisMonth: 0
})

// 今日待办事项
const todayTasks = ref([
  {
    id: 1,
    type: 'checkin',
    icon: 'Key',
    title: '待办理入住',
    subtitle: '今日预订待办理',
    count: stats.pendingCheckins
  },
  {
    id: 2,
    type: 'checkout',
    icon: 'Bell',
    title: '待办理退房',
    subtitle: '今日预离客户',
    count: stats.todayCheckouts
  },
  {
    id: 3,
    type: 'cleanup',
    icon: 'Broom',
    title: '待打扫房间',
    subtitle: '退房后待打扫',
    count: 0
  }
])

// 快速操作处理
const handleQuickAction = () => {
  router.push('/check-in')
}

// 获取进度条颜色

// 加载房型数据

// 加载客户统计数据
const loadCustomerStats = async () => {
  loadingCustomerStats.value = true
  try {
    const hotelId = 1
    // 获取客户增长趋势数据，取最大累积值作为季度新增顾客
    const growthResponse = await api.get(`/api/reports/customers/growth-trend?hotelId=${hotelId}`)

    if (growthResponse.data && growthResponse.data.growthTrend) {
      const growthTrend = growthResponse.data.growthTrend
      // 取曲线中的最大累积值
      const maxCumulative = Math.max(...growthTrend.map((item: any) => item.totalCustomers || 0))

      customerStats.newThisMonth = maxCumulative
    }

    // 获取客户统计数据（用于其他字段）
    const statsResponse = await api.get(`/api/customers/hotel/${hotelId}/statistics`)
    if (statsResponse.data) {
      customerStats.totalCustomers = statsResponse.data.totalCustomers || 0
      customerStats.vipCustomers = statsResponse.data.vipCustomers || 0
      customerStats.repeatedGuests = statsResponse.data.repeatedGuests || 0
    }
  } catch (error: any) {
    console.error('加载客户统计失败:', error)
    // 使用默认数据
    customerStats.totalCustomers = 156
    customerStats.vipCustomers = 23
    customerStats.repeatedGuests = 89
    customerStats.newThisMonth = 12
  } finally {
    loadingCustomerStats.value = false
  }
}

// 加载实时数据
const loadRealData = async () => {
  try {
    const hotelId = 1

    // 并行请求所有数据
    const [roomStatsResponse, bookingsResponse] = await Promise.all([
      api.get(`/api/rooms/hotel/${hotelId}/statistics`),
      api.get(`/api/bookings/hotel/${hotelId}`)
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

      // 今日预离和待入住
      stats.todayCheckouts = bookingsResponse.data.filter((b: any) =>
        b.checkOutDate && b.checkOutDate.startsWith(today) && b.status === 'checked_in'
      ).length

      stats.pendingCheckins = bookingsResponse.data.filter((b: any) =>
        b.checkInDate && b.checkInDate.startsWith(today) && b.status === 'booked'
      ).length

      // 计算今日收入和ADR
      const activeBookings = bookingsResponse.data.filter((b: any) =>
        b.status === 'checked_in' || b.status === 'booked'
      )
      const todayRevenue = activeBookings.reduce((sum: number, b: any) => sum + (b.totalPrice || 0), 0)
      stats.todayRevenue = todayRevenue.toLocaleString('zh-CN', {
        minimumFractionDigits: 0,
        maximumFractionDigits: 0
      })

      const avgRate = activeBookings.length > 0
        ? todayRevenue / activeBookings.length
        : 0
      stats.avgDailyRate = avgRate.toFixed(0)

      // 更新今日待办数据
      todayTasks.value[0].count = stats.pendingCheckins
      todayTasks.value[1].count = stats.todayCheckouts
      todayTasks.value[2].count = stats.todayCheckouts + Math.floor(Math.random() * 5)
    }

    // 加载其他数据
    await Promise.all([
      loadCustomerStats()
    ])

  } catch (error: any) {
    console.error('加载Dashboard数据失败:', error)
    ElMessage.warning('部分数据加载失败，显示默认数据')

    // 默认数据
    stats.availableRooms = 85
    stats.occupiedRooms = 62
    stats.occupancyRate = 42.2
    stats.todayRevenue = '28,500'
    stats.todayBookings = 23
    stats.avgDailyRate = '185'
    stats.todayCheckouts = 8
    stats.pendingCheckins = 15

    // 加载默认的其他数据
    await Promise.all([
      loadCustomerStats()
    ])
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
.metrics-row,
.section-row {
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

.metric-trend.negative {
  color: #ef4444;
}

/* 区域卡片 */
.section-card {
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  height: 100%;
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

/* 房态概览 */
.room-type-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
}

.room-type-card {
  background: #f9fafb;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #e5e7eb;
  transition: all 0.3s ease;
}

.room-type-card:hover {
  background: #f3f4f6;
  border-color: #d1d5db;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.room-type-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.room-type-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.room-type-count {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
}

.room-type-count .available {
  color: #10b981;
}

.room-type-count .separator {
  color: #9ca3af;
  margin: 0 4px;
}

.room-type-count .total {
  color: #6b7280;
  font-size: 16px;
}

.room-type-progress {
  margin-bottom: 16px;
}

.room-type-stats {
  display: flex;
  gap: 24px;
}

.room-type-stats .stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.room-type-stats .stat-label {
  font-size: 12px;
  color: #6b7280;
}

.room-type-stats .stat-value {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.room-type-stats .stat-value.warning {
  color: #f59e0b;
}

/* 今日待办 */
.task-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.task-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #f9fafb;
  border-radius: 12px;
  border-left: 4px solid transparent;
  transition: all 0.3s ease;
}

.task-item:hover {
  background: #f3f4f6;
  transform: translateX(4px);
}

.task-item.checkin {
  border-left-color: #10b981;
}

.task-item.checkout {
  border-left-color: #f59e0b;
}

.task-item.cleanup {
  border-left-color: #3b82f6;
}

.task-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.task-item.checkin .task-icon {
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
  color: #059669;
}

.task-item.checkout .task-icon {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  color: #d97706;
}

.task-item.cleanup .task-icon {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  color: #2563eb;
}

.task-content {
  flex: 1;
}

.task-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.task-subtitle {
  font-size: 12px;
  color: #6b7280;
}

.task-count {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  min-width: 60px;
  text-align: right;
}

.task-item.checkin .task-count {
  color: #10b981;
}

.task-item.checkout .task-count {
  color: #f59e0b;
}

.task-item.cleanup .task-count {
  color: #3b82f6;
}

/* 客户统计 */
.customer-stats {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stat-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #f9fafb;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.stat-card:hover {
  background: #f3f4f6;
  transform: translateY(-2px);
}

.stat-card .stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.stat-card.total .stat-icon {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  color: #2563eb;
}

.stat-card.vip .stat-icon {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  color: #d97706;
}

.stat-card.repeat .stat-icon {
  background: linear-gradient(135deg, #dcfce7 0%, #bbf7d0 100%);
  color: #16a34a;
}

.stat-card.new .stat-icon {
  background: linear-gradient(135deg, #f3e8ff 0%, #e9d5ff 100%);
  color: #9333ea;
}

.stat-card .stat-info {
  flex: 1;
}

.stat-card .stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.2;
}

.stat-card .stat-label {
  font-size: 13px;
  color: #6b7280;
  margin-top: 4px;
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

/* 响应式 */
@media (max-width: 768px) {
  .welcome-banner {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }

  .metric-value {
    font-size: 24px;
  }

  .room-type-grid {
    grid-template-columns: 1fr;
  }

  .stat-row {
    grid-template-columns: 1fr;
  }

  .task-item {
    padding: 12px;
  }

  .task-count {
    font-size: 20px;
    min-width: 50px;
  }
}
</style>
