<template>
  <div class="vip-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">VIP会员管理</h1>
        <p class="page-subtitle">管理酒店VIP会员，提供个性化服务</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="showUpgradeDialog = true">
          <el-icon><Plus /></el-icon>
          升级会员
        </el-button>
        <el-button @click="exportVIPs">
          <el-icon><Download /></el-icon>
          导出数据
        </el-button>
      </div>
    </div>

    <!-- VIP统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="6" :md="3">
        <div class="stat-card total">
          <div class="stat-icon">
            <el-icon :size="24"><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ vipStats.total }}</div>
            <div class="stat-label">总会员</div>
          </div>
        </div>
      </el-col>

      <el-col :xs="12" :sm="6" :md="3">
        <div class="stat-card platinum">
          <div class="stat-icon">
            <el-icon :size="24"><Star /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ vipStats.platinum }}</div>
            <div class="stat-label">铂金会员</div>
          </div>
        </div>
      </el-col>

      <el-col :xs="12" :sm="6" :md="3">
        <div class="stat-card gold">
          <div class="stat-icon">
            <el-icon :size="24"><Star /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ vipStats.gold }}</div>
            <div class="stat-label">黄金会员</div>
          </div>
        </div>
      </el-col>

      <el-col :xs="12" :sm="6" :md="3">
        <div class="stat-card silver">
          <div class="stat-icon">
            <el-icon :size="24"><Medal /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ vipStats.silver }}</div>
            <div class="stat-label">白银会员</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 筛选栏 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filters">
        <el-form-item label="会员等级">
          <el-select v-model="filters.vipLevel" placeholder="全部等级" clearable @change="handleFilterChange">
            <el-option label="全部等级" value="" />
            <el-option label="铂金会员" value="platinum" />
            <el-option label="黄金会员" value="gold" />
            <el-option label="白银会员" value="silver" />
            <el-option label="普通会员" value="normal" />
          </el-select>
        </el-form-item>

        <el-form-item label="会员状态">
          <el-select v-model="filters.status" placeholder="全部状态" clearable @change="handleFilterChange">
            <el-option label="全部状态" value="" />
            <el-option label="活跃" value="active" />
            <el-option label="休眠" value="inactive" />
            <el-option label="过期" value="expired" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-input
            v-model="filters.search"
            placeholder="搜索会员姓名或手机号"
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
          <el-button type="primary" @click="loadVIPCustomers">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- VIP会员列表 -->
    <el-card class="vip-list-card">
      <template #header>
        <div class="card-header">
          <span>VIP会员列表</span>
          <div class="header-stats">
            <el-tag size="small">共 {{ filteredVIPs.length }} 个会员</el-tag>
          </div>
        </div>
      </template>

      <el-table :data="filteredVIPs" stripe style="width: 100%">
        <el-table-column prop="name" label="会员姓名" width="120" />
        <el-table-column prop="phone" label="联系电话" width="130" />

        <el-table-column label="会员等级" width="120">
          <template #default="{ row }">
            <el-tag :type="getVipLevelTagType(row.vipLevel)">
              {{ getVipLevelText(row.vipLevel) }}
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

        <el-table-column prop="lastVisitDate" label="最近入住" width="120">
          <template #default="{ row }">
            {{ formatDate(row.lastVisitDate) }}
          </template>
        </el-table-column>

        <el-table-column prop="vipExpiryDate" label="会员到期" width="120">
          <template #default="{ row }">
            <span :class="{ 'expired': isExpired(row.vipExpiryDate) }">
              {{ formatDate(row.vipExpiryDate) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button-group>
              <el-button size="small" @click="viewVIPDetail(row)">
                <el-icon><View /></el-icon>
                详情
              </el-button>
              <el-button size="small" type="primary" @click="upgradeVIP(row)">
                <el-icon><Top /></el-icon>
                升级
              </el-button>
              <el-button size="small" type="warning" @click="editVIP(row)">
                <el-icon><Edit /></el-icon>
                编辑
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
          :total="totalVIPs"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- VIP详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      :title="`${selectedVIP?.name} - 会员详情`"
      width="800px"
    >
      <div v-if="selectedVIP" class="vip-detail">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="基本信息" name="basic">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="会员姓名">{{ selectedVIP.name }}</el-descriptions-item>
              <el-descriptions-item label="联系电话">{{ selectedVIP.phone }}</el-descriptions-item>
              <el-descriptions-item label="会员等级">
                <el-tag :type="getVipLevelTagType(selectedVIP.vipLevel)">
                  {{ getVipLevelText(selectedVIP.vipLevel) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="会员状态">
                <el-tag :type="getStatusTagType(selectedVIP.status)">
                  {{ getStatusText(selectedVIP.status) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="累计消费">¥{{ selectedVIP.totalSpent || 0 }}</el-descriptions-item>
              <el-descriptions-item label="入住次数">{{ selectedVIP.visitCount || 0 }}次</el-descriptions-item>
              <el-descriptions-item label="注册时间">{{ formatDate(selectedVIP.createdAt) }}</el-descriptions-item>
              <el-descriptions-item label="会员到期">{{ formatDate(selectedVIP.vipExpiryDate) }}</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>

          <el-tab-pane label="消费记录" name="consumption">
            <el-table :data="selectedVIP.consumptionHistory || []" stripe size="small">
              <el-table-column prop="date" label="消费日期" width="120" />
              <el-table-column prop="amount" label="消费金额" width="100">
                <template #default="{ row }">
                  <span class="amount-text">¥{{ row.amount }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="type" label="消费类型" width="120" />
              <el-table-column prop="description" label="消费描述" />
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="积分记录" name="points">
            <div class="points-info">
              <el-statistic title="当前积分" :value="selectedVIP.points || 0" />
              <el-divider />
              <el-table :data="selectedVIP.pointsHistory || []" stripe size="small">
                <el-table-column prop="date" label="日期" width="120" />
                <el-table-column prop="points" label="积分变动" width="100">
                  <template #default="{ row }">
                    <span :class="row.points > 0 ? 'positive' : 'negative'">
                      {{ row.points > 0 ? '+' : '' }}{{ row.points }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column prop="type" label="变动类型" width="120" />
                <el-table-column prop="description" label="变动描述" />
              </el-table>
            </div>
          </el-tab-pane>

          <el-tab-pane label="优惠券" name="coupons">
            <el-table :data="selectedVIP.coupons || []" stripe size="small">
              <el-table-column prop="name" label="优惠券名称" width="150" />
              <el-table-column prop="type" label="优惠类型" width="100" />
              <el-table-column prop="value" label="优惠金额" width="100">
                <template #default="{ row }">
                  <span class="amount-text">{{ row.type === '折扣' ? row.value + '%' : '¥' + row.value }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="expiryDate" label="到期时间" width="120">
                <template #default="{ row }">
                  {{ formatDate(row.expiryDate) }}
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="80">
                <template #default="{ row }">
                  <el-tag :type="row.status === '可用' ? 'success' : 'info'">
                    {{ row.status }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>

    <!-- 升级会员对话框 -->
    <el-dialog v-model="showUpgradeDialog" title="升级会员" width="500px">
      <el-form :model="upgradeForm" :rules="upgradeRules" ref="upgradeFormRef" label-width="100px">
        <el-form-item label="选择会员" prop="customerId">
          <el-select
            v-model="upgradeForm.customerId"
            placeholder="选择要升级的会员"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="customer in normalCustomers"
              :key="customer.id"
              :label="`${customer.name} - ${customer.phone}`"
              :value="customer.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="目标等级" prop="targetLevel">
          <el-select v-model="upgradeForm.targetLevel" placeholder="选择目标等级" style="width: 100%">
            <el-option label="白银会员" value="silver" />
            <el-option label="黄金会员" value="gold" />
            <el-option label="铂金会员" value="platinum" />
          </el-select>
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model="upgradeForm.notes"
            type="textarea"
            :rows="3"
            placeholder="升级备注信息"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showUpgradeDialog = false">取消</el-button>
        <el-button type="primary" @click="submitUpgrade" :loading="upgrading">
          确认升级
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../../api/index'
import {
  Plus, Download, User, Star, Medal, Search, View, Top, Edit, Refresh
} from '@element-plus/icons-vue'

// 状态
const loading = ref(false)
const upgrading = ref(false)
const showDetailDialog = ref(false)
const showUpgradeDialog = ref(false)
const activeTab = ref('basic')

// 数据
const vips = ref<any[]>([])
const normalCustomers = ref<any[]>([])
const selectedVIP = ref<any>(null)
const currentPage = ref(1)
const pageSize = ref(10)
const totalVIPs = ref(0)

// 统计数据
const vipStats = reactive({
  total: 0,
  platinum: 0,
  gold: 0,
  silver: 0
})

// 筛选条件
const filters = reactive({
  vipLevel: '',
  status: '',
  search: ''
})

// 升级表单
const upgradeForm = reactive({
  customerId: null as number | null,
  targetLevel: '',
  notes: ''
})

const upgradeRules = {
  customerId: [{ required: true, message: '请选择要升级的会员', trigger: 'change' }],
  targetLevel: [{ required: true, message: '请选择目标等级', trigger: 'change' }]
}

const upgradeFormRef = ref()

// 过滤后的VIP列表
const filteredVIPs = computed(() => {
  let result = vips.value

  if (filters.vipLevel) {
    result = result.filter(vip => vip.vipLevel === filters.vipLevel)
  }

  if (filters.status) {
    result = result.filter(vip => vip.status === filters.status)
  }

  if (filters.search) {
    const search = filters.search.toLowerCase()
    result = result.filter(vip =>
      vip.name?.toLowerCase().includes(search) ||
      vip.phone?.toLowerCase().includes(search)
    )
  }

  return result
})

// 获取VIP等级标签类型
const getVipLevelTagType = (level: string) => {
  const typeMap: Record<string, string> = {
    platinum: 'danger',
    gold: 'warning',
    silver: 'info',
    normal: ''
  }
  return typeMap[level] || ''
}

// 获取VIP等级文本
const getVipLevelText = (level: string) => {
  const textMap: Record<string, string> = {
    platinum: '铂金会员',
    gold: '黄金会员',
    silver: '白银会员',
    normal: '普通会员'
  }
  return textMap[level] || level
}

// 获取状态标签类型
const getStatusTagType = (status: string) => {
  const typeMap: Record<string, string> = {
    active: 'success',
    inactive: 'warning',
    expired: 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    active: '活跃',
    inactive: '休眠',
    expired: '过期'
  }
  return textMap[status] || status
}

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

// 检查是否过期
const isExpired = (date: string) => {
  if (!date) return false
  return new Date(date) < new Date()
}

// 加载VIP客户
const loadVIPCustomers = async () => {
  loading.value = true
  try {
    const response = await api.get('/api/customers/hotel/1', {
      params: {
        vipLevel: 'platinum,gold,silver', // 只获取VIP会员
        page: currentPage.value - 1,
        size: pageSize.value
      }
    })

    if (response.data && response.data.content) {
      vips.value = response.data.content.map((customer: any) => ({
        ...customer,
        vipLevel: customer.vipLevel || 'normal',
        status: 'active', // 模拟状态
        totalSpent: Math.floor(Math.random() * 50000) + 1000,
        visitCount: Math.floor(Math.random() * 20) + 1,
        lastVisitDate: new Date(Date.now() - Math.random() * 365 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
        vipExpiryDate: new Date(Date.now() + Math.random() * 365 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
        points: Math.floor(Math.random() * 10000),
        consumptionHistory: [
          { date: '2025-12-20', amount: 680, type: '住宿', description: '标准间一晚' },
          { date: '2025-11-15', amount: 1200, type: '餐饮', description: '餐厅消费' }
        ],
        pointsHistory: [
          { date: '2025-12-20', points: 68, type: '消费积分', description: '住宿消费获得积分' },
          { date: '2025-11-15', points: 120, type: '消费积分', description: '餐饮消费获得积分' }
        ],
        coupons: [
          { name: '住宿8折券', type: '折扣', value: 20, expiryDate: '2026-12-31', status: '可用' },
          { name: '餐饮优惠券', type: '金额', value: 50, expiryDate: '2026-06-30', status: '可用' }
        ]
      }))

      totalVIPs.value = response.data.totalElements || vips.value.length

      // 更新统计
      vipStats.total = vips.value.length
      vipStats.platinum = vips.value.filter(v => v.vipLevel === 'platinum').length
      vipStats.gold = vips.value.filter(v => v.vipLevel === 'gold').length
      vipStats.silver = vips.value.filter(v => v.vipLevel === 'silver').length
    }
  } catch (error: any) {
    console.error('加载VIP客户失败:', error)
    ElMessage.error('加载VIP客户失败')
  } finally {
    loading.value = false
  }
}

// 加载普通客户（用于升级）
const loadNormalCustomers = async () => {
  try {
    const response = await api.get('/api/customers/hotel/1', {
      params: {
        vipLevel: 'normal',
        page: 0,
        size: 100
      }
    })

    if (response.data && response.data.content) {
      normalCustomers.value = response.data.content
    }
  } catch (error: any) {
    console.error('加载普通客户失败:', error)
  }
}

// 查看VIP详情
const viewVIPDetail = (vip: any) => {
  selectedVIP.value = vip
  showDetailDialog.value = true
}

// 升级VIP
const upgradeVIP = (vip: any) => {
  upgradeForm.customerId = vip.id
  upgradeForm.targetLevel = vip.vipLevel === 'normal' ? 'silver' :
                           vip.vipLevel === 'silver' ? 'gold' : 'platinum'
  showUpgradeDialog.value = true
}

// 编辑VIP
const editVIP = (vip: any) => {
  ElMessage.info('编辑VIP功能待实现')
}

// 提交升级
const submitUpgrade = async () => {
  if (!upgradeFormRef.value) return

  try {
    await upgradeFormRef.value.validate()
    upgrading.value = true

    const response = await api.put(`/api/customers/${upgradeForm.customerId}`, {
      vipLevel: upgradeForm.targetLevel,
      notes: upgradeForm.notes
    })

    if (response.data) {
      ElMessage.success('VIP升级成功')
      showUpgradeDialog.value = false
      loadVIPCustomers()
      loadNormalCustomers()
    }
  } catch (error: any) {
    console.error('VIP升级失败:', error)
    ElMessage.error(error.response?.data?.message || 'VIP升级失败')
  } finally {
    upgrading.value = false
  }
}

// 导出VIP数据
const exportVIPs = () => {
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
  loadVIPCustomers()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  loadVIPCustomers()
}

// 初始化
onMounted(() => {
  loadVIPCustomers()
  loadNormalCustomers()
})
</script>

<style scoped>
.vip-container {
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

.stat-card.platinum .stat-icon {
  background: linear-gradient(135deg, #a855f7 0%, #9333ea 100%);
}

.stat-card.gold .stat-icon {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}

.stat-card.silver .stat-icon {
  background: linear-gradient(135deg, #6b7280 0%, #4b5563 100%);
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

.filter-card, .vip-list-card {
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

.expired {
  color: #ef4444;
  font-weight: 600;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.vip-detail {
  padding: 16px 0;
}

.points-info {
  text-align: center;
  margin-bottom: 20px;
}

.positive {
  color: #10b981;
  font-weight: 600;
}

.negative {
  color: #ef4444;
  font-weight: 600;
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
