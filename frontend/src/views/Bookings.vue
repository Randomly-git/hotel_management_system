<template>
  <div class="bookings">
    <div class="page-header">
      <h1>预订管理</h1>
      <el-button type="primary" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon>
        新增预订
      </el-button>
    </div>

    <!-- 标签页切换 -->
    <el-tabs v-model="activeTab" @tab-click="handleTabChange" class="booking-tabs">
      <!-- 预订管理标签页 -->
      <el-tab-pane label="预订管理" name="active">
        <template #label>
          <el-icon><Calendar /></el-icon>
          预订管理
        </template>

        <!-- 筛选和搜索 - 预订管理 -->
        <el-card class="filter-card">
          <el-form :inline="true" :model="activeFilters">
            <el-form-item label="预订状态">
              <el-select v-model="activeFilters.status" placeholder="全部状态" clearable @change="loadActiveBookings">
                <el-option label="全部" value="" />
                <el-option label="已预订" value="booked" />
                <el-option label="已入住" value="checked_in" />
              </el-select>
            </el-form-item>
            <el-form-item label="预订编号">
              <el-input v-model="activeFilters.bookingNumber" placeholder="输入预订编号" clearable @input="debounceSearch" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadActiveBookings">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
              <el-button @click="resetActiveFilters">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 取消订单标签页 -->
      <el-tab-pane label="取消订单" name="cancelled">
        <template #label>
          <el-badge :is-dot="cancelledStats.count > 0">
            取消订单
          </el-badge>
        </template>
      </el-tab-pane>

      <!-- 历史预订标签页 -->
      <el-tab-pane label="历史预订" name="history">
        <template #label>
          <el-icon><Clock /></el-icon>
          历史预订
        </template>

        <!-- 筛选和搜索 - 历史预订 -->
        <el-card class="filter-card">
          <el-form :inline="true" :model="historyFilters">
            <el-form-item label="预订状态">
              <el-select v-model="historyFilters.status" placeholder="全部状态" clearable @change="loadHistoryBookings">
                <el-option label="全部" value="" />
                <el-option label="已完成" value="completed" />
                <el-option label="已取消" value="canceled" />
              </el-select>
            </el-form-item>
            <el-form-item label="预订编号">
              <el-input v-model="historyFilters.bookingNumber" placeholder="输入预订编号" clearable @input="debounceSearch" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadHistoryBookings">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
              <el-button @click="resetHistoryFilters">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 预订管理列表 -->
    <el-card class="table-card" v-if="activeTab === 'active'">
      <el-table :data="activeBookings" v-loading="activeLoading" stripe>
        <el-table-column prop="bookingNumber" label="预订编号" width="180" />
        <el-table-column prop="customerName" label="客户姓名" width="120">
          <template #default="{ row }">
            {{ row.customerName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="typeName" label="房型" width="120">
          <template #default="{ row }">
            {{ row.typeName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="checkInDate" label="入住日期" width="120" />
        <el-table-column prop="checkOutDate" label="退房日期" width="120" />
        <el-table-column prop="totalNights" label="晚数" width="80" />
        <el-table-column prop="totalPrice" label="总价" width="100">
          <template #default="{ row }">
            €{{ row.totalPrice?.toLocaleString() || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="viewDetails(row)">查看</el-button>
            <el-button
              v-if="row.status === 'booked'"
              link
              type="warning"
              size="small"
              @click="cancelBooking(row)"
            >
              取消
            </el-button>
            <el-button
              v-if="row.status === 'booked' && canCheckIn(row)"
              link
              type="success"
              size="small"
              @click="checkIn(row)"
            >
              入住
            </el-button>
            <el-button
              v-if="row.status === 'checked_in'"
              link
              type="info"
              size="small"
              @click="checkOut(row)"
            >
              退房
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="activeTotal > 0"
        @size-change="handleActiveSizeChange"
        @current-change="handleActivePageChange"
        :current-page="activePagination.page"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="activePagination.pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="activeTotal"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>

    <!-- 历史预订列表 -->
    <el-card class="table-card" v-if="activeTab === 'history'">
      <el-table :data="historyBookings" v-loading="historyLoading" stripe>
        <el-table-column prop="bookingNumber" label="预订编号" width="180" />
        <el-table-column prop="customerName" label="客户姓名" width="120">
          <template #default="{ row }">
            {{ row.customerName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="typeName" label="房型" width="120">
          <template #default="{ row }">
            {{ row.typeName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="checkInDate" label="入住日期" width="120" />
        <el-table-column label="退房日期" width="120">
          <template #default="{ row }">
            {{ row.status === 'completed' ? (row.actualCheckOutDate || row.checkOutDate) : row.checkOutDate }}
          </template>
        </el-table-column>
        <el-table-column prop="totalNights" label="晚数" width="80" />
        <el-table-column prop="adults" label="人数" width="100">
          <template #default="{ row }">
            成人: {{ row.adults }}, 儿童: {{ row.children }}, 婴儿: {{ row.babies }}
          </template>
        </el-table-column>
        <el-table-column prop="totalPrice" label="总价" width="100">
          <template #default="{ row }">
            €{{ row.totalPrice?.toLocaleString() || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              size="small"
              @click="viewDetails(row)"
            >
              详情
            </el-button>
            <el-button
              link
              type="danger"
              size="small"
              @click="handleDeleteBooking(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="historyTotal > 0"
        @size-change="handleHistorySizeChange"
        @current-change="handleHistoryPageChange"
        :current-page="historyPagination.page"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="historyPagination.pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="historyTotal"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>

    <!-- 取消订单列表 -->
    <el-card class="table-card" v-if="activeTab === 'cancelled'">
      <el-table :data="cancelledBookings" v-loading="cancelledLoading" stripe>
        <el-table-column prop="bookingNumber" label="预订编号" width="180" />
        <el-table-column prop="customerName" label="客户姓名" width="120">
          <template #default="{ row }">
            {{ row.customerName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="typeName" label="房型" width="120">
          <template #default="{ row }">
            {{ row.typeName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="checkInDate" label="原入住日期" width="120" />
        <el-table-column prop="cancelDate" label="取消时间" width="160" />
        <el-table-column prop="cancelReason" label="取消原因" min-width="150">
          <template #default="{ row }">
            {{ row.cancelReason || '系统自动取消' }}
          </template>
        </el-table-column>
        <el-table-column prop="totalPrice" label="原总价" width="100">
          <template #default="{ row }">
            €{{ row.totalPrice?.toLocaleString() || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              size="small"
              @click="viewDetails(row)"
            >
              详情
            </el-button>
            <el-button
              link
              type="danger"
              size="small"
              @click="handleDeleteBooking(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="cancelledTotal > 0"
        @size-change="handleCancelledSizeChange"
        @current-change="handleCancelledPageChange"
        :current-page="cancelledPagination.page"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="cancelledPagination.pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="cancelledTotal"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>

    <!-- 预订详情对话框 -->
    <el-dialog v-model="showDetailsDialog" title="预订详情" width="700px">
      <div v-if="currentBooking" class="booking-details">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="预订编号">
            {{ currentBooking.bookingNumber }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentBooking.status)">
              {{ getStatusText(currentBooking.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="客户姓名">
            {{ currentBooking.customerName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="联系电话">
            {{ currentBooking.customerPhone || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="房型">
            {{ currentBooking.typeName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="房间号">
            {{ currentBooking.roomNumber || '未分配' }}
          </el-descriptions-item>
          <el-descriptions-item label="入住日期">
            {{ currentBooking.checkInDate }}
          </el-descriptions-item>
          <el-descriptions-item label="退房日期">
            {{ currentBooking.checkOutDate }}
          </el-descriptions-item>
          <el-descriptions-item label="晚数">
            {{ currentBooking.totalNights }}晚
          </el-descriptions-item>
          <el-descriptions-item label="入住人数">
            成人{{ currentBooking.adults }}人
            <span v-if="currentBooking.children">，儿童{{ currentBooking.children }}人</span>
          </el-descriptions-item>
          <el-descriptions-item label="总价">
            €{{ currentBooking.totalPrice?.toLocaleString() || 0 }}
          </el-descriptions-item>
          <el-descriptions-item label="预订日期">
            {{ formatDateTime(currentBooking.bookingDate || currentBooking.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="特殊要求" :span="2">
            {{ currentBooking.requestsText || '无' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 创建预订对话框 -->
    <el-dialog v-model="showCreateDialog" title="新增预订" width="700px" @close="resetCreateForm">
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="120px">
        <!-- 宾客信息 -->
        <el-divider>宾客信息</el-divider>

        <!-- 选择用户类型 -->
        <el-form-item label="用户类型" prop="userType">
          <el-radio-group v-model="createForm.userType" @change="handleUserTypeChange">
            <el-radio label="existing">选择现有用户</el-radio>
            <el-radio label="new">创建新用户</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 选择现有用户 -->
        <el-form-item v-if="createForm.userType === 'existing'" label="选择用户" :prop="createForm.userType === 'existing' ? 'customerId' : ''">
          <el-autocomplete
            v-model="customerSearchText"
            :fetch-suggestions="searchCustomers"
            placeholder="输入用户名搜索现有用户"
            style="width: 100%"
            @select="onCustomerSelect"
            :trigger-on-focus="false"
          >
            <template #default="{ item }">
              <div>
                <span>{{ item.name }}</span>
                <span class="customer-id">(ID: {{ item.id }})</span>
              </div>
            </template>
          </el-autocomplete>
          <div v-if="customerSearchText && !createForm.customerId" class="search-hint">
            输入用户名搜索，如果未找到匹配用户将提示创建新用户
          </div>
        </el-form-item>

        <!-- 新用户表单 -->
        <div v-if="createForm.userType === 'new'">
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="姓名" :prop="createForm.userType === 'new' ? 'customerName' : ''" label-width="80px">
                <el-input v-model="createForm.customerName" placeholder="请输入宾客姓名" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="国籍" :prop="createForm.userType === 'new' ? 'customerCountry' : ''" label-width="80px">
                <el-select v-model="createForm.customerCountry" placeholder="选择国籍" style="width: 100%">
                  <el-option label="中国" value="CN" />
                  <el-option label="美国" value="US" />
                  <el-option label="日本" value="JP" />
                  <el-option label="韩国" value="KR" />
                  <el-option label="英国" value="GB" />
                  <el-option label="德国" value="DE" />
                  <el-option label="法国" value="FR" />
                  <el-option label="澳大利亚" value="AU" />
                  <el-option label="加拿大" value="CA" />
                  <el-option label="其他" value="OTHER" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="联系电话" label-width="80px">
                <el-input v-model="createForm.customerPhone" placeholder="请输入联系电话" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="邮箱" label-width="80px">
                <el-input v-model="createForm.customerEmail" placeholder="请输入邮箱地址" />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- 入住信息 -->
        <el-divider>入住信息</el-divider>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="入住日期" prop="checkInDate" label-width="80px">
              <el-date-picker
                v-model="createForm.checkInDate"
                type="date"
                placeholder="选择入住日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                :disabled-date="disabledCheckInDate"
                @change="onCheckInDateChange"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="退房日期" prop="checkOutDate" label-width="80px">
              <el-date-picker
                v-model="createForm.checkOutDate"
                type="date"
                placeholder="选择退房日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                :disabled-date="disabledCheckOutDate"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 房型选择 -->
        <el-form-item label="房型" prop="roomTypeId">
          <el-select v-model="createForm.roomTypeId" placeholder="选择房型" @change="onRoomTypeChange" style="width: 100%">
            <el-option
              v-for="type in roomTypes"
              :key="type.id"
              :label="`${type.typeName} - €${type.displayPrice || type.basePrice}/晚 (最多入住${type.maxOccupancy}人)${type.isDynamicPrice ? ' (动态价格)' : ''}`"
              :value="type.id"
            />
          </el-select>
        </el-form-item>

        <!-- 入住人数 -->
        <el-divider>入住人数</el-divider>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="成人" prop="adults" label-width="60px">
              <el-input-number v-model="createForm.adults" :min="1" :max="10" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="儿童" label-width="60px">
              <el-input-number v-model="createForm.children" :min="0" :max="10" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="婴儿" label-width="60px">
              <el-input-number v-model="createForm.babies" :min="0" :max="5" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 价格预览 -->
        <el-divider>价格预览</el-divider>
        <el-alert
          title="价格将在提交后由系统根据动态定价计算"
          type="info"
          show-icon
        />

        <!-- 其他信息 -->
        <el-divider>其他信息</el-divider>
        <el-form-item label="特殊要求">
          <el-input
            v-model="createForm.requestsText"
            type="textarea"
            :rows="3"
            placeholder="请输入特殊要求（如吸烟房、加床等）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="createBooking" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
declare global {
  interface Window {
    refreshRoomStatus?: () => void
  }
}
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import api from '../api/index'
import { Plus, Search, Calendar, Clock } from '@element-plus/icons-vue'

// 标签页
const activeTab = ref('active')

// 预订管理数据
const activeLoading = ref(false)
const activeBookings = ref<any[]>([])
const activeTotal = ref(0)
const activeFilters = reactive({
  status: '',
  bookingNumber: ''
})
const activePagination = reactive({
  page: 1,
  pageSize: 10
})

// 历史预订数据
const historyLoading = ref(false)
const historyBookings = ref<any[]>([])
const historyTotal = ref(0)

// 取消订单数据
const cancelledBookings = ref<any[]>([])
const cancelledLoading = ref(false)
const cancelledTotal = ref(0)
const cancelledStats = ref({ count: 0 })
const cancelledPagination = reactive({
  page: 1,
  pageSize: 20
})
const historyFilters = reactive({
  status: '',
  bookingNumber: ''
})
const historyPagination = reactive({
  page: 1,
  pageSize: 10
})

// 其他状态
const submitting = ref(false)
const roomTypes = ref<any[]>([])
const customers = ref<any[]>([])
const total = ref(0)

// 对话框
const showDetailsDialog = ref(false)
const showCreateDialog = ref(false)
const currentBooking = ref<any>(null)
const createFormRef = ref<FormInstance>()

// 创建表单
const createForm = reactive({
  hotelId: 1,
  // 用户类型选择
  userType: 'existing', // 'existing' 或 'new'
  // 客户信息
  customerId: undefined as number | undefined,
  customerName: '',
  customerCountry: 'CN',
  customerPhone: '',
  customerEmail: '',
  // 预订信息
  roomTypeId: undefined as number | undefined,
  checkInDate: '',
  checkOutDate: '',
  adults: 1,
  children: 0,
  babies: 0,
  requestsText: '',
  depositType: 'no_deposit',
  mealType: 'bb',
  marketSegment: 'Online TA',
  distributionChannel: 'TA/TO'
})

// 用户名检查状态
const customerSearchText = ref('')
const nameCheckMessage = ref('')
const nameCheckValid = ref(true)

const createRules: FormRules = {
  userType: [{ required: true, message: '请选择用户类型', trigger: 'change' }],
  customerId: [
    {
      validator: (rule: any, value: any, callback: any) => {
        if (createForm.userType === 'existing' && !value) {
          callback(new Error('请选择现有用户'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  customerName: [
    { required: true, message: '请输入宾客姓名', trigger: 'blur' },
    {
      validator: (rule: any, value: any, callback: any) => {
        if (createForm.userType === 'new' && (!value || !value.trim())) {
          callback(new Error('请输入宾客姓名'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  customerCountry: [
    { required: true, message: '请选择国籍', trigger: 'change' },
    {
      validator: (rule: any, value: any, callback: any) => {
        if (createForm.userType === 'new' && !value) {
          callback(new Error('请选择国籍'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  roomTypeId: [{ required: true, message: '请选择房型', trigger: 'change' }],
  checkInDate: [{ required: true, message: '请选择入住日期', trigger: 'change' }],
  checkOutDate: [{ required: true, message: '请选择退房日期', trigger: 'change' }],
  adults: [{ required: true, message: '请输入成人人数', trigger: 'blur' }]
}

// 获取状态类型
const getStatusType = (status: string) => {
  const map: Record<string, any> = {
    booked: 'success',
    checked_in: 'primary',
    completed: 'info',
    canceled: 'danger'
  }
  return map[status] || 'info'
}

// 检查是否可以入住
const canCheckIn = (booking: any) => {
  if (!booking.checkInDate) return false

  const now = new Date()
  const checkInDate = new Date(booking.checkInDate)
  const noon = new Date(checkInDate)
  noon.setHours(12, 0, 0, 0)

  return now >= noon
}

// 检查是否可以退房
const canCheckOut = (booking: any) => {
  if (!booking.checkOutDate) return false

  const now = new Date()
  const checkOutDate = new Date(booking.checkOutDate)
  const noon = new Date(checkOutDate)
  noon.setHours(12, 0, 0, 0)

  return now >= noon
}

// 获取状态文本
const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    booked: '已预订',
    checked_in: '已入住',
    completed: '已完成',
    canceled: '已取消'
  }
  return map[status] || status
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 入住日期验证（不能选择今天之前的日期）
const disabledCheckInDate = (date: Date) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return date < today
}

// 退房日期验证（不能早于入住日期）
const disabledCheckOutDate = (date: Date) => {
  if (!createForm.checkInDate) return false
  const checkInDate = new Date(createForm.checkInDate)
  return date <= checkInDate
}

// 加载预订列表
// 加载预订管理数据（已预订和已入住）- 按入住时间正序
const loadActiveBookings = async () => {
  activeLoading.value = true
  try {
    const hotelId = 1

    if (activeFilters.status) {
      // 筛选特定状态 - 使用后端分页，按入住时间正序
      const url = `/api/bookings/hotel/${hotelId}?page=${activePagination.page - 1}&size=${activePagination.pageSize}&status=${activeFilters.status}&sortBy=checkInDate&sortDir=asc`
      const response = await api.get(url)

      if (response.data) {
        activeBookings.value = response.data.content || []
        activeTotal.value = response.data.totalElements || 0
      }
    } else {
      // 显示所有活跃预订（已预订 + 已入住）- 使用多状态查询，按入住时间正序
      const url = `/api/bookings/hotel/${hotelId}?page=${activePagination.page - 1}&size=${activePagination.pageSize}&status=booked,checked_in&sortBy=checkInDate&sortDir=asc`
      const response = await api.get(url)

      if (response.data) {
        activeBookings.value = response.data.content || []
        activeTotal.value = response.data.totalElements || 0
      }
    }

    // 按预订编号筛选（前端筛选）
    if (activeFilters.bookingNumber) {
      const filtered = activeBookings.value.filter((b: any) =>
        b.bookingNumber.toLowerCase().includes(activeFilters.bookingNumber.toLowerCase())
      )
      // 注意：这里我们只在前端筛选，所以需要更新显示的列表
      // 但我们已经在从API获取后赋值给activeBookings了
      // 为了避免覆盖，我们可以使用一个临时变量
      const originalBookings = [...activeBookings.value]
      activeBookings.value = originalBookings.filter((b: any) =>
        b.bookingNumber.toLowerCase().includes(activeFilters.bookingNumber.toLowerCase())
      )
      // 如果筛选后为空，恢复原始数据
      if (activeFilters.bookingNumber === '') {
        activeBookings.value = originalBookings
      }
    }
  } catch (error: any) {
    console.error('加载预订管理数据失败:', error)
    ElMessage.error('加载预订管理数据失败')
  } finally {
    activeLoading.value = false
  }
}

// 加载历史预订数据（已完成和已取消）- 按入住时间倒序
const loadHistoryBookings = async () => {
  historyLoading.value = true
  try {
    const hotelId = 1

    // 只显示已完成的预订，按入住时间倒序
    const url = `/api/bookings/hotel/${hotelId}?page=${historyPagination.page - 1}&size=${historyPagination.pageSize}&status=completed&sortBy=checkInDate&sortDir=desc`
    const response = await api.get(url)

    if (response.data) {
      historyBookings.value = response.data.content || []
      historyTotal.value = response.data.totalElements || 0
    }

    // 按预订编号筛选（前端筛选）
    if (historyFilters.bookingNumber) {
      const filtered = historyBookings.value.filter((b: any) =>
        b.bookingNumber.toLowerCase().includes(historyFilters.bookingNumber.toLowerCase())
      )
      // 注意：这里我们只在前端筛选，所以需要更新显示的列表
      const originalBookings = [...historyBookings.value]
      historyBookings.value = originalBookings.filter((b: any) =>
        b.bookingNumber.toLowerCase().includes(historyFilters.bookingNumber.toLowerCase())
      )
      // 如果筛选后为空，恢复原始数据
      if (historyFilters.bookingNumber === '') {
        historyBookings.value = originalBookings
      }
    }
  } catch (error: any) {
    console.error('加载历史预订数据失败:', error)
    ElMessage.error('加载历史预订数据失败')
  } finally {
    historyLoading.value = false
  }
}

// 加载取消订单
const loadCancelledBookings = async () => {
  cancelledLoading.value = true
  try {
    const hotelId = 1

    // 使用分页加载取消的预订，按原入住时间倒序（最近的入住日期在前面）
    const url = `/api/bookings/hotel/${hotelId}?page=${cancelledPagination.page - 1}&size=${cancelledPagination.pageSize}&status=canceled&sortBy=checkInDate&sortDir=desc`
    const response = await api.get(url)

    if (response.data) {
      cancelledBookings.value = response.data.content || []
      cancelledTotal.value = response.data.totalElements || 0
      // 只显示是否有新的取消订单，不显示具体数量
      cancelledStats.value.count = cancelledTotal.value > 0 ? 1 : 0
    }
  } catch (error) {
    console.error('加载取消订单失败:', error)
    ElMessage.error('加载取消订单失败')
  } finally {
    cancelledLoading.value = false
  }
}

// 兼容旧方法
const loadBookings = loadActiveBookings

// 加载房型列表
const loadRoomTypes = async (checkInDate?: string) => {
  try {
    let url = `/api/rooms/room-types/hotel/1`
    if (checkInDate) {
      url += `?checkInDate=${checkInDate}`
    }
    const response = await api.get(url)
    if (response.data) {
      roomTypes.value = response.data
    }
  } catch (error) {
    console.error('加载房型失败:', error)
  }
}

// 入住日期变化处理
const onCheckInDateChange = (date: string) => {
  // 当用户选择入住日期时，重新加载房型价格
  if (date) {
    loadRoomTypes(date)
  } else {
    loadRoomTypes()
  }
}

// 用户类型切换处理
const handleUserTypeChange = (value: string) => {
  // 重置相关字段
  createForm.customerId = undefined
  createForm.customerName = ''
  createForm.customerCountry = 'CN'
  createForm.customerPhone = ''
  createForm.customerEmail = ''
  nameCheckMessage.value = ''
  nameCheckValid.value = true
}

// 搜索客户
const searchCustomers = async (queryString: string, cb: (results: any[]) => void) => {
  if (!queryString.trim()) {
    cb([])
    return
  }

  try {
    const response = await api.get(`/api/customers/search?keyword=${encodeURIComponent(queryString)}&hotelId=1`)
    const customerList = response.data || []

    // 格式化为autocomplete需要的格式
    const suggestions = customerList.map((customer: any) => ({
      value: customer.name,
      id: customer.id,
      name: customer.name,
      phone: customer.phone,
      email: customer.email,
      country: customer.country
    }))

    cb(suggestions)
  } catch (error) {
    console.error('搜索客户失败:', error)
    cb([])
  }
}

// 检查用户名是否已存在
const checkCustomerName = async () => {
  if (!createForm.customerName.trim()) {
    nameCheckMessage.value = ''
    return
  }

  try {
    const response = await api.get(`/api/customers/search?keyword=${encodeURIComponent(createForm.customerName)}&hotelId=1`)
    const existingCustomers = response.data || []

    const exists = existingCustomers.some((customer: any) =>
      customer.name.toLowerCase() === createForm.customerName.toLowerCase()
    )

    if (exists) {
      nameCheckMessage.value = '用户名已存在，建议选择现有用户'
      nameCheckValid.value = false
    } else {
      nameCheckMessage.value = '用户名可用'
      nameCheckValid.value = true
    }
  } catch (error) {
    console.error('检查用户名失败:', error)
    nameCheckMessage.value = '检查失败，请稍后重试'
    nameCheckValid.value = false
  }
}

// 选择现有用户时填充信息
const onCustomerSelect = (item: any) => {
  if (item && item.id) {
    createForm.customerId = item.id
    createForm.customerName = item.name
    createForm.customerCountry = item.country || 'CN'
    createForm.customerPhone = item.phone || ''
    createForm.customerEmail = item.email || ''
  }
}

// 重置创建表单
const resetCreateForm = () => {
  if (createFormRef.value) {
    createFormRef.value.resetFields()
  }
  // 重置所有字段
  createForm.userType = 'existing'
  createForm.customerId = undefined
  customerSearchText.value = ''
  createForm.customerName = ''
  createForm.customerCountry = 'CN'
  createForm.customerPhone = ''
  createForm.customerEmail = ''
  createForm.roomTypeId = undefined
  createForm.checkInDate = ''
  createForm.checkOutDate = ''
  createForm.adults = 1
  createForm.children = 0
  createForm.babies = 0
  createForm.requestsText = ''
  nameCheckMessage.value = ''
  nameCheckValid.value = true
}

// 查看详情
const viewDetails = (booking: any) => {
  currentBooking.value = booking
  showDetailsDialog.value = true
}

// 取消预订
const cancelBooking = async (booking: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消预订 ${booking.bookingNumber} 吗？`,
      '确认取消',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await api.patch(`/api/bookings/${booking.id}/cancel`)
    ElMessage.success('预订已取消')
    loadActiveBookings()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('取消预订失败:', error)
      ElMessage.error(error.response?.data || '取消预订失败')
    }
  }
}

// 入住
const checkIn = async (booking: any) => {
  try {
    await ElMessageBox.confirm(
      `确认要为预订 ${booking.bookingNumber} 办理入住吗？`,
      '确认入住',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await api.patch(`/api/bookings/${booking.id}/checkin`)
    ElMessage.success('入住办理成功')
    loadActiveBookings()

    // 刷新房态图
    if ((window as any).refreshRoomStatus) {
      (window as any).refreshRoomStatus()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('入住办理失败:', error)
      ElMessage.error('入住办理失败')
    }
  }
}

// 退房
const checkOut = async (booking: any) => {
  try {
    await ElMessageBox.confirm(
      `确认要为预订 ${booking.bookingNumber} 办理退房吗？`,
      '确认退房',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await api.patch(`/api/bookings/${booking.id}/checkout`)
    ElMessage.success('退房办理成功')
    loadActiveBookings()

    // 刷新房态图
    if ((window as any).refreshRoomStatus) {
      (window as any).refreshRoomStatus()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('退房办理失败:', error)
      ElMessage.error('退房办理失败')
    }
  }
}

// 房型 change 事件 - 验证人数容量
const onRoomTypeChange = (roomTypeId: number) => {
  const roomType = roomTypes.value.find((r) => r.id === roomTypeId)
  if (roomType) {
    // 验证当前输入的人数是否超过房型容量
    const totalGuests = (createForm.adults || 0) + (createForm.children || 0)
    if (totalGuests > roomType.maxOccupancy) {
      ElMessage.warning(`所选房型最多可容纳${roomType.maxOccupancy}人，当前登记${totalGuests}人，请调整入住人数或更换房型`)
    }
  }
}

// 创建预订
const createBooking = async () => {
  if (!createFormRef.value) return

  try {
    await createFormRef.value.validate()
    submitting.value = true

    // 验证房型容量
    const roomType = roomTypes.value.find((r) => r.id === createForm.roomTypeId)
    if (roomType) {
      const totalGuests = (createForm.adults || 0) + (createForm.children || 0)
      if (totalGuests > roomType.maxOccupancy) {
        ElMessage.error(`所选房型最多可容纳${roomType.maxOccupancy}人，当前登记${totalGuests}人，请调整入住人数或更换房型`)
        submitting.value = false
        return
      }
    }

    let customerId: number | undefined

    if (createForm.userType === 'existing') {
      // 使用现有用户
      if (!createForm.customerId) {
        // 没有选择用户，检查是否输入了用户名
        if (!customerSearchText.value.trim()) {
          ElMessage.error('请输入用户名搜索现有用户')
          submitting.value = false
          return
        }

        // 直接切换到创建新用户模式，让后端处理用户名检查
        createForm.userType = 'new'
        createForm.customerName = customerSearchText.value
      } else {
        customerId = createForm.customerId
      }
    }

    if (createForm.userType === 'new') {
      // 创建新客户
      const customerData = {
        hotelId: createForm.hotelId,
        name: createForm.customerName,
        country: createForm.customerCountry,
        phone: createForm.customerPhone,
        email: createForm.customerEmail
      }

      console.log('发送创建用户请求:', customerData)

      try {
        const customerResponse = await api.post('/api/customers', customerData)
        customerId = customerResponse.data.id
        console.log('用户创建成功:', customerId)
      } catch (error: any) {
        console.error('创建用户失败:', error)
        // 如果创建用户失败，可能是用户名已存在，显示错误消息
        const errorMessage = error.response?.data
        console.log('错误消息:', errorMessage)
        if (typeof errorMessage === 'string' && errorMessage.includes('用户名')) {
          ElMessage.error(errorMessage)
        } else {
          ElMessage.error('创建用户失败: ' + (errorMessage || '未知错误'))
        }
        submitting.value = false
        return
      }
    }

    // 创建预订
    const bookingData = {
      hotelId: createForm.hotelId,
      customerId: customerId,
      roomTypeId: createForm.roomTypeId,
      checkInDate: createForm.checkInDate,
      checkOutDate: createForm.checkOutDate,
      adults: createForm.adults,
      children: createForm.children,
      babies: createForm.babies,
      requestsText: createForm.requestsText,
      depositType: 'no_deposit',
      mealType: 'bb',
      marketSegment: createForm.marketSegment,
      distributionChannel: createForm.distributionChannel
    }

    await api.post(`/api/bookings`, bookingData)

    ElMessage.success('预订创建成功')
    showCreateDialog.value = false
    resetCreateForm()
    loadActiveBookings()
  } catch (error: any) {
    console.error('创建预订失败:', error)
    if (error !== 'cancel') {
      const errorMessage = error.response?.data
      if (typeof errorMessage === 'string' && errorMessage.includes('用户名')) {
        ElMessage.error(errorMessage)
      } else {
        ElMessage.error('创建预订失败，请检查输入信息')
      }
    }
  } finally {
    submitting.value = false
  }
}

// 标签页切换处理
const handleTabChange = (tab: any) => {
  if (tab.props.name === 'active') {
    loadActiveBookings()
  } else if (tab.props.name === 'history') {
    loadHistoryBookings()
  } else if (tab.props.name === 'cancelled') {
    loadCancelledBookings()
  }
}

// 删除预订处理
const handleDeleteBooking = async (booking: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除预订 "${booking.bookingNumber}" 吗？此操作不可撤销！`,
      '确认删除',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    await api.delete(`/api/bookings/${booking.id}`)
    ElMessage.success('预订删除成功')

    // 重新加载当前标签页的数据
    if (activeTab.value === 'history') {
      loadHistoryBookings()
    } else if (activeTab.value === 'cancelled') {
      loadCancelledBookings()
    }

    // 刷新房间状态
    window.refreshRoomStatus?.()

  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除预订失败:', error)
      ElMessage.error(error.response?.data || '删除预订失败')
    }
  }
}

// 预订管理分页处理
const handleActiveSizeChange = (size: number) => {
  activePagination.pageSize = size
  activePagination.page = 1
  loadActiveBookings()
}

const handleActivePageChange = (page: number) => {
  activePagination.page = page
  loadActiveBookings()
}

// 历史预订分页处理
const handleHistorySizeChange = (size: number) => {
  historyPagination.pageSize = size
  historyPagination.page = 1
  loadHistoryBookings()
}

const handleHistoryPageChange = (page: number) => {
  historyPagination.page = page
  loadHistoryBookings()
}

const handleCancelledSizeChange = (size: number) => {
  cancelledPagination.pageSize = size
  cancelledPagination.page = 1
  loadCancelledBookings()
}

const handleCancelledPageChange = (page: number) => {
  cancelledPagination.page = page
  loadCancelledBookings()
}

// 重置筛选条件
const resetActiveFilters = () => {
  activeFilters.status = ''
  activeFilters.bookingNumber = ''
  activePagination.page = 1
  loadActiveBookings()
}

const resetHistoryFilters = () => {
  historyFilters.status = ''
  historyFilters.bookingNumber = ''
  historyPagination.page = 1
  loadHistoryBookings()
}

// 防抖搜索
let searchTimer: number | null = null
const debounceSearch = () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    if (activeTab.value === 'active') {
      loadActiveBookings()
    } else {
      loadHistoryBookings()
    }
  }, 500)
}

// 初始化
onMounted(() => {
  loadActiveBookings() // 默认加载预订管理
  loadRoomTypes()
})
</script>

<style scoped>
.bookings {
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

.booking-tabs {
  margin-bottom: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.booking-details {
  padding: 10px;
}

.name-check-message {
  font-size: 12px;
  margin-top: 4px;
}

.name-check-message.valid {
  color: #67c23a;
}

.name-check-message.invalid {
  color: #f56c6c;
}

.search-hint {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
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