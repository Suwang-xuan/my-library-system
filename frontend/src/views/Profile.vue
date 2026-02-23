<template>
  <div class="profile-page">
    <div class="page-header">
      <h2 class="page-title">个人中心</h2>
    </div>

    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="user-card">
          <div class="user-info">
            <div class="avatar-container">
              <el-avatar :size="100" :src="avatarUrl" class="user-avatar">
                {{ userInfo.adminName?.charAt(0) }}
              </el-avatar>
              <div class="avatar-upload-overlay">
                <el-button
                  type="primary"
                  size="small"
                  @click="triggerAvatarUpload"
                  class="upload-btn"
                >
                  <el-icon><Camera /></el-icon>
                  更换头像
                </el-button>
                <input
                  type="file"
                  ref="avatarInput"
                  accept="image/*"
                  style="display: none"
                  @change="handleAvatarChange"
                />
              </div>
            </div>
            <h3>{{ userInfo.adminName }}</h3>
            <p>{{ userInfo.adminAccount }}</p>
            <el-tag :type="userInfo.roleId === 1 ? 'danger' : ''">
              {{ userInfo.roleId === 1 ? '超级管理员' : '普通管理员' }}
            </el-tag>
          </div>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card>
          <el-tabs v-model="activeTab">
            <el-tab-pane label="基本信息" name="info">
              <el-form label-width="100px" size="default" style="max-width: 400px;">
                <el-form-item label="管理员账号">
                  <el-input :value="userInfo.adminAccount" disabled />
                </el-form-item>
                <el-form-item label="管理员姓名">
                  <el-input v-model="formData.adminName" />
                </el-form-item>
                <el-form-item label="联系电话">
                  <el-input v-model="formData.phone" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleUpdateInfo" :loading="updating">
                    {{ updating ? '保存中...' : '保存修改' }}
                  </el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <el-tab-pane label="修改密码" name="password">
              <el-form
                ref="passwordFormRef"
                :model="passwordForm"
                :rules="passwordRules"
                label-width="100px"
                size="default"
                style="max-width: 400px;"
              >
                <el-form-item label="旧密码" prop="oldPassword">
                  <el-input v-model="passwordForm.oldPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input v-model="passwordForm.newPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleChangePassword" :loading="changing">
                    {{ changing ? '修改中...' : '修改密码' }}
                  </el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { authApi, adminApi } from '@/api'

const activeTab = ref('info')
const updating = ref(false)
const changing = ref(false)
const passwordFormRef = ref(null)
const avatarInput = ref(null)
const uploadingAvatar = ref(false)

const userInfo = ref({})
const avatarUrl = ref('')

const formData = reactive({
  adminName: '',
  phone: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const loadUserInfo = async () => {
  try {
    const res = await authApi.getCurrentUser()
    userInfo.value = res.data
    formData.adminName = res.data.adminName || ''
    formData.phone = res.data.phone || ''
    avatarUrl.value = res.data.avatar || ''
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

const triggerAvatarUpload = () => {
  avatarInput.value?.click()
}

const handleAvatarChange = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return

  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('请选择图片文件')
    return
  }

  const maxSize = 2 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('图片大小不能超过2MB')
    return
  }

  uploadingAvatar.value = true
  try {
    const formDataObj = new FormData()
    formDataObj.append('file', file)
    formDataObj.append('adminId', userInfo.value.adminId)

    const res = await adminApi.uploadAvatar(formDataObj)
    avatarUrl.value = res.data.avatarUrl
    userInfo.value.avatar = res.data.avatarUrl
    ElMessage.success('头像上传成功')

    const adminInfo = JSON.parse(sessionStorage.getItem('adminInfo') || '{}')
    adminInfo.avatar = res.data.avatarUrl
    sessionStorage.setItem('adminInfo', JSON.stringify(adminInfo))
  } catch (error) {
    ElMessage.error(error.message || '头像上传失败')
  } finally {
    uploadingAvatar.value = false
    event.target.value = ''
  }
}

const handleUpdateInfo = async () => {
  updating.value = true
  try {
    await adminApi.update({
      adminId: userInfo.value.adminId,
      adminName: formData.adminName,
      phone: formData.phone
    })
    ElMessage.success('信息更新成功')
    loadUserInfo()
  } catch (error) {
    ElMessage.error(error.message || '更新失败')
  } finally {
    updating.value = false
  }
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return

  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return

    changing.value = true
    try {
      await adminApi.changePassword({
        adminId: userInfo.value.adminId,
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      })
      ElMessage.success('密码修改成功')
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
    } catch (error) {
      ElMessage.error(error.message || '密码修改失败')
    } finally {
      changing.value = false
    }
  })
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style lang="scss" scoped>
@import '@/styles/index.scss';

.profile-page {
  .user-card {
    text-align: center;

    .user-info {
      padding: 20px 0;

      .avatar-container {
        position: relative;
        display: inline-block;
        margin: 0 auto;

        .user-avatar {
          border: 3px solid #fff;
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
          cursor: pointer;
          transition: all 0.3s ease;

          &:hover {
            transform: scale(1.05);
            box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
          }
        }

        .avatar-upload-overlay {
          position: absolute;
          bottom: 0;
          left: 50%;
          transform: translateX(-50%);
          background: rgba(0, 0, 0, 0.6);
          color: #fff;
          padding: 8px 16px;
          border-radius: 20px;
          cursor: pointer;
          transition: all 0.3s ease;
          opacity: 0;
          font-size: 12px;

          .upload-btn {
            background: transparent;
            border: none;
            color: #fff;
            padding: 0;
            margin: 0;
            font-size: 12px;

            &:hover {
              background: transparent;
              color: #409EFF;
            }

            .el-icon {
              margin-right: 4px;
            }
          }
        }

        &:hover {
          .avatar-upload-overlay {
            opacity: 1;
            bottom: -5px;
          }
        }
      }

      h3 {
        margin: 16px 0 8px;
        font-size: 20px;
        color: #303133;
        font-weight: 600;
      }

      p {
        color: #909399;
        margin-bottom: 16px;
        font-size: 14px;
      }

      .el-tag {
        margin-top: 8px;
        font-size: 13px;
        font-weight: 500;
      }
    }
  }

  .el-card {
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
    }
  }

  .el-form {
    .el-form-item {
      margin-bottom: 20px;

      .el-form-item__label {
        font-weight: 500;
        color: #303133;
      }

      .el-input {
        .el-input__inner {
          border-radius: 6px;
          transition: all 0.3s ease;

          &:focus {
            border-color: #409EFF;
            box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
          }
        }
      }
    }
  }

  .el-tabs {
    .el-tabs__nav-wrap {
      .el-tabs__nav {
        padding: 0 20px;

        .el-tabs__item {
          font-weight: 500;
          padding: 16px 20px;
          
          &.is-active {
            color: #409EFF;
            font-weight: 600;
          }
        }
      }
    }
  }
}
</style>
