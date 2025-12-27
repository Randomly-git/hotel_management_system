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
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('../views/Dashboard.vue'),
          meta: { title: '数据概览', icon: 'DataLine' }
        },
        {
          path: 'rooms',
          name: 'Rooms',
          component: () => import('../views/Rooms.vue'),
          meta: { title: '房间管理', icon: 'House' }
        },
        {
          path: 'bookings',
          name: 'Bookings',
          component: () => import('../views/Bookings.vue'),
          meta: { title: '预订管理', icon: 'Calendar' }
        },
        {
          path: 'customers',
          name: 'Customers',
          component: () => import('../views/Customers.vue'),
          meta: { title: '客户管理', icon: 'User' }
        },
        {
          path: 'services',
          name: 'Services',
          component: () => import('../views/Services.vue'),
          meta: { title: '服务管理', icon: 'Service' }
        },
        {
          path: 'reputation',
          name: 'Reputation',
          component: () => import('../views/Reputation.vue'),
          meta: { title: '声誉管理', icon: 'Star' }
        }
      ]
    }
  ]
})

export default router
