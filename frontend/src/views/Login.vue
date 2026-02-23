<template>
  <div class="login-container">
    <!-- 背景图书元素 -->
    <div class="background-elements">
      <div class="book-textures"></div>
    </div>
    
    <div class="login-box">
      <h1 class="login-title">图书馆管理系统</h1>
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-width="0"
        size="large"
      >
        <!-- 登录类型选择 -->
        <el-form-item>
          <div class="login-type-selector">
            <el-radio-group v-model="loginForm.loginType" size="large">
              <el-radio-button label="admin">管理员登录</el-radio-button>
              <el-radio-button label="reader">读者登录</el-radio-button>
            </el-radio-group>
          </div>
        </el-form-item>
        
        <el-form-item prop="account">
          <el-input
            v-model="loginForm.account"
            placeholder="请输入账号"
            prefix-icon="User"
            clearable
            class="custom-input"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
            class="custom-input"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            class="custom-login-btn"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>
      <div class="default-account">
        本项目由Suwang-xuan提供
      </div>
      <div class="register-link">
        还不是本馆读者？<a href="javascript:void(0)" @click="handleRegister">点击注册，快速开通借阅权限</a>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  position: relative;
  overflow: hidden;
}

/* 背景图书元素 - 优化为低透明度淡蓝色图书轮廓纹理 */
.background-elements {
  position: absolute;
  width: 100%;
  height: 100%;
  opacity: 0.08;
  pointer-events: none;
}

.book-textures {
  position: absolute;
  width: 100%;
  height: 100%;
  background-image: 
    /* 书架线条 */
    repeating-linear-gradient(to bottom, transparent 0, transparent 60px, #4080FF 60px, #4080FF 61px),
    /* 书本轮廓 - 左侧 */
    linear-gradient(to right, transparent 20%, #4080FF 20%, #4080FF 21%, transparent 21%),
    linear-gradient(to right, transparent 23%, #4080FF 23%, #4080FF 24%, transparent 24%),
    linear-gradient(to right, transparent 26%, #4080FF 26%, #4080FF 27%, transparent 27%),
    /* 书本轮廓 - 右侧 */
    linear-gradient(to right, transparent 75%, #4080FF 75%, #4080FF 76%, transparent 76%),
    linear-gradient(to right, transparent 78%, #4080FF 78%, #4080FF 79%, transparent 79%),
    linear-gradient(to right, transparent 81%, #4080FF 81%, #4080FF 82%, transparent 82%),
    /* 书本轮廓 - 顶部 */
    linear-gradient(to bottom, transparent 30%, #4080FF 30%, #4080FF 31%, transparent 31%),
    linear-gradient(to bottom, transparent 34%, #4080FF 34%, #4080FF 35%, transparent 35%),
    linear-gradient(to bottom, transparent 38%, #4080FF 38%, #4080FF 39%, transparent 39%),
    /* 书本轮廓 - 底部 */
    linear-gradient(to bottom, transparent 65%, #4080FF 65%, #4080FF 66%, transparent 66%),
    linear-gradient(to bottom, transparent 69%, #4080FF 69%, #4080FF 70%, transparent 70%),
    linear-gradient(to bottom, transparent 73%, #4080FF 73%, #4080FF 74%, transparent 74%);
}

/* 登录卡片样式 - 升级 */
.login-box {
  width: 400px;
  background: white;
  padding: 45px;
  border-radius: 10px;
  box-shadow: 0 8px 24px rgba(64, 128, 255, 0.12);
  position: relative;
  z-index: 10;
  transition: all 0.3s ease;
}

/* 登录标题样式 - 优化 */
.login-title {
  text-align: center;
  margin-bottom: 30px;
  font-size: 18px;
  font-weight: bold;
  font-family: 'Microsoft YaHei', sans-serif;
  background: linear-gradient(135deg, #4080FF 0%, #86A8FF 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 自定义输入框样式 - 优化 */
.custom-input {
  --el-input-border-color: #e5e5e5;
  margin-bottom: 16px;
}

.custom-input :deep(.el-input__wrapper) {
  border-radius: 4px;
  padding: 10px 12px;
  transition: all 0.3s ease;
  border: 1px solid #e5e5e5;
}

.custom-input :deep(.el-input__wrapper):focus-within {
  box-shadow: none;
  border-color: #4080FF;
  border-width: 2px;
  background: white;
}

/* 优化密码框显示/隐藏图标 */
.custom-input :deep(.el-input__suffix-inner .el-icon-view) {
  color: #999;
  transition: color 0.3s ease;
}

.custom-input :deep(.el-input__suffix-inner .el-icon-view):hover {
  color: #4080FF;
}

/* 登录类型选择器样式 */
.login-type-selector {
  margin-bottom: 20px;
  text-align: center;
}

.login-type-selector .el-radio-group {
  display: inline-flex;
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.login-type-selector .el-radio-button__inner {
  border: none;
  border-radius: 0;
  background: #f5f7fa;
  color: #606266;
  transition: all 0.3s ease;
  padding: 8px 24px;
}

.login-type-selector .el-radio-button__orig-radio:checked + .el-radio-button__inner {
  background: linear-gradient(135deg, #4080FF 0%, #69b1ff 100%);
  color: white;
}

.login-type-selector .el-radio-button__inner:hover {
  background: #e4e7ed;
  color: #4080FF;
}

/* 自定义登录按钮 - 优化 */
.custom-login-btn {
  width: 100%;
  background: linear-gradient(135deg, #4080FF 0%, #69b1ff 100%);
  border: none;
  border-radius: 4px;
  font-weight: bold;
  font-size: 14px;
  transition: all 0.3s ease;
  padding: 12px;
}

.custom-login-btn:hover {
  filter: brightness(1.05);
  transform: scale(1.03);
  box-shadow: 0 12px 32px rgba(64, 128, 255, 0.2);
  background: linear-gradient(135deg, #4080FF 0%, #69b1ff 100%);
}

.custom-login-btn:active {
  transform: scale(0.98);
}

/* 默认账号密码样式 - 优化 */
.default-account {
  text-align: center;
  margin-top: 16px;
  color: #999;
  font-size: 12px;
  font-family: 'Microsoft YaHei', sans-serif;
}

/* 注册链接样式 */
.register-link {
  text-align: center;
  margin-top: 12px;
  color: #606266;
  font-size: 13px;
  font-family: 'Microsoft YaHei', sans-serif;
}

.register-link a {
  color: #4080FF;
  text-decoration: none;
}

.register-link a:hover {
  text-decoration: underline;
}
</style>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api'

const router = useRouter()
const route = useRoute()

const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  account: '',
  password: '',
  loginType: 'admin' // 默认管理员登录
})

const loginRules = {
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    await loginFormRef.value.validate()
    
    loading.value = true
    console.log('登录请求参数:', loginForm)
    
    // 构建登录请求参数
    const loginParams = {
      account: loginForm.account,
      password: loginForm.password,
      loginType: loginForm.loginType
    }
    
    // 调用后端登录API
    const res = await authApi.login(loginParams)
    
    console.log('登录响应:', res)
    console.log('响应类型:', typeof res)
    console.log('响应结构:', Object.keys(res))
    
    if (res && res.code === 200) {
      console.log('登录成功，开始跳转...')
      if (loginForm.loginType === 'admin') {
        // 管理员登录成功
        sessionStorage.setItem('adminInfo', JSON.stringify(res.data))
        ElMessage.success('管理员登录成功')
        const redirect = route.query.redirect || '/dashboard'
        console.log('管理员跳转到:', redirect)
        await router.push(redirect)
        console.log('管理员跳转完成')
      } else {
        // 读者登录成功
        console.log('读者登录成功，返回数据:', res.data)
        console.log('读者数据结构:', Object.keys(res.data))
        sessionStorage.setItem('readerInfo', JSON.stringify(res.data))
        ElMessage.success('读者登录成功')
        // 读者登录总是跳转到读者首页，不受redirect影响
        console.log('读者跳转到:/reader/dashboard')
        await router.push('/reader/dashboard')
        console.log('读者跳转完成')
      }
    } else {
      // 登录失败
      console.error('登录失败，code:', res?.code, 'message:', res?.message)
      throw new Error(res.message || '登录失败')
    }
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}

const handleRegister = () => {
  router.push('/register')
}
</script>