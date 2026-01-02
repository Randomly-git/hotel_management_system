<template>
  <div class="check-in-container">
    <div class="page-header">
      <h1>入住办理</h1>
      <p class="page-subtitle">办理客户入住手续</p>
    </div>

    <el-card class="check-in-card">
      <template #header>
        <div class="card-header">
          <span>入住信息</span>
        </div>
      </template>

      <el-form :model="checkInForm" :rules="checkInRules" ref="checkInFormRef" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="预订号" prop="bookingId">
              <el-input
                v-model="checkInForm.bookingId"
                placeholder="请输入预订编号"
                @input="searchBooking"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="房间号">
              <el-select
                v-model="checkInForm.roomId"
                placeholder="选择房间"
                filterable
                style="width: 100%"
              >
                <el-option
                  v-for="room in availableRooms"
                  :key="room.id"
                  :label="`${room.roomNumber} - ${room.roomTypeName}`"
                  :value="room.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider>客户信息</el-divider>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="姓名" prop="guestName">
              <el-input v-model="checkInForm.guestName" placeholder="客户姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="联系电话" prop="guestPhone">
              <el-input v-model="checkInForm.guestPhone" placeholder="联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="身份证号">
              <el-input v-model="checkInForm.idCardNumber" placeholder="身份证号" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider>入住信息</el-divider>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="入住日期" prop="checkInDate">
              <el-date-picker
                v-model="checkInForm.checkInDate"
                type="date"
                placeholder="入住日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="退房日期" prop="checkOutDate">
              <el-date-picker
                v-model="checkInForm.checkOutDate"
                type="date"
                placeholder="退房日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="入住人数" prop="adults">
              <el-input-number
                v-model="checkInForm.adults"
                :min="1"
                :max="10"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="特殊要求">
          <el-input
            v-model="checkInForm.specialRequests"
            type="textarea"
            :rows="3"
            placeholder="请输入特殊要求（如高楼层、安静房间等）"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="resetForm">重置</el-button>
          <el-button type="primary" @click="submitCheckIn" :loading="submitting">
            确认入住
          </el-button>
        </div>
      </template>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../../api/index'

// 状态
const submitting = ref(false)
const availableRooms = ref<any[]>([])
const checkInFormRef = ref()

// 表单数据
const checkInForm = reactive({
  bookingId: '',
  roomId: null as number | null,
  guestName: '',
  guestPhone: '',
  idCardNumber: '',
  checkInDate: '',
  checkOutDate: '',
  adults: 1,
  specialRequests: ''
})

// 表单验证规则
const checkInRules = {
  bookingId: [{ required: true, message: '请输入预订号', trigger: 'blur' }],
  guestName: [{ required: true, message: '请输入客户姓名', trigger: 'blur' }],
  guestPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  checkInDate: [{ required: true, message: '请选择入住日期', trigger: 'change' }],
  checkOutDate: [{ required: true, message: '请选择退房日期', trigger: 'change' }],
  adults: [{ required: true, message: '请输入入住人数', trigger: 'blur' }]
}

// 加载可用房间
const loadAvailableRooms = async () => {
  try {
    const response = await api.get('/api/rooms/hotel/1')
    if (response.data && Array.isArray(response.data)) {
      availableRooms.value = response.data.filter((room: any) => room.status === 'available')
    }
  } catch (error: any) {
    console.error('加载可用房间失败:', error)
  }
}

// 搜索预订信息
const searchBooking = async () => {
  if (!checkInForm.bookingId || checkInForm.bookingId.length < 3) return

  try {
    const response = await api.get(`/api/bookings/number/${checkInForm.bookingId}`)
    if (response.data) {
      const booking = response.data
      // 自动填充表单
      checkInForm.guestName = booking.customerName || ''
      checkInForm.checkInDate = booking.checkInDate || ''
      checkInForm.checkOutDate = booking.checkOutDate || ''
      checkInForm.adults = booking.adults || 1

      // 如果预订已有分配房间，自动选择
      if (booking.assignedRoomId) {
        checkInForm.roomId = booking.assignedRoomId
      }

      ElMessage.success('预订信息已加载')
    }
  } catch (error: any) {
    // 预订不存在，继续手动输入
    console.log('预订未找到，继续手动输入')
  }
}

// 提交入住
const submitCheckIn = async () => {
  if (!checkInFormRef.value) return

  try {
    await checkInFormRef.value.validate()
    submitting.value = true

    // 使用新的CheckIn API
    const bookingId = extractBookingId(checkInForm.bookingId)

    const requestData: any = {
      roomId: checkInForm.roomId,
      notes: checkInForm.specialRequests
    }

    await api.post(`/api/check-in/booking/${bookingId}`, requestData)

    ElMessage.success('入住办理成功')
    resetForm()
    await loadAvailableRooms()

  } catch (error: any) {
    console.error('入住办理失败:', error)
    ElMessage.error(error.response?.data || '入住办理失败')
  } finally {
    submitting.value = false
  }
}

// 从预订号中提取ID（如果输入的是BK开头的编号，需要先查询）
const extractBookingId = async (bookingInput: string): Promise<number> => {
  // 如果是纯数字，直接返回
  if (/^\d+$/.test(bookingInput)) {
    return parseInt(bookingInput)
  }

  // 如果是BK开头的预订号，需要查询获取ID
  try {
    const response = await api.get(`/api/bookings/number/${bookingInput}`)
    return response.data.id
  } catch {
    throw new Error('预订不存在')
  }
}

// 重置表单
const resetForm = () => {
  if (checkInFormRef.value) {
    checkInFormRef.value.resetFields()
  }
  checkInForm.bookingId = ''
  checkInForm.roomId = null
  checkInForm.guestName = ''
  checkInForm.guestPhone = ''
  checkInForm.idCardNumber = ''
  checkInForm.checkInDate = ''
  checkInForm.checkOutDate = ''
  checkInForm.adults = 1
  checkInForm.specialRequests = ''
}

// 初始化
onMounted(() => {
  loadAvailableRooms()
})
</script>

<style scoped>
.check-in-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0 0 8px 0;
  color: #1e293b;
  font-size: 24px;
}

.page-subtitle {
  margin: 0;
  color: #64748b;
  font-size: 14px;
}

.check-in-card {
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.card-header {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.dialog-footer {
  text-align: right;
}

.dialog-footer .el-button {
  margin-left: 12px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-divider__text) {
  font-weight: 600;
  color: #1e293b;
}
</style>
