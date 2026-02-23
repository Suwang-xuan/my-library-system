<template>
  <div class="layout-container">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-left">
        <!-- 折叠/展开按钮 -->
        <button class="collapse-btn" @click="toggleCollapse">
          <el-icon>{{ isCollapsed ? 'ArrowRight' : 'ArrowLeft' }}</el-icon>
        </button>
        <div class="logo header-logo" v-if="!isCollapsed">图书馆管理系统</div>
      </div>
      <div class="header-right">
        <span class="user-info">欢迎，{{ userName }}</span>
        <button class="logout-btn" @click="handleLogout">退出登录</button>
      </div>
    </header>
    
    <!-- 主体内容 -->
    <div class="main">
      <!-- 左侧侧边栏 -->
      <aside class="aside" :class="{ 'aside-collapsed': isCollapsed }">
        <div class="logo-container" :class="{ 'logo-container-collapsed': isCollapsed }">
          <div class="logo" :class="{ 'logo-text': !isCollapsed, 'logo-mini': isCollapsed }">
            <template v-if="isCollapsed">图管</template>
            <template v-else>图书馆管理系统</template>
          </div>
        </div>
        <nav class="menu">
          <div 
            class="menu-item" 
            :class="{ 'menu-item-active': currentPath === '/dashboard' }"
            @click="navigateTo('/dashboard')"
          >
            <el-icon><House /></el-icon>
            <span class="menu-text" v-if="!isCollapsed">首页</span>
            <span class="menu-item-dot" v-if="currentPath === '/dashboard' && !isCollapsed"></span>
          </div>
          <div 
            class="menu-item" 
            :class="{ 'menu-item-active': currentPath === '/book' }"
            @click="navigateTo('/book')"
          >
            <el-icon><Document /></el-icon>
            <span class="menu-text" v-if="!isCollapsed">图书管理</span>
            <span class="menu-item-dot" v-if="currentPath === '/book' && !isCollapsed"></span>
          </div>
          <div 
            class="menu-item" 
            :class="{ 'menu-item-active': currentPath === '/category' }"
            @click="navigateTo('/category')"
          >
            <el-icon><Grid /></el-icon>
            <span class="menu-text" v-if="!isCollapsed">图书分类</span>
            <span class="menu-item-dot" v-if="currentPath === '/category' && !isCollapsed"></span>
          </div>
          <div 
            class="menu-item" 
            :class="{ 'menu-item-active': currentPath === '/reader' }"
            @click="navigateTo('/reader')"
          >
            <el-icon><User /></el-icon>
            <span class="menu-text" v-if="!isCollapsed">读者管理</span>
            <span class="menu-item-dot" v-if="currentPath === '/reader' && !isCollapsed"></span>
          </div>
          <div 
            class="menu-item" 
            :class="{ 'menu-item-active': currentPath === '/borrow' }"
            @click="navigateTo('/borrow')"
          >
            <el-icon><Tickets /></el-icon>
            <span class="menu-text" v-if="!isCollapsed">借阅管理</span>
            <span class="menu-item-dot" v-if="currentPath === '/borrow' && !isCollapsed"></span>
          </div>
          <div 
            class="menu-item" 
            :class="{ 'menu-item-active': currentPath === '/statistics' }"
            @click="navigateTo('/statistics')"
          >
            <el-icon><TrendCharts /></el-icon>
            <span class="menu-text" v-if="!isCollapsed">数据统计</span>
            <span class="menu-item-dot" v-if="currentPath === '/statistics' && !isCollapsed"></span>
          </div>
          <div 
            class="menu-item" 
            :class="{ 'menu-item-active': currentPath === '/admin' }"
            @click="navigateTo('/admin')"
          >
            <el-icon><Setting /></el-icon>
            <span class="menu-text" v-if="!isCollapsed">管理员管理</span>
            <span class="menu-item-dot" v-if="currentPath === '/admin' && !isCollapsed"></span>
          </div>
          
          <!-- 个人中心上方分割线 -->
          <div class="menu-item-divider"></div>
          
          <div 
            class="menu-item" 
            :class="{ 'menu-item-active': currentPath === '/profile' }"
            @click="navigateTo('/profile')"
          >
            <el-icon><UserFilled /></el-icon>
            <span class="menu-text" v-if="!isCollapsed">个人中心</span>
            <span class="menu-item-dot" v-if="currentPath === '/profile' && !isCollapsed"></span>
          </div>
        </nav>
      </aside>
      
      <!-- 中间内容区域 -->
      <main class="content" :class="{ 'content-expanded': isCollapsed }">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { authApi } from '@/api'
import { House, Document, Grid, User, Tickets, TrendCharts, Setting, UserFilled, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

// 用户信息
const userInfo = ref(null)

// 侧边栏折叠状态
const isCollapsed = ref(false)

// 当前路径
const currentPath = computed(() => {
  return route.path
})

// 计算用户名
const userName = computed(() => {
  return userInfo.value?.adminName || userInfo.value?.adminAccount || '管理员'
})

// 切换侧边栏折叠状态
const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
  // 保存折叠状态到sessionStorage
  sessionStorage.setItem('sidebarCollapsed', JSON.stringify(isCollapsed.value))
}

// 导航方法
const navigateTo = (path) => {
  router.push(path)
}

// 退出登录
const handleLogout = async () => {
  try {
    // 尝试调用logout API
    await authApi.logout().catch(() => {})
  } finally {
    // 清除会话信息并跳转到登录页
    sessionStorage.removeItem('adminInfo')
    router.push('/login')
  }
}

// 页面加载时初始化
onMounted(() => {
  try {
    // 获取并解析adminInfo
    const adminInfoStr = sessionStorage.getItem('adminInfo')
    if (adminInfoStr) {
      const adminInfo = JSON.parse(adminInfoStr)
      if (adminInfo && adminInfo.adminId) {
        userInfo.value = adminInfo
      } else {
        // adminInfo无效，清除并跳转到登录页
        sessionStorage.removeItem('adminInfo')
        router.push('/login')
      }
    } else {
      // 未登录，跳转到登录页
      router.push('/login')
    }
    
    // 恢复侧边栏折叠状态
    const collapsedStr = sessionStorage.getItem('sidebarCollapsed')
    if (collapsedStr) {
      isCollapsed.value = JSON.parse(collapsedStr)
    }
  } catch (error) {
    // 解析失败，清除并跳转到登录页
    sessionStorage.removeItem('adminInfo')
    router.push('/login')
  }
})

// 监听路由变化，更新当前路径
watch(() => route.path, (newPath) => {
  // 路由变化时不需要额外操作，currentPath会自动更新
})
</script>

<style lang="scss" scoped>
/* 基础布局 */
.layout-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #F8FAFC;
}

/* 侧边栏样式 */
.aside {
  background-color: #2c3e50;
  overflow: hidden;
  transition: width 0.3s ease;
  width: 220px;
  display: flex;
  flex-direction: column;
}

/* 侧边栏折叠样式 */
.aside-collapsed {
  width: 64px;
}

/* Logo容器样式 */
.logo-container {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #2D3B4A;
  border-bottom: 1px solid #405060;
  transition: all 0.3s ease;
}

/* 折叠状态下的Logo容器 */
.logo-container-collapsed {
  justify-content: center;
}

/* Logo样式 */
.logo {
  font-size: 18px;
  font-weight: 600;
  /* 浅白到淡蓝的轻微渐变效果 */
  background: linear-gradient(135deg, #FFFFFF, #86A8FF);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  white-space: nowrap;
  letter-spacing: 0.5px;
  transition: all 0.3s ease;
}

/* 顶部导航栏Logo样式 */
.header-logo {
  font-size: 20px;
  font-weight: bold;
  font-family: 'Microsoft YaHei', sans-serif;
  background: linear-gradient(135deg, #4080FF 0%, #69b1ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 折叠状态下的Logo */
.logo-mini {
  font-size: 18px;
  font-weight: 600;
  color: #6B90C6;
}

/* 菜单样式 */
.menu {
  padding: 16px 0;
  flex: 1;
  overflow-y: auto;
}

/* 菜单项样式 */
.menu-item {
  display: flex;
  align-items: center;
  padding: 12px 24px;
  color: #E2E8F0;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
  gap: 12px;
  position: relative;
  overflow: hidden;
}

/* 折叠状态下的菜单项 */
.aside-collapsed .menu-item {
  padding: 12px 16px;
  justify-content: center;
}

/* 菜单项hover效果 */
.menu-item:hover {
  background-color: rgba(255, 255, 255, 0.08);
  color: #f5f5f5;
  /* 文字右侧微移1px */
  .menu-text {
    transform: translateX(1px);
  }
  
  .el-icon {
    color: #f5f5f5;
  }
}

/* 当前选中菜单项样式 */
.menu-item-active {
  background-color: rgba(0, 0, 0, 0.1) !important;
  color: #FFFFFF !important;
  /* 添加2px线性渐变侧边条 */
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 2px;
    background: linear-gradient(180deg, #4080FF, #69b1ff);
  }
  
  .el-icon {
    /* 图标高亮 - 模块主题色 */
    color: #4080FF !important;
  }
}

/* 菜单项图标样式 */
.el-icon {
  font-size: 18px;
  transition: all 0.3s ease;
  width: 20px;
  text-align: center;
  color: #CCCCCC; /* 默认浅灰图标 */
}

/* 菜单项文字样式 */
.menu-text {
  font-size: 14px;
  font-weight: 500;
  transition: transform 0.3s ease;
  position: relative;
}

/* 菜单项激活状态的小圆点标记 */
.menu-item-dot {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background-color: #4080FF;
  margin-left: -8px;
}

/* 个人中心上方分割线 */
.menu-item-divider {
  height: 1px;
  background-color: #444444;
  margin: 8px 0;
}

/* 主体内容布局 */
.main {
  display: flex;
  flex: 1;
  overflow: hidden;
}

/* 内容区域样式 */
.content {
  background: #F8FAFC;
  padding: 24px;
  overflow-y: auto;
  flex: 1;
  transition: all 0.3s ease;
}

/* 折叠状态下的内容区域 */
.content-expanded {
  /* 内容区域可以根据需要调整，这里保持不变 */
}

/* 折叠/展开按钮样式 */
.collapse-btn {
  font-size: 20px;
  color: #606266;
  cursor: pointer;
  padding: 8px;
  border-radius: 4px;
  transition: all 0.3s ease;
  background: none;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
}

.collapse-btn:hover {
  color: #6B90C6;
  background-color: #F1F5F9;
}

/* 菜单项图标样式 */
.el-icon {
  font-size: 18px;
  transition: all 0.3s ease;
  width: 20px;
  text-align: center;
}

/* Logo容器样式 */
.logo-container {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #2D3B4A;
  border-bottom: 1px solid #405060;

  .logo {
    font-size: 18px;
    font-weight: 600;
    color: #FFFFFF;
    white-space: nowrap;
    letter-spacing: 0.5px;
  }
}

/* 头部样式 */
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #FFFFFF;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  padding: 0 24px;
  height: 64px;
  border-bottom: 1px solid #E2E8F0;
  transition: all 0.3s ease;
}

/* 头部左侧区域 */
.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

/* 折叠按钮样式 */
.collapse-btn {
  font-size: 20px;
  color: #606266;
  cursor: pointer;
  padding: 8px;
  border-radius: 4px;
  transition: all 0.3s ease;

  &:hover {
    color: #6B90C6;
    background-color: #F1F5F9;
  }
}

/* 小屏幕Logo */
.logo-mini {
  .logo-text {
    font-size: 18px;
    font-weight: 600;
    color: #6B90C6;
  }
}

/* 头部中间区域 - 搜索框 */
.header-center {
  display: flex;
  align-items: center;
  flex: 1;
  max-width: 600px;
  margin: 0 24px;
  transition: all 0.3s ease;
}

/* 全局搜索框样式 */
.global-search {
  width: 100%;
  max-width: 600px;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;

  :deep(.el-input__wrapper) {
    border-radius: 8px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
    border-color: #E2E8F0;
  }

  :deep(.el-input__inner) {
    font-size: 14px;
    padding: 10px 16px;
  }

  :deep(.el-button) {
    border-radius: 0 8px 8px 0;
    background-color: #6B90C6;
    border-color: #6B90C6;
    
    &:hover {
      background-color: #5A7FA6;
      border-color: #5A7FA6;
    }
  }
}

/* 头部右侧区域 */
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

/* 通知图标样式 */
.notification-badge {
  cursor: pointer;
  font-size: 20px;
  color: #606266;
  transition: all 0.3s ease;
  padding: 8px;
  border-radius: 4px;

  &:hover {
    color: #6B90C6;
    background-color: #F1F5F9;
  }

  :deep(.el-badge__content) {
    background-color: #FF7043;
    box-shadow: 0 2px 6px rgba(255, 112, 67, 0.4);
  }
}

/* 角色标签样式 */
.role-tag {
  font-weight: 500;
  transition: all 0.3s ease;
}

/* 主内容区域样式 */
.main {
  background: #F8FAFC;
  padding: 24px;
  overflow-y: auto;
  flex: 1;
  transition: all 0.3s ease;
}

/* 页脚样式 */
.footer {
  background: #FFFFFF;
  text-align: center;
  line-height: 60px;
  font-size: 12px;
  color: #718096;
  border-top: 1px solid #E2E8F0;
  transition: all 0.3s ease;
}

/* 自定义下拉菜单样式 */
.custom-dropdown {
  position: relative;
  display: inline-block;
}

.dropdown-trigger {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.3s ease;

  &:hover {
    background-color: #F1F5F9;
  }

  .user-avatar {
    margin-right: 0;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  }

  .user-name {
    font-size: 14px;
    font-weight: 500;
    color: #2D3748;
  }
}

.dropdown-menu {
  position: absolute;
  right: 0;
  top: 100%;
  margin-top: 8px;
  background: #FFFFFF;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.07);
  min-width: 180px;
  z-index: 1000;
  transition: all 0.3s ease;
  border: 1px solid #E2E8F0;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #2D3748;
  text-decoration: none;
  font-size: 14px;

  &:hover {
    background-color: #F1F5F9;
    color: #6B90C6;
  }

  .el-icon {
    font-size: 16px;
  }
}

.dropdown-divider {
  height: 1px;
  background-color: #E2E8F0;
  margin: 4px 0;
}

/* 淡入淡出动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .header {
    padding: 0 16px;
  }
  
  .header-center {
    margin: 0 16px;
    max-width: 400px;
  }
  
  .global-search {
    max-width: 400px;
  }
  
  .main {
    padding: 16px;
  }
}

@media (max-width: 768px) {
  .header-center {
    display: none;
  }
  
  .header-right {
    gap: 8px;
  }
  
  .role-tag {
    display: none;
  }
  
  .dropdown-trigger {
    padding: 8px;
  }
  
  .user-name {
    display: none;
  }
}
</style>
