<template>
  <div class="revenue-report">
    <!-- 营收概览 -->
    <el-row :gutter="20" class="metrics-row">
      <el-col :xs="12" :sm="6" :md="3">
        <div class="metric-card total">
          <div class="metric-icon">
            <el-icon :size="24"><Money /></el-icon>
          </div>
          <div class="metric-content">
            <div class="metric-value">€{{ formatNumber(revenueData.totalRevenue) }}</div>
            <div class="metric-label">总营收</div>
            <div class="metric-trend positive">
              <el-icon><Top /></el-icon>
              <span>+12.5%</span>
            </div>
          </div>
        </div>
      </el-col>

      <el-col :xs="12" :sm="6" :md="3">
        <div class="metric-card room">
          <div class="metric-icon">
            <el-icon :size="24"><House /></el-icon>
          </div>
          <div class="metric-content">
            <div class="metric-value">€{{ formatNumber(revenueData.roomRevenue) }}</div>
            <div class="metric-label">客房收入</div>
            <div class="metric-trend positive">
              <el-icon><Top /></el-icon>
              <span>+8.3%</span>
            </div>
          </div>
        </div>
      </el-col>

      <el-col :xs="12" :sm="6" :md="3">
        <div class="metric-card food">
          <div class="metric-icon">
            <el-icon :size="24"><ForkSpoon /></el-icon>
          </div>
          <div class="metric-content">
            <div class="metric-value">€{{ formatNumber(revenueData.foodRevenue) }}</div>
            <div class="metric-label">餐饮收入</div>
            <div class="metric-trend positive">
              <el-icon><Top /></el-icon>
              <span>+15.2%</span>
            </div>
          </div>
        </div>
      </el-col>

      <el-col :xs="12" :sm="6" :md="3">
        <div class="metric-card other">
          <div class="metric-icon">
            <el-icon :size="24"><Service /></el-icon>
          </div>
          <div class="metric-content">
            <div class="metric-value">€{{ formatNumber(revenueData.otherRevenue) }}</div>
            <div class="metric-label">其他收入</div>
            <div class="metric-trend negative">
              <el-icon><Bottom /></el-icon>
              <span>-2.1%</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <!-- 营收趋势图 -->
      <el-col :xs="24" :sm="24" :md="16" :lg="16">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <el-icon><TrendCharts /></el-icon>
              <span>营收趋势</span>
            </div>
          </template>
          <div class="chart-placeholder">
            <div class="chart-content">
              <el-icon :size="48" class="chart-icon"><TrendCharts /></el-icon>
              <p>营收趋势图表</p>
              <small>基于{{ periodText }}的数据</small>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 营收构成 -->
      <el-col :xs="24" :sm="24" :md="8" :lg="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <el-icon><PieChart /></el-icon>
              <span>营收构成</span>
            </div>
          </template>
          <div class="revenue-composition">
            <div class="composition-item">
              <div class="item-label">客房收入</div>
              <div class="item-bar">
                <div class="bar-fill room-bar" :style="{ width: '65%' }"></div>
              </div>
              <div class="item-value">65%</div>
            </div>
            <div class="composition-item">
              <div class="item-label">餐饮收入</div>
              <div class="item-bar">
                <div class="bar-fill food-bar" :style="{ width: '25%' }"></div>
              </div>
              <div class="item-value">25%</div>
            </div>
            <div class="composition-item">
              <div class="item-label">其他收入</div>
              <div class="item-bar">
                <div class="bar-fill other-bar" :style="{ width: '10%' }"></div>
              </div>
              <div class="item-value">10%</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细数据表格 -->
    <el-card class="data-table-card">
      <template #header>
        <div class="card-header">
          <el-icon><DataLine /></el-icon>
          <span>营收明细</span>
        </div>
      </template>

      <el-table :data="revenueDetails" stripe style="width: 100%">
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="roomRevenue" label="客房收入" width="120">
          <template #default="{ row }">
            <span class="amount-text">€{{ formatNumber(row.roomRevenue) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="foodRevenue" label="餐饮收入" width="120">
          <template #default="{ row }">
            <span class="amount-text">€{{ formatNumber(row.foodRevenue) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="otherRevenue" label="其他收入" width="120">
          <template #default="{ row }">
            <span class="amount-text">€{{ formatNumber(row.otherRevenue) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalRevenue" label="总收入" width="120">
          <template #default="{ row }">
            <span class="total-amount">€{{ formatNumber(row.totalRevenue) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="occupancyRate" label="入住率" width="100">
          <template #default="{ row }">
            <span>{{ row.occupancyRate }}%</span>
          </template>
        </el-table-column>
        <el-table-column prop="avgRoomRate" label="平均房价" width="120">
          <template #default="{ row }">
            <span class="amount-text">€{{ formatNumber(row.avgRoomRate) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import {
  Money, House, ForkSpoon, Service, TrendCharts, PieChart, DataLine, Top, Bottom
} from '@element-plus/icons-vue'

// Props
interface Props {
  period: string
}

const props = defineProps<Props>()

// 营收数据
const revenueData = reactive({
  totalRevenue: 1250000,
  roomRevenue: 812500,
  foodRevenue: 312500,
  otherRevenue: 125000
})

// 营收明细
const revenueDetails = ref([
  {
    date: '2025-12-25',
    roomRevenue: 45000,
    foodRevenue: 12000,
    otherRevenue: 3000,
    totalRevenue: 60000,
    occupancyRate: 85,
    avgRoomRate: 380
  },
  {
    date: '2025-12-24',
    roomRevenue: 52000,
    foodRevenue: 15000,
    otherRevenue: 4000,
    totalRevenue: 71000,
    occupancyRate: 92,
    avgRoomRate: 420
  },
  {
    date: '2025-12-23',
    roomRevenue: 38000,
    foodRevenue: 10000,
    otherRevenue: 2000,
    totalRevenue: 50000,
    occupancyRate: 78,
    avgRoomRate: 350
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

// 监听period变化，更新数据
watch(() => props.period, (newPeriod) => {
  // 根据不同时期更新数据
  updateRevenueData(newPeriod)
})

const updateRevenueData = (period: string) => {
  // 模拟不同时期的数据
  const dataMap: Record<string, any> = {
    today: {
      totalRevenue: 60000,
      roomRevenue: 45000,
      foodRevenue: 12000,
      otherRevenue: 3000
    },
    week: {
      totalRevenue: 420000,
      roomRevenue: 280000,
      foodRevenue: 120000,
      otherRevenue: 20000
    },
    month: {
      totalRevenue: 1250000,
      roomRevenue: 812500,
      foodRevenue: 312500,
      otherRevenue: 125000
    },
    year: {
      totalRevenue: 15000000,
      roomRevenue: 9750000,
      foodRevenue: 3750000,
      otherRevenue: 1500000
    }
  }

  const data = dataMap[period] || dataMap.month
  Object.assign(revenueData, data)
}
</script>

<style scoped>
.revenue-report {
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
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.metric-card.room .metric-icon {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
}

.metric-card.food .metric-icon {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}

.metric-card.other .metric-icon {
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
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

.metric-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  margin-top: 4px;
}

.metric-trend.positive {
  color: #10b981;
}

.metric-trend.negative {
  color: #ef4444;
}

.charts-row {
  margin-bottom: 20px;
}

.chart-card {
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  height: 400px;
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
  height: 320px;
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

.revenue-composition {
  padding: 20px;
}

.composition-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.item-label {
  width: 80px;
  font-size: 14px;
  color: #475569;
}

.item-bar {
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

.room-bar {
  background: linear-gradient(90deg, #3b82f6 0%, #2563eb 100%);
}

.food-bar {
  background: linear-gradient(90deg, #f59e0b 0%, #d97706 100%);
}

.other-bar {
  background: linear-gradient(90deg, #8b5cf6 0%, #7c3aed 100%);
}

.item-value {
  width: 40px;
  text-align: right;
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}

.data-table-card {
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.amount-text {
  color: #f59e0b;
  font-weight: 600;
}

.total-amount {
  color: #10b981;
  font-weight: 700;
}

/* 响应式 */
@media (max-width: 768px) {
  .metrics-row {
    grid-template-columns: repeat(2, 1fr);
  }

  .charts-row {
    grid-template-columns: 1fr;
  }

  .composition-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .item-label {
    width: auto;
  }
}
</style>
