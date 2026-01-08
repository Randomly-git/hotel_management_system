<template>
  <div class="room-report">
    <!-- 客房概览 -->
    <el-row :gutter="20" class="metrics-row">
      <el-col :xs="12" :sm="6" :md="3">
        <div class="metric-card total">
          <div class="metric-icon">
            <el-icon :size="24"><House /></el-icon>
          </div>
          <div class="metric-content">
            <div class="metric-value">{{ roomData.totalRooms }}</div>
            <div class="metric-label">总房间数</div>
          </div>
        </div>
      </el-col>

      <el-col :xs="12" :sm="6" :md="3">
        <div class="metric-card occupied">
          <div class="metric-icon">
            <el-icon :size="24"><UserFilled /></el-icon>
          </div>
          <div class="metric-content">
            <div class="metric-value">{{ roomData.occupiedRooms }}</div>
            <div class="metric-label">已入住</div>
            <div class="metric-rate">{{ roomData.occupancyRate }}%</div>
          </div>
        </div>
      </el-col>

      <el-col :xs="12" :sm="6" :md="3">
        <div class="metric-card available">
          <div class="metric-icon">
            <el-icon :size="24"><CircleCheck /></el-icon>
          </div>
          <div class="metric-content">
            <div class="metric-value">{{ roomData.availableRooms }}</div>
            <div class="metric-label">可用房间</div>
          </div>
        </div>
      </el-col>

      <el-col :xs="12" :sm="6" :md="3">
        <div class="metric-card maintenance">
          <div class="metric-icon">
            <el-icon :size="24"><Tools /></el-icon>
          </div>
          <div class="metric-content">
            <div class="metric-value">{{ roomData.maintenanceRooms }}</div>
            <div class="metric-label">维护中</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <!-- 总体入住率趋势 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <el-icon><TrendCharts /></el-icon>
              <span>总体入住率趋势</span>
            </div>
          </template>
          <div class="chart-container">
            <LineChart
              v-if="overallOccupancyTrend.length > 0"
              :data="overallOccupancyTrend"
              :xField="'date'"
              :yField="'occupancyRate'"
              :smooth="true"
              :point="true"
              :height="300"
            />
            <div v-else class="chart-placeholder">
              <div class="chart-content">
                <el-icon :size="48" class="chart-icon"><TrendCharts /></el-icon>
                <p>暂无数据</p>
                <small>基于{{ periodText }}的数据</small>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 房间状态分布 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <el-icon><PieChart /></el-icon>
              <span>房间状态分布</span>
            </div>
          </template>
          <div class="room-status-distribution">
            <div class="status-item">
              <div class="status-color available"></div>
              <span class="status-label">可用</span>
              <span class="status-count">{{ roomData.availableRooms }}</span>
              <span class="status-percent">{{ ((roomData.availableRooms / roomData.totalRooms) * 100).toFixed(1) }}%</span>
            </div>
            <div class="status-item">
              <div class="status-color occupied"></div>
              <span class="status-label">已入住</span>
              <span class="status-count">{{ roomData.occupiedRooms }}</span>
              <span class="status-percent">{{ ((roomData.occupiedRooms / roomData.totalRooms) * 100).toFixed(1) }}%</span>
            </div>
            <div class="status-item">
              <div class="status-color cleaning"></div>
              <span class="status-label">清洁中</span>
              <span class="status-count">{{ roomData.cleaningRooms }}</span>
              <span class="status-percent">{{ ((roomData.cleaningRooms / roomData.totalRooms) * 100).toFixed(1) }}%</span>
            </div>
            <div class="status-item">
              <div class="status-color maintenance"></div>
              <span class="status-label">维护中</span>
              <span class="status-count">{{ roomData.maintenanceRooms }}</span>
              <span class="status-percent">{{ ((roomData.maintenanceRooms / roomData.totalRooms) * 100).toFixed(1) }}%</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 房型分析和房型入住趋势 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="room-type-analysis-card">
          <template #header>
            <div class="card-header">
              <el-icon><DataLine /></el-icon>
              <span>房型分析</span>
            </div>
          </template>

          <el-table :data="roomTypeData" stripe style="width: 100%" :height="500">
            <el-table-column prop="typeName" label="房型" width="120" />
            <el-table-column prop="totalRooms" label="房间总数" width="80" />
            <el-table-column prop="occupiedRooms" label="已入住" width="70" />
            <el-table-column prop="occupancyRate" label="入住率" width="70">
              <template #default="{ row }">
                <span>{{ row.occupancyRate }}%</span>
              </template>
            </el-table-column>
            <el-table-column prop="avgPrice" label="平均价格" width="90">
              <template #default="{ row }">
                <span class="amount-text">€{{ row.avgPrice }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="revenue" label="营收贡献" width="90">
              <template #default="{ row }">
                <span class="amount-text">€{{ formatNumber(row.revenue) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="rank" label="利用率排名" width="80">
              <template #default="{ row }">
                <el-tag :type="getRankTagType(row.rank)" size="small">
                  第{{ row.rank }}名
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="room-type-trend-card">
          <template #header>
            <div class="card-header">
              <el-icon><TrendCharts /></el-icon>
              <span>房型入住趋势</span>
            </div>
          </template>
          <div class="chart-container">
            <LineChart
              v-if="roomTypeTrendData.length > 0"
              :data="roomTypeTrendData"
              :xField="'date'"
              :yField="'occupied'"
              :seriesField="'roomType'"
              :smooth="true"
              :point="false"
              :height="490"
            />
            <div v-else class="chart-placeholder">
              <div class="chart-content">
                <el-icon :size="48" class="chart-icon"><TrendCharts /></el-icon>
                <p>暂无数据</p>
                <small>基于过去3个月的数据</small>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import {
  House, UserFilled, CircleCheck, Tools, TrendCharts, PieChart, DataLine, Calendar
} from '@element-plus/icons-vue'
import LineChart from '@/components/LineChart.vue'
import api from '@/api'

// Props
interface Props {
  period?: string
}

const props = withDefaults(defineProps<Props>(), {
  period: 'month'
})

// 房间数据（从room statistics API获取）
const roomData = reactive({
  totalRooms: 0,
  occupiedRooms: 0,
  availableRooms: 0,
  cleaningRooms: 0,
  maintenanceRooms: 0,
  occupancyRate: 0
})

// 房型数据
const roomTypeData = ref<any[]>([])

// 趋势图数据
const overallOccupancyTrend = ref<any[]>([])
const roomTypeTrendData = ref<any[]>([])

const loading = ref(false)

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const hotelId = 1 // 默认酒店ID

    // 并行获取房间统计和房型报表数据
    const [roomStatsResponse, roomReportResponse] = await Promise.all([
      api.get(`/api/rooms/hotel/${hotelId}/statistics`),
      api.get(`/api/reports/rooms?hotelId=${hotelId}&period=${props.period}`)
    ])

    // 更新房间统计数据
    if (roomStatsResponse.data) {
      roomData.totalRooms = roomStatsResponse.data.total || 0
      roomData.occupiedRooms = roomStatsResponse.data.occupied || 0
      roomData.availableRooms = roomStatsResponse.data.available || 0
      roomData.cleaningRooms = roomStatsResponse.data.cleaning || 0
      roomData.maintenanceRooms = roomStatsResponse.data.maintenance || 0

      const total = roomData.totalRooms
      roomData.occupancyRate = total > 0 ? Math.round((roomData.occupiedRooms / total) * 100) : 0
    }

    // 更新房型数据
    if (roomReportResponse.data && roomReportResponse.data.roomStats) {
      roomTypeData.value = roomReportResponse.data.roomStats.map((stat: any, index: number) => ({
        ...stat,
        rank: index + 1
      }))
    }

    // 更新总体入住率趋势数据
    if (roomReportResponse.data && roomReportResponse.data.overallOccupancyTrend) {
      overallOccupancyTrend.value = roomReportResponse.data.overallOccupancyTrend
    }

    // 更新房型入住趋势数据
    if (roomReportResponse.data && roomReportResponse.data.roomTypeTrends) {
      roomTypeTrendData.value = []
      roomReportResponse.data.roomTypeTrends.forEach((trend: any) => {
        if (trend.data && Array.isArray(trend.data)) {
          trend.data.forEach((item: any) => {
            roomTypeTrendData.value.push({
              date: item.date,
              occupied: item.occupied,
              roomType: trend.roomType
            })
          })
        }
      })
    }

  } catch (error) {
    console.error('加载客房报表数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 每日房间状态
const dailyRoomStatus = ref([
  {
    date: '2025-12-25',
    totalRooms: 120,
    available: 35,
    occupied: 78,
    cleaning: 5,
    maintenance: 2,
    occupancyRate: 65,
    avgRate: 420
  },
  {
    date: '2025-12-24',
    totalRooms: 120,
    available: 28,
    occupied: 85,
    cleaning: 5,
    maintenance: 2,
    occupancyRate: 71,
    avgRate: 445
  },
  {
    date: '2025-12-23',
    totalRooms: 120,
    available: 42,
    occupied: 72,
    cleaning: 4,
    maintenance: 2,
    occupancyRate: 60,
    avgRate: 395
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

// 格式化数字
const formatNumber = (num: number) => {
  return num.toLocaleString()
}

// 获取排名标签类型
const getRankTagType = (rank: number) => {
  if (rank === 1) return 'danger'
  if (rank === 2) return 'warning'
  if (rank === 3) return 'info'
  return 'info'
}

// 获取入住率样式
const getOccupancyClass = (rate: number) => {
  if (rate >= 80) return 'high-occupancy'
  if (rate >= 60) return 'medium-occupancy'
  return 'low-occupancy'
}

// 监听period变化，重新加载数据
watch(() => props.period, () => {
  loadData()
})

// 组件挂载时加载数据
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.room-report {
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

.metric-card.total .metric-icon {
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
}

.metric-card.occupied .metric-icon {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
}

.metric-card.available .metric-icon {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.metric-card.maintenance .metric-icon {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
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
  color: #3b82f6;
  margin-top: 4px;
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

.room-status-distribution {
  padding: 20px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px;
  background: #f8fafc;
  border-radius: 8px;
}

.status-color {
  width: 16px;
  height: 16px;
  border-radius: 50%;
}

.status-color.available {
  background: #10b981;
}

.status-color.occupied {
  background: #3b82f6;
}

.status-color.cleaning {
  background: #f59e0b;
}

.status-color.maintenance {
  background: #ef4444;
}

.status-label {
  flex: 1;
  font-size: 14px;
  color: #475569;
}

.status-count {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  min-width: 40px;
  text-align: right;
}

.status-percent {
  font-size: 14px;
  color: #64748b;
  min-width: 50px;
  text-align: right;
}

.occupancy-trend-card, .room-type-analysis-card, .room-type-trend-card {
  margin-bottom: 20px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.chart-container {
  width: 100%;
  min-height: 200px;
}

.room-type-trend-card .chart-container {
  min-height: 500px;
}

.amount-text {
  color: #f59e0b;
  font-weight: 600;
}

.high-occupancy {
  color: #10b981;
  font-weight: 600;
}

.medium-occupancy {
  color: #f59e0b;
  font-weight: 600;
}

.low-occupancy {
  color: #ef4444;
  font-weight: 600;
}

/* 响应式 */
@media (max-width: 768px) {
  .metrics-row {
    grid-template-columns: repeat(2, 1fr);
  }

  .charts-row {
    grid-template-columns: 1fr;
  }

  .status-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
