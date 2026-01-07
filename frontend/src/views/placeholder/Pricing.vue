<template>
  <div class="pricing-container">
    <el-card class="control-card">
      <div class="header-content">
        <div class="title-section">
          <h2 class="page-title">智能动态定价工作台</h2>
          <p class="page-subtitle">基于市场需求与预订率的 AI 定价建议</p>
        </div>
        <div class="action-section">
          <el-button
              type="primary"
              :icon="Refresh"
              :loading="adjustLoading"
              @click="handleGenerateProposals"
          >
            重新计算调价建议
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span class="header-title">待审批调价单</span>
          <el-tag type="info">{{ pendingList.length }} 条待处理</el-tag>
        </div>
      </template>

      <el-table :data="pendingList" stripe style="width: 100%" border>
        <el-table-column prop="effectiveDate" label="生效日期" width="120" sortable />

        <el-table-column label="房型信息" width="150">
          <template #default="scope">
            <span class="room-type-name">{{ scope.row.roomType?.typeName }}</span>
          </template>
        </el-table-column>

        <el-table-column label="价格对比 (原价 -> 建议价)" width="220">
          <template #default="scope">
            <div class="price-compare">
              <span class="old-price">€{{ scope.row.originalPrice }}</span>
              <el-icon class="arrow-icon"><Right /></el-icon>
              <span class="new-price">€{{ scope.row.adjustedPrice }}</span>
              <el-tag
                  size="small"
                  :type="getPriceTagType(scope.row.adjustedPrice, scope.row.originalPrice)"
                  class="price-tag"
              >
                {{ getPriceChange(scope.row.adjustedPrice, scope.row.originalPrice) }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="adjustFactor" label="调价依据 / 算法理由">
          <template #default="scope">
            <el-tooltip :content="scope.row.adjustFactor" placement="top">
              <span class="factor-text">{{ scope.row.adjustFactor }}</span>
            </el-tooltip>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button
                type="success"
                size="small"
                :icon="Check"
                @click="handleApprove(scope.row.recordId)"
            >
              批准生效
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <template #empty>
        <el-empty description="暂无待审批建议，点击上方按钮重新生成" />
      </template>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Check, Right } from '@element-plus/icons-vue'
import { pricingApi } from '@/api'

// --- 数据定义 ---
interface PricingRecord {
  recordId: number
  roomType: {
    typeName: string
  }
  originalPrice: number
  adjustedPrice: number
  effectiveDate: string
  adjustFactor: string
  status: string
}

const pendingList = ref<PricingRecord[]>([])
const loading = ref(false)
const adjustLoading = ref(false)

// --- 逻辑方法 ---

// 1. 获取待审批列表
const fetchPendingData = async () => {
  loading.value = true
  try {
    const res = await pricingApi.getPendingList()
    pendingList.value = res.data
  } catch (error) {
    // 拦截器已处理
  } finally {
    loading.value = false
  }
}

// 2. 生成建议
const handleGenerateProposals = async () => {
  adjustLoading.value = true // 已修正：.ref -> .value
  try {
    await pricingApi.generateProposals()
    ElMessage.success('算法计算完成，建议已更新')
    await fetchPendingData()
  } catch (error) {
    // 拦截器已处理
  } finally {
    adjustLoading.value = false // 已修正：.ref -> .value
  }
}

// 3. 审批通过
const handleApprove = (id: number) => {
  ElMessageBox.confirm('确认批准该调价建议并立即应用到线上售卖吗？', '审批确认', {
    confirmButtonText: '批准',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await pricingApi.approveProposal(id)
      ElMessage.success('价格已正式生效')
      fetchPendingData()
    } catch (error) {
      // 拦截器已处理
    }
  }).catch(() => {})
}

// 辅助：计算价格变化百分比
const getPriceChange = (newPrice: number, oldPrice: number) => {
  if (!oldPrice) return '0%'
  const diff = (((newPrice - oldPrice) / oldPrice) * 100).toFixed(0)
  return `${Number(diff) > 0 ? '+' : ''}${diff}%`
}

// 辅助：获取标签颜色
const getPriceTagType = (newPrice: number, oldPrice: number) => {
  if (newPrice > oldPrice) return 'danger'
  if (newPrice < oldPrice) return 'success'
  return 'info'
}

onMounted(() => {
  fetchPendingData()
})
</script>

<style scoped>
.pricing-container {
  padding: 24px;
  background-color: #f8fafc;
  min-height: calc(100vh - 100px);
}

.control-card {
  margin-bottom: 24px;
  border-radius: 12px;
  border: none;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 4px 0;
}

.page-subtitle {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

.table-card {
  border-radius: 12px;
  border: none;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-title {
  font-weight: 600;
  color: #334155;
}

.room-type-name {
  font-weight: 500;
  color: #0f172a;
}

.price-compare {
  display: flex;
  align-items: center;
  gap: 8px;
}

.old-price {
  color: #94a3b8;
  text-decoration: line-through;
  font-size: 13px;
}

.new-price {
  color: #1e293b;
  font-weight: 600;
  font-size: 15px;
}

.arrow-icon {
  color: #cbd5e1;
}

.factor-text {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #475569;
  font-size: 13px;
}

.price-tag {
  font-weight: bold;
}
</style>