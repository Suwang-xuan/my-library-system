import axios from 'axios'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

request.interceptors.request.use(
  (config) => {
        try {
          // 检查是否是读者相关API或借阅相关API
          if (config.url && (config.url.includes('/api/reader/') || config.url.includes('/api/borrow/'))) {
            // 对于读者和借阅API，不添加管理员头
            return config
          }
          
          // 管理员API，添加管理员信息
          const adminInfo = sessionStorage.getItem('adminInfo')
          if (adminInfo) {
            const parsed = JSON.parse(adminInfo)
            if (parsed && parsed.adminId) {
              config.headers['X-Admin-Id'] = parsed.adminId
            }
          }
        } catch (error) {
          console.error('Failed to parse adminInfo:', error)
        }
        return config
      },
  (error) => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response) => {
    const res = response.data
    // 检查响应结构，如果是CommonResult类型且code为200，返回data
    if (res.code !== undefined) {
      if (res.code === 200) {
        return res
      } else {
        return Promise.reject(new Error(res.message || '请求失败'))
      }
    } else {
      // 如果不是CommonResult类型，直接返回响应数据
      return { code: 200, data: res, message: '请求成功' }
    }
  },
  (error) => {
        if (error.response) {
            const { status, data } = error.response
            console.error('API错误响应:', {
                url: error.config.url,
                status: status,
                data: data
            })
            // 对于读者API和借阅记录API的401，不重定向到登录页
            const isReaderApi = error.config.url && (error.config.url.includes('/api/reader/') || error.config.url.includes('/api/borrow/'))
            if (status === 401 && !isReaderApi) {
                sessionStorage.removeItem('adminInfo')
                window.location.href = '/login'
            } else if (status === 403) {
                return Promise.reject(new Error('没有权限访问'))
            } else {
                const errorMsg = data.message || data.msg || `请求失败 (${status})`
                return Promise.reject(new Error(errorMsg))
            }
        } else if (error.request) {
            console.error('API请求错误:', {
                url: error.config.url,
                request: error.request
            })
            return Promise.reject(new Error('网络错误，无法连接到服务器'))
        } else {
            console.error('API请求配置错误:', error.message)
            return Promise.reject(new Error('请求配置错误'))
        }
    }
)

export default request
