import axios from 'axios'
import { ElMessage } from 'element-plus'

// API 基础地址
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8082'

// 创建 axios 实例
const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    // 可以在这里添加token等认证信息
    // const token = localStorage.getItem('token')
    // if (token) {
    //   config.headers.Authorization = `Bearer ${token}`
    // }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    // 统一处理响应数据
    return response
  },
  (error) => {
    // 统一错误处理
    if (error.response) {
      const { status, data } = error.response
      switch (status) {
        case 400:
          ElMessage.error(data?.message || '请求参数错误')
          break
        case 401:
          ElMessage.error('未授权，请重新登录')
          // 可以在这里处理登录跳转
          break
        case 403:
          ElMessage.error('拒绝访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error(data?.message || '服务器内部错误')
          break
        default:
          ElMessage.error(data?.message || '网络错误，请稍后重试')
      }
    } else if (error.request) {
      ElMessage.error('网络连接失败，请检查网络')
    } else {
      ElMessage.error(error.message || '请求失败')
    }
    return Promise.reject(error)
  }
)

export default api
export { API_BASE_URL }

/**
 * 动态定价相关接口
 */
export const pricingApi = {
    /**
     * 1. 触发计算建议 (生成 PENDING 状态记录)
     * 对应后端: POST /api/v1/pricing/adjust
     */
    generateProposals() {
        return api.post('/api/v1/pricing/adjust')
    },

    /**
     * 2. 获取待审批列表 (店长交互核心)
     * 对应后端: GET /api/v1/pricing/pending
     */
    getPendingList() {
        return api.get('/api/v1/pricing/pending')
    },

    /**
     * 3. 批准调价建议 (状态转为 APPLIED)
     * 对应后端: POST /api/v1/pricing/approve/{recordId}
     */
    approveProposal(recordId: number) {
        return api.post(`/api/v1/pricing/approve/${recordId}`)
    },

    /**
     * 4. 查询生效房价 (含保底降级逻辑)
     * 对应后端: GET /api/v1/pricing/current?date=YYYY-MM-DD
     */
    getCurrentPrices(date: string) {
        return api.get('/api/v1/pricing/current', { params: { date } })
    }
}

/**
 * 部门绩效相关接口
 */
export const performanceApi = {
    /**
     * 1. 计算绩效 (触发指定日期的绩效计算)
     * 对应后端: POST /api/v1/performance/calculate
     */
    calculatePerformance(params: { hotelId: string; date: string }) {
        return api.post('/api/v1/performance/calculate', null, { params })
    },

    /**
     * 2. 获取绩效历史 (指定日期范围内的所有部门绩效)
     * 对应后端: GET /api/v1/performance/history
     */
    getPerformanceHistory(params: { hotelId: string; startDate: string; endDate: string }) {
        return api.get('/api/v1/performance/history', { params })
    },

    /**
     * 3. 获取部门今日绩效
     * 对应后端: GET /api/v1/performance/today
     */
    getTodayPerformance(params: { hotelId: string; deptId: number }) {
        return api.get('/api/v1/performance/today', { params })
    },

    /**
     * 4. 获取部门绩效对比 (Dashboard API)
     * 对应后端: GET /api/v1/dashboard/department-performance
     */
    getDepartmentPerformanceComparison(params: { hotelId: number; timeRange: string }) {
        return api.get('/api/v1/dashboard/department-performance', { params })
    },

    /**
     * 5. 获取系统告警信息
     * 对应后端: GET /api/v1/dashboard/alerts
     */
    getAlerts(params: { hotelId: number }) {
        return api.get('/api/v1/dashboard/alerts', { params })
    },

    /**
     * 6. 获取运营概览数据
     * 对应后端: GET /api/v1/dashboard/overview
     */
    getOverview(params: { hotelId: number }) {
        return api.get('/api/v1/dashboard/overview', { params })
    },

    /**
     * 7. 获取仪表盘统计数据
     * 对应后端: GET /api/v1/dashboard/statistics
     */
    getStatistics(params: { hotelId: number; timeRange?: string }) {
        return api.get('/api/v1/dashboard/statistics', { params })
    },

    /**
     * 8. 获取部门绩效对比
     * 对应后端: GET /api/v1/dashboard/department-performance
     */
    getDepartmentPerformance(params: { hotelId: number; timeRange?: string }) {
        return api.get('/api/v1/dashboard/department-performance', { params })
    },

    /**
     * 9. 获取请求趋势数据
     * 对应后端: GET /api/v1/dashboard/request-trend
     */
    getRequestTrend(params: { hotelId: number; timeRange?: string }) {
        return api.get('/api/v1/dashboard/request-trend', { params })
    },

    /**
     * 10. 获取AI解析统计
     * 对应后端: GET /api/v1/dashboard/ai-statistics
     */
    getAIStatistics(params: { hotelId: number; timeRange?: string }) {
        return api.get('/api/v1/dashboard/ai-statistics', { params })
    }
}















