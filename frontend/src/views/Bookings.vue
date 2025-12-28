<template>
  <div class="bookings">
    <div class="page-header">
      <h1>预订管理</h1>
      <el-button type="primary" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon>
        新建预订
      </el-button>
    </div>

    <!-- 筛选和搜索 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filters">
        <el-form-item label="预订状态">
          <el-select v-model="filters.status" placeholder="全部状态" clearable @change="loadBookings">
            <el-option label="全部" value="" />
            <el-option label="已确认" value="confirmed" />
            <el-option label="已入住" value="checked_in" />
            <el-option label="已退房" value="checked_out" />
            <el-option label="已取消" value="canceled" />
          </el-select>
        </el-form-item>
        <el-form-item label="预订编号">
          <el-input v-model="filters.bookingNumber" placeholder="输入预订编号" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadBookings">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 预订列表 -->
    <el-card class="table-card">
      <el-table :data="bookings" v-loading="loading" stripe>
        <el-table-column prop="bookingNumber" label="预订编号" width="180" />
        <el-table-column prop="customerName" label="客户姓名" width="120">
          <template #default="{ row }">
            {{ row.customer?.name || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="roomTypeName" label="房型" width="120">
          <template #default="{ row }">
            {{ row.roomType?.typeName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="checkInDate" label="入住日期" width="120" />
        <el-table-column prop="checkOutDate" label="退房日期" width="120" />
        <el-table-column prop="totalNights" label="晚数" width="80" />
        <el-table-column prop="totalPrice" label="总价" width="100">
          <template #default="{ row }">
            ¥{{ row.totalPrice?.toLocaleString() || 0 }}
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
              v-if="row.status === 'confirmed'"
              link
              type="warning"
              size="small"
              @click="cancelBooking(row)"
            >
              取消
            </el-button>
            <el-button
              v-if="row.status === 'confirmed'"
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
            {{ currentBooking.customer?.name || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="联系电话">
            {{ currentBooking.customer?.phone || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="房型">
            {{ currentBooking.roomType?.typeName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="房间号">
            {{ currentBooking.assignedRoom?.roomNumber || '未分配' }}
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
            ¥{{ currentBooking.totalPrice?.toLocaleString() || 0 }}
          </el-descriptions-item>
          <el-descriptions-item label="预订日期">
            {{ formatDateTime(currentBooking.bookingDate) }}
          </el-descriptions-item>
          <el-descriptions-item label="特殊要求" :span="2">
            {{ currentBooking.requestsText || '无' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 创建预订对话框 -->
    <el-dialog v-model="showCreateDialog" title="新建预订" width="600px" @close="resetCreateForm">
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="120px">
        <el-form-item label="客户" prop="customerId">
          <el-select v-model="createForm.customerId" placeholder="选择客户" filterable style="width: 100%">
            <el-option
              v-for="customer in customers"
              :key="customer.id"
              :label="`${customer.name} (ID: ${customer.id})`"
              :value="customer.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="房型" prop="roomTypeId">
          <el-select v-model="createForm.roomTypeId" placeholder="选择房型" @change="onRoomTypeChange" style="width: 100%">
            <el-option
              v-for="type in roomTypes"
              :key="type.id"
              :label="`${type.typeName} - ¥${type.basePrice}/晚`"
              :value="type.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="入住日期" prop="checkInDate">
          <el-date-picker
            v-model="createForm.checkInDate"
            type="date"
            placeholder="选择入住日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="退房日期" prop="checkOutDate">
          <el-date-picker
            v-model="createForm.checkOutDate"
            type="date"
            placeholder="选择退房日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="入住人数" prop="adults">
          <el-input-number v-model="createForm.adults" :min="1" :max="10" style="width: 100%" />
        </el-form-item>
        <el-form-item label="特殊要求">
          <el-input
            v-model="createForm.requestsText"
            type="textarea"
            :rows="3"
            placeholder="请输入特殊要求"
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import api from '../api/index'
import { Plus, Search } from '@element-plus/icons-vue'

// 状态
const loading = ref(false)
const submitting = ref(false)
const bookings = ref<any[]>([])
const roomTypes = ref<any[]>([])
const customers = ref<any[]>([])
const total = ref(0)

// 筛选条件
const filters = reactive({
  status: '',
  bookingNumber: ''
})

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10
})

// 对话框
const showDetailsDialog = ref(false)
const showCreateDialog = ref(false)
const currentBooking = ref<any>(null)
const createFormRef = ref<FormInstance>()

// 创建表单
const createForm = reactive({
  hotelId: 1,
  customerId: undefined as number | undefined,
  roomTypeId: undefined as number | undefined,
  checkInDate: '',
  checkOutDate: '',
  adults: 1,
  children: 0,
  babies: 0,
  requestsText: '',
  depositType: 'No Deposit',
  mealType: 'Bed & Breakfast',
  marketSegment: 'Online TA',
  distributionChannel: 'TA/TO'
})

const createRules: FormRules = {
  customerId: [{ required: true, message: '请选择客户', trigger: 'change', type: 'number' }],
  roomTypeId: [{ required: true, message: '请选择房型', trigger: 'change' }],
  checkInDate: [{ required: true, message: '请选择入住日期', trigger: 'change' }],
  checkOutDate: [{ required: true, message: '请选择退房日期', trigger: 'change' }],
  adults: [{ required: true, message: '请输入成人人数', trigger: 'blur' }]
}

// 获取状态类型
const getStatusType = (status: string) => {
  const map: Record<string, any> = {
    confirmed: 'success',
    checked_in: 'primary',
    checked_out: 'info',
    canceled: 'danger'
  }
  return map[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    confirmed: '已确认',
    checked_in: '已入住',
    checked_out: '已退房',
    canceled: '已取消'
  }
  return map[status] || status
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 加载预订列表
const loadBookings = async () => {
  loading.value = true
  try {
    const hotelId = 1
    let url = `/api/bookings/hotel/${hotelId}?page=${pagination.page - 1}&size=${pagination.pageSize}`

    if (filters.status) {
      url += `&status=${filters.status}`
    }

    const response = await api.get(url)

    if (response.data) {
      bookings.value = response.data.content || []
      total.value = response.data.totalElements || 0

      // 按预订编号筛选（前端筛选）
      if (filters.bookingNumber) {
        bookings.value = bookings.value.filter((b: any) =>
          b.bookingNumber.toLowerCase().includes(filters.bookingNumber.toLowerCase())
        )
      }
    }
  } catch (error: any) {
    console.error('加载预订列表失败:', error)
    ElMessage.error('加载预订列表失败')
  } finally {
    loading.value = false
  }
}

// 加载房型列表
const loadRoomTypes = async () => {
  try {
    const response = await api.get(`/api/rooms/room-types/hotel/1`)
    if (response.data) {
      roomTypes.value = response.data
    }
  } catch (error) {
    console.error('加载房型失败:', error)
  }
}

// 加载客户列表（模拟数据，因为后端没有CustomerController）
const loadCustomers = async () => {
  // 使用模拟客户数据
  customers.value = [
    { id: 1, name: '张三', phone: '13800138001', email: 'zhangsan@example.com' },
    { id: 2, name: '李四', phone: '13800138002', email: 'lisi@example.com' },
    { id: 3, name: 'John Smith', phone: '+1-234-567-8901', email: 'john@example.com' },
    { id: 4, name: '王五', phone: '13800138003', email: 'wangwu@example.com' },
    { id: 5, name: '赵六', phone: '13800138004', email: 'zhaoliu@example.com' }
  ]
}

// 重置创建表单
const resetCreateForm = () => {
  if (createFormRef.value) {
    createFormRef.value.resetFields()
  }
  createForm.customerId = undefined
  createForm.roomTypeId = undefined
  createForm.checkInDate = ''
  createForm.checkOutDate = ''
  createForm.adults = 1
  createForm.children = 0
  createForm.babies = 0
  createForm.requestsText = ''
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
    loadBookings()
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
    loadBookings()
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
    loadBookings()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('退房办理失败:', error)
      ElMessage.error('退房办理失败')
    }
  }
}

// 房型 change 事件
const onRoomTypeChange = (roomTypeId: number) => {
  const roomType = roomTypes.value.find((r) => r.id === roomTypeId)
  if (roomType) {
    console.log('选择房型:', roomType)
  }
}

// 创建预订
const createBooking = async () => {
  if (!createFormRef.value) return

  try {
    await createFormRef.value.validate()
    submitting.value = true

    await api.post(`/api/bookings`, createForm)

    ElMessage.success('预订创建成功')
    showCreateDialog.value = false
    loadBookings()

    // 重置表单
    createFormRef.value.resetFields()
  } catch (error: any) {
    console.error('创建预订失败:', error)
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data || '创建预订失败')
    }
  } finally {
    submitting.value = false
  }
}

// 重置筛选
const resetFilters = () => {
  filters.status = ''
  filters.bookingNumber = ''
  loadBookings()
}

// 分页事件
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  loadBookings()
}

const handlePageChange = (page: number) => {
  pagination.page = page
  loadBookings()
}

// 初始化
onMounted(() => {
  loadBookings()
  loadRoomTypes()
  loadCustomers()
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

.filter-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.booking-details {
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
