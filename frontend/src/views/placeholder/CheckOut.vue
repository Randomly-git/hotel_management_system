<template>
  <div class="check-out-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1>退房办理</h1>
      <p class="page-subtitle">办理客户退房手续</p>
    </div>

    <el-card class="check-out-card">
      <template #header>
        <div class="card-header">
          <span>退房信息</span>
        </div>
      </template>

      <el-form :model="checkOutForm" :rules="checkOutRules" ref="checkOutFormRef" label-width="120px">
        <!-- 预订搜索 -->
        <el-form-item label="预订号/房间号" prop="searchKey">
          <el-input
            v-model="checkOutForm.searchKey"
            placeholder="请输入预订编号或房间号"
            @input="searchBooking"
            clearable
          >
            <template #suffix>
              <el-button @click="searchBooking" :loading="searching" size="small">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>
        </el-form-item>

        <!-- 预订信息展示 -->
        <div v-if="bookingInfo" class="booking-info-section">
          <el-divider>预订信息</el-divider>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-descriptions :column="1" size="small">
                <el-descriptions-item label="预订号">{{ bookingInfo.bookingNumber }}</el-descriptions-item>
                <el-descriptions-item label="客户姓名">{{ bookingInfo.customerName }}</el-descriptions-item>
                <el-descriptions-item label="联系电话">{{ bookingInfo.customerPhone }}</el-descriptions-item>
              </el-descriptions>
            </el-col>
            <el-col :span="12">
              <el-descriptions :column="1" size="small">
                <el-descriptions-item label="房间号">{{ bookingInfo.roomNumber }}</el-descriptions-item>
                <el-descriptions-item label="房型">{{ bookingInfo.typeName }}</el-descriptions-item>
                <el-descriptions-item label="入住日期">{{ bookingInfo.checkInDate }}</el-descriptions-item>
                <el-descriptions-item label="退房日期">{{ bookingInfo.checkOutDate }}</el-descriptions-item>
              </el-descriptions>
            </el-col>
          </el-row>

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
        </div>

        <!-- 退房确认 -->
        <div v-if="bookingInfo" class="checkout-section">
          <el-divider>退房确认</el-divider>

          <el-form-item label="实际退房时间">
            <el-date-picker
              v-model="checkOutForm.actualCheckOutTime"
              type="datetime"
              placeholder="选择实际退房时间"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm"
              :default-time="new Date()"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="退房备注">
            <el-input
              v-model="checkOutForm.notes"
              type="textarea"
              :rows="3"
              placeholder="请输入退房备注信息"
            />
          </el-form-item>

          <el-form-item>
            <el-checkbox v-model="checkOutForm.confirmCheckout">
              我确认已完成退房手续，所有费用已结清
            </el-checkbox>
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="resetForm">重置</el-button>
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
          :sub-title="`房间 ${bookingInfo?.roomNumber} 已退房`"
        >
          <template #extra>
            <el-button @click="successVisible = false; resetForm()">继续办理</el-button>
            <el-button type="primary" @click="printReceipt()">打印收据</el-button>
          </template>
        </el-result>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../../api/index'
import { Search } from '@element-plus/icons-vue'

// 状态
const submitting = ref(false)
const searching = ref(false)
const successVisible = ref(false)
const checkOutFormRef = ref()

// 表单数据
const checkOutForm = reactive({
  searchKey: '',
  actualCheckOutTime: '',
  notes: '',
  confirmCheckout: false
})

// 预订信息
const bookingInfo = ref<any>(null)

// 费用明细
const feeDetails = ref([
  { item: '房费', amount: 680, description: '标准间 1晚' },
  { item: '押金', amount: -500, description: '押金退还' },
  { item: '服务费', amount: 20, description: '客房服务费' }
])

// 总金额
const totalAmount = computed(() => {
  return feeDetails.value.reduce((sum, item) => sum + item.amount, 0)
})

// 表单验证规则
const checkOutRules = {
  searchKey: [{ required: true, message: '请输入预订号或房间号', trigger: 'blur' }],
  actualCheckOutTime: [{ required: true, message: '请选择实际退房时间', trigger: 'change' }]
}

// 搜索预订信息
const searchBooking = async () => {
  if (!checkOutForm.searchKey || checkOutForm.searchKey.length < 2) {
    return
  }

  searching.value = true
  try {
    let booking = null

    // 尝试按预订号搜索
    try {
      const response = await api.get(`/api/bookings/number/${checkOutForm.searchKey}`)
      if (response.data) {
        booking = response.data
      }
    } catch (error) {
      // 如果预订号搜索失败，尝试按房间号搜索
      console.log('预订号搜索失败，尝试房间号搜索')
    }

    // 如果还没找到，尝试从入住记录中搜索
    if (!booking) {
      try {
        // 这里可以调用一个API来根据房间号查找入住记录
        // 暂时模拟
        const response = await api.get(`/api/bookings/hotel/1`)
        const bookings = response.data?.content || []
        booking = bookings.find((b: any) =>
          b.roomNumber === checkOutForm.searchKey &&
          b.status === 'checked_in'
        )
      } catch (error) {
        console.error('搜索入住记录失败:', error)
      }
    }

    if (booking) {
      bookingInfo.value = booking
      // 设置默认退房时间为当前时间
      checkOutForm.actualCheckOutTime = new Date().toISOString().slice(0, 16)
      ElMessage.success('预订信息已加载')
    } else {
      ElMessage.warning('未找到对应的入住记录')
      bookingInfo.value = null
    }
  } catch (error: any) {
    console.error('搜索预订失败:', error)
    ElMessage.error('搜索预订失败')
    bookingInfo.value = null
  } finally {
    searching.value = false
  }
}

// 提交退房
const submitCheckOut = async () => {
  if (!checkOutFormRef.value) return

  try {
    await checkOutFormRef.value.validate()
    submitting.value = true

    if (!bookingInfo.value) {
      ElMessage.error('请先搜索并选择预订信息')
      return
    }

    // 调用退房API
    const response = await api.patch(`/api/bookings/${bookingInfo.value.id}/checkout`)

    if (response.data) {
      successVisible.value = true
      ElMessage.success('退房办理成功')
    }
  } catch (error: any) {
    console.error('退房办理失败:', error)
    ElMessage.error(error.response?.data?.message || '退房办理失败')
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  if (checkOutFormRef.value) {
    checkOutFormRef.value.resetFields()
  }
  checkOutForm.searchKey = ''
  checkOutForm.actualCheckOutTime = ''
  checkOutForm.notes = ''
  checkOutForm.confirmCheckout = false
  bookingInfo.value = null
  successVisible.value = false
}

// 打印收据
const printReceipt = () => {
  // 实现打印功能
  ElMessage.info('打印功能待实现')
  successVisible.value = false
  resetForm()
}
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

.check-out-card {
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.card-header {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.booking-info-section {
  margin: 20px 0;
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

.checkout-section {
  margin-top: 20px;
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
