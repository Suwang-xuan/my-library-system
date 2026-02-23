<template>
  <div class="layout-container">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-left">
        <!-- 折叠/展开按钮 -->
        <button class="collapse-btn" @click="toggleCollapse">
          <el-icon>{{ isCollapsed ? 'ArrowRight' : 'ArrowLeft' }}</el-icon>
        </button>
        <div class="logo header-logo" v-if="!isCollapsed">图书馆管理系统 - 读者中心</div>
      </div>
      <div class="header-right">
        <span class="user-info">欢迎，{{ readerName }}</span>
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
            <template v-else>读者中心</template>
          </div>
        </div>
        <nav class="menu">
          <div 
            class="menu-item" 
            :class="{ 'menu-item-active': currentPath === '/reader/dashboard' }"
            @click="navigateTo('/reader/dashboard')"
          >
            <el-icon><House /></el-icon>
            <span class="menu-text" v-if="!isCollapsed">读者首页</span>
            <span class="menu-item-dot" v-if="currentPath === '/reader/dashboard' && !isCollapsed"></span>
          </div>
          <div 
            class="menu-item" 
            :class="{ 'menu-item-active': currentPath === '/reader/books' }"
            @click="navigateTo('/reader/books')"
          >
            <el-icon><Reading /></el-icon>
            <span class="menu-text" v-if="!isCollapsed">图书搜索</span>
            <span class="menu-item-dot" v-if="currentPath === '/reader/books' && !isCollapsed"></span>
          </div>
          <div 
            class="menu-item" 
            :class="{ 'menu-item-active': currentPath === '/reader/borrow-records' }"
            @click="navigateTo('/reader/borrow-records')"
          >
            <el-icon><Document /></el-icon>
            <span class="menu-text" v-if="!isCollapsed">借阅记录</span>
            <span class="menu-item-dot" v-if="currentPath === '/reader/borrow-records' && !isCollapsed"></span>
          </div>
          <div 
            class="menu-item" 
            :class="{ 'menu-item-active': currentPath === '/reader/profile' }"
            @click="navigateTo('/reader/profile')"
          >
            <el-icon><UserFilled /></el-icon>
            <span class="menu-text" v-if="!isCollapsed">个人设置</span>
            <span class="menu-item-dot" v-if="currentPath === '/reader/profile' && !isCollapsed"></span>
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
import { House, Reading, Document, UserFilled, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

// 读者信息
const readerInfo = ref(null)

// 侧边栏折叠状态
const isCollapsed = ref(false)

// 当前路径
const currentPath = computed(() => {
  return route.path
})

// 计算读者名
const readerName = computed(() => {
  return readerInfo.value?.readerName || '读者'
})

// 切换侧边栏折叠状态
const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
  // 保存折叠状态到sessionStorage
  sessionStorage.setItem('readerSidebarCollapsed', JSON.stringify(isCollapsed.value))
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
    sessionStorage.removeItem('readerInfo')
    router.push('/login')
  }
}

// 页面加载时初始化
onMounted(() => {
  try {
    // 获取并解析readerInfo
    const readerInfoStr = sessionStorage.getItem('readerInfo')
    if (readerInfoStr) {
      const info = JSON.parse(readerInfoStr)
      if (info) {
        // 只要info存在就保存，不严格要求readerId字段
        readerInfo.value = info
      }
    }
    
    // 恢复侧边栏折叠状态
    const collapsedStr = sessionStorage.getItem('readerSidebarCollapsed')
    if (collapsedStr) {
      isCollapsed.value = JSON.parse(collapsedStr)
    }
  } catch (error) {
    console.error('Failed to parse readerInfo:', error)
    // 解析失败，不清除readerInfo，避免影响跳转
  }
})

// 监听路由变化
watch(() => route.path, (newPath) => {
  // 路由变化时不需要额外操作，currentPath会自动更新
})
</script>

<style lang="scss" scoped>
/* 导入全局变量 */
@import "@/styles/variables.scss";

/* 基础布局 */
.layout-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: $background-primary;
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

/* 头部右侧区域 */
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

/* 用户信息样式 */
.user-info {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

/* 退出按钮样式 */
.logout-btn {
  padding: 6px 16px;
  background-color: #f56c6c;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;

  &:hover {
    background-color: #f78989;
    transform: translateY(-1px);
  }
}
</style>