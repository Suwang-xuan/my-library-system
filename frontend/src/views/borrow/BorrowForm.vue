<template>
  <div class="borrow-form">
    <div class="page-header">
      <h2 class="page-title">图书借阅</h2>
    </div>

    <div class="form-container">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        size="default"
        style="max-width: 500px;"
      >
        <el-form-item label="图书" prop="bookId">
          <el-select
            v-model="formData.bookId"
            placeholder="请选择要借阅的图书"
            filterable
            remote
            :remote-method="searchBooks"
            :loading="bookLoading"
            style="width: 100%;"
          >
            <el-option
              v-for="book in bookOptions"
              :key="book.bookId"
              :label="`${book.bookNo} - ${book.bookName}`"
              :value="book.bookId"
              :disabled="book.stock <= 0"
            >
              <span>{{ book.bookNo }} - {{ book.bookName }}</span>
              <span style="float: right; color: #8492a6; font-size: 12px;">
                库存: {{ book.stock }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="读者" prop="readerId">
          <el-select
            v-model="formData.readerId"
            placeholder="请选择借阅读者"
            filterable
            :loading="readerLoading"
            style="width: 100%;"
          >
            <el-option
              v-for="reader in readerOptions"
              :key="reader.readerId"
              :label="`${reader.readerNo} - ${reader.readerName}`"
              :value="reader.readerId"
            />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ submitting ? '借阅中...' : '确 定 借 阅' }}
          </el-button>
          <el-button @click="handleBack">返 回</el-button>
        </el-form-item>
      </el-form>

      <el-card style="margin-top: 20px; max-width: 500px;">
        <template #header>
          <span>借阅说明</span>
        </template>
        <ul style="margin: 0; padding-left: 20px; color: #606266;">
          <li style="margin-bottom: 8px;">学生可借阅图书期限为 30 天，教师为 60 天</li>
          <li style="margin-bottom: 8px;">每位读者同时借阅图书数量有限制</li>
          <li style="margin-bottom: 8px;">超期归还将按每天 0.5 元收取超期费用</li>
          <li style="margin-bottom: 8px;">图书续借最多可续借 2 次，每次延长 15 天</li>
        </ul>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { bookApi, readerApi, borrowApi } from '@/api'

const router = useRouter()

const formRef = ref(null)
const submitting = ref(false)
const bookLoading = ref(false)
const readerLoading = ref(false)

const bookOptions = ref([])
const readerOptions = ref([])

const formData = reactive({
  bookId: null,
  readerId: null
})

const formRules = {
  bookId: [
    { required: true, message: '请选择要借阅的图书', trigger: 'change' }
  ],
  readerId: [
    { required: true, message: '请选择借阅读者', trigger: 'change' }
  ]
}

const searchBooks = async (keyword) => {
  if (!keyword) {
    bookOptions.value = []
    return
  }

  bookLoading.value = true
  try {
    const res = await bookApi.getList({ bookName: keyword, pageSize: 20 })
    bookOptions.value = res.data.list.filter(b => b.isDeleted === 0)
  } catch (error) {
    console.error('搜索图书失败:', error)
  } finally {
    bookLoading.value = false
  }
}

const loadReaders = async () => {
  readerLoading.value = true
  try {
    const res = await readerApi.getList({ status: 1, pageSize: 100 })
    readerOptions.value = res.data.list
  } catch (error) {
    console.error('加载读者失败:', error)
  } finally {
    readerLoading.value = false
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      await borrowApi.borrow(formData)
      ElMessage.success('借阅成功')
      router.push('/borrow')
    } catch (error) {
      ElMessage.error(error.message || '借阅失败')
    } finally {
      submitting.value = false
    }
  })
}

const handleBack = () => {
  router.push('/borrow')
}

onMounted(() => {
  loadReaders()
})
</script>
