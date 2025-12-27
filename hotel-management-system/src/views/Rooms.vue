<template>
  <div class="rooms">
    <div class="page-header">
      <h1>房间管理</h1>
      <el-button type="primary" @click="handleAddRoom">
        <el-icon><Plus /></el-icon>
        添加房间
      </el-button>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="房间号">
          <el-input v-model="searchForm.roomNumber" placeholder="请输入房间号" clearable />
        </el-form-item>
        <el-form-item label="房型">
          <el-select v-model="searchForm.roomType" placeholder="请选择房型" clearable>
            <el-option label="标准间" value="standard" />
            <el-option label="豪华间" value="deluxe" />
            <el-option label="套房" value="suite" />
            <el-option label="总统套房" value="presidential" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="可用" value="available" />
            <el-option label="已入住" value="occupied" />
            <el-option label="维护中" value="maintenance" />
            <el-option label="清洁中" value="cleaning" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 房间列表 -->
    <el-card>
      <el-table :data="roomList" style="width: 100%">
        <el-table-column prop="roomNumber" label="房间号" width="100" />
        <el-table-column prop="roomType" label="房型" width="120">
          <template #default="scope">
            <el-tag :type="getRoomTypeTag(scope.row.roomType)">
              {{ scope.row.roomType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="100">
          <template #default="scope">
            <span style="color: #e6a23c; font-weight: bold">¥{{ scope.row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTag(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="floor" label="楼层" width="80" />
        <el-table-column prop="facilities" label="设施" min-width="200">
          <template #default="scope">
            <el-tag
              v-for="facility in scope.row.facilities"
              :key="facility"
              size="small"
              style="margin-right: 5px"
            >
              {{ facility }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleEdit(scope.row)">
              编辑
            </el-button>
            <el-button
              size="small"
              :type="scope.row.status === 'available' ? 'success' : 'warning'"
              @click="handleStatusChange(scope.row)"
            >
              {{ scope.row.status === 'available' ? '入住' : '退房' }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 搜索表单
const searchForm = reactive({
  roomNumber: '',
  roomType: '',
  status: ''
})

// 分页
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 模拟房间数据
const roomList = ref([
  {
    id: 1,
    roomNumber: 'A101',
    roomType: '标准间',
    price: 680,
    status: '可用',
    floor: 1,
    facilities: ['WiFi', '空调', '电视', '热水器'],
    description: '舒适的标准间，配备基本设施'
  },
  {
    id: 2,
    roomNumber: 'A102',
    roomType: '豪华间',
    price: 980,
    status: '已入住',
    floor: 1,
    facilities: ['WiFi', '空调', '电视', '热水器', '迷你吧', '保险箱'],
    description: '宽敞的豪华间，设施齐全'
  },
  {
    id: 3,
    roomNumber: 'B201',
    roomType: '套房',
    price: 1580,
    status: '可用',
    floor: 2,
    facilities: ['WiFi', '空调', '电视', '热水器', '迷你吧', '保险箱', '客厅', '办公桌'],
    description: '豪华套房，包含独立客厅'
  },
  {
    id: 4,
    roomNumber: 'C301',
    roomType: '总统套房',
    price: 3280,
    status: '维护中',
    floor: 3,
    facilities: ['WiFi', '空调', '电视', '热水器', '迷你吧', '保险箱', '客厅', '办公桌', '按摩浴缸', '厨房'],
    description: '顶级总统套房，奢华享受'
  },
  {
    id: 5,
    roomNumber: 'A103',
    roomType: '标准间',
    price: 680,
    status: '清洁中',
    floor: 1,
    facilities: ['WiFi', '空调', '电视', '热水器'],
    description: '舒适的标准间，配备基本设施'
  }
])

total.value = roomList.value.length

// 获取房型标签颜色
const getRoomTypeTag = (type: string) => {
  const typeMap: Record<string, string> = {
    '标准间': 'primary',
    '豪华间': 'success',
    '套房': 'warning',
    '总统套房': 'danger'
  }
  return typeMap[type] || 'primary'
}

// 获取状态标签颜色
const getStatusTag = (status: string) => {
  const statusMap: Record<string, string> = {
    '可用': 'success',
    '已入住': 'primary',
    '维护中': 'danger',
    '清洁中': 'warning'
  }
  return statusMap[status] || 'primary'
}

// 搜索
const handleSearch = () => {
  ElMessage.success('搜索功能待实现')
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  ElMessage.success('搜索条件已重置')
}

// 添加房间
const handleAddRoom = () => {
  ElMessage.success('添加房间功能待实现')
}

// 编辑房间
const handleEdit = (room: any) => {
  ElMessage.success(`编辑房间 ${room.roomNumber} 功能待实现`)
}

// 状态变更
const handleStatusChange = (room: any) => {
  const action = room.status === 'available' ? '入住' : '退房'
  ElMessageBox.confirm(
    `确认要为房间 ${room.roomNumber} 执行${action}操作吗？`,
    '确认操作',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    ElMessage.success(`${action}操作成功`)
  }).catch(() => {
    ElMessage.info('已取消操作')
  })
}

// 删除房间
const handleDelete = (room: any) => {
  ElMessageBox.confirm(
    `确认要删除房间 ${room.roomNumber} 吗？此操作不可恢复！`,
    '确认删除',
    {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    ElMessage.success(`房间 ${room.roomNumber} 删除成功`)
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

// 分页大小改变
const handleSizeChange = (val: number) => {
  pageSize.value = val
  ElMessage.success(`每页显示 ${val} 条数据`)
}

// 当前页改变
const handleCurrentChange = (val: number) => {
  currentPage.value = val
  ElMessage.success(`当前第 ${val} 页`)
}
</script>

<style scoped>
.rooms h1 {
  margin-bottom: 20px;
  color: #303133;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-card {
  margin-bottom: 20px;
}
</style>