<template>
  <div class="placeholder-container">
    <el-card class="placeholder-card">
      <div class="placeholder-content">
        <div class="placeholder-icon">
          <el-icon :size="80" color="#94a3b8">
            <Tools />
          </el-icon>
        </div>
        <h2 class="placeholder-title">{{ title }}</h2>
        <p class="placeholder-description">
          该页面正在开发中，敬请期待...
        </p>
        <div class="placeholder-info">
          <el-alert
            type="info"
            :closable="false"
            show-icon
          >
            <template #title>
              <div class="info-content">
                <span>功能说明：</span>
                <ul>
                  <li v-for="feature in features" :key="feature">{{ feature }}</li>
                </ul>
              </div>
            </template>
          </el-alert>
        </div>
        <div class="placeholder-actions">
          <el-button type="primary" @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            返回上一页
          </el-button>
          <el-button @click="goDashboard">
            <el-icon><House /></el-icon>
            回到首页
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Tools, ArrowLeft, House } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const title = computed(() => route.meta.title as string || '功能开发中')

const features = computed(() => {
  const featureMap: Record<string, string[]> = {
    '报表分析': [
      '营收报表自动生成',
      '客房销售分析',
      '客户来源统计',
      '多维度数据导出'
    ],
    '经营分析': [
      '入住率趋势分析',
      '平均房价(ADR)监控',
      '每间可售房收入(RevPAR)',
      '经营绩效对比'
    ],
    '入住办理': [
      '快速入住登记',
      '身份证件扫描',
      '押金计算与收取',
      '房卡自动生成'
    ],
    '退房办理': [
      '账单自动结算',
      '押金退还处理',
      '房间状态更新',
      '发票打印功能'
    ],
    '换房/续住': [
      '房间变更处理',
      '续住费用计算',
      '房卡重新制作',
      '历史记录查询'
    ],
    '房态日历': [
      '可视化房态图',
      '拖拽式预订调整',
      '批量房态修改',
      '房态预警提示'
    ],
    '团队预订': [
      '批量房间预订',
      '团队信息管理',
      '团体账单处理',
      '特殊需求记录'
    ],
    '会员管理': [
      '会员等级体系',
      '积分累计规则',
      '会员专属权益',
      '会员生日提醒'
    ],
    '客户画像': [
      '消费习惯分析',
      '偏好标签管理',
      '客源价值评估',
      '个性化推荐'
    ],
    '任务中心': [
      '任务智能分配',
      '执行进度追踪',
      '超时预警机制',
      '绩效数据统计'
    ],
    '客户反馈': [
      '多渠道反馈收集',
      '情感智能分析',
      '问题分类处理',
      '满意度调查'
    ],
    '动态定价': [
      '市场需求预测',
      '智能价格建议',
      '竞争对手分析',
      '促销策略优化'
    ],
    '系统设置': [
      '用户权限管理',
      '系统参数配置',
      '日志审计追踪',
      '数据备份恢复'
    ]
  }

  return featureMap[title.value] || ['功能开发中，即将上线']
})

const goBack = () => {
  router.back()
}

const goDashboard = () => {
  router.push('/dashboard')
}
</script>

<style scoped>
.placeholder-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  padding: 40px 20px;
}

.placeholder-card {
  max-width: 600px;
  width: 100%;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
  border-radius: 16px;
  border: none;
}

.placeholder-content {
  text-align: center;
  padding: 20px;
}

.placeholder-icon {
  margin-bottom: 24px;
  opacity: 0.6;
}

.placeholder-title {
  font-size: 28px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 16px 0;
}

.placeholder-description {
  font-size: 16px;
  color: #64748b;
  margin: 0 0 32px 0;
}

.placeholder-info {
  margin-bottom: 32px;
  text-align: left;
}

.info-content {
  font-size: 14px;
  line-height: 1.8;
}

.info-content span {
  font-weight: 600;
  color: #334155;
  display: block;
  margin-bottom: 8px;
}

.info-content ul {
  margin: 8px 0 0 0;
  padding-left: 20px;
}

.info-content li {
  color: #475569;
  margin-bottom: 4px;
}

.placeholder-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.placeholder-actions .el-button {
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: 500;
}

.placeholder-actions .el-button .el-icon {
  margin-right: 6px;
}
</style>
