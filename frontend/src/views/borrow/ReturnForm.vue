<template>
  <div class="return-form">
    <div class="page-header">
      <h2 class="page-title">图书归还查询</h2>
    </div>

    <div class="form-container">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        size="default"
        style="max-width: 600px;"
      >
        <el-form-item label="借阅记录ID" prop="recordId">
          <el-input-number v-model="formData.recordId" :min="1" style="width: 100%;" />
          <div class="form-tip">请输入借阅记录ID号进行查询</div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">
            {{ loading ? '查询中...' : '查 询' }}
          </el-button>
          <el-button @click="handleBack">返 回</el-button>
        </el-form-item>
      </el-form>

      <el-card v-if="record" style="margin-top: 20px; max-width: 600px;" class="record-card">
        <template #header>
          <div class="card-header">
            <span>借阅记录详情</span>
            <el-tag :type="getStatusType(record.status)" size="small">
              {{ getStatusText(record.status) }}
            </el-tag>
          </div>
        </template>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="图书编号">{{ record.bookNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="图书名称">
            <span class="book-name">{{ record.bookName || '未知' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="读者学号/证件">{{ record.readerNo || '校外读者' }}</el-descriptions-item>
          <el-descriptions-item label="读者姓名">{{ record.readerName || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="借阅时间">{{ formatTime(record.borrowTime) }}</el-descriptions-item>
          <el-descriptions-item label="应还时间">{{ formatTime(record.dueTime) }}</el-descriptions-item>
          <el-descriptions-item label="实际归还时间">
            <span v-if="record.returnTime">{{ formatTime(record.returnTime) }}</span>
            <span v-else class="text-warning">未归还</span>
          </el-descriptions-item>
          <el-descriptions-item label="超期天数">
            <span v-if="record.overdueDays && record.overdueDays > 0" class="text-danger">
              {{ record.overdueDays }} 天
            </span>
            <span v-else class="text-success">-</span>
          </el-descriptions-item>
          <el-descriptions-item label="超期费用">
            <span v-if="record.overdueFee && record.overdueFee > 0" class="text-danger">
              ¥{{ record.overdueFee }}
            </span>
            <span v-else class="text-success">-</span>
          </el-descriptions-item>
          <el-descriptions-item label="操作员">{{ record.adminName || '系统' }}</el-descriptions-item>
        </el-descriptions>

        <div style="margin-top: 20px; text-align: center;" v-if="record.status === 1">
          <el-button type="success" size="large" @click="handleReturn" :loading="returning">
            <el-icon><Check /></el-icon>
            {{ returning ? '归还中...' : '确认归还图书' }}
          </el-button>
        </div>
        <div style="margin-top: 20px; text-align: center;" v-else-if="record.status === 2">
          <el-result
            icon="success"
            title="已归还"
            :sub-title="`归还时间：${formatTime(record.returnTime)}`"
          >
          </el-result>
        </div>
      </el-card>

      <el-empty v-else-if="searched" description="未找到该借阅记录" style="margin-top: 40px;" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import { borrowApi } from '@/api'

const router = useRouter()
const route = useRoute()

const formRef = ref(null)
const loading = ref(false)
const returning = ref(false)
const searched = ref(false)
const record = ref(null)

const formData = reactive({
  recordId: null
})

const formRules = {
  recordId: [
    { required: true, message: '请输入借阅记录ID', trigger: 'blur' }
  ]
}

const getStatusType = (status) => {
  const map = { 1: 'primary', 2: 'success', 3: 'danger' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 1: '借阅中', 2: '已归还', 3: '超期未还' }
  return map[status] || '未知'
}

const formatTime = (time) => {
  if (!time) return '-'
  if (typeof time === 'string') {
    return time.replace('T', ' ').substring(0, 19)
  }
  return time.toString().replace('T', ' ').substring(0, 19)
}

const loadRecord = async () => {
  if (!route.params.id) return

  formData.recordId = parseInt(route.params.id)
  await queryRecord()
}

const queryRecord = async () => {
  if (!formData.recordId) return

  loading.value = true
  searched.value = true
  try {
    const res = await borrowApi.getById(formData.recordId)
    record.value = res.data
  } catch (error) {
    ElMessage.error(error.message || '查询失败')
    record.value = null
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return
    await queryRecord()
  })
}

const handleReturn = async () => {
  if (!record.value) return

  returning.value = true
  try {
    await borrowApi.return({ recordId: record.value.recordId })
    ElMessage.success('归还成功')
    await queryRecord()
  } catch (error) {
    ElMessage.error(error.message || '归还失败')
  } finally {
    returning.value = false
  }
}

const handleBack = () => {
  router.push('/borrow')
}

onMounted(() => {
  loadRecord()
})
</script>

<style lang="scss" scoped>
.return-form {
  .page-header {
    margin-bottom: 24px;
    
    .page-title {
      font-size: 22px;
      font-weight: 600;
      color: #303133;
      margin: 0;
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

  .el-form {
    .el-form-item {
      margin-bottom: 20px;
      
      .el-form-item__label {
        font-weight: 500;
        color: #303133;
      }
      
      .form-tip {
        margin-top: 8px;
        font-size: 12px;
        color: #909399;
      }
    }
  }

  .record-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .book-name {
      font-weight: 600;
      color: #303133;
    }
  }

  .text-danger {
    color: #F56C6C;
    font-weight: 600;
  }

  .text-success {
    color: #67C23A;
  }

  .text-warning {
    color: #E6A23C;
  }

  .el-button {
    padding: 12px 24px;
    font-size: 15px;
    border-radius: 6px;
    
    &.el-button--primary {
      background-color: #409EFF;
      border-color: #409EFF;
    }
    
    &.el-button--success {
      background-color: #67C23A;
      border-color: #67C23A;
    }
  }

  .el-descriptions {
    .el-descriptions__table {
      .el-descriptions__label {
        width: 140px;
        font-weight: 500;
        color: #606266;
        background-color: #fafafa;
      }
      
      .el-descriptions__content {
        color: #303133;
      }
    }
  }

  .el-result {
    padding: 20px 0;
  }
}
</style>
