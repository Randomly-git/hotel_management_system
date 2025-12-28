<template>
  <div class="customer-report">
    <!-- 客户概览 -->
    <el-row :gutter="20" class="metrics-row">
      <el-col :xs="12" :sm="6" :md="3">
        <div class="metric-card total">
          <div class="metric-icon">
            <el-icon :size="24"><User /></el-icon>
          </div>
          <div class="metric-content">
            <div class="metric-value">{{ customerData.totalCustomers }}</div>
            <div class="metric-label">总客户数</div>
          </div>
        </div>
      </el-col>

      <el-col :xs="12" :sm="6" :md="3">
        <div class="metric-card new">
          <div class="metric-icon">
            <el-icon :size="24"><UserAdd /></el-icon>
          </div>
          <div class="metric-content">
            <div class="metric-value">{{ customerData.newCustomers }}</div>
            <div class="metric-label">新增客户</div>
            <div class="metric-trend positive">
              <el-icon><Top /></el-icon>
              <span>+15.3%</span>
            </div>
          </div>
        </div>
      </el-col>

      <el-col :xs="12" :sm="6" :md="3">
        <div class="metric-card repeat">
          <div class="metric-icon">
            <el-icon :size="24"><RefreshRight /></el-icon>
          </div>
          <div class="metric-content">
            <div class="metric-value">{{ customerData.repeatCustomers }}</div>
            <div class="metric-label">回头客</div>
            <div class="metric-rate">{{ customerData.repeatRate }}%</div>
          </div>
        </div>
      </el-col>

      <el-col :xs="12" :sm="6" :md="3">
        <div class="metric-card vip">
          <div class="metric-icon">
            <el-icon :size="24"><Star /></el-icon>
          </div>
          <div class="metric-content">
            <div class="metric-value">{{ customerData.vipCustomers }}</div>
            <div class="metric-label">VIP客户</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <!-- 客户增长趋势 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <el-icon><TrendCharts /></el-icon>
              <span>客户增长趋势</span>
            </div>
          </template>
          <div class="chart-placeholder">
            <div class="chart-content">
              <el-icon :size="48" class="chart-icon"><TrendCharts /></el-icon>
              <p>客户增长趋势图表</p>
              <small>基于{{ periodText }}的数据</small>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 客户来源分析 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <el-icon><PieChart /></el-icon>
              <span>客户来源分析</span>
            </div>
          </template>
          <div class="customer-source">
            <div class="source-item">
              <div class="source-label">OTA平台</div>
              <div class="source-bar">
                <div class="bar-fill" :style="{ width: '45%' }"></div>
              </div>
              <div class="source-value">45%</div>
            </div>
            <div class="source-item">
              <div class="source-label">直客预订</div>
              <div class="source-bar">
                <div class="bar-fill" :style="{ width: '30%' }"></div>
              </div>
              <div class="source-value">30%</div>
            </div>
            <div class="source-item">
              <div class="source-label">电话预订</div>
              <div class="source-bar">
                <div class="bar-fill" :style="{ width: '15%' }"></div>
              </div>
              <div class="source-value">15%</div>
            </div>
            <div class="source-item">
              <div class="source-label">其他渠道</div>
              <div class="source-bar">
                <div class="bar-fill" :style="{ width: '10%' }"></div>
              </div>
              <div class="source-value">10%</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 客户细分分析 -->
    <el-card class="segmentation-card">
      <template #header>
        <div class="card-header">
          <el-icon><DataLine /></el-icon>
          <span>客户细分分析</span>
        </div>
      </template>

      <el-table :data="customerSegments" stripe style="width: 100%">
        <el-table-column prop="segment" label="客户群体" width="150" />
        <el-table-column prop="count" label="客户数量" width="120" />
        <el-table-column prop="percentage" label="占比" width="100">
          <template #default="{ row }">
            <span>{{ row.percentage }}%</span>
          </template>
        </el-table-column>
        <el-table-column prop="avgSpent" label="平均消费" width="120">
          <template #default="{ row }">
            <span class="amount-text">¥{{ formatNumber(row.avgSpent) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="visitFrequency" label="到访频率" width="120">
          <template #default="{ row }">
            <span>{{ row.visitFrequency }}次/月</span>
          </template>
        </el-table-column>
        <el-table-column prop="satisfaction" label="满意度" width="120">
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
        <el-table-column prop="recommendation" label="营销建议" min-width="200" />
      </el-table>
    </el-card>

    <!-- 客户流失预警 -->
    <el-card class="churn-card">
      <template #header>
        <div class="card-header">
          <el-icon><Warning /></el-icon>
          <span>客户流失预警</span>
        </div>
      </template>

      <el-table :data="churnRisks" stripe style="width: 100%">
        <el-table-column prop="customerName" label="客户姓名" width="120" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="lastVisit" label="最后入住" width="120" />
        <el-table-column prop="daysSinceLastVisit" label="距今天数" width="100">
          <template #default="{ row }">
            <span :class="getDaysClass(row.daysSinceLastVisit)">
              {{ row.daysSinceLastVisit }}天
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="riskLevel" label="流失风险" width="120">
          <template #default="{ row }">
            <el-tag :type="getRiskTagType(row.riskLevel)">
              {{ row.riskLevel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalSpent" label="累计消费" width="120">
          <template #default="{ row }">
            <span class="amount-text">¥{{ formatNumber(row.totalSpent) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="挽留建议" min-width="200">
          <template #default="{ row }">
            <div class="retention-suggestion">
              <p v-for="suggestion in row.suggestions" :key="suggestion">
                • {{ suggestion }}
              </p>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import {
  User, Plus, RefreshRight, Star, TrendCharts, PieChart, DataLine, Warning, Top
} from '@element-plus/icons-vue'

// Props
interface Props {
  period: string
}

const props = defineProps<Props>()

// 客户数据
const customerData = reactive({
  totalCustomers: 1250,
  newCustomers: 85,
  repeatCustomers: 920,
  repeatRate: 74,
  vipCustomers: 156
})

// 客户细分数据
const customerSegments = ref([
  {
    segment: '高端商务客户',
    count: 156,
    percentage: 12.5,
    avgSpent: 2850,
    visitFrequency: 2.5,
    satisfaction: 4.8,
    recommendation: '提供专属管家服务，优先安排高层景观房'
  },
  {
    segment: '家庭度假客户',
    count: 312,
    percentage: 25.0,
    avgSpent: 1650,
    visitFrequency: 1.8,
    satisfaction: 4.6,
    recommendation: '推荐亲子房及儿童娱乐设施，推出家庭套餐'
  },
  {
    segment: '普通商务客户',
    count: 468,
    percentage: 37.4,
    avgSpent: 850,
    visitFrequency: 1.2,
    satisfaction: 4.2,
    recommendation: '提供标准商务服务，关注性价比'
  },
  {
    segment: '休闲旅游客户',
    count: 314,
    percentage: 25.1,
    avgSpent: 650,
    visitFrequency: 0.8,
    satisfaction: 4.0,
    recommendation: '推荐温泉SPA服务，提供旅游资讯'
  }
])

// 客户流失风险
const churnRisks = ref([
  {
    customerName: '张先生',
    phone: '138****1234',
    lastVisit: '2025-10-15',
    daysSinceLastVisit: 71,
    riskLevel: '高风险',
    totalSpent: 12500,
    suggestions: [
      '发送专属优惠券',
      '邀请参加会员活动',
      '提供个性化服务方案'
    ]
  },
  {
    customerName: '李女士',
    phone: '139****5678',
    lastVisit: '2025-11-02',
    daysSinceLastVisit: 53,
    riskLevel: '中风险',
    totalSpent: 8900,
    suggestions: [
      '发送节日问候',
      '推荐新服务项目',
      '提供积分兑换优惠'
    ]
  },
  {
    customerName: '王先生',
    phone: '137****9012',
    lastVisit: '2025-11-20',
    daysSinceLastVisit: 35,
    riskLevel: '低风险',
    totalSpent: 15600,
    suggestions: [
      '保持定期沟通',
      '推荐会员升级',
      '提供生日祝福'
    ]
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

// 获取天数样式
const getDaysClass = (days: number) => {
  if (days > 60) return 'high-risk'
  if (days > 30) return 'medium-risk'
  return 'low-risk'
}

// 获取风险标签类型
const getRiskTagType = (risk: string) => {
  const typeMap: Record<string, string> = {
    '高风险': 'danger',
    '中风险': 'warning',
    '低风险': 'success'
  }
  return typeMap[risk] || 'info'
}

// 监听period变化，更新数据
watch(() => props.period, (newPeriod) => {
  updateCustomerData(newPeriod)
})

const updateCustomerData = (period: string) => {
  // 根据不同时期更新数据
  const dataMap: Record<string, any> = {
    today: {
      totalCustomers: 1250,
      newCustomers: 3,
      repeatCustomers: 920,
      repeatRate: 74,
      vipCustomers: 156
    },
    week: {
      totalCustomers: 1250,
      newCustomers: 22,
      repeatCustomers: 920,
      repeatRate: 74,
      vipCustomers: 156
    },
    month: {
      totalCustomers: 1250,
      newCustomers: 85,
      repeatCustomers: 920,
      repeatRate: 74,
      vipCustomers: 156
    },
    year: {
      totalCustomers: 1250,
      newCustomers: 285,
      repeatCustomers: 920,
      repeatRate: 74,
      vipCustomers: 156
    }
  }

  const data = dataMap[period] || dataMap.month
  Object.assign(customerData, data)
}
</script>

<style scoped>
.customer-report {
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

.metric-card.new .metric-icon {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.metric-card.repeat .metric-icon {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
}

.metric-card.vip .metric-icon {
  background: linear-gradient(135deg, #a855f7 0%, #9333ea 100%);
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
  color: #10b981;
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

.customer-source {
  padding: 20px;
}

.source-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.source-label {
  width: 100px;
  font-size: 14px;
  color: #475569;
}

.source-bar {
  flex: 1;
  height: 12px;
  background: #e2e8f0;
  border-radius: 6px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #3b82f6 0%, #2563eb 100%);
  border-radius: 6px;
  transition: width 0.3s ease;
}

.source-value {
  width: 50px;
  text-align: right;
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}

.segmentation-card, .churn-card {
  margin-bottom: 20px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.amount-text {
  color: #f59e0b;
  font-weight: 600;
}

.retention-suggestion p {
  margin: 4px 0;
  font-size: 14px;
  color: #475569;
}

.high-risk {
  color: #ef4444;
  font-weight: 600;
}

.medium-risk {
  color: #f59e0b;
  font-weight: 600;
}

.low-risk {
  color: #10b981;
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

  .source-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .source-label {
    width: auto;
  }
}
</style>
