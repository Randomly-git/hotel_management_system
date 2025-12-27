<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside width="250px" class="sidebar">
      <div class="logo">
        <el-icon size="24"><House /></el-icon>
        <span>精品酒店管理系统</span>
      </div>

      <el-menu
        :default-active="$route.path"
        router
        class="menu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item
          v-for="route in menuRoutes"
          :key="route.path"
          :index="route.path"
        >
          <el-icon><component :is="route.meta.icon" /></el-icon>
          <span>{{ route.meta.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <!-- 顶部导航栏 -->
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>{{ currentRouteTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-dropdown>
            <span class="user-info">
              <el-icon><User /></el-icon>
              管理员
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>个人信息</el-dropdown-item>
                <el-dropdown-item>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区域 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

// 菜单路由
const menuRoutes = computed(() => {
  return [
    { path: '/dashboard', meta: { title: '数据概览', icon: 'DataLine' } },
    { path: '/rooms', meta: { title: '房间管理', icon: 'House' } },
    { path: '/bookings', meta: { title: '预订管理', icon: 'Calendar' } },
    { path: '/customers', meta: { title: '客户管理', icon: 'User' } },
    { path: '/services', meta: { title: '服务管理', icon: 'Service' } },
    { path: '/reputation', meta: { title: '声誉管理', icon: 'Star' } }
  ]
})

// 当前路由标题
const currentRouteTitle = computed(() => {
  const currentRoute = menuRoutes.value.find(r => r.path === route.path)
  return currentRoute?.meta.title || '酒店管理系统'
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
  width: 100vw;
}

.sidebar {
  background: linear-gradient(135deg, #2c5282 0%, #3182ce 100%);
  color: #ffffff;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 70px;
  padding: 0 20px;
  font-size: 20px;
  font-weight: 600;
  color: #ffffff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.05);
}

.logo .el-icon {
  margin-right: 12px;
  color: #ffd700;
  filter: drop-shadow(0 0 3px rgba(255, 215, 0, 0.3));
}

.menu {
  border: none;
  height: calc(100vh - 70px);
  background: transparent !important;
}

.menu :deep(.el-menu-item) {
  color: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  margin: 4px 12px;
  transition: all 0.3s ease;
}

.menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.1);
  color: #ffffff;
  transform: translateX(4px);
}

.menu :deep(.el-menu-item.is-active) {
  background: rgba(255, 215, 0, 0.2);
  color: #ffd700;
  border-left: 4px solid #ffd700;
}

.menu :deep(.el-menu-item .el-icon) {
  color: rgba(255, 255, 255, 0.8);
}

.menu :deep(.el-menu-item.is-active .el-icon) {
  color: #ffd700;
}

.header {
  background: linear-gradient(90deg, #ffffff 0%, #f8fafc 100%);
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  height: 70px;
}

.header-left {
  flex: 1;
}

.header-left :deep(.el-breadcrumb-item) {
  font-size: 16px;
  font-weight: 500;
  color: #2d3748;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 12px 20px;
  height: 46px;
  border-radius: 25px;
  transition: all 0.3s ease;
  background: rgba(49, 130, 206, 0.05);
  border: 1px solid rgba(49, 130, 206, 0.1);
}

.user-info:hover {
  background: rgba(49, 130, 206, 0.1);
  border-color: rgba(49, 130, 206, 0.2);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(49, 130, 206, 0.15);
}

.user-info .el-icon {
  margin-right: 10px;
  color: #3182ce;
}

.main-content {
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  padding: 30px;
  min-height: calc(100vh - 70px);
}
</style>