<template>
  <el-container class="layout-container">
    <!-- 左侧菜单栏 -->
    <el-aside :width="sidebarWidth" class="sidebar">
      <!-- Logo区域 -->
      <div class="logo-section">
        <div class="logo-icon">
          <el-icon :size="28"><House /></el-icon>
        </div>
        <transition name="fade">
          <div v-show="!isCollapse" class="logo-text">
            <div class="logo-title">同济酒店</div>
            <div class="logo-subtitle">管理系统 v2.0</div>
          </div>
        </transition>
      </div>

      <!-- 菜单区域 -->
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :unique-opened="true"
        router
        class="sidebar-menu"
      >
        <!-- 一级菜单：数据中心 -->
        <el-sub-menu index="dashboard">
          <template #title>
            <el-icon><DataAnalysis /></el-icon>
            <span>数据中心</span>
          </template>
          <el-menu-item index="/dashboard">数据概览</el-menu-item>
          <el-menu-item index="/dashboard/reports">报表分析</el-menu-item>
          <el-menu-item index="/dashboard/performance">经营分析</el-menu-item>
        </el-sub-menu>

        <!-- 一级菜单：房务管理 -->
        <el-sub-menu index="room">
          <template #title>
            <el-icon><House /></el-icon>
            <span>房务管理</span>
          </template>
          <el-menu-item index="/rooms">房态总览</el-menu-item>
          <el-menu-item index="/rooms/check-in">入住办理</el-menu-item>
          <el-menu-item index="/rooms/check-out">退房办理</el-menu-item>
          <el-menu-item index="/rooms/change">换房/续住</el-menu-item>
        </el-sub-menu>

        <!-- 一级菜单：预订中心 -->
        <el-sub-menu index="booking">
          <template #title>
            <el-icon><Calendar /></el-icon>
            <span>预订中心</span>
          </template>
          <el-menu-item index="/bookings">预订管理</el-menu-item>
          <el-menu-item index="/bookings/calendar">房态日历</el-menu-item>
          <el-menu-item index="/bookings/group">团队预订</el-menu-item>
        </el-sub-menu>

        <!-- 一级菜单：客户管理 -->
        <el-sub-menu index="customer">
          <template #title>
            <el-icon><User /></el-icon>
            <span>客户管理</span>
          </template>
          <el-menu-item index="/customers">客户列表</el-menu-item>
          <el-menu-item index="/customers/vip">会员管理</el-menu-item>
          <el-menu-item index="/customers/profile">客户画像</el-menu-item>
        </el-sub-menu>

        <!-- 一级菜单：智能服务 -->
        <el-sub-menu index="service">
          <template #title>
            <el-icon><Service /></el-icon>
            <span>智能服务</span>
          </template>
          <el-menu-item index="/services">个性化服务</el-menu-item>
          <el-menu-item index="/services/tasks">任务中心</el-menu-item>
          <el-menu-item index="/services/feedback">客户反馈</el-menu-item>
        </el-sub-menu>

        <!-- 一级菜单：智能营销 -->
        <el-sub-menu index="marketing">
          <template #title>
            <el-icon><TrendCharts /></el-icon>
            <span>智能营销</span>
          </template>
          <el-menu-item index="/overbooking">智能超售</el-menu-item>
          <el-menu-item index="/reputation">声誉管理</el-menu-item>
          <el-menu-item index="/pricing">动态定价</el-menu-item>
        </el-sub-menu>

        <el-divider style="margin: 8px 0; border-color: rgba(255,255,255,0.08)" />

        <!-- 系统设置 -->
        <el-menu-item index="/settings">
          <el-icon><Setting /></el-icon>
          <template #title>系统设置</template>
        </el-menu-item>
      </el-menu>

      <!-- 底部折叠按钮 -->
      <div class="collapse-btn" @click="toggleCollapse">
        <el-icon>
          <Fold v-if="!isCollapse" />
          <Expand v-else />
        </el-icon>
      </div>
    </el-aside>

    <!-- 右侧主内容区 -->
    <el-container class="main-container">
      <!-- 顶部导航栏 -->
      <el-header class="top-header">
        <div class="header-left">
          <!-- 面包屑 -->
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <!-- 全局搜索 -->
          <div class="search-box">
            <el-input
              v-model="searchText"
              placeholder="搜索预订、客户、房间..."
              :prefix-icon="Search"
              clearable
              class="search-input"
            />
          </div>

          <!-- 快捷操作按钮组 -->
          <div class="action-buttons">
            <el-tooltip content="消息通知" placement="bottom">
              <el-badge :value="5" :max="99" class="badge-item">
                <el-button :icon="Bell" circle />
              </el-badge>
            </el-tooltip>

            <el-tooltip content="刷新数据" placement="bottom">
              <el-button :icon="Refresh" circle />
            </el-tooltip>

            <el-tooltip content="全屏显示" placement="bottom">
              <el-button :icon="FullScreen" circle />
            </el-tooltip>
          </div>

          <!-- 用户信息下拉菜单 -->
          <el-dropdown class="user-dropdown" trigger="click">
            <div class="user-info">
              <el-avatar :size="36" :src="userAvatar">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
              <div class="user-detail">
                <div class="user-name">{{ userName }}</div>
                <div class="user-role">{{ userRole }}</div>
              </div>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item>
                  <el-icon><Lock /></el-icon>
                  修改密码
                </el-dropdown-item>
                <el-dropdown-item>
                  <el-icon><Setting /></el-icon>
                  系统设置
                </el-dropdown-item>
                <el-dropdown-item divided>
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="content-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" :key="$route.path" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import {
  House, DataAnalysis, Calendar, User, Service, TrendCharts,
  Setting, Fold, Expand, Search, Bell, Refresh, FullScreen,
  ArrowDown, UserFilled, Lock, SwitchButton
} from '@element-plus/icons-vue'

const route = useRoute()
const isCollapse = ref(false)
const searchText = ref('')

// 用户信息
const userName = ref('管理员')
const userRole = ref('超级管理员')
const userAvatar = ref('https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png')

// 当前激活的菜单
const activeMenu = computed(() => {
  return route.path
})

// 侧边栏宽度
const sidebarWidth = computed(() => {
  return isCollapse.value ? '64px' : '240px'
})

// 当前页面标题
const currentTitle = computed(() => {
  const titles: Record<string, string> = {
    '/dashboard': '数据概览',
    '/dashboard/reports': '报表分析',
    '/dashboard/performance': '部门绩效',
    '/rooms': '房态总览',
    '/rooms/check-in': '入住办理',
    '/rooms/check-out': '退房办理',
    '/bookings': '预订管理',
    '/bookings/calendar': '房态日历',
    '/customers': '客户列表',
    '/customers/vip': '会员管理',
    '/services': '个性化服务',
    '/services/tasks': '任务中心',
    '/reputation': '声誉管理',
    '/overbooking': '智能超售',
    '/settings': '系统设置'
  }
  return titles[route.path] || '酒店管理系统'
})

// 切换折叠
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

// 监听路由变化，更新激活菜单
watch(() => route.path, (newPath) => {
  console.log('路由变化:', newPath)
}, { immediate: true })
</script>

<style scoped>
/* ========== 布局容器 ========== */
.layout-container {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

/* ========== 左侧侧边栏 ========== */
.sidebar {
  background: linear-gradient(180deg, #0f172a 0%, #1e293b 100%);
  box-shadow: 4px 0 24px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 100;
}

/* Logo区域 */
.logo-section {
  display: flex;
  align-items: center;
  padding: 20px;
  height: 70px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(0, 0, 0, 0.2);
}

.logo-icon {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
  border-radius: 10px;
  color: #ffffff;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
}

.logo-text {
  margin-left: 12px;
  flex: 1;
}

.logo-title {
  font-size: 16px;
  font-weight: 600;
  color: #ffffff;
  line-height: 1.2;
  letter-spacing: 0.5px;
}

.logo-subtitle {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.5);
  margin-top: 2px;
  letter-spacing: 0.3px;
}

/* 菜单样式 */
.sidebar-menu {
  flex: 1;
  border: none;
  background: transparent;
  padding: 12px 8px;
  overflow-y: auto;
  overflow-x: hidden;
}

.sidebar-menu::-webkit-scrollbar {
  width: 4px;
}

.sidebar-menu::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 2px;
}

.sidebar-menu :deep(.el-sub-menu__title) {
  color: rgba(255, 255, 255, 0.95) !important;
  padding: 0 12px;
  height: 44px;
  margin: 2px 0;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.sidebar-menu :deep(.el-sub-menu__title:hover) {
  background: rgba(255, 255, 255, 0.12) !important;
  color: #ffffff !important;
}

/* 子菜单折叠面板 */
.sidebar-menu :deep(.el-sub-menu .el-menu) {
  background: transparent !important;
}

.sidebar-menu :deep(.el-sub-menu .el-menu-item) {
  color: rgba(255, 255, 255, 0.9) !important;
  background-color: transparent !important;
}

.sidebar-menu :deep(.el-menu-item) {
  color: rgba(255, 255, 255, 0.9) !important;
  padding-left: 48px !important;
  height: 40px;
  margin: 1px 0;
  border-radius: 6px;
  transition: all 0.3s ease;
  background-color: transparent !important;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.12) !important;
  color: #ffffff !important;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(59, 130, 246, 0.8) 0%, rgba(139, 92, 246, 0.6) 100%) !important;
  color: #ffffff !important;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
}

/* 子菜单弹出面板 */
.sidebar-menu :deep(.el-menu--popup) {
  background: #1e293b !important;
  min-width: 200px;
  padding: 8px;
}

.sidebar-menu :deep(.el-menu--popup .el-menu-item) {
  color: rgba(255, 255, 255, 0.9) !important;
  padding-left: 12px !important;
  background: transparent !important;
}

.sidebar-menu :deep(.el-menu--popup .el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.12) !important;
  color: #ffffff !important;
}

/* 图标和文字颜色 */
.sidebar-menu :deep(.el-icon) {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.95) !important;
}

.sidebar-menu :deep(.el-sub-menu__title span),
.sidebar-menu :deep(.el-menu-item span) {
  color: rgba(255, 255, 255, 0.9) !important;
}

.sidebar-menu :deep(.el-sub-menu__title:hover span),
.sidebar-menu :deep(.el-menu-item:hover span) {
  color: #ffffff !important;
}

/* 折叠按钮 */
.collapse-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 48px;
  color: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  transition: all 0.3s ease;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.collapse-btn:hover {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.08);
}

/* ========== 右侧主容器 ========== */
.main-container {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* ========== 顶部导航栏 ========== */
.top-header {
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 64px;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.05);
  z-index: 50;
}

.header-left {
  flex: 1;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

/* 搜索框 */
.search-box {
  width: 300px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 20px;
  background: #f9fafb;
  box-shadow: none;
  border: 1px solid transparent;
  transition: all 0.3s ease;
}

.search-input :deep(.el-input__wrapper:hover) {
  border-color: #d1d5db;
}

.search-input :deep(.el-input__wrapper.is-focus) {
  background: #ffffff;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

/* 快捷操作按钮 */
.action-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-buttons .el-button {
  border: none;
  background: transparent;
  color: #64748b;
  transition: all 0.3s ease;
}

.action-buttons .el-button:hover {
  background: #f1f5f9;
  color: #3b82f6;
}

.badge-item {
  margin-right: 4px;
}

/* 用户下拉菜单 */
.user-dropdown {
  margin-left: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 24px;
  transition: all 0.3s ease;
  background: #f9fafb;
  border: 1px solid transparent;
}

.user-info:hover {
  background: #f3f4f6;
  border-color: #e5e7eb;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.user-detail {
  margin: 0 10px;
  text-align: left;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  line-height: 1.2;
}

.user-role {
  font-size: 12px;
  color: #6b7280;
  margin-top: 2px;
}

.dropdown-icon {
  color: #9ca3af;
  transition: transform 0.3s ease;
}

/* ========== 主内容区 ========== */
.content-main {
  background: #f8fafc;
  padding: 24px;
  overflow-y: auto;
  overflow-x: hidden;
}

.content-main::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.content-main::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 3px;
}

.content-main::-webkit-scrollbar-track {
  background: transparent;
}

/* ========== 页面切换动画 ========== */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* Logo文字淡入淡出动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* ========== Element Plus样式覆盖 ========== */
:deep(.el-breadcrumb-item) {
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
}

:deep(.el-breadcrumb-item:last-child) {
  color: #1f2937;
  font-weight: 600;
}

:deep(.el-divider--horizontal) {
  margin: 12px 0;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .search-box {
    display: none;
  }

  .user-detail {
    display: none;
  }

  .top-header {
    padding: 0 16px;
  }

  .content-main {
    padding: 16px;
  }
}
</style>
