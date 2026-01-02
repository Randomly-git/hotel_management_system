<template>
  <div class="check-out-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1>退房办理</h1>
      <p class="page-subtitle">选择房间办理退房手续</p>
    </div>

    <!-- 在住房间列表 -->
    <el-card class="rooms-card">
      <template #header>
        <div class="card-header">
          <span>当前在住房间</span>
          <el-button @click="loadActiveRooms" :icon="Refresh" circle size="small" />
        </div>
      </template>

      <div v-loading="loading" class="rooms-grid">
        <el-empty v-if="activeRooms.length === 0" description="暂无在住房间" />

        <div
          v-for="room in activeRooms"
          :key="room.checkInRecordId"
          class="room-card"
          :class="{ 'selected': selectedRoom?.checkInRecordId === room.checkInRecordId }"
          @click="selectRoom(room)"
        >
          <div class="room-header">
            <el-tag type="success" size="large">{{ room.roomNumber }}</el-tag>
            <el-tag size="small">{{ room.typeName }}</el-tag>
          </div>

          <el-divider style="margin: 12px 0" />

          <el-descriptions :column="1" size="small" class="room-info">
            <el-descriptions-item label="客户姓名">{{ room.customerName }}</el-descriptions-item>
            <el-descriptions-item label="入住时间">{{ formatDateTime(room.checkInTime) }}</el-descriptions-item>
            <el-descriptions-item label="预订退房">{{ room.scheduledCheckOutDate }}</el-descriptions-item>
            <el-descriptions-item label="入住天数">{{ room.stayDays }}晚</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
    </el-card>

    <!-- 退房操作面板 -->
    <el-card v-if="selectedRoom" class="checkout-panel">
      <template #header>
        <div class="card-header">
          <span>退房操作 - 房间 {{ selectedRoom.roomNumber }}</span>
        </div>
      </template>

      <el-form :model="checkOutForm" :rules="checkOutRules" ref="checkOutFormRef" label-width="120px">
        <!-- 费用明细 -->
        <el-divider>费用明细</el-divider>

        <el-table :data="feeDetails" style="width: 100%" size="small">
          <el-table-column prop="item" label="项目" width="200" />
          <el-table-column prop="amount" label="金额" width="120">
            <template #default="{ row }">
              <span class="amount-text">¥{{ row.amount }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="说明" />
        </el-table>

        <div class="total-section">
          <div class="total-row">
            <span class="total-label">总计：</span>
            <span class="total-amount">¥{{ totalAmount }}</span>
          </div>
        </div>

        <!-- 退房确认 -->
        <el-divider>退房确认</el-divider>

        <el-form-item label="退房备注">
          <el-input
            v-model="checkOutForm.notes"
            type="textarea"
            :rows="3"
            placeholder="请输入退房备注信息（可选）"
          />
        </el-form-item>

        <el-form-item>
          <el-checkbox v-model="checkOutForm.confirmCheckout">
            我确认已完成退房手续，所有费用已结清
          </el-checkbox>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="selectedRoom = null">取消</el-button>
          <el-button
            type="primary"
            @click="submitCheckOut"
            :loading="submitting"
            :disabled="!checkOutForm.confirmCheckout"
          >
            确认退房
          </el-button>
        </div>
      </template>
    </el-card>

    <!-- 退房成功对话框 -->
    <el-dialog v-model="successVisible" title="退房成功" width="400px" center>
      <div class="success-content">
        <el-result
          icon="success"
          title="退房办理成功"
          :sub-title="`房间 ${checkedOutRoomNumber} 已退房`"
        >
          <template #extra>
            <el-button @click="successVisible = false; handleSuccess()">继续办理</el-button>
            <el-button type="primary" @click="printReceipt()">打印收据</el-button>
          </template>
        </el-result>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import api from '../../api/index'

// 状态
const loading = ref(false)
const submitting = ref(false)
const successVisible = ref(false)
const checkOutFormRef = ref()
const activeRooms = ref<any[]>([])
const selectedRoom = ref<any>(null)
const checkedOutRoomNumber = ref('')

// 表单数据
const checkOutForm = reactive({
  notes: '',
  confirmCheckout: false
})

// 费用明细（动态计算）
const feeDetails = computed(() => {
  if (!selectedRoom.value) return []

  const items = [
    { item: '房费', amount: selectedRoom.value.actualPrice || 0, description: `${selectedRoom.value.typeName} ${selectedRoom.value.stayDays}晚` }
  ]

  if (selectedRoom.value.deposit) {
    items.push({ item: '押金', amount: -selectedRoom.value.deposit, description: '押金退还' })
  }

  return items
})

// 总金额
const totalAmount = computed(() => {
  return feeDetails.value.reduce((sum, item) => sum + item.amount, 0)
})

// 表单验证规则
const checkOutRules = {}

// 加载在住房间列表
const loadActiveRooms = async () => {
  loading.value = true
  try {
    const response = await api.get('/api/check-in/active/1')
    if (response.data) {
      // 计算入住天数
      const now = new Date()
      activeRooms.value = response.data.map((room: any) => {
        const checkInTime = new Date(room.checkInTime)
        const stayDays = Math.max(1, Math.ceil((now.getTime() - checkInTime.getTime()) / (1000 * 60 * 60 * 24)))
        return {
          ...room,
          stayDays,
          typeName: room.typeName || '标准间',
          customerName: room.customerName || '未知'
        }
      })
    }
  } catch (error: any) {
    console.error('加载在住房间失败:', error)
    ElMessage.error('加载在住房间失败')
  } finally {
    loading.value = false
  }
}

// 选择房间
const selectRoom = (room: any) => {
  selectedRoom.value = room
  checkOutForm.notes = ''
  checkOutForm.confirmCheckout = false
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 提交退房
const submitCheckOut = async () => {
  if (!checkOutFormRef.value) return

  try {
    if (!selectedRoom.value || !selectedRoom.value.checkInRecordId) {
      ElMessage.error('请先选择房间')
      return
    }

    if (!checkOutForm.confirmCheckout) {
      ElMessage.warning('请确认退房信息')
      return
    }

    submitting.value = true

    // 使用CheckIn API办理退房
    const requestData: any = {
      notes: checkOutForm.notes
    }

    const response = await api.post(
      `/api/check-in/checkout/${selectedRoom.value.checkInRecordId}`,
      requestData
    )

    if (response.data) {
      checkedOutRoomNumber.value = selectedRoom.value.roomNumber
      successVisible.value = true
      ElMessage.success('退房办理成功')
    }
  } catch (error: any) {
    console.error('退房办理失败:', error)
    ElMessage.error(error.response?.data || '退房办理失败')
  } finally {
    submitting.value = false
  }
}

// 退房成功后处理
const handleSuccess = async () => {
  selectedRoom.value = null
  checkOutForm.notes = ''
  checkOutForm.confirmCheckout = false
  await loadActiveRooms()
}

// 打印收据
const printReceipt = () => {
  ElMessage.info('打印功能待实现')
  successVisible.value = false
  handleSuccess()
}

// 初始化
onMounted(() => {
  loadActiveRooms()
})
</script>

<style scoped>
.check-out-container {
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

.rooms-card {
  margin-bottom: 20px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.rooms-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
  min-height: 200px;
}

.room-card {
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: white;
}

.room-card:hover {
  border-color: #3b82f6;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
  transform: translateY(-2px);
}

.room-card.selected {
  border-color: #3b82f6;
  background: #eff6ff;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.room-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.room-info {
  width: 100%;
}

.room-info :deep(.el-descriptions__label) {
  font-weight: 500;
  color: #64748b;
}

.checkout-panel {
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.amount-text {
  color: #f59e0b;
  font-weight: 600;
}

.total-section {
  margin-top: 16px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 8px;
}

.total-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.total-label {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.total-amount {
  font-size: 20px;
  font-weight: 700;
  color: #10b981;
}

.dialog-footer {
  text-align: right;
}

.dialog-footer .el-button {
  margin-left: 12px;
}

.success-content {
  text-align: center;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-divider__text) {
  font-weight: 600;
  color: #1e293b;
}
</style>
