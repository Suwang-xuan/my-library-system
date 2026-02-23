<template>
  <div class="register-container">
    <div class="background-elements">
      <div class="book-textures"></div>
    </div>
    
    <div class="register-box">
      <h1 class="register-title">读者注册</h1>
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-width="100px"
        size="large"
      >
        <el-form-item label="身份标识" prop="readerNo">
          <el-input
            v-model="registerForm.readerNo"
            placeholder="请输入学号/工号/身份证号"
            prefix-icon="User"
            clearable
          />
          <div class="form-hint">{{ identityHint }}</div>
        </el-form-item>
        
        <el-form-item label="姓名" prop="readerName">
          <el-input
            v-model="registerForm.readerName"
            placeholder="请输入真实姓名"
            prefix-icon="User"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="读者类型" prop="readerType">
          <el-select
            v-model="registerForm.readerType"
            placeholder="请选择读者类型"
            clearable
          >
            <el-option
              v-for="type in readerTypes"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="性别" prop="gender">
          <el-select
            v-model="registerForm.gender"
            placeholder="请选择性别"
            clearable
          >
            <el-option label="男" :value="1" />
            <el-option label="女" :value="2" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="请输入手机号"
            prefix-icon="Phone"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="电子邮箱" prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入电子邮箱（选填）"
            prefix-icon="Message"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="所属院系" prop="department">
          <el-input
            v-model="registerForm.department"
            placeholder="请输入所属院系/单位（选填）"
            prefix-icon="OfficeBuilding"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="登录密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请设置登录密码"
            prefix-icon="Lock"
            show-password
          />
          <div class="form-hint">密码需6-16位，包含字母和数字</div>
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请确认登录密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            class="custom-register-btn"
            @click="handleRegister"
          >
            {{ loading ? '注册中...' : '注 册' }}
          </el-button>
          <el-button
            class="custom-cancel-btn"
            @click="handleBack"
          >
            返 回
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-link">
        已有账号？<a href="javascript:void(0)" @click="handleBack">返回登录</a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { readerApi } from '@/api'

const router = useRouter()

const registerFormRef = ref(null)
const loading = ref(false)

const readerTypes = [
  { value: 1, label: '本科生' },
  { value: 2, label: '研究生' },
  { value: 3, label: '教师' },
  { value: 4, label: '教职工' },
  { value: 5, label: '校友' },
  { value: 6, label: '校外读者' },
  { value: 7, label: '访问学者' },
  { value: 8, label: '退休教师' }
]

const registerForm = reactive({
  readerNo: '',
  readerName: '',
  readerType: null,
  gender: null,
  phone: '',
  email: '',
  department: '',
  password: '',
  confirmPassword: ''
})

// 根据读者类型动态显示身份标识提示
const identityHint = computed(() => {
  if (!registerForm.readerType) {
    return '请先选择读者类型'
  }
  switch (registerForm.readerType) {
    case 1:
    case 2:
      return '本科生/研究生请输入学号'
    case 3:
    case 4:
    case 8:
      return '教师/教职工/退休教师请输入工号'
    case 5:
      return '校友请输入校友编号'
    case 6:
    case 7:
      return '校外读者/访问学者请输入身份证号'
    default:
      return '请输入身份标识'
  }
})

const validatePassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入密码'))
  } else if (value.length < 6 || value.length > 16) {
    callback(new Error('密码长度需6-16位'))
  } else if (!/^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]+$/.test(value)) {
    callback(new Error('密码需包含字母和数字'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请确认密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const validatePhone = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入手机号'))
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号格式'))
  } else {
    callback()
  }
}

const registerRules = {
  readerNo: [
    { required: true, message: '请输入身份标识', trigger: 'blur' }
  ],
  readerName: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  readerType: [
    { required: true, message: '请选择读者类型', trigger: 'change' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  phone: [
    { required: true, validator: validatePhone, trigger: 'blur' }
  ],
  password: [
    { required: true, validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return

  try {
    await registerFormRef.value.validate()
    
    loading.value = true
    
    // 调用读者注册API
    const response = await readerApi.register(registerForm)
    
    console.log('注册响应:', response)
    
    // 保存读者信息到sessionStorage
    let readerInfo = null
    if (response && response.code === 200) {
      // 正确处理CommonResult格式
      if (response.data && response.data.readerId) {
        readerInfo = response.data
      } else {
        // 如果API返回的不是完整读者信息，构造一个基本信息对象
        readerInfo = {
          readerId: Date.now(), // 使用时间戳作为临时ID
          readerName: registerForm.readerName,
          readerType: registerForm.readerType
        }
      }
      sessionStorage.setItem('readerInfo', JSON.stringify(readerInfo))
      
      ElMessage.success(response.message || '注册成功！欢迎加入本馆读者大家庭')
      // 注册成功后直接跳转到读者仪表板
      router.push('/reader/dashboard')
    } else {
      throw new Error(response.message || '注册失败')
    }
  } catch (error) {
    console.error('注册失败:', error)
    ElMessage.error(error.message || '注册失败')
  } finally {
    loading.value = false
  }
}

const handleBack = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  position: relative;
  overflow: hidden;
}

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
    repeating-linear-gradient(to bottom, transparent 0, transparent 60px, #4080FF 60px, #4080FF 61px),
    linear-gradient(to right, transparent 20%, #4080FF 20%, #4080FF 21%, transparent 21%),
    linear-gradient(to right, transparent 23%, #4080FF 23%, #4080FF 24%, transparent 24%),
    linear-gradient(to right, transparent 26%, #4080FF 26%, #4080FF 27%, transparent 27%),
    linear-gradient(to right, transparent 75%, #4080FF 75%, #4080FF 76%, transparent 76%),
    linear-gradient(to right, transparent 78%, #4080FF 78%, #4080FF 79%, transparent 79%),
    linear-gradient(to right, transparent 81%, #4080FF 81%, #4080FF 82%, transparent 82%),
    linear-gradient(to bottom, transparent 30%, #4080FF 30%, #4080FF 31%, transparent 31%),
    linear-gradient(to bottom, transparent 34%, #4080FF 34%, #4080FF 35%, transparent 35%),
    linear-gradient(to bottom, transparent 38%, #4080FF 38%, #4080FF 39%, transparent 39%),
    linear-gradient(to bottom, transparent 65%, #4080FF 65%, #4080FF 66%, transparent 66%),
    linear-gradient(to bottom, transparent 69%, #4080FF 69%, #4080FF 70%, transparent 70%),
    linear-gradient(to bottom, transparent 73%, #4080FF 73%, #4080FF 74%, transparent 74%);
}

.register-box {
  width: 500px;
  background: white;
  padding: 30px;
  border-radius: 10px;
  box-shadow: 0 8px 24px rgba(64, 128, 255, 0.12);
  position: relative;
  z-index: 10;
}

.register-title {
  text-align: center;
  margin-bottom: 20px;
  font-size: 20px;
  font-weight: bold;
  font-family: 'Microsoft YaHei', sans-serif;
  background: linear-gradient(135deg, #4080FF 0%, #86A8FF 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.form-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.custom-register-btn {
  width: 60%;
  background: linear-gradient(135deg, #4080FF 0%, #69b1ff 100%);
  border: none;
  border-radius: 4px;
  font-weight: bold;
  transition: all 0.3s ease;
}

.custom-register-btn:hover {
  filter: brightness(1.05);
  transform: scale(1.02);
  box-shadow: 0 8px 20px rgba(64, 128, 255, 0.2);
  background: linear-gradient(135deg, #4080FF 0%, #69b1ff 100%);
}

.custom-cancel-btn {
  width: 35%;
  margin-left: 5%;
  border-radius: 4px;
  font-weight: bold;
  transition: all 0.3s ease;
}

.custom-cancel-btn:hover {
  background-color: #f5f7fa;
  transform: scale(1.02);
}

.login-link {
  text-align: center;
  margin-top: 20px;
  color: #606266;
  font-size: 14px;
}

.login-link a {
  color: #4080FF;
  text-decoration: none;
}

.login-link a:hover {
  text-decoration: underline;
}
</style>
