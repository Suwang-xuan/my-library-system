<template>
  <div class="reader-profile">
    <div class="page-header">
      <h2 class="page-title">个人设置</h2>
    </div>

    <div class="profile-content">
      <el-card class="profile-card">
        <template #header>
          <div class="card-header">
            <span>个人信息</span>
          </div>
        </template>
        
        <el-form
          ref="profileFormRef"
          :model="profileForm"
          :rules="profileRules"
          label-width="100px"
          size="large"
          class="profile-form"
        >
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="身份标识" prop="readerNo">
                <el-input
                  v-model="profileForm.readerNo"
                  readonly
                  placeholder="请输入身份标识"
                />
                <div class="form-hint">身份标识不可修改</div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="姓名" prop="readerName">
                <el-input
                  v-model="profileForm.readerName"
                  placeholder="请输入姓名"
                />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="读者类型" prop="readerType">
                <el-select
                  v-model="profileForm.readerType"
                  placeholder="请选择读者类型"
                  disabled
                >
                  <el-option
                    v-for="type in readerTypes"
                    :key="type.value"
                    :label="type.label"
                    :value="type.value"
                  />
                </el-select>
                <div class="form-hint">读者类型不可修改</div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="性别" prop="gender">
                <el-select
                  v-model="profileForm.gender"
                  placeholder="请选择性别"
                >
                  <el-option label="男" :value="1" />
                  <el-option label="女" :value="2" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="手机号" prop="phone">
                <el-input
                  v-model="profileForm.phone"
                  placeholder="请输入手机号"
                  maxlength="11"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="电子邮箱" prop="email">
                <el-input
                  v-model="profileForm.email"
                  placeholder="请输入电子邮箱"
                />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="所属院系" prop="department">
                <el-input
                  v-model="profileForm.department"
                  placeholder="请输入所属院系/单位"
                />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-form-item class="form-actions">
            <el-button type="primary" :loading="loading" @click="handleUpdate">
              {{ loading ? '保存中...' : '保存修改' }}
            </el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
      
      <el-card class="password-card" style="margin-top: 20px;">
        <template #header>
          <div class="card-header">
            <span>修改密码</span>
          </div>
        </template>
        
        <el-form
          ref="passwordFormRef"
          :model="passwordForm"
          :rules="passwordRules"
          label-width="100px"
          size="large"
          class="password-form"
        >
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="原密码" prop="oldPassword">
                <el-input
                  v-model="passwordForm.oldPassword"
                  type="password"
                  placeholder="请输入原密码"
                  show-password
                />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="新密码" prop="newPassword">
                <el-input
                  v-model="passwordForm.newPassword"
                  type="password"
                  placeholder="请设置新密码"
                  show-password
                />
                <div class="form-hint">密码需6-16位，包含字母和数字</div>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  placeholder="请确认新密码"
                  show-password
                />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-form-item class="form-actions">
            <el-button type="primary" :loading="passwordLoading" @click="handleChangePassword">
              {{ passwordLoading ? '修改中...' : '修改密码' }}
            </el-button>
            <el-button @click="handleResetPassword">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { readerApi } from '@/api'

const profileFormRef = ref(null)
const passwordFormRef = ref(null)
const loading = ref(false)
const passwordLoading = ref(false)

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

const profileForm = reactive({
  readerId: null,
  readerNo: '',
  readerName: '',
  readerType: null,
  gender: null,
  phone: '',
  email: '',
  department: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validatePhone = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入手机号'))
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号格式'))
  } else {
    callback()
  }
}

const validateEmail = (rule, value, callback) => {
  if (value && !/^\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/.test(value)) {
    callback(new Error('请输入正确的邮箱格式'))
  } else {
    callback()
  }
}

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
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const profileRules = {
  readerNo: [
    { required: true, message: '请输入身份标识', trigger: 'blur' }
  ],
  readerName: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  phone: [
    { required: true, validator: validatePhone, trigger: 'blur' }
  ],
  email: [
    { validator: validateEmail, trigger: 'blur' }
  ]
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const loadProfileInfo = async () => {
  try {
    // 从sessionStorage获取读者信息
    const readerInfoStr = sessionStorage.getItem('readerInfo')
    if (readerInfoStr) {
      const readerInfo = JSON.parse(readerInfoStr)
      if (readerInfo && readerInfo.readerId) {
        // 使用存储的读者ID获取详细信息
        const res = await readerApi.getById(readerInfo.readerId)
        if (res.data) {
          Object.assign(profileForm, res.data)
        } else {
          // 如果API返回为空，使用存储的信息
          Object.assign(profileForm, readerInfo)
        }
      }
    }
  } catch (error) {
    console.error('加载个人信息失败:', error)
    ElMessage.error('加载个人信息失败')
  }
}

const handleUpdate = async () => {
  if (!profileFormRef.value) return

  try {
    await profileFormRef.value.validate()
    
    loading.value = true
    
    // 调用更新API
    const res = await readerApi.update(profileForm)
    
    if (res.code === 200) {
      ElMessage.success('个人信息更新成功')
      
      // 更新sessionStorage中的读者信息
      sessionStorage.setItem('readerInfo', JSON.stringify(profileForm))
    } else {
      throw new Error(res.message || '更新失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '更新失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  loadProfileInfo()
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return

  try {
    await passwordFormRef.value.validate()
    
    passwordLoading.value = true
    
    // 调用修改密码API
    const res = await readerApi.changePassword({
      readerId: profileForm.readerId,
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    
    if (res.code === 200) {
      ElMessage.success('密码修改成功')
      handleResetPassword()
    } else {
      throw new Error(res.message || '密码修改失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '密码修改失败')
  } finally {
    passwordLoading.value = false
  }
}

const handleResetPassword = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}

onMounted(() => {
  loadProfileInfo()
})
</script>

<style lang="scss" scoped>
.reader-profile {
  .page-header {
    margin-bottom: 24px;
    
    .page-title {
      font-size: 22px;
      font-weight: 600;
      color: #303133;
      margin: 0;
    }
  }
  
  .profile-content {
    max-width: 1000px;
  }
  
  .profile-card,
  .password-card {
    background: #fff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }
  
  .card-header {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }
  
  .profile-form,
  .password-form {
    margin-top: 20px;
  }
  
  .form-hint {
    font-size: 12px;
    color: #909399;
    margin-top: 5px;
  }
  
  .form-actions {
    text-align: center;
    margin-top: 20px;
  }
  
  .el-button {
    margin: 0 10px;
  }
}
</style>