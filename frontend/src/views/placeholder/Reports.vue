<template>
  <div class="reports-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">报表分析</h1>
        <p class="page-subtitle">全面的数据分析和业务报表</p>
      </div>
      <div class="header-actions">
        <el-button-group>
          <el-button :type="reportPeriod === 'today' ? 'primary' : ''" @click="reportPeriod = 'today'">
            今日
          </el-button>
          <el-button :type="reportPeriod === 'week' ? 'primary' : ''" @click="reportPeriod = 'week'">
            本周
          </el-button>
          <el-button :type="reportPeriod === 'month' ? 'primary' : ''" @click="reportPeriod = 'month'">
            本月
          </el-button>
          <el-button :type="reportPeriod === 'year' ? 'primary' : ''" @click="reportPeriod = 'year'">
            今年
          </el-button>
        </el-button-group>
        <el-button @click="exportReport">
          <el-icon><Download /></el-icon>
          导出报表
        </el-button>
      </div>
    </div>

    <!-- 报表导航 -->
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <el-tab-pane label="营收报表" name="revenue">
        <RevenueReport :period="reportPeriod" />
      </el-tab-pane>
      <el-tab-pane label="客房报表" name="rooms">
        <RoomReport :period="reportPeriod" />
      </el-tab-pane>
      <el-tab-pane label="客户报表" name="customers">
        <CustomerReport :period="reportPeriod" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Download } from '@element-plus/icons-vue'
import RevenueReport from './reports/RevenueReport.vue'
import RoomReport from './reports/RoomReport.vue'
import CustomerReport from './reports/CustomerReport.vue'

// 状态
const activeTab = ref('revenue')
const reportPeriod = ref('month')

// 标签页切换
const handleTabClick = (tab: any) => {
  console.log('切换到:', tab.props.name)
}

// 导出报表
const exportReport = () => {
  // 导出当前报表
  console.log('导出报表:', activeTab.value, reportPeriod.value)
}
</script>

<style scoped>
.reports-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.page-subtitle {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

/* 响应式 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
}
</style>
