<template>
  <div class="borrow-rule">
    <div class="page-header">
      <h2 class="page-title">借阅规则管理</h2>
    </div>

    <div class="rule-container">
      <el-card v-for="rule in rules" :key="rule.ruleId" class="rule-card">
        <template #header>
          <div class="card-header">
            <span class="rule-title">{{ getReaderTypeName(rule.readerType) }}借阅规则</span>
            <el-button 
              type="primary" 
              size="small" 
              @click="handleEditRule(rule)"
              :loading="editingRuleId === rule.ruleId"
            >
              编辑
            </el-button>
          </div>
        </template>

        <el-form :model="rule" label-width="120px" size="small">
          <el-form-item label="读者类型">
            <el-tag :type="getReaderTypeTag(rule.readerType)">
              {{ getReaderTypeName(rule.readerType) }}
            </el-tag>
          </el-form-item>
          <el-form-item label="最大可借数量">
            <span class="rule-value">{{ rule.maxBorrowNum }}本</span>
          </el-form-item>
          <el-form-item label="借阅期限">
            <span class="rule-value">{{ rule.borrowDays }}天</span>
          </el-form-item>
          <el-form-item label="最大续借次数">
            <span class="rule-value">{{ rule.maxRenewTimes }}次</span>
          </el-form-item>
          <el-form-item label="每次续借天数">
            <span class="rule-value">{{ rule.renewDays }}天</span>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 编辑规则对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="编辑借阅规则"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="ruleFormRef"
        :model="editForm"
        :rules="editFormRules"
        label-width="120px"
      >
        <el-form-item label="读者类型" prop="readerType">
          <el-select v-model="editForm.readerType" disabled>
            <el-option
              v-for="type in readerTypes"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="最大可借数量" prop="maxBorrowNum">
          <el-input-number
            v-model="editForm.maxBorrowNum"
            :min="1"
            :max="20"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="借阅期限" prop="borrowDays">
          <el-input-number
            v-model="editForm.borrowDays"
            :min="7"
            :max="180"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="最大续借次数" prop="maxRenewTimes">
          <el-input-number
            v-model="editForm.maxRenewTimes"
            :min="0"
            :max="5"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="每次续借天数" prop="renewDays">
          <el-input-number
            v-model="editForm.renewDays"
            :min="7"
            :max="60"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveRule" :loading="saving">
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { borrowRuleApi } from '@/api'

const rules = ref([])
const dialogVisible = ref(false)
const editingRuleId = ref(null)
const saving = ref(false)
const ruleFormRef = ref(null)

const readerTypes = [
  { value: 1, label: '本科生', tag: '' },
  { value: 2, label: '研究生', tag: 'success' },
  { value: 3, label: '教师', tag: 'primary' },
  { value: 4, label: '教职工', tag: 'warning' },
  { value: 5, label: '校友', tag: 'info' },
  { value: 6, label: '校外读者', tag: 'danger' },
  { value: 7, label: '访问学者', tag: '' },
  { value: 8, label: '退休教师', tag: 'warning' }
]

const editForm = reactive({
  ruleId: null,
  readerType: '',
  maxBorrowNum: 5,
  borrowDays: 30,
  maxRenewTimes: 2,
  renewDays: 15
})

const editFormRules = {
  maxBorrowNum: [
    { required: true, message: '请输入最大可借数量', trigger: 'blur' }
  ],
  borrowDays: [
    { required: true, message: '请输入借阅期限', trigger: 'blur' }
  ],
  maxRenewTimes: [
    { required: true, message: '请输入最大续借次数', trigger: 'blur' }
  ],
  renewDays: [
    { required: true, message: '请输入每次续借天数', trigger: 'blur' }
  ]
}

const getReaderTypeName = (type) => {
  const readerType = readerTypes.find(item => item.value === type)
  return readerType ? readerType.label : '未知'
}

const getReaderTypeTag = (type) => {
  const readerType = readerTypes.find(item => item.value === type)
  return readerType ? readerType.tag : ''
}

const loadRules = async () => {
  try {
    const res = await borrowRuleApi.getList()
    rules.value = res.data
  } catch (error) {
    ElMessage.error('加载借阅规则失败')
  }
}

const handleEditRule = (rule) => {
  Object.assign(editForm, rule)
  dialogVisible.value = true
  editingRuleId.value = rule.ruleId
}

const handleSaveRule = async () => {
  if (!ruleFormRef.value) return
  
  await ruleFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    saving.value = true
    try {
      await borrowRuleApi.update(editForm)
      ElMessage.success('借阅规则更新成功')
      dialogVisible.value = false
      loadRules()
    } catch (error) {
      ElMessage.error(error.message || '更新借阅规则失败')
    } finally {
      saving.value = false
      editingRuleId.value = null
    }
  })
}

onMounted(() => {
  loadRules()
})
</script>

<style lang="scss" scoped>
.borrow-rule {
  .page-header {
    margin-bottom: 24px;
    
    .page-title {
      font-size: 22px;
      font-weight: 600;
      color: #303133;
      margin: 0;
    }
  }
  
  .rule-container {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
    gap: 20px;
    margin-bottom: 24px;
  }
  
  .rule-card {
    transition: all 0.3s ease;
    
    &:hover {
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
    }
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .rule-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
    
    .rule-value {
      font-size: 16px;
      font-weight: 600;
      color: #409EFF;
    }
  }
}
</style>
