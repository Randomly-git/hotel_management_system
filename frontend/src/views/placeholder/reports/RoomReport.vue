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
      <!-- 入住率趋势 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <el-icon><TrendCharts /></el-icon>
              <span>入住率趋势</span>
            </div>
          </template>
          <div class="chart-placeholder">
            <div class="chart-content">
              <el-icon :size="48" class="chart-icon"><TrendCharts /></el-icon>
              <p>入住率趋势图表</p>
              <small>基于{{ periodText }}的数据</small>
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

    <!-- 房型分析 -->
    <el-card class="room-type-card">
      <template #header>
        <div class="card-header">
          <el-icon><DataLine /></el-icon>
          <span>房型分析</span>
        </div>
      </template>

      <el-table :data="roomTypeData" stripe style="width: 100%">
        <el-table-column prop="typeName" label="房型" width="150" />
        <el-table-column prop="totalRooms" label="房间总数" width="100" />
        <el-table-column prop="occupiedRooms" label="已入住" width="100" />
        <el-table-column prop="occupancyRate" label="入住率" width="100">
          <template #default="{ row }">
            <span>{{ row.occupancyRate }}%</span>
          </template>
        </el-table-column>
        <el-table-column prop="avgPrice" label="平均价格" width="120">
          <template #default="{ row }">
            <span class="amount-text">€{{ row.avgPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="revenue" label="营收贡献" width="120">
          <template #default="{ row }">
            <span class="amount-text">€{{ formatNumber(row.revenue) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="utilizationRate" label="利用率排名" width="120">
          <template #default="{ row }">
            <el-tag :type="getRankTagType(row.rank)">
              第{{ row.rank }}名
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 每日房间状态详情 -->
    <el-card class="daily-status-card">
      <template #header>
        <div class="card-header">
          <el-icon><Calendar /></el-icon>
          <span>每日房间状态详情</span>
        </div>
      </template>

      <el-table :data="dailyRoomStatus" stripe style="width: 100%">
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="totalRooms" label="总房间" width="100" />
        <el-table-column prop="available" label="可用" width="100" />
        <el-table-column prop="occupied" label="已入住" width="100" />
        <el-table-column prop="cleaning" label="清洁中" width="100" />
        <el-table-column prop="maintenance" label="维护中" width="100" />
        <el-table-column prop="occupancyRate" label="入住率" width="100">
          <template #default="{ row }">
            <span :class="getOccupancyClass(row.occupancyRate)">
              {{ row.occupancyRate }}%
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="avgRate" label="平均房价" width="120">
          <template #default="{ row }">
            <span class="amount-text">€{{ row.avgRate }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import {
  House, UserFilled, CircleCheck, Tools, TrendCharts, PieChart, DataLine, Calendar
} from '@element-plus/icons-vue'

// Props
interface Props {
  period: string
}

const props = defineProps<Props>()

// 房间数据
const roomData = reactive({
  totalRooms: 120,
  occupiedRooms: 85,
  availableRooms: 28,
  cleaningRooms: 5,
  maintenanceRooms: 2,
  occupancyRate: 71
})

// 房型数据
const roomTypeData = ref([
  {
    typeName: '标准间',
    totalRooms: 60,
    occupiedRooms: 42,
    occupancyRate: 70,
    avgPrice: 380,
    revenue: 15960,
    rank: 1
  },
  {
    typeName: '大床房',
    totalRooms: 30,
    occupiedRooms: 24,
    occupancyRate: 80,
    avgPrice: 450,
    revenue: 10800,
    rank: 2
  },
  {
    typeName: '豪华套房',
    totalRooms: 20,
    occupiedRooms: 14,
    occupancyRate: 70,
    avgPrice: 680,
    revenue: 9520,
    rank: 3
  },
  {
    typeName: '总统套房',
    totalRooms: 10,
    occupiedRooms: 5,
    occupancyRate: 50,
    avgPrice: 1280,
    revenue: 6400,
    rank: 4
  }
])

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

// 监听period变化，更新数据
watch(() => props.period, (newPeriod) => {
  updateRoomData(newPeriod)
})

const updateRoomData = (period: string) => {
  // 根据不同时期更新数据
  const dataMap: Record<string, any> = {
    today: {
      totalRooms: 120,
      occupiedRooms: 85,
      availableRooms: 28,
      cleaningRooms: 5,
      maintenanceRooms: 2,
      occupancyRate: 71
    },
    week: {
      totalRooms: 120,
      occupiedRooms: 75,
      availableRooms: 38,
      cleaningRooms: 5,
      maintenanceRooms: 2,
      occupancyRate: 63
    },
    month: {
      totalRooms: 120,
      occupiedRooms: 82,
      availableRooms: 31,
      cleaningRooms: 5,
      maintenanceRooms: 2,
      occupancyRate: 68
    },
    year: {
      totalRooms: 120,
      occupiedRooms: 78,
      availableRooms: 35,
      cleaningRooms: 5,
      maintenanceRooms: 2,
      occupancyRate: 65
    }
  }

  const data = dataMap[period] || dataMap.month
  Object.assign(roomData, data)
}
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

.room-type-card, .daily-status-card {
  margin-bottom: 20px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
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
