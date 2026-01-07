<template>
  <div class="customers">
    <div class="page-header">
      <h1>客户管理</h1>
      <el-button type="primary" @click="handleAddCustomer">
        <el-icon><Plus /></el-icon>
        添加客户
      </el-button>
    </div>

    <!-- 筛选和搜索 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="VIP等级">
          <el-select
            v-model="filters.vipLevel"
            placeholder="全部等级"
            clearable
            @change="loadCustomers"
            class="vip-select"
            filterable
          >
            <el-option label="全部等级" value="" />
            <el-option label="普通会员" value="normal" />
            <el-option label="银卡会员" value="silver" />
            <el-option label="金卡会员" value="gold" />
            <el-option label="白金会员" value="platinum" />
          </el-select>
        </el-form-item>
        <el-form-item label="客户姓名">
          <el-input
            v-model="filters.name"
            placeholder="输入客户姓名"
            clearable
            class="name-input"
          />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input
            v-model="filters.phone"
            placeholder="输入联系电话"
            clearable
            class="phone-input"
          />
        </el-form-item>
        <el-form-item label="回头客">
          <el-select
            v-model="filters.isRepeatedGuest"
            placeholder="全部"
            clearable
            @change="loadCustomers"
            class="guest-select"
          >
            <el-option label="全部" value="" />
            <el-option label="是" value="true" />
            <el-option label="否" value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadCustomers" class="large-button">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetFilters" class="large-button">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 当前筛选条件摘要 -->
      <div v-if="hasActiveFilters" class="filter-summary">
        <strong>当前筛选：</strong>
        <span v-if="filters.vipLevel">VIP等级: {{ getVipLevelText(filters.vipLevel) }} </span>
        <span v-if="filters.name">姓名: {{ filters.name }} </span>
        <span v-if="filters.phone">电话: {{ filters.phone }} </span>
        <span v-if="filters.isRepeatedGuest !== ''">回头客: {{ filters.isRepeatedGuest === 'true' ? '是' : '否' }}</span>
      </div>
    </el-card>

    <!-- 客户统计 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
              <el-icon :size="24"><UserFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalCustomers }}</div>
              <div class="stat-label">总客户数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
              <el-icon :size="24"><Star /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.vipCustomers }}</div>
              <div class="stat-label">VIP客户</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
              <el-icon :size="24"><Refresh /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.repeatedGuests }}</div>
              <div class="stat-label">回头客</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)">
              <el-icon :size="24"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.newThisMonth }}</div>
              <div class="stat-label">本月新增</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 客户列表 -->
    <el-card class="table-card">
      <el-table :data="customers" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="country" label="国家/地区" width="100" />
        <el-table-column prop="vipLevel" label="VIP等级" width="100">
          <template #default="{ row }">
            <el-tag :type="getVipType(row.vipLevel)">
              {{ getVipText(row.vipLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isRepeatedGuest" label="回头客" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isRepeatedGuest" type="success" size="small">是</el-tag>
            <el-tag v-else type="info" size="small">否</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalStays" label="入住次数" width="90" />
        <el-table-column prop="totalCancellations" label="取消次数" width="90" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="viewDetails(row)">查看</el-button>
            <el-button link type="primary" size="small" @click="editCustomer(row)">编辑</el-button>
            <el-button link type="primary" size="small" @click="viewBookings(row)">预订</el-button>
            <el-button link type="danger" size="small" @click="deleteCustomer(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
        :current-page="pagination.page"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pagination.pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>

    <!-- 客户详情对话框 -->
    <el-dialog v-model="showDetailsDialog" title="客户详情" width="700px">
      <div v-if="currentCustomer" class="customer-details">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="客户ID">
            {{ currentCustomer.id }}
          </el-descriptions-item>
          <el-descriptions-item label="VIP等级">
            <el-tag :type="getVipType(currentCustomer.vipLevel)">
              {{ getVipText(currentCustomer.vipLevel) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="姓名">
            {{ currentCustomer.name }}
          </el-descriptions-item>
          <el-descriptions-item label="联系电话">
            {{ currentCustomer.phone || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="邮箱">
            {{ currentCustomer.email || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="国家/地区">
            {{ currentCustomer.country || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="证件号码">
            {{ currentCustomer.idCardNumber || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="回头客">
            <el-tag v-if="currentCustomer.isRepeatedGuest" type="success">是</el-tag>
            <el-tag v-else type="info">否</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="入住次数">
            {{ currentCustomer.totalStays }}次
          </el-descriptions-item>
          <el-descriptions-item label="取消次数">
            {{ currentCustomer.totalCancellations }}次
          </el-descriptions-item>
          <el-descriptions-item label="注册时间" :span="2">
            {{ formatDateTime(currentCustomer.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">
            {{ currentCustomer.notes || '无' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 创建/编辑客户对话框 -->
    <el-dialog v-model="showCreateDialog" :title="isEdit ? '编辑客户' : '添加客户'" width="600px" @close="handleDialogClose">
      <el-form :model="customerForm" :rules="customerRules" ref="customerFormRef" label-width="100px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="customerForm.name" placeholder="请输入客户姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="customerForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="customerForm.email" placeholder="请输入邮箱地址" />
        </el-form-item>
        <el-form-item label="国家/地区">
          <el-input v-model="customerForm.country" placeholder="请输入国家或地区" />
        </el-form-item>
        <el-form-item label="证件号码">
          <el-input v-model="customerForm.idCardNumber" placeholder="请输入证件号码" />
        </el-form-item>
        <el-form-item label="VIP等级">
          <el-select v-model="customerForm.vipLevel" placeholder="选择VIP等级">
            <el-option label="普通会员" value="normal" />
            <el-option label="银卡会员" value="silver" />
            <el-option label="金卡会员" value="gold" />
            <el-option label="白金会员" value="platinum" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="customerForm.notes"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="saveCustomer" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Search, UserFilled, Star, Refresh, TrendCharts } from '@element-plus/icons-vue'
import api from '../api/index'

// 状态
const loading = ref(false)
const submitting = ref(false)
const customers = ref<any[]>([])
const total = ref(0)

// 筛选条件
const filters = reactive({
  vipLevel: '',
  name: '',
  phone: '',
  isRepeatedGuest: ''
})

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10
})

// 统计数据
const stats = reactive({
  totalCustomers: 0,
  vipCustomers: 0,
  repeatedGuests: 0,
  newThisMonth: 0
})

// 对话框
const showDetailsDialog = ref(false)
const showCreateDialog = ref(false)
const isEdit = ref(false)
const currentCustomer = ref<any>(null)
const customerFormRef = ref<FormInstance>()

// 客户表单
const customerForm = reactive({
  hotelId: 1,
  name: '',
  email: '',
  phone: '',
  country: 'CN',
  idCardNumber: '',
  vipLevel: 'normal',
  notes: ''
})

const customerRules: FormRules = {
  name: [{ required: true, message: '请输入客户姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 获取VIP类型
const getVipType = (vipLevel: string) => {
  const map: Record<string, any> = {
    normal: 'info',
    silver: '',
    gold: 'warning',
    platinum: 'danger'
  }
  return map[vipLevel] || 'info'
}

// 获取VIP文本
const getVipText = (vipLevel: string) => {
  const map: Record<string, string> = {
    normal: '普通会员',
    silver: '银卡会员',
    gold: '金卡会员',
    platinum: '白金会员'
  }
  return map[vipLevel] || vipLevel
}

// 获取VIP等级文本（用于筛选摘要）
const getVipLevelText = (vipLevel: string) => {
  const map: Record<string, string> = {
    normal: '普通会员',
    silver: '银卡会员',
    gold: '金卡会员',
    platinum: '白金会员'
  }
  return map[vipLevel] || '全部'
}

// 判断是否有活跃的筛选条件
const hasActiveFilters = computed(() => {
  return filters.vipLevel !== '' ||
         filters.name.trim() !== '' ||
         filters.phone.trim() !== '' ||
         filters.isRepeatedGuest !== ''
})

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 加载客户列表
const loadCustomers = async () => {
  loading.value = true
  try {
    const hotelId = 1
    let url = `/api/customers/hotel/${hotelId}?page=${pagination.page - 1}&size=${pagination.pageSize}`

    // 添加筛选参数
    if (filters.name) url += `&name=${encodeURIComponent(filters.name)}`
    if (filters.phone) url += `&phone=${encodeURIComponent(filters.phone)}`
    if (filters.vipLevel) url += `&vipLevel=${filters.vipLevel}`
    if (filters.isRepeatedGuest !== '') {
      url += `&isRepeatedGuest=${filters.isRepeatedGuest === 'true'}`
    }

    const response = await api.get(url)

    if (response.data) {
      customers.value = response.data.content || []
      total.value = response.data.totalElements || 0
    }
  } catch (error: any) {
    console.error('加载客户列表失败:', error)
    // 如果API失败，不显示错误消息（因为拦截器已经处理了）
  } finally {
    loading.value = false
  }
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    const hotelId = 1
    const response = await api.get(`/api/customers/hotel/${hotelId}/statistics`)
    if (response.data) {
      stats.totalCustomers = response.data.totalCustomers || 0
      stats.vipCustomers = response.data.vipCustomers || 0
      stats.repeatedGuests = response.data.repeatedGuests || 0
      stats.newThisMonth = response.data.newThisMonth || 0
    }
  } catch (error: any) {
    console.error('加载统计数据失败:', error)
  }
}

// 查看详情
const viewDetails = (customer: any) => {
  currentCustomer.value = customer
  showDetailsDialog.value = true
}

// 添加客户
const handleAddCustomer = () => {
  isEdit.value = false
  resetCustomerForm()
  showCreateDialog.value = true
}

// 对话框关闭处理
const handleDialogClose = () => {
  resetCustomerForm()
  isEdit.value = false
}

// 重置客户表单
const resetCustomerForm = () => {
  if (customerFormRef.value) {
    customerFormRef.value.resetFields()
  }
  customerForm.hotelId = 1
  customerForm.name = ''
  customerForm.email = ''
  customerForm.phone = ''
  customerForm.country = 'CN'
  customerForm.idCardNumber = ''
  customerForm.vipLevel = 'normal'
  customerForm.notes = ''
}

// 编辑客户
const editCustomer = async (customer: any) => {
  try {
    // 获取完整的客户信息
    const response = await api.get(`/api/customers/${customer.id}`)
    currentCustomer.value = response.data
    isEdit.value = true

    // 填充表单
    Object.assign(customerForm, {
      name: response.data.name,
      email: response.data.email || '',
      phone: response.data.phone || '',
      country: response.data.country || 'CN',
      idCardNumber: response.data.idCardNumber || '',
      vipLevel: response.data.vipLevel || 'normal',
      notes: response.data.notes || ''
    })

    showCreateDialog.value = true
  } catch (error: any) {
    console.error('获取客户详情失败:', error)
  }
}

// 保存客户
const saveCustomer = async () => {
  if (!customerFormRef.value) return

  try {
    await customerFormRef.value.validate()
    submitting.value = true

    if (isEdit.value && currentCustomer.value) {
      // 更新客户
      await api.put(`/api/customers/${currentCustomer.value.id}`, customerForm)
      ElMessage.success('客户信息已更新')
    } else {
      // 创建客户
      await api.post('/api/customers', customerForm)
      ElMessage.success('客户添加成功')
    }

    showCreateDialog.value = false
    handleDialogClose()
    await loadCustomers()
    await loadStatistics()
  } catch (error: any) {
    // 错误已在拦截器中处理
    console.error('保存客户失败:', error)
  } finally {
    submitting.value = false
  }
}

// 查看客户预订
const viewBookings = (customer: any) => {
  // 跳转到预订管理页面，并筛选该客户的预订
  window.location.href = `/bookings?customerId=${customer.id}`
}

// 删除客户
const deleteCustomer = async (customer: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除客户 "${customer.name}" 吗？此操作不可撤销。`,
      '确认删除',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await api.delete(`/api/customers/${customer.id}`)
    ElMessage.success('客户删除成功')
    await loadCustomers()
    await loadStatistics()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除客户失败:', error)
      const errorMessage = error.response?.data
      if (typeof errorMessage === 'string') {
        ElMessage.error(errorMessage)
      } else {
        ElMessage.error('删除客户失败，请检查客户是否有未完成的预订')
      }
    }
  }
}

// 重置筛选
const resetFilters = () => {
  filters.vipLevel = ''
  filters.name = ''
  filters.phone = ''
  filters.isRepeatedGuest = ''
  loadCustomers()
}

// 分页事件
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  loadCustomers()
}

const handlePageChange = (page: number) => {
  pagination.page = page
  loadCustomers()
}

// 初始化
onMounted(() => {
  loadCustomers()
  loadStatistics()
})
</script>

<style scoped>
.customers {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  color: #303133;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-form {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.filter-form .el-form-item {
  margin-bottom: 16px;
}

/* 专门的筛选框样式 */
.vip-select {
  width: 220px !important;
  min-width: 220px;
}

.name-input {
  width: 240px !important;
  min-width: 240px;
}

.phone-input {
  width: 240px !important;
  min-width: 240px;
}

.guest-select {
  width: 160px !important;
  min-width: 160px;
}

.large-button {
  min-height: 40px;
  padding: 10px 20px;
  font-size: 15px;
  font-weight: 500;
}

/* 确保选择框样式正确应用 */
.vip-select :deep(.el-input__inner),
.name-input :deep(.el-input__inner),
.phone-input :deep(.el-input__inner),
.guest-select :deep(.el-input__inner) {
  font-size: 14px;
  font-weight: 400;
}

/* 优化选择框的显示效果 */
.vip-select :deep(.el-select__selected-item),
.guest-select :deep(.el-select__selected-item) {
  overflow: visible;
  white-space: nowrap;
  max-width: none;
}

.vip-select :deep(.el-select__tags-text),
.guest-select :deep(.el-select__tags-text) {
  max-width: none;
  display: inline-block;
}

/* 确保下拉框内容完全可见 */
.vip-select :deep(.el-select-dropdown),
.guest-select :deep(.el-select-dropdown) {
  min-width: 180px;
}

/* 让表单项有更好的间距 */
.filter-form .el-form-item {
  margin-bottom: 16px;
  margin-right: 20px;
}

/* 确保标签和输入框对齐 */
.filter-form .el-form-item__content {
  display: flex;
  align-items: center;
}

.large-button {
  min-height: 36px;
  padding: 8px 16px;
  font-size: 14px;
}

/* 让筛选表单项标签更清晰 */
.filter-form .el-form-item__label {
  font-weight: 500;
  color: #303133;
  margin-right: 12px;
  min-width: 70px;
}

/* 让输入框和选择框有更好的视觉效果 */
.filter-form .el-input__inner,
.filter-form .el-select .el-input__inner {
  height: 36px;
  border-radius: 6px;
  border: 1px solid #dcdfe6;
  transition: all 0.3s;
  font-size: 14px;
}

.filter-form .el-input__inner:focus,
.filter-form .el-select .el-input__inner:focus {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

/* 响应式布局 */
@media (max-width: 1400px) {
  .vip-select {
    width: 200px !important;
    min-width: 200px;
  }

  .name-input,
  .phone-input {
    width: 220px !important;
    min-width: 220px;
  }

  .guest-select {
    width: 140px !important;
    min-width: 140px;
  }
}

@media (max-width: 1200px) {
  .vip-select {
    width: 180px !important;
    min-width: 180px;
  }

  .name-input,
  .phone-input {
    width: 200px !important;
    min-width: 200px;
  }

  .guest-select {
    width: 130px !important;
    min-width: 130px;
  }
}

@media (max-width: 768px) {
  .filter-form {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-form .el-form-item {
    width: 100%;
    margin-right: 0;
  }

  .vip-select,
  .name-input,
  .phone-input,
  .guest-select {
    width: 100% !important;
    min-width: unset;
  }

  .large-button {
    width: 100%;
    margin-top: 8px;
  }
}

/* 当前筛选条件的显示 */
.filter-summary {
  margin-top: 8px;
  padding: 8px 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-size: 12px;
  color: #606266;
}

.filter-summary strong {
  color: #409eff;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 12px;
  overflow: hidden;
}

.stat-card :deep(.el-card__body) {
  padding: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
  margin-top: 4px;
}

.table-card {
  margin-bottom: 20px;
}

.customer-details {
  padding: 10px;
}

/* Element Plus 样式优化 */
:deep(.el-card__body) {
  padding: 20px;
}

:deep(.el-table) {
  border-radius: 8px;
}

:deep(.el-pagination) {
  display: flex;
}

:deep(.el-descriptions__label) {
  font-weight: 600;
}
</style>
