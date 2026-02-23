import { createRouter, createWebHistory } from 'vue-router'
import routes from './routes'

const router = createRouter({
  history: createWebHistory(),
  routes: routes.routes
})

router.beforeEach((to, from, next) => {
  document.title = `${to.meta.title || '图书馆管理系统'} - 图书借阅管理系统`

  // 公共路由直接放行
  if (to.meta.public) {
    next()
    return
  }

  try {
    // 检查是否是读者相关路由
    if (to.path.startsWith('/reader/')) {
      // 检查读者信息
      const readerInfoStr = sessionStorage.getItem('readerInfo')
      if (readerInfoStr) {
        const readerInfo = JSON.parse(readerInfoStr)
        if (readerInfo && readerInfo.readerId) {
          next()
          return
        }
      }
      // 读者未登录，放行到读者首页（不需要重定向到登录）
      next()
      return
    }

    // 管理员相关路由，检查管理员信息
    const adminInfoStr = sessionStorage.getItem('adminInfo')
    if (!adminInfoStr) {
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }
    
    const adminInfo = JSON.parse(adminInfoStr)
    if (!adminInfo || !adminInfo.adminId) {
      sessionStorage.removeItem('adminInfo')
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }
    
    next()
  } catch (error) {
    // 如果解析出错，清除相关信息
    sessionStorage.removeItem('adminInfo')
    sessionStorage.removeItem('readerInfo')
    
    if (to.path.startsWith('/reader/')) {
      next()
    } else {
      next({ name: 'Login', query: { redirect: to.fullPath } })
    }
  }
})

export default router
