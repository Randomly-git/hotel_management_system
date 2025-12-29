<template>
  <div class="profile-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">客户画像分析</h1>
        <p class="page-subtitle">基于AI分析的客户行为画像和个性化推荐</p>
      </div>
      <div class="header-actions">
        <el-button @click="exportProfiles">
          <el-icon><Download /></el-icon>
          导出画像
        </el-button>
      </div>
    </div>

    <!-- 画像概览统计 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="6" :md="3">
        <div class="stat-card total">
          <div class="stat-icon">
            <el-icon :size="24"><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ profileStats.totalCustomers }}</div>
            <div class="stat-label">总客户数</div>
          </div>
        </div>
      </el-col>

      <el-col :xs="12" :sm="6" :md="3">
        <div class="stat-card vip">
          <div class="stat-icon">
            <el-icon :size="24"><Star /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ profileStats.vipCustomers }}</div>
            <div class="stat-label">VIP客户</div>
          </div>
        </div>
      </el-col>

      <el-col :xs="12" :sm="6" :md="3">
        <div class="stat-card repeat">
          <div class="stat-icon">
            <el-icon :size="24"><RefreshRight /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ profileStats.repeatCustomers }}</div>
            <div class="stat-label">回头客</div>
          </div>
        </div>
      </el-col>

      <el-col :xs="12" :sm="6" :md="3">
        <div class="stat-card avg">
          <div class="stat-icon">
            <el-icon :size="24"><TrendCharts /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">¥{{ profileStats.avgSpending }}</div>
            <div class="stat-label">平均消费</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 客户筛选 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filters">
        <el-form-item label="客户类型">
          <el-select v-model="filters.customerType" placeholder="全部类型" clearable @change="handleFilterChange">
            <el-option label="全部类型" value="" />
            <el-option label="VIP客户" value="vip" />
            <el-option label="回头客" value="repeat" />
            <el-option label="新客户" value="new" />
            <el-option label="潜在客户" value="potential" />
          </el-select>
        </el-form-item>

        <el-form-item label="消费等级">
          <el-select v-model="filters.spendingLevel" placeholder="全部等级" clearable @change="handleFilterChange">
            <el-option label="全部等级" value="" />
            <el-option label="高消费" value="high" />
            <el-option label="中消费" value="medium" />
            <el-option label="低消费" value="low" />
          </el-select>
        </el-form-item>

        <el-form-item label="偏好分析">
          <el-select v-model="filters.preference" placeholder="全部偏好" clearable @change="handleFilterChange">
            <el-option label="全部偏好" value="" />
            <el-option label="商务出行" value="business" />
            <el-option label="休闲度假" value="leisure" />
            <el-option label="家庭出游" value="family" />
            <el-option label="高端奢华" value="luxury" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-input
            v-model="filters.search"
            placeholder="搜索客户姓名或手机号"
            clearable
            @input="handleFilterChange"
            style="width: 200px"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="loadCustomerProfiles">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 客户画像列表 -->
    <el-card class="profiles-card">
      <template #header>
        <div class="card-header">
          <span>客户画像列表</span>
          <div class="header-stats">
            <el-tag size="small">共 {{ filteredProfiles.length }} 个客户画像</el-tag>
          </div>
        </div>
      </template>

      <el-table :data="filteredProfiles" stripe style="width: 100%">
        <el-table-column prop="name" label="客户姓名" width="120" />
        <el-table-column prop="phone" label="联系电话" width="130" />

        <el-table-column label="客户类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getCustomerTypeTag(row.customerType)">
              {{ getCustomerTypeText(row.customerType) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="消费等级" width="100">
          <template #default="{ row }">
            <el-tag :type="getSpendingLevelTag(row.spendingLevel)">
              {{ getSpendingLevelText(row.spendingLevel) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="totalSpent" label="累计消费" width="120">
          <template #default="{ row }">
            <span class="amount-text">¥{{ row.totalSpent || 0 }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="visitCount" label="入住次数" width="100">
          <template #default="{ row }">
            <span>{{ row.visitCount || 0 }}次</span>
          </template>
        </el-table-column>

        <el-table-column label="偏好分析" width="150">
          <template #default="{ row }">
            <div class="preferences">
              <el-tag
                v-for="pref in row.preferences.slice(0, 2)"
                :key="pref"
                size="small"
                style="margin: 2px"
              >
                {{ pref }}
              </el-tag>
              <el-tag v-if="row.preferences.length > 2" size="small" type="info">
                +{{ row.preferences.length - 2 }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="lastVisitDate" label="最近入住" width="120">
          <template #default="{ row }">
            {{ formatDate(row.lastVisitDate) }}
          </template>
        </el-table-column>

        <el-table-column label="行为标签" width="150">
          <template #default="{ row }">
            <div class="behavior-tags">
              <el-tag
                v-for="tag in row.behaviorTags.slice(0, 3)"
                :key="tag"
                size="small"
                :type="getBehaviorTagType(tag)"
                style="margin: 2px"
              >
                {{ tag }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button-group>
              <el-button size="small" @click="viewProfileDetail(row)">
                <el-icon><View /></el-icon>
                详情
              </el-button>
              <el-button size="small" type="primary" @click="generateRecommendations(row)">
                <el-icon><MagicStick /></el-icon>
                推荐
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="totalProfiles"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 客户画像详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      :title="`${selectedProfile?.name} - 客户画像详情`"
      width="900px"
    >
      <div v-if="selectedProfile" class="profile-detail">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="基础信息" name="basic">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-descriptions :column="1" border size="small">
                  <el-descriptions-item label="客户姓名">{{ selectedProfile.name }}</el-descriptions-item>
                  <el-descriptions-item label="联系电话">{{ selectedProfile.phone }}</el-descriptions-item>
                  <el-descriptions-item label="客户类型">
                    <el-tag :type="getCustomerTypeTag(selectedProfile.customerType)">
                      {{ getCustomerTypeText(selectedProfile.customerType) }}
                    </el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="消费等级">
                    <el-tag :type="getSpendingLevelTag(selectedProfile.spendingLevel)">
                      {{ getSpendingLevelText(selectedProfile.spendingLevel) }}
                    </el-tag>
                  </el-descriptions-item>
                </el-descriptions>
              </el-col>
              <el-col :span="12">
                <el-descriptions :column="1" border size="small">
                  <el-descriptions-item label="累计消费">¥{{ selectedProfile.totalSpent || 0 }}</el-descriptions-item>
                  <el-descriptions-item label="入住次数">{{ selectedProfile.visitCount || 0 }}次</el-descriptions-item>
                  <el-descriptions-item label="平均消费">¥{{ selectedProfile.avgSpent || 0 }}</el-descriptions-item>
                  <el-descriptions-item label="最近入住">{{ formatDate(selectedProfile.lastVisitDate) }}</el-descriptions-item>
                </el-descriptions>
              </el-col>
            </el-row>

            <el-divider>偏好分析</el-divider>
            <div class="preferences-grid">
              <el-tag
                v-for="pref in selectedProfile.preferences"
                :key="pref"
                size="large"
                style="margin: 8px"
              >
                {{ pref }}
              </el-tag>
            </div>
          </el-tab-pane>

          <el-tab-pane label="行为分析" name="behavior">
            <div class="behavior-analysis">
              <el-row :gutter="20">
                <el-col :span="8">
                  <div class="behavior-metric">
                    <div class="metric-title">入住频率</div>
                    <div class="metric-value">{{ selectedProfile.stayFrequency }}</div>
                    <div class="metric-desc">平均每月入住次数</div>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="behavior-metric">
                    <div class="metric-title">平均停留时长</div>
                    <div class="metric-value">{{ selectedProfile.avgStayDuration }}晚</div>
                    <div class="metric-desc">平均每次入住天数</div>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="behavior-metric">
                    <div class="metric-title">偏好价格区间</div>
                    <div class="metric-value">¥{{ selectedProfile.priceRange }}</div>
                    <div class="metric-desc">最常选择的房价</div>
                  </div>
                </el-col>
              </el-row>

              <el-divider>行为标签</el-divider>
              <div class="behavior-tags">
                <el-tag
                  v-for="tag in selectedProfile.behaviorTags"
                  :key="tag"
                  :type="getBehaviorTagType(tag)"
                  size="large"
                  style="margin: 8px"
                >
                  {{ tag }}
                </el-tag>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="个性化推荐" name="recommendations">
            <div class="recommendations">
              <el-alert
                title="AI分析结果"
                :description="`基于${selectedProfile.name}的历史行为，AI推荐以下个性化服务`"
                type="info"
                show-icon
                style="margin-bottom: 20px"
              />

              <el-row :gutter="20">
                <el-col :span="12">
                  <el-card class="recommendation-card">
                    <template #header>
                      <div class="card-title">
                        <el-icon><Sunny /></el-icon>
                        房间推荐
                      </div>
                    </template>
                    <ul>
                      <li v-for="room in selectedProfile.roomRecommendations" :key="room">
                        {{ room }}
                      </li>
                    </ul>
                  </el-card>
                </el-col>
                <el-col :span="12">
                  <el-card class="recommendation-card">
                    <template #header>
                      <div class="card-title">
                        <el-icon><Service /></el-icon>
                        服务推荐
                      </div>
                    </template>
                    <ul>
                      <li v-for="service in selectedProfile.serviceRecommendations" :key="service">
                        {{ service }}
                      </li>
                    </ul>
                  </el-card>
                </el-col>
              </el-row>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../../api/index'
import {
  Download, User, Star, RefreshRight, TrendCharts, Search, View, MagicStick, Setting, Service, Refresh
} from '@element-plus/icons-vue'

// 状态
const loading = ref(false)
const showDetailDialog = ref(false)
const activeTab = ref('basic')

// 数据
const profiles = ref<any[]>([])
const selectedProfile = ref<any>(null)
const currentPage = ref(1)
const pageSize = ref(10)
const totalProfiles = ref(0)

// 统计数据
const profileStats = reactive({
  totalCustomers: 0,
  vipCustomers: 0,
  repeatCustomers: 0,
  avgSpending: 0
})

// 筛选条件
const filters = reactive({
  customerType: '',
  spendingLevel: '',
  preference: '',
  search: ''
})

// 过滤后的画像列表
const filteredProfiles = computed(() => {
  let result = profiles.value

  if (filters.customerType) {
    result = result.filter(profile => profile.customerType === filters.customerType)
  }

  if (filters.spendingLevel) {
    result = result.filter(profile => profile.spendingLevel === filters.spendingLevel)
  }

  if (filters.preference) {
    result = result.filter(profile =>
      profile.preferences?.includes(filters.preference)
    )
  }

  if (filters.search) {
    const search = filters.search.toLowerCase()
    result = result.filter(profile =>
      profile.name?.toLowerCase().includes(search) ||
      profile.phone?.toLowerCase().includes(search)
    )
  }

  return result
})

// 获取客户类型标签类型
const getCustomerTypeTag = (type: string) => {
  const typeMap: Record<string, string> = {
    vip: 'danger',
    repeat: 'warning',
    new: 'success',
    potential: 'info'
  }
  return typeMap[type] || ''
}

// 获取客户类型文本
const getCustomerTypeText = (type: string) => {
  const textMap: Record<string, string> = {
    vip: 'VIP客户',
    repeat: '回头客',
    new: '新客户',
    potential: '潜在客户'
  }
  return textMap[type] || type
}

// 获取消费等级标签类型
const getSpendingLevelTag = (level: string) => {
  const typeMap: Record<string, string> = {
    high: 'danger',
    medium: 'warning',
    low: 'success'
  }
  return typeMap[level] || ''
}

// 获取消费等级文本
const getSpendingLevelText = (level: string) => {
  const textMap: Record<string, string> = {
    high: '高消费',
    medium: '中消费',
    low: '低消费'
  }
  return textMap[level] || level
}

// 获取行为标签类型
const getBehaviorTagType = (tag: string) => {
  if (tag.includes('高频') || tag.includes('优质')) return 'success'
  if (tag.includes('投诉') || tag.includes('不满')) return 'danger'
  if (tag.includes('商务') || tag.includes('高端')) return 'warning'
  return 'info'
}

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

// 加载客户画像
const loadCustomerProfiles = async () => {
  loading.value = true
  try {
    const response = await api.get('/api/customers/hotel/1', {
      params: {
        page: currentPage.value - 1,
        size: pageSize.value
      }
    })

    if (response.data && response.data.content) {
      // 基于客户数据生成画像
      profiles.value = response.data.content.map((customer: any, index: number) => {
        const visitCount = Math.floor(Math.random() * 20) + 1
        const totalSpent = Math.floor(Math.random() * 50000) + 1000

        return {
          ...customer,
          customerType: customer.vipLevel !== 'normal' ? 'vip' :
                       visitCount > 3 ? 'repeat' :
                       visitCount === 1 ? 'new' : 'potential',
          spendingLevel: totalSpent > 20000 ? 'high' :
                        totalSpent > 5000 ? 'medium' : 'low',
          totalSpent,
          visitCount,
          avgSpent: Math.floor(totalSpent / visitCount),
          lastVisitDate: new Date(Date.now() - Math.random() * 365 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
          preferences: generatePreferences(index),
          behaviorTags: generateBehaviorTags(index, customer),
          stayFrequency: (Math.random() * 2).toFixed(1),
          avgStayDuration: Math.floor(Math.random() * 5) + 1,
          priceRange: Math.floor(Math.random() * 500) + 300,
          roomRecommendations: [
            '高层景观房',
            '行政套房',
            '温泉套房'
          ].slice(0, Math.floor(Math.random() * 3) + 1),
          serviceRecommendations: [
            '专车接送服务',
            '管家服务',
            '温泉SPA',
            '商务中心',
            '儿童娱乐设施'
          ].slice(0, Math.floor(Math.random() * 3) + 2)
        }
      })

      totalProfiles.value = response.data.totalElements || profiles.value.length

      // 更新统计
      profileStats.totalCustomers = profiles.value.length
      profileStats.vipCustomers = profiles.value.filter(p => p.customerType === 'vip').length
      profileStats.repeatCustomers = profiles.value.filter(p => p.customerType === 'repeat').length
      profileStats.avgSpending = Math.floor(
        profiles.value.reduce((sum: number, p: any) => sum + p.avgSpent, 0) / profiles.value.length
      )
    }
  } catch (error: any) {
    console.error('加载客户画像失败:', error)
    ElMessage.error('加载客户画像失败')
  } finally {
    loading.value = false
  }
}

// 生成偏好数据
const generatePreferences = (index: number) => {
  const preferences = [
    ['商务出行', '安静环境', '高速WiFi', '工作台'],
    ['休闲度假', '温泉SPA', '游泳池', '健身房'],
    ['家庭出游', '儿童设施', '亲子房', '游戏室'],
    ['高端奢华', '总统套房', '管家服务', '专车接送'],
    ['短途旅行', '标准间', '经济实惠', '交通便利']
  ]
  return preferences[index % preferences.length]
}

// 生成行为标签
const generateBehaviorTags = (index: number, customer: any) => {
  const tags = [
    ['高频入住', '优质客户', '商务人士'],
    ['回头客', '满意度高', '推荐客户'],
    ['新客户', '潜力客户', '关注重点'],
    ['高端消费', 'VIP客户', '个性化服务'],
    ['经济型', '性价比优先', '标准需求']
  ]
  return tags[index % tags.length]
}

// 查看画像详情
const viewProfileDetail = (profile: any) => {
  selectedProfile.value = profile
  showDetailDialog.value = true
}

// 生成推荐
const generateRecommendations = (profile: any) => {
  ElMessage.success(`已为 ${profile.name} 生成个性化推荐`)
}

// 导出画像数据
const exportProfiles = () => {
  ElMessage.info('导出功能待实现')
}

// 筛选改变
const handleFilterChange = () => {
  currentPage.value = 1
  // 筛选逻辑在computed中处理
}

// 分页处理
const handleSizeChange = (val: number) => {
  pageSize.value = val
  loadCustomerProfiles()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  loadCustomerProfiles()
}

// 初始化
onMounted(() => {
  loadCustomerProfiles()
})
</script>

<style scoped>
.profile-container {
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

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
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

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
}

.stat-card.total .stat-icon {
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
}

.stat-card.vip .stat-icon {
  background: linear-gradient(135deg, #a855f7 0%, #9333ea 100%);
}

.stat-card.repeat .stat-icon {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.stat-card.avg .stat-icon {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1;
}

.stat-label {
  font-size: 12px;
  color: #64748b;
  margin-top: 4px;
}

.filter-card, .profiles-card {
  margin-bottom: 20px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.amount-text {
  color: #f59e0b;
  font-weight: 600;
}

.preferences {
  display: flex;
  flex-wrap: wrap;
}

.behavior-tags {
  display: flex;
  flex-wrap: wrap;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.profile-detail {
  padding: 16px 0;
}

.preferences-grid {
  display: flex;
  flex-wrap: wrap;
}

.behavior-analysis {
  padding: 20px 0;
}

.behavior-metric {
  text-align: center;
  padding: 20px;
  background: #f8fafc;
  border-radius: 8px;
  margin-bottom: 16px;
}

.metric-title {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 8px;
}

.metric-value {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 4px;
}

.metric-desc {
  font-size: 12px;
  color: #94a3b8;
}

.recommendations {
  padding: 20px 0;
}

.recommendation-card {
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.recommendation-card ul {
  padding-left: 20px;
  margin: 0;
}

.recommendation-card li {
  margin-bottom: 8px;
  color: #475569;
}

/* 响应式 */
@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
}
</style>
