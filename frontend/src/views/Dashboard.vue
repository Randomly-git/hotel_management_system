<template>
  <div class="dashboard">
    <h1>数据概览</h1>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card class="stat-card available">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon size="32"><House /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">85</div>
              <div class="stat-label">可用房间</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card occupied">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon size="32"><UserFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">62</div>
              <div class="stat-label">已入住</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card revenue">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon size="32"><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">¥28.5K</div>
              <div class="stat-label">今日收入</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card booking">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon size="32"><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">23</div>
              <div class="stat-label">今日预订</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据表格 -->
    <el-row :gutter="20" class="data-tables">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近入住</span>
            </div>
          </template>
          <el-table :data="recentCheckIns" style="width: 100%">
            <el-table-column prop="customerName" label="客户姓名" width="100" />
            <el-table-column prop="roomNumber" label="房间号" width="100" />
            <el-table-column prop="checkInTime" label="入住时间" width="150" />
            <el-table-column prop="nights" label="天数" width="80" />
            <el-table-column prop="totalAmount" label="总金额" width="120">
              <template #default="scope">
                <span style="color: #409eff; font-weight: bold">
                  ¥{{ scope.row.totalAmount }}
                </span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>今日待处理</span>
            </div>
          </template>
          <el-table :data="pendingTasks" style="width: 100%">
            <el-table-column prop="type" label="类型" width="100">
              <template #default="scope">
                <el-tag :type="getTaskType(scope.row.type)" size="small">
                  {{ scope.row.type }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" />
            <el-table-column prop="priority" label="优先级" width="80">
              <template #default="scope">
                <el-tag :type="getPriorityType(scope.row.priority)" size="small">
                  {{ scope.row.priority }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="time" label="时间" width="100" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
// 模拟数据
const recentCheckIns = [
  { customerName: '张先生', roomNumber: 'A101', checkInTime: '14:30', nights: 2, totalAmount: 1360 },
  { customerName: '李女士', roomNumber: 'B102', checkInTime: '15:45', nights: 3, totalAmount: 2040 },
  { customerName: '王先生', roomNumber: 'C203', checkInTime: '16:20', nights: 1, totalAmount: 680 },
  { customerName: '赵女士', roomNumber: 'A305', checkInTime: '17:10', nights: 4, totalAmount: 2720 },
  { customerName: '陈先生', roomNumber: 'B201', checkInTime: '18:00', nights: 2, totalAmount: 1360 }
]

const pendingTasks = [
  { type: '客房服务', description: '301房间需要额外毛巾', priority: '高', time: '14:30' },
  { type: '维修', description: 'B102空调故障', priority: '紧急', time: '15:20' },
  { type: '餐饮', description: 'VIP客户晚餐预订', priority: '中', time: '16:00' },
  { type: '保洁', description: 'A101退房清洁', priority: '高', time: '17:30' },
  { type: '前台', description: '客户入住登记排队', priority: '中', time: '18:00' }
]

// 获取任务类型标签颜色
const getTaskType = (type: string) => {
  const typeMap: Record<string, string> = {
    '客房服务': 'primary',
    '维修': 'danger',
    '餐饮': 'success',
    '保洁': 'warning',
    '前台': 'info'
  }
  return typeMap[type] || 'primary'
}

// 获取优先级标签颜色
const getPriorityType = (priority: string) => {
  const priorityMap: Record<string, string> = {
    '紧急': 'danger',
    '高': 'warning',
    '中': 'primary',
    '低': 'info'
  }
  return priorityMap[priority] || 'primary'
}
</script>

<style scoped>
.dashboard h1 {
  margin-bottom: 30px;
  color: #2d3748;
  font-size: 28px;
  font-weight: 600;
  background: linear-gradient(135deg, #2c5282 0%, #3182ce 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-align: center;
}

.stats-cards {
  margin-bottom: 30px;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid rgba(255, 215, 0, 0.2);
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(49, 130, 206, 0.15);
  border-color: rgba(255, 215, 0, 0.4);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 24px;
}

.stat-icon {
  margin-right: 24px;
  width: 72px;
  height: 72px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.available .stat-icon {
  background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
  color: #1890ff;
}

.occupied .stat-icon {
  background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
  color: #52c41a;
}

.revenue .stat-icon {
  background: linear-gradient(135deg, #fff7e6 0%, #ffd591 100%);
  color: #ffd700;
}

.booking .stat-icon {
  background: linear-gradient(135deg, #f9f0ff 0%, #efdbff 100%);
  color: #722ed1;
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 8px;
  background: linear-gradient(135deg, #2d3748 0%, #4a5568 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.stat-label {
  font-size: 16px;
  color: #718096;
  font-weight: 500;
}

.data-tables {
  margin-top: 30px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
  color: #2d3748;
}

/* 美化卡片 */
:deep(.el-card) {
  border-radius: 16px;
  border: 1px solid rgba(49, 130, 206, 0.1);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

:deep(.el-card:hover) {
  box-shadow: 0 8px 24px rgba(49, 130, 206, 0.12);
}

:deep(.el-card__header) {
  background: linear-gradient(135deg, #f8fafc 0%, #ffffff 100%);
  border-bottom: 1px solid rgba(49, 130, 206, 0.1);
  padding: 20px 24px;
}

:deep(.el-card__body) {
  padding: 24px;
}

/* 美化表格 */
:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table th) {
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  color: #2d3748;
  font-weight: 600;
  border-bottom: 2px solid rgba(49, 130, 206, 0.2);
}

:deep(.el-table td) {
  border-bottom: 1px solid rgba(49, 130, 206, 0.1);
  padding: 16px 0;
}

:deep(.el-table--border td) {
  border-right: 1px solid rgba(49, 130, 206, 0.1);
}

/* 美化标签 */
:deep(.el-tag) {
  border-radius: 20px;
  padding: 6px 12px;
  font-weight: 500;
  border: none;
}
</style>