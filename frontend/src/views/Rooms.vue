<template>
  <div class="room-status-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">房态总览</h1>
        <p class="page-subtitle">实时监控房间状态，快速操作</p>
      </div>
      <div class="header-actions">
        <el-button-group>
          <el-button :type="viewMode === 'grid' ? 'primary' : ''" @click="viewMode = 'grid'">
            <el-icon><Grid /></el-icon>
            网格视图
          </el-button>
          <el-button :type="viewMode === 'floor' ? 'primary' : ''" @click="viewMode = 'floor'">
            <el-icon><Files /></el-icon>
            楼层视图
          </el-button>
          <el-button :type="viewMode === 'list' ? 'primary' : ''" @click="viewMode = 'list'">
            <el-icon><List /></el-icon>
            列表视图
          </el-button>
        </el-button-group>
        <el-button type="success" @click="showAddRoomDialog">
          <el-icon><Plus /></el-icon>
          添加房间
        </el-button>
        <el-button type="primary" @click="showQuickCheckIn">
          <el-icon><Plus /></el-icon>
          快速入住
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <el-card class="stat-card available">
        <div class="stat-content">
          <div class="stat-icon">
            <el-icon :size="32"><CircleCheck /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.available }}</div>
            <div class="stat-label">可用房间</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card occupied">
        <div class="stat-content">
          <div class="stat-icon">
            <el-icon :size="32"><UserFilled /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.occupied }}</div>
            <div class="stat-label">已入住</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card cleaning">
        <div class="stat-content">
          <div class="stat-icon">
            <el-icon :size="32"><Brush /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.cleaning }}</div>
            <div class="stat-label">清洁中</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card maintenance">
        <div class="stat-content">
          <div class="stat-icon">
            <el-icon :size="32"><Tools /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.maintenance }}</div>
            <div class="stat-label">维护中</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card checkout">
        <div class="stat-content">
          <div class="stat-icon">
            <el-icon :size="32"><Clock /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.todayCheckout }}</div>
            <div class="stat-label">今日退房</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 筛选栏 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filters">
        <el-form-item label="楼层">
          <el-select v-model="filters.floor" placeholder="全部楼层" clearable @change="handleFilterChange">
            <el-option label="全部楼层" value="" />
            <el-option v-for="f in floors" :key="f" :label="`${f}楼`" :value="f" />
          </el-select>
        </el-form-item>
        <el-form-item label="房型">
          <el-select v-model="filters.roomType" placeholder="全部房型" clearable @change="handleFilterChange">
            <el-option label="全部房型" value="" />
            <el-option v-for="type in roomTypes" :key="type" :label="type" :value="type" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="全部状态" clearable @change="handleFilterChange">
            <el-option label="全部状态" value="" />
            <el-option label="可用" value="available" />
            <el-option label="已入住" value="occupied" />
            <el-option label="清洁中" value="cleaning" />
            <el-option label="维护中" value="maintenance" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="filters.search"
            placeholder="搜索房间号"
            clearable
            @input="handleFilterChange"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="refreshData">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 房态图例 -->
    <div class="legend-bar">
      <div class="legend-item" v-for="legend in statusLegend" :key="legend.value">
        <div class="legend-color" :style="{ background: legend.color }"></div>
        <span class="legend-label">{{ legend.label }}</span>
        <span class="legend-count">({{ legend.count }})</span>
      </div>
    </div>

    <!-- 网格视图 -->
    <div v-if="viewMode === 'grid'" class="room-grid-view">
      <div v-for="floor in groupedRooms" :key="floor.floor" class="floor-section">
        <div class="floor-header">
          <h3>{{ floor.floor }}楼</h3>
          <div class="floor-stats">
            <span>总计: {{ floor.total }}</span>
            <el-tag size="small" type="success">可用: {{ floor.available }}</el-tag>
            <el-tag size="small" type="primary">入住: {{ floor.occupied }}</el-tag>
          </div>
        </div>
        <div class="room-grid">
          <div
            v-for="room in floor.rooms"
            :key="room.id"
            class="room-card"
            :class="getRoomCardClass(room)"
            @click="handleRoomClick(room)"
            @contextmenu.prevent="showContextMenu($event, room)"
          >
            <div class="room-number">{{ room.roomNumber }}</div>
            <div class="room-type">{{ room.roomTypeName }}</div>
            <div class="room-price">¥{{ room.price }}</div>
            <div class="room-status">
              <el-icon><component :is="getStatusIcon(room.status)" /></el-icon>
              <span>{{ getStatusText(room.status) }}</span>
            </div>
            <div v-if="room.status === 'occupied'" class="room-guest">
              <el-icon><User /></el-icon>
              {{ room.guestName }}
            </div>
            <div v-if="room.note" class="room-note">
              <el-tooltip :content="room.note" placement="top">
                <el-icon><Bell /></el-icon>
              </el-tooltip>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 列表视图 -->
    <div v-else-if="viewMode === 'list'" class="room-list-view">
      <el-table :data="filteredRooms" stripe>
        <el-table-column prop="roomNumber" label="房间号" width="100" />
        <el-table-column prop="floor" label="楼层" width="80" />
        <el-table-column prop="roomTypeName" label="房型" width="120" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            <span class="price-text">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="guestName" label="入住客人" width="120">
          <template #default="{ row }">
            {{ row.status === 'occupied' ? row.guestName : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="checkInDate" label="入住日期" width="120">
          <template #default="{ row }">
            {{ row.status === 'occupied' ? row.checkInDate : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="checkOutDate" label="预计退房" width="120">
          <template #default="{ row }">
            {{ row.status === 'occupied' ? row.checkOutDate : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button-group>
              <el-button
                v-if="row.status === 'available'"
                size="small"
                type="success"
                @click="handleCheckIn(row)"
              >
                <el-icon><UserFilled /></el-icon>
                入住
              </el-button>
              <el-button
                v-if="row.status === 'occupied'"
                size="small"
                type="warning"
                @click="handleCheckOut(row)"
              >
                <el-icon><SwitchButton /></el-icon>
                退房
              </el-button>
              <el-button
                v-if="row.status === 'cleaning'"
                size="small"
                type="primary"
                @click="handleCompleteCleaning(row)"
              >
                <el-icon><Check /></el-icon>
                完成
              </el-button>
              <el-button size="small" @click="handleEditRoom(row)">
                <el-icon><Edit /></el-icon>
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 右键菜单 -->
    <div
      v-show="contextMenu.visible"
      class="context-menu"
      :style="{ left: contextMenu.x + 'px', top: contextMenu.y + 'px' }"
    >
      <div class="context-menu-item" @click="handleContextAction('checkin')">
        <el-icon><UserFilled /></el-icon>
        办理入住
      </div>
      <div class="context-menu-item" @click="handleContextAction('checkout')">
        <el-icon><SwitchButton /></el-icon>
        办理退房
      </div>
      <div class="context-menu-item" @click="handleContextAction('cleaning')">
        <el-icon><Brush /></el-icon>
        标记清洁
      </div>
      <div class="context-menu-item" @click="handleContextAction('maintenance')">
        <el-icon><Tools /></el-icon>
        维护设置
      </div>
      <div class="context-menu-divider"></div>
      <div class="context-menu-item" @click="handleContextAction('note')">
        <el-icon><EditPen /></el-icon>
        添加备注
      </div>
      <div class="context-menu-item" @click="handleContextAction('detail')">
        <el-icon><View /></el-icon>
        查看详情
      </div>
    </div>

    <!-- 房间详情对话框 -->
    <el-dialog
      v-model="roomDetailVisible"
      :title="`房间 ${selectedRoom?.roomNumber} 详情`"
      width="600px"
    >
      <div v-if="selectedRoom" class="room-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="房间号">{{ selectedRoom.roomNumber }}</el-descriptions-item>
          <el-descriptions-item label="楼层">{{ selectedRoom.floor }}楼</el-descriptions-item>
          <el-descriptions-item label="房型">{{ selectedRoom.roomTypeName }}</el-descriptions-item>
          <el-descriptions-item label="价格">¥{{ selectedRoom.price }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(selectedRoom.status)">
              {{ getStatusText(selectedRoom.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="面积">{{ selectedRoom.area }}㎡</el-descriptions-item>
          <el-descriptions-item label="床型">{{ selectedRoom.bedType }}</el-descriptions-item>
          <el-descriptions-item label="最多入住">{{ selectedRoom.maxGuests }}人</el-descriptions-item>
        </el-descriptions>

        <el-divider>设施服务</el-divider>
        <div class="facilities">
          <el-tag
            v-for="facility in selectedRoom.facilities"
            :key="facility"
            style="margin: 5px"
          >
            {{ facility }}
          </el-tag>
        </div>

        <el-divider v-if="selectedRoom.status === 'occupied'">入住信息</el-divider>
        <div v-if="selectedRoom.status === 'occupied'" class="guest-info">
          <p><strong>客人姓名：</strong>{{ selectedRoom.guestName }}</p>
          <p><strong>联系电话：</strong>{{ selectedRoom.guestPhone }}</p>
          <p><strong>入住日期：</strong>{{ selectedRoom.checkInDate }}</p>
          <p><strong>预计退房：</strong>{{ selectedRoom.checkOutDate }}</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="roomDetailVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleEditRoom(selectedRoom)">编辑房间</el-button>
      </template>
    </el-dialog>

    <!-- 快速入住对话框 -->
    <el-dialog v-model="checkInVisible" title="快速入住" width="500px">
      <el-form :model="checkInForm" label-width="100px">
        <el-form-item label="房间号">
          <el-select v-model="checkInForm.roomId" placeholder="选择房间" filterable>
            <el-option
              v-for="room in availableRooms"
              :key="room.id"
              :label="`${room.roomNumber} - ${room.roomTypeName}`"
              :value="room.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="客人姓名">
          <el-input v-model="checkInForm.guestName" placeholder="请输入客人姓名" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="checkInForm.guestPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="入住天数">
          <el-input-number v-model="checkInForm.days" :min="1" :max="30" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="checkInVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCheckIn">确认入住</el-button>
      </template>
    </el-dialog>

    <!-- 添加房间对话框 -->
    <el-dialog v-model="addRoomVisible" title="添加房间" width="600px">
      <el-form :model="addRoomForm" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="房型" required>
              <el-select v-model="addRoomForm.roomTypeId" placeholder="选择房型" style="width: 100%">
                <el-option
                  v-for="type in roomTypeOptions"
                  :key="type.id"
                  :label="type.typeName"
                  :value="type.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="房间号" required>
              <el-input v-model="addRoomForm.roomNumber" placeholder="如: A101" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="楼层" required>
              <el-input-number v-model="addRoomForm.floor" :min="1" :max="50" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" required>
              <el-select v-model="addRoomForm.status" style="width: 100%">
                <el-option label="可用" value="available" />
                <el-option label="已入住" value="occupied" />
                <el-option label="清洁中" value="cleaning" />
                <el-option label="维护中" value="maintenance" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider>设施配置</el-divider>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item>
              <el-checkbox v-model="addRoomForm.hasAc">空调</el-checkbox>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item>
              <el-checkbox v-model="addRoomForm.hasTv">电视</el-checkbox>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item>
              <el-checkbox v-model="addRoomForm.hasWifi">WiFi</el-checkbox>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item>
              <el-checkbox v-model="addRoomForm.hasBalcony">阳台</el-checkbox>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item>
              <el-checkbox v-model="addRoomForm.hasKitchen">厨房</el-checkbox>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="停车位">
              <el-input-number v-model="addRoomForm.parkingSpaces" :min="0" :max="10" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="房间描述">
          <el-input
            v-model="addRoomForm.description"
            type="textarea"
            :rows="3"
            placeholder="房间描述信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addRoomVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAddRoom" :loading="loading">添加房间</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api/index'
import {
  Grid, Files, List, Plus, CircleCheck, UserFilled, Brush, Tools, Clock,
  Search, Refresh, User, Bell, Check, SwitchButton, Edit, EditPen, View
} from '@element-plus/icons-vue'

// 视图模式
const viewMode = ref('grid')

// 加载状态
const loading = ref(false)

// 筛选条件
const filters = reactive({
  floor: '',
  roomType: '',
  status: '',
  search: ''
})

// 统计数据
const stats = reactive({
  available: 0,
  occupied: 0,
  cleaning: 0,
  maintenance: 0,
  todayCheckout: 0
})

// 楼层列表
const floors = ref([1, 2, 3, 4, 5, 6])

// 房型列表
const roomTypes = ref<any[]>([])
const roomTypeOptions = ref<any[]>([])

// 房间列表
const rooms = ref<any[]>([])

// 选中的房间
const selectedRoom = ref<any>(null)

// 对话框显示状态
const roomDetailVisible = ref(false)
const checkInVisible = ref(false)
const addRoomVisible = ref(false)

// 右键菜单
const contextMenu = reactive({
  visible: false,
  x: 0,
  y: 0,
  room: null
})

// 入住表单
const checkInForm = reactive({
  roomId: null,
  guestName: '',
  guestPhone: '',
  days: 1
})

// 添加房间表单
const addRoomForm = reactive({
  roomTypeId: null,
  roomNumber: '',
  floor: 1,
  status: 'available',
  hasAc: true,
  hasTv: true,
  hasWifi: true,
  hasBalcony: false,
  hasKitchen: false,
  parkingSpaces: 0,
  description: ''
})

// 状态图例
const statusLegend = computed(() => [
  { label: '可用', value: 'available', color: '#10b981', count: stats.available },
  { label: '已入住', value: 'occupied', color: '#3b82f6', count: stats.occupied },
  { label: '清洁中', value: 'cleaning', color: '#f59e0b', count: stats.cleaning },
  { label: '维护中', value: 'maintenance', color: '#ef4444', count: stats.maintenance }
])

// 可用房间列表
const availableRooms = computed(() => {
  const filtered = rooms.value.filter((r: any) => r.status === 'available')
  console.log('Available rooms:', filtered) // 调试日志
  return filtered
})

// 过滤后的房间
const filteredRooms = computed(() => {
  let result = rooms.value

  if (filters.floor) {
    result = result.filter((r: any) => r.floor === filters.floor)
  }
  if (filters.roomType) {
    result = result.filter((r: any) => r.roomTypeName === filters.roomType)
  }
  if (filters.status) {
    result = result.filter((r: any) => r.status === filters.status)
  }
  if (filters.search) {
    result = result.filter((r: any) =>
      r.roomNumber.toLowerCase().includes(filters.search.toLowerCase())
    )
  }

  return result
})

// 按楼层分组的房间
const groupedRooms = computed(() => {
  const groups = new Map()

  filteredRooms.value.forEach((room: any) => {
    const floor = room.floor
    if (!groups.has(floor)) {
      groups.set(floor, {
        floor,
        total: 0,
        available: 0,
        occupied: 0,
        rooms: []
      })
    }
    const group = groups.get(floor)
    group.total++
    group.rooms.push(room)
    if (room.status === 'available') group.available++
    if (room.status === 'occupied') group.occupied++
  })

  return Array.from(groups.values()).sort((a, b) => a.floor - b.floor)
})

// 获取房间卡片样式类
const getRoomCardClass = (room: any) => {
  return {
    'room-available': room.status === 'available',
    'room-occupied': room.status === 'occupied',
    'room-cleaning': room.status === 'cleaning',
    'room-maintenance': room.status === 'maintenance'
  }
}

// 获取状态图标
const getStatusIcon = (status: string) => {
  const iconMap: Record<string, any> = {
    available: CircleCheck,
    occupied: UserFilled,
    cleaning: Brush,
    maintenance: Tools
  }
  return iconMap[status] || CircleCheck
}

// 获取状态文本
const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    available: '可用',
    occupied: '已入住',
    cleaning: '清洁中',
    maintenance: '维护中'
  }
  return textMap[status] || status
}

// 获取状态标签类型
const getStatusTagType = (status: string) => {
  const typeMap: Record<string, string> = {
    available: 'success',
    occupied: 'primary',
    cleaning: 'warning',
    maintenance: 'danger'
  }
  return typeMap[status] || ''
}

// 点击房间
const handleRoomClick = (room: any) => {
  selectedRoom.value = room
  roomDetailVisible.value = true
}

// 显示右键菜单
const showContextMenu = (event: MouseEvent, room: any) => {
  contextMenu.visible = true
  contextMenu.x = event.clientX
  contextMenu.y = event.clientY
  contextMenu.room = room
}

// 处理右键菜单操作
const handleContextAction = (action: string) => {
  contextMenu.visible = false
  const room = contextMenu.room

  switch (action) {
    case 'checkin':
      handleCheckIn(room)
      break
    case 'checkout':
      handleCheckOut(room)
      break
    case 'cleaning':
      handleMarkCleaning(room)
      break
    case 'maintenance':
      handleMarkMaintenance(room)
      break
    case 'note':
      handleAddNote(room)
      break
    case 'detail':
      handleRoomClick(room)
      break
  }
}

// 办理入住
const handleCheckIn = (room: any) => {
  checkInForm.roomId = room.id
  checkInVisible.value = true
}

// 提交入住
const submitCheckIn = async () => {
  try {
    // 注意：从房间页面快速入住需要先找到对应的预订，或者创建一个新预订
    // 这里简化处理，提示用户从预订管理页面办理入住
    ElMessage.info('请从预订管理页面为已确认的预订办理入住')
    checkInVisible.value = false
  } catch (error) {
    ElMessage.error('入住办理失败')
  }
}

// 办理退房
const handleCheckOut = async (room: any) => {
  try {
    await ElMessageBox.confirm(
      `确认要为房间 ${room.roomNumber} 办理退房吗？`,
      '确认退房',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // 注意：退房需要找到该房间对应的预订，然后调用退房API
    // 这里简化处理，提示用户从预订管理页面办理退房
    ElMessage.info('请从预订管理页面办理退房')
    await refreshData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('退房办理失败')
    }
  }
}

// 标记清洁
const handleMarkCleaning = async (room: any) => {
  try {
    await updateRoomStatus(room.id, 'cleaning')
    ElMessage.success(`房间 ${room.roomNumber} 已标记为清洁中`)
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 完成清洁
const handleCompleteCleaning = async (room: any) => {
  try {
    await updateRoomStatus(room.id, 'available')
    ElMessage.success(`房间 ${room.roomNumber} 清洁完成`)
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 标记维护
const handleMarkMaintenance = async (room: any) => {
  try {
    const reason = await ElMessageBox.prompt('请输入维护原因', '维护设置', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '请输入维护原因'
    })

    await updateRoomStatus(room.id, 'maintenance')
    ElMessage.success(`房间 ${room.roomNumber} 已标记为维护中`)
  } catch (error) {
    // 用户取消
  }
}

// 更新房间状态的通用函数
const updateRoomStatus = async (roomId: number, status: string) => {
  try {
    const response = await api.patch(
      `/api/rooms/${roomId}/status`,
      { status }
    )

    if (response.data) {
      // 更新本地数据
      const index = rooms.value.findIndex((r: any) => r.id === roomId)
      if (index !== -1 && rooms.value[index]) {
        rooms.value[index].status = status
      }
    }
  } catch (error: any) {
    console.error('更新房间状态失败:', error)
    throw error
  }
}

// 添加备注
const handleAddNote = async (room: any) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入备注信息', '添加备注', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '请输入备注内容'
    })

    // 注意：后端Room实体可能没有备注字段，这里暂时仅做提示
    ElMessage.info('备注功能需要后端支持，当前版本暂未实现')
  } catch (error) {
    // 用户取消
  }
}

// 编辑房间
const handleEditRoom = (room: any) => {
  ElMessage.info(`编辑房间 ${room.roomNumber} 功能待实现，当前版本仅支持状态更新`)
}

// 显示快速入住对话框
const showQuickCheckIn = () => {
  checkInForm.roomId = null
  checkInForm.guestName = ''
  checkInForm.guestPhone = ''
  checkInVisible.value = true
  // 房间数据应该已经在页面加载时获取，无需额外加载
}

// 显示添加房间对话框
const showAddRoomDialog = async () => {
  // 加载房型列表
  await loadRoomTypes()
  // 重置表单
  addRoomForm.roomTypeId = null
  addRoomForm.roomNumber = ''
  addRoomForm.floor = 1
  addRoomForm.status = 'available'
  addRoomForm.hasAc = true
  addRoomForm.hasTv = true
  addRoomForm.hasWifi = true
  addRoomForm.hasBalcony = false
  addRoomForm.hasKitchen = false
  addRoomForm.parkingSpaces = 0
  addRoomForm.description = ''
  addRoomVisible.value = true
}

// 筛选改变
const handleFilterChange = () => {
  // 筛选条件改变时自动更新
}

// 刷新数据
const refreshData = async () => {
  loading.value = true
  try {
    await loadRealData()
  } catch (error: any) {
    console.error('加载真实数据失败:', error)
    // 如果API调用失败，回退到模拟数据
    ElMessage.warning('后端连接失败，使用模拟数据')
    await loadMockData()
  } finally {
    loading.value = false
  }
}

// 加载房型列表
const loadRoomTypes = async () => {
  try {
    const hotelId = 1 // 默认酒店ID
    const response = await api.get(`/api/rooms/room-types/hotel/${hotelId}`)
    roomTypeOptions.value = response.data || []
  } catch (error) {
    console.error('加载房型列表失败:', error)
    ElMessage.error('加载房型列表失败')
  }
}

// 加载真实数据
const loadRealData = async () => {
  try {
    const hotelId = 1 // 默认酒店ID

    // 获取所有房间
    const roomsResponse = await api.get(`/api/rooms/hotel/${hotelId}`)

    // 获取统计数据
    const statsResponse = await api.get(`/api/rooms/hotel/${hotelId}/statistics`)

    if (roomsResponse.data) {
      // 转换数据格式
      rooms.value = roomsResponse.data.map((room: any) => {
        const roomTypeName = room.typeName || '标准间' // 使用标准间作为默认值
        return {
          id: room.id,
          roomNumber: room.roomNumber,
          floor: room.floor,
          roomTypeId: room.roomTypeId,
          roomTypeName: roomTypeName,
          price: room.basePrice || 0,
          status: room.status,
          facilities: room.facilities || [],
          hasAc: room.hasAc,
          hasTv: room.hasTv,
          hasWifi: room.hasWifi,
          hasBalcony: room.hasBalcony,
          description: room.description,
          area: calculateArea(roomTypeName),
          bedType: getBedType(roomTypeName),
          maxGuests: getMaxGuests(roomTypeName),
          guestName: null, // 需要从booking表获取
          guestPhone: null,
          checkInDate: null,
          checkOutDate: null,
          note: null
        }
      })

      console.log('加载了', rooms.value.length, '个房间')
    }

    if (statsResponse.data) {
      stats.available = statsResponse.data.available || 0
      stats.occupied = statsResponse.data.occupied || 0
      stats.cleaning = statsResponse.data.cleaning || 0
      stats.maintenance = statsResponse.data.maintenance || 0
      // 获取今日退房数量
      try {
        const checkoutResponse = await api.get(`/api/bookings/hotel/${hotelId}/today/checkouts`)
        stats.todayCheckout = Array.isArray(checkoutResponse.data) ? checkoutResponse.data.length : 0
      } catch {
        stats.todayCheckout = 0
      }
    }

    ElMessage.success('数据加载成功')
  } catch (error: any) {
    console.error('加载房间数据失败:', error)
    throw error
  }
}

// 根据房型名称计算面积
const calculateArea = (typeName: string | undefined) => {
  if (!typeName) return 40
  const areaMap: Record<string, number> = {
    '标准间': 35,
    '大床房': 45,
    '豪华套房': 65,
    '总统套房': 120
  }
  return areaMap[typeName] || 40
}

// 根据房型名称获取床型
const getBedType = (typeName: string | undefined) => {
  if (!typeName) return '大床'
  const bedMap: Record<string, string> = {
    '标准间': '大床',
    '大床房': '特大床',
    '豪华套房': '定制床',
    '总统套房': '定制床'
  }
  return bedMap[typeName] || '大床'
}

// 根据房型名称获取最大入住人数
const getMaxGuests = (typeName: string | undefined) => {
  if (!typeName) return 2
  const guestMap: Record<string, number> = {
    '标准间': 2,
    '大床房': 2,
    '豪华套房': 3,
    '总统套房': 4
  }
  return guestMap[typeName] || 2
}

// 提交添加房间
const submitAddRoom = async () => {
  try {
    const hotelId = 1 // 默认酒店ID
    const response = await api.post(`/api/rooms/hotel/${hotelId}`, addRoomForm)

    ElMessage.success('房间添加成功')
    addRoomVisible.value = false
    // 刷新房间列表
    await refreshData()
  } catch (error: any) {
    console.error('添加房间失败:', error)
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('添加房间失败')
    }
  }
}

// 加载模拟数据（备用）
const loadMockData = async () => {
  // 模拟房间数据
  const mockRooms = []
  const statuses = ['available', 'occupied', 'cleaning', 'maintenance']
  const roomTypeNames = ['标准间', '大床房', '豪华套房', '总统套房']

  for (let floor = 1; floor <= 6; floor++) {
    for (let num = 1; num <= 10; num++) {
      const roomNumber = `${String.fromCharCode(64 + floor)}${String(num).padStart(2, '0')}`
      const status = statuses[Math.floor(Math.random() * statuses.length)]
      const roomTypeName = roomTypeNames[Math.floor(Math.random() * roomTypeNames.length)] as string
      const typeIndex = roomTypeNames.indexOf(roomTypeName)
      const price = [680, 880, 1280, 2880][typeIndex] || 680

      mockRooms.push({
        id: mockRooms.length + 1,
        roomNumber,
        floor,
        roomTypeName,
        price,
        status,
        area: [35, 45, 65, 120][typeIndex] || 35,
        bedType: ['大床', '双床', '特大床', '定制床'][typeIndex] || '大床',
        maxGuests: [2, 2, 3, 4][typeIndex] || 2,
        facilities: ['WiFi', '空调', '电视', '热水器', '迷你吧', '保险箱'].slice(0, Math.floor(Math.random() * 4) + 3),
        guestName: status === 'occupied' ? `张三${mockRooms.length}` : null,
        guestPhone: status === 'occupied' ? '138****8888' : null,
        checkInDate: status === 'occupied' ? '2025-12-25' : null,
        checkOutDate: status === 'occupied' ? '2025-12-28' : null,
        note: Math.random() > 0.8 ? 'VIP客户' : null
      })
    }
  }

  rooms.value = mockRooms

  // 更新统计
  stats.available = mockRooms.filter((r: any) => r.status === 'available').length
  stats.occupied = mockRooms.filter((r: any) => r.status === 'occupied').length
  stats.cleaning = mockRooms.filter((r: any) => r.status === 'cleaning').length
  stats.maintenance = mockRooms.filter((r: any) => r.status === 'maintenance').length
  stats.todayCheckout = Math.floor(Math.random() * 10) + 5
}

// 点击其他地方关闭右键菜单
const closeContextMenu = () => {
  contextMenu.visible = false
}

// 初始化
onMounted(() => {
  loadRealData()
  document.addEventListener('click', closeContextMenu)
})
</script>

<style scoped>
.room-status-container {
  padding: 0;
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
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
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

.stat-card.available .stat-icon {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.stat-card.occupied .stat-icon {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
}

.stat-card.cleaning .stat-icon {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}

.stat-card.maintenance .stat-icon {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
}

.stat-card.checkout .stat-icon {
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #64748b;
  margin-top: 4px;
}

.filter-card {
  margin-bottom: 16px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.legend-bar {
  display: flex;
  gap: 24px;
  padding: 12px 16px;
  background: #f8fafc;
  border-radius: 8px;
  margin-bottom: 20px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 4px;
}

.legend-label {
  color: #475569;
}

.legend-count {
  color: #94a3b8;
  font-size: 12px;
}

.floor-section {
  margin-bottom: 32px;
}

.floor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid #e2e8f0;
}

.floor-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.floor-stats {
  display: flex;
  gap: 12px;
  align-items: center;
  font-size: 14px;
  color: #64748b;
}

.room-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 16px;
}

.room-card {
  position: relative;
  padding: 16px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.room-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.room-available {
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
  border-color: #10b981;
}

.room-occupied {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  border-color: #3b82f6;
}

.room-cleaning {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border-color: #f59e0b;
}

.room-maintenance {
  background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
  border-color: #ef4444;
}

.room-number {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 4px;
}

.room-type {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 8px;
}

.room-price {
  font-size: 16px;
  font-weight: 600;
  color: #f59e0b;
  margin-bottom: 8px;
}

.room-status {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #475569;
}

.room-guest {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed rgba(0, 0, 0, 0.1);
  font-size: 12px;
  color: #64748b;
  display: flex;
  align-items: center;
  gap: 4px;
}

.room-note {
  position: absolute;
  top: 8px;
  right: 8px;
  color: #f59e0b;
  cursor: pointer;
}

.context-menu {
  position: fixed;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  padding: 8px 0;
  z-index: 1000;
  min-width: 160px;
}

.context-menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  cursor: pointer;
  transition: background 0.2s ease;
  font-size: 14px;
  color: #475569;
}

.context-menu-item:hover {
  background: #f1f5f9;
}

.context-menu-item .el-icon {
  font-size: 16px;
}

.context-menu-divider {
  height: 1px;
  background: #e2e8f0;
  margin: 4px 0;
}

.room-list-view {
  background: #ffffff;
  border-radius: 12px;
  padding: 20px;
}

.price-text {
  color: #f59e0b;
  font-weight: 600;
}

.room-detail {
  padding: 16px 0;
}

.facilities {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.guest-info p {
  margin: 8px 0;
  line-height: 1.8;
}

/* 响应式 */
@media (max-width: 1200px) {
  .stats-row {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }

  .room-grid {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  }
}
</style>
