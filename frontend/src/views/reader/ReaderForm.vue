<template>
  <div class="reader-form" :class="{ 'edit-mode': isEdit }">
    <div class="page-header">
      <h2 class="page-title">{{ isEdit ? '编辑读者信息' : '注册新读者' }}</h2>
    </div>

    <el-row :gutter="20" v-if="isEdit && formData.readerId">
      <el-col :span="24">
        <el-card class="reader-preview-card">
          <div class="reader-preview">
            <el-avatar :size="80" class="reader-avatar">
              {{ formData.readerName?.charAt(0) }}
            </el-avatar>
            <div class="reader-info-summary">
              <h3 class="reader-name">{{ formData.readerName || '未命名' }}</h3>
              <p class="reader-meta">
                <el-tag :type="getReaderTypeTag(formData.readerType)" size="small">
                  {{ getReaderTypeName(formData.readerType) }}
                </el-tag>
                <el-tag type="info" size="small" v-if="formData.readerNo">
                  {{ formData.readerNo }}
                </el-tag>
                <el-tag :type="formData.status === 1 ? 'success' : 'danger'" size="small">
                  {{ formData.status === 1 ? '正常' : '黑名单' }}
                </el-tag>
              </p>
              <div class="reader-stats">
                <div class="stat-item">
                  <span class="stat-value">{{ formData.borrowedCount || 0 }}</span>
                  <span class="stat-label">当前借阅</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value">{{ formData.maxBorrowNum || 5 }}</span>
                  <span class="stat-label">可借数量</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value">{{ formData.totalBorrowed || 0 }}</span>
                  <span class="stat-label">累计借阅</span>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div class="form-container">
      <el-tabs v-model="activeTab" v-if="isEdit">
        <el-tab-pane label="基本信息" name="basic">
          <el-form
            ref="formRef"
            :model="formData"
            :rules="formRules"
            label-width="100px"
            size="default"
            style="max-width: 600px;"
          >
            <el-form-item label="学号/工号" prop="readerNo">
              <el-input v-model="formData.readerNo" placeholder="请输入学号/工号" :disabled="isEdit" />
            </el-form-item>
            <el-form-item label="读者姓名" prop="readerName">
              <el-input v-model="formData.readerName" placeholder="请输入读者姓名" />
            </el-form-item>
            <el-form-item label="读者类型" prop="readerType">
              <el-radio-group v-model="formData.readerType">
                <el-radio :label="1">本科生</el-radio>
                <el-radio :label="2">研究生</el-radio>
                <el-radio :label="3">教师</el-radio>
                <el-radio :label="4">教职工</el-radio>
                <el-radio :label="5">校友</el-radio>
                <el-radio :label="6">校外读者</el-radio>
                <el-radio :label="7">访问学者</el-radio>
                <el-radio :label="8">退休教师</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="formData.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="formData.phone" placeholder="请输入联系电话" />
            </el-form-item>
            <el-form-item label="电子邮箱" prop="email">
              <el-input v-model="formData.email" placeholder="请输入电子邮箱" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSubmit" :loading="submitting">
                {{ submitting ? '保存中...' : '保存修改' }}
              </el-button>
              <el-button @click="handleBack">返 回</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="借阅设置" name="borrow">
          <el-form
            label-width="120px"
            size="default"
            style="max-width: 600px;"
          >
            <el-form-item label="可借数量">
              <el-input-number v-model="formData.maxBorrowNum" :min="1" :max="20" />
              <span class="form-tip">允许同时借阅的图书数量</span>
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio :label="1">正常</el-radio>
                <el-radio :label="0">黑名单</el-radio>
              </el-radio-group>
              <div class="form-tip" style="margin-top: 8px;">黑名单读者无法借阅图书</div>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUpdateSettings" :loading="updatingSettings">
                {{ updatingSettings ? '保存中...' : '保存设置' }}
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <el-form
        v-if="!isEdit"
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        size="default"
        style="max-width: 600px;"
      >
        <el-form-item label="学号/工号" prop="readerNo">
          <el-input v-model="formData.readerNo" placeholder="校内读者请输入学号/工号，校外读者可不填" />
        </el-form-item>
        <el-form-item label="读者姓名" prop="readerName">
          <el-input v-model="formData.readerName" placeholder="请输入读者姓名" />
        </el-form-item>
        <el-form-item label="读者类型" prop="readerType">
          <el-radio-group v-model="formData.readerType">
            <el-radio :label="1">本科生</el-radio>
            <el-radio :label="2">研究生</el-radio>
            <el-radio :label="3">教师</el-radio>
            <el-radio :label="4">教职工</el-radio>
            <el-radio :label="5">校友</el-radio>
            <el-radio :label="6">校外读者</el-radio>
            <el-radio :label="7">访问学者</el-radio>
            <el-radio :label="8">退休教师</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="formData.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="电子邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入电子邮箱" />
        </el-form-item>
        <el-form-item label="可借数量" prop="maxBorrowNum">
          <el-input-number v-model="formData.maxBorrowNum" :min="1" :max="20" />
          <span class="form-tip">默认可借阅数量（校外读者默认5本）</span>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ submitting ? '保存中...' : '注 册' }}
          </el-button>
          <el-button @click="handleBack">返 回</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { readerApi } from '@/api'

const router = useRouter()
const route = useRoute()

const formRef = ref(null)
const submitting = ref(false)
const updatingSettings = ref(false)
const activeTab = ref('basic')

const isEdit = computed(() => !!route.params.id)

const readerTypeMap = {
  1: { name: '本科生', tag: '' },
  2: { name: '研究生', tag: 'success' },
  3: { name: '教师', tag: 'primary' },
  4: { name: '教职工', tag: 'warning' },
  5: { name: '校友', tag: 'info' },
  6: { name: '校外读者', tag: 'danger' },
  7: { name: '访问学者', tag: '' },
  8: { name: '退休教师', tag: 'warning' }
}

const getReaderTypeName = (type) => {
  return readerTypeMap[type]?.name || '未知'
}

const getReaderTypeTag = (type) => {
  return readerTypeMap[type]?.tag || ''
}

const formData = reactive({
  readerId: null,
  readerNo: '',
  readerName: '',
  readerType: 1,
  gender: 1,
  phone: '',
  email: '',
  maxBorrowNum: 5,
  status: 1,
  borrowedCount: 0,
  totalBorrowed: 0
})

const validateReaderNo = (rule, value, callback) => {
  if (formData.readerType === 6) {
    callback()
  } else if (!value || value.trim() === '') {
    callback(new Error('请输入学号/工号'))
  } else {
    callback()
  }
}

const formRules = {
  readerNo: [
    { validator: validateReaderNo, trigger: 'blur' }
  ],
  readerName: [
    { required: true, message: '请输入读者姓名', trigger: 'blur' }
  ]
}

watch(() => formData.readerType, (newType) => {
  if (newType === 6) {
    formData.maxBorrowNum = 5
  } else if (formData.maxBorrowNum === 5 && !isEdit.value) {
    formData.maxBorrowNum = 10
  }
})

const loadReaderData = async () => {
  if (!isEdit.value) {
    formData.maxBorrowNum = 10
    return
  }

  try {
    const res = await readerApi.getById(route.params.id)
    Object.assign(formData, res.data)
    activeTab.value = 'basic'
  } catch (error) {
    ElMessage.error('加载读者信息失败')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      if (isEdit.value) {
        await readerApi.update(formData)
        ElMessage.success('更新成功')
      } else {
        await readerApi.add(formData)
        ElMessage.success('注册成功')
      }
      router.push('/reader')
    } catch (error) {
      ElMessage.error(error.message || '操作失败')
    } finally {
      submitting.value = false
    }
  })
}

const handleUpdateSettings = async () => {
  updatingSettings.value = true
  try {
    await readerApi.update({
      readerId: formData.readerId,
      maxBorrowNum: formData.maxBorrowNum,
      status: formData.status
    })
    ElMessage.success('设置更新成功')
  } catch (error) {
    ElMessage.error(error.message || '设置更新失败')
  } finally {
    updatingSettings.value = false
  }
}

const handleBack = () => {
  router.push('/reader')
}

onMounted(() => {
  loadReaderData()
})
</script>

<style lang="scss" scoped>
.reader-form {
  .page-header {
    margin-bottom: 24px;
    
    .page-title {
      font-size: 22px;
      font-weight: 600;
      color: #303133;
      margin: 0;
    }
  }

  .reader-preview-card {
    margin-bottom: 24px;
    border-radius: 8px;
    
    .reader-preview {
      display: flex;
      align-items: center;
      gap: 20px;
      
      .reader-avatar {
        background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
        color: #fff;
        font-size: 32px;
        font-weight: 600;
      }
      
      .reader-info-summary {
        flex: 1;
        
        .reader-name {
          font-size: 20px;
          font-weight: 600;
          color: #303133;
          margin: 0 0 12px 0;
        }
        
        .reader-meta {
          display: flex;
          gap: 8px;
          flex-wrap: wrap;
          margin-bottom: 16px;
        }
        
        .reader-stats {
          display: flex;
          gap: 32px;
          
          .stat-item {
            text-align: center;
            
            .stat-value {
              display: block;
              font-size: 24px;
              font-weight: 600;
              color: #409EFF;
            }
            
            .stat-label {
              font-size: 13px;
              color: #909399;
            }
          }
        }
      }
    }
  }

  .form-container {
    background-color: #fff;
    border-radius: 8px;
    padding: 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    transition: all 0.3s ease;
    max-width: 800px;
    
    &:hover {
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
    }
  }

  .el-tabs {
    :deep(.el-tabs__nav-wrap) {
      &:after {
        height: 1px;
        background-color: #EBEEF5;
      }
    }
    
    :deep(.el-tabs__item) {
      font-size: 15px;
      padding: 0 24px;
      
      &.is-active {
        color: #409EFF;
        font-weight: 600;
      }
    }
    
    :deep(.el-tabs__active-bar) {
      background-color: #409EFF;
    }
  }

  .el-form {
    max-width: 100%;
    
    .el-form-item {
      margin-bottom: 20px;
      
      .el-form-item__label {
        font-weight: 500;
        color: #303133;
      }
      
      .el-input,
      .el-radio-group,
      .el-input-number {
        width: 100%;
        max-width: 400px;
        
        .el-input__inner {
          border-radius: 6px;
          transition: all 0.3s ease;
          
          &:focus {
            border-color: #409EFF;
            box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
          }
        }
      }
      
      .form-tip {
        margin-left: 12px;
        font-size: 13px;
        color: #909399;
      }
    }
    
    .el-form-item--feedback {
      .el-form-item__error {
        margin-top: 4px;
        font-size: 12px;
        color: #F56C6C;
      }
    }
  }

  .el-button {
    padding: 10px 20px;
    font-size: 14px;
    font-weight: 500;
    border-radius: 6px;
    transition: all 0.3s ease;
    
    &.el-button--primary {
      background-color: #409EFF;
      border-color: #409EFF;
      
      &:hover {
        background-color: #66b1ff;
        border-color: #66b1ff;
        transform: translateY(-1px);
        box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
      }
    }
    
    &:not(.el-button--primary) {
      &:hover {
        background-color: #f5f7fa;
        transform: translateY(-1px);
      }
    }
  }

  .el-input-number {
    .el-input-number__decrease,
    .el-input-number__increase {
      border-radius: 0 6px 6px 0;
      
      &:hover {
        background-color: #f5f7fa;
      }
    }
    
    .el-input-number__decrease {
      border-radius: 6px 0 0 6px;
    }
  }

  .el-radio-group {
    display: flex;
    gap: 20px;
    margin-top: 8px;
    
    .el-radio {
      font-size: 14px;
      color: #303133;
      
      .el-radio__input {
        .el-radio__inner {
          width: 16px;
          height: 16px;
          
          &:after {
            width: 8px;
            height: 8px;
          }
        }
        
        &.is-checked {
          .el-radio__inner {
            background-color: #409EFF;
            border-color: #409EFF;
            
            &:after {
              transform: translate(-50%, -50%) scale(1);
            }
          }
          
          + .el-radio__label {
            color: #409EFF;
          }
        }
      }
    }
  }
}
</style>
