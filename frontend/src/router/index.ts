import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../views/Layout.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: Layout,
      redirect: '/dashboard',
      children: [
        // 数据中心
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('../views/Dashboard.vue'),
          meta: { title: '数据概览', icon: 'DataLine' }
        },
        {
          path: 'dashboard/reports',
          name: 'Reports',
          component: () => import('../views/placeholder/Reports.vue'),
          meta: { title: '报表分析', icon: 'DataLine' }
        },
        //{
        //  path: 'dashboard/performance',
        //  name: 'Performance',
        //  component: () => import('../views/placeholder/Pricing.vue'),
        //  meta: { title: '经营分析', icon: 'DataLine' }
        //},
        // 房务管理
        {
          path: 'rooms',
          name: 'Rooms',
          component: () => import('../views/Rooms.vue'),
          meta: { title: '房态总览', icon: 'House' }
        },
        {
          path: 'rooms/check-in',
          name: 'CheckIn',
          component: () => import('../views/placeholder/CheckIn.vue'),
          meta: { title: '入住办理', icon: 'House' }
        },
        {
          path: 'rooms/check-out',
          name: 'CheckOut',
          component: () => import('../views/placeholder/CheckOut.vue'),
          meta: { title: '退房办理', icon: 'House' }
        },
        {
          path: 'rooms/change',
          name: 'RoomChange',
          component: () => import('../views/placeholder/RoomChange.vue'),
          meta: { title: '换房/续住', icon: 'House' }
        },
        // 预订中心
        {
          path: 'bookings',
          name: 'Bookings',
          component: () => import('../views/Bookings.vue'),
          meta: { title: '预订管理', icon: 'Calendar' }
        },
        {
          path: 'bookings/calendar',
          name: 'BookingCalendar',
          component: () => import('../views/placeholder/BookingCalendar.vue'),
          meta: { title: '房态日历', icon: 'Calendar' }
        },
        {
          path: 'bookings/group',
          name: 'GroupBooking',
          component: () => import('../views/placeholder/GroupBooking.vue'),
          meta: { title: '团队预订', icon: 'Calendar' }
        },
        // 客户管理
        {
          path: 'customers',
          name: 'Customers',
          component: () => import('../views/Customers.vue'),
          meta: { title: '客户列表', icon: 'User' }
        },
        {
          path: 'customers/vip',
          name: 'VIP',
          component: () => import('../views/placeholder/VIP.vue'),
          meta: { title: '会员管理', icon: 'User' }
        },
        {
          path: 'customers/profile',
          name: 'CustomerProfile',
          component: () => import('../views/placeholder/CustomerProfile.vue'),
          meta: { title: '客户画像', icon: 'User' }
        },
        // 智能服务
        {
          path: 'services',
          name: 'Services',
          component: () => import('../views/Services.vue'),
          meta: { title: '个性化服务', icon: 'Service' }
        },
        {
          path: 'services/tasks',
          name: 'TaskCenter',
          component: () => import('../views/placeholder/TaskCenter.vue'),
          meta: { title: '任务中心', icon: 'Service' }
        },
        {
          path: 'services/feedback',
          name: 'Feedback',
          component: () => import('../views/placeholder/Feedback.vue'),
          meta: { title: '客户反馈', icon: 'Service' }
        },
        // 智能营销
        {
          path: 'overbooking',
          name: 'Overbooking',
          component: () => import('../views/Overbooking.vue'),
          meta: { title: '智能超售', icon: 'TrendCharts' }
        },
        {
          path: 'reputation',
          name: 'Reputation',
          component: () => import('../views/Reputation.vue'),
          meta: { title: '声誉管理', icon: 'Star' }
        },
        {
          path: 'pricing',
          name: 'Pricing',
          component: () => import('../views/placeholder/Pricing.vue'),
          meta: { title: '动态定价', icon: 'TrendCharts' }
        },
        // 系统设置
        {
          path: 'settings',
          name: 'Settings',
          component: () => import('../views/placeholder/Settings.vue'),
          meta: { title: '系统设置', icon: 'Setting' }
        }
      ]
    }
  ]
})

export default router
