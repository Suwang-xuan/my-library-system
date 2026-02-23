<template>
  <div class="book-form" :class="{ 'edit-mode': isEdit }">
    <div class="page-header">
      <h2 class="page-title">{{ isEdit ? '编辑图书信息' : '添加新图书' }}</h2>
    </div>

    <el-row :gutter="20" v-if="isEdit && formData.bookId">
      <el-col :span="24">
        <el-card class="book-preview-card">
          <div class="book-preview">
            <div class="book-cover">
              <img
                v-if="formData.coverUrl"
                :src="formData.coverUrl"
                :alt="formData.bookName"
                class="book-cover-image"
                @error="handleCoverError"
              />
              <el-icon v-else :size="48" color="#909399"><Document /></el-icon>
              <span class="book-category-tag">{{ formData.categoryName || '未分类' }}</span>
            </div>
            <div class="book-info-summary">
              <h3 class="book-title">{{ formData.bookName || '未命名' }}</h3>
              <p class="book-author">{{ formData.author || '未知作者' }}</p>
              <div class="book-meta">
                <el-tag type="info" size="small">ISBN: {{ formData.bookNo }}</el-tag>
                <el-tag type="success" size="small" v-if="formData.stock > 0">库存: {{ formData.stock }}</el-tag>
                <el-tag type="danger" size="small" v-else>缺货</el-tag>
                <el-tag type="primary" size="small">借阅次数: {{ formData.borrowCount || 0 }}</el-tag>
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
            <el-form-item label="图书编号" prop="bookNo">
              <el-input v-model="formData.bookNo" placeholder="请输入图书编号" disabled />
            </el-form-item>
            <el-form-item label="图书名称" prop="bookName">
              <el-input v-model="formData.bookName" placeholder="请输入图书名称" />
            </el-form-item>
            <el-form-item label="作者" prop="author">
              <el-input v-model="formData.author" placeholder="请输入作者" />
            </el-form-item>
            <el-form-item label="分类" prop="categoryId">
              <el-select v-model="formData.categoryId" placeholder="请选择分类" style="width: 100%;">
                <el-option
                  v-for="cat in categories"
                  :key="cat.categoryId"
                  :label="cat.categoryName"
                  :value="cat.categoryId"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="出版社" prop="publisher">
              <el-input v-model="formData.publisher" placeholder="请输入出版社" />
            </el-form-item>
            <el-form-item label="出版时间" prop="publishTime">
              <el-date-picker
                v-model="formData.publishTime"
                type="date"
                placeholder="请选择出版时间"
                value-format="yyyy-MM-dd"
                style="width: 100%;"
              />
            </el-form-item>
            <el-form-item label="封面图片URL">
          <el-input
            v-model="formData.coverUrl"
            placeholder="请输入封面图片URL"
          />
          <div class="form-tip">支持网络图片URL，如https://example.com/cover.jpg</div>
          <el-upload
            class="cover-uploader"
            action="/api/book/upload-cover"
            :data="{ bookId: formData.bookId }"
            :headers="{ 'Content-Type': 'multipart/form-data' }"
            :on-success="handleCoverUploadSuccess"
            :on-error="handleCoverUploadError"
            :show-file-list="false"
            accept="image/*"
            :disabled="!formData.bookId"
          >
            <el-button size="small" type="primary" v-if="formData.bookId">
              <el-icon><Plus /></el-icon>
              上传封面图片
            </el-button>
            <div class="form-tip" v-if="!formData.bookId">保存图书后可上传封面</div>
          </el-upload>
        </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSubmit" :loading="submitting">
                {{ submitting ? '保存中...' : '保存修改' }}
              </el-button>
              <el-button @click="handleBack">返 回</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="库存信息" name="stock">
          <el-form
            label-width="120px"
            size="default"
            style="max-width: 600px;"
          >
            <el-form-item label="当前库存">
              <el-input-number v-model="formData.stock" :min="0" :max="9999" />
              <span class="form-tip">当前可借阅数量</span>
            </el-form-item>
            <el-form-item label="累计借阅">
              <el-input :value="formData.borrowCount || 0" disabled />
              <span class="form-tip">图书被借阅的总次数</span>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUpdateStock" :loading="updatingStock">
                {{ updatingStock ? '保存中...' : '更新库存' }}
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
        <el-form-item label="图书编号" prop="bookNo">
          <el-input v-model="formData.bookNo" placeholder="请输入图书编号" />
        </el-form-item>
        <el-form-item label="图书名称" prop="bookName">
          <el-input v-model="formData.bookName" placeholder="请输入图书名称" />
        </el-form-item>
        <el-form-item label="作者" prop="author">
          <el-input v-model="formData.author" placeholder="请输入作者" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="formData.categoryId" placeholder="请选择分类" style="width: 100%;">
            <el-option
              v-for="cat in categories"
              :key="cat.categoryId"
              :label="cat.categoryName"
              :value="cat.categoryId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="出版社" prop="publisher">
          <el-input v-model="formData.publisher" placeholder="请输入出版社" />
        </el-form-item>
        <el-form-item label="出版时间" prop="publishTime">
          <el-date-picker
            v-model="formData.publishTime"
            type="date"
            placeholder="请选择出版时间"
            value-format="yyyy-MM-dd"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="封面图片URL">
          <el-input
            v-model="formData.coverUrl"
            placeholder="请输入封面图片URL"
          />
          <div class="form-tip">支持网络图片URL，如https://example.com/cover.jpg</div>
        </el-form-item>
        <el-form-item label="库存数量" prop="stock">
          <el-input-number v-model="formData.stock" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ submitting ? '保存中...' : '添 加' }}
          </el-button>
          <el-button @click="handleBack">返 回</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Document } from '@element-plus/icons-vue'
import { bookApi, categoryApi } from '@/api'

const router = useRouter()
const route = useRoute()

const formRef = ref(null)
const submitting = ref(false)
const updatingStock = ref(false)
const categories = ref([])
const activeTab = ref('basic')

const isEdit = computed(() => !!route.params.id)

const formData = reactive({
  bookId: null,
  bookNo: '',
  bookName: '',
  author: '',
  categoryId: null,
  categoryName: '',
  publisher: '',
  publishTime: '',
  coverUrl: '',
  stock: 0,
  borrowCount: 0
})

const formRules = {
  bookNo: [
    { required: true, message: '请输入图书编号', trigger: 'blur' }
  ],
  bookName: [
    { required: true, message: '请输入图书名称', trigger: 'blur' }
  ]
}

const loadCategories = async () => {
  try {
    const res = await categoryApi.getList()
    categories.value = res.data
  } catch (error) {
    ElMessage.error('加载分类失败')
  }
}

const loadBookData = async () => {
  if (!isEdit.value) return

  try {
    const res = await bookApi.getById(route.params.id)
    Object.assign(formData, res.data)
    activeTab.value = 'basic'
  } catch (error) {
    ElMessage.error('加载图书信息失败')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      if (isEdit.value) {
        await bookApi.update(formData)
        ElMessage.success('更新成功')
      } else {
        await bookApi.add(formData)
        ElMessage.success('添加成功')
      }
      router.push('/book')
    } catch (error) {
      ElMessage.error(error.message || '操作失败')
    } finally {
      submitting.value = false
    }
  })
}

const handleUpdateStock = async () => {
  updatingStock.value = true
  try {
    await bookApi.update({
      bookId: formData.bookId,
      stock: formData.stock
    })
    ElMessage.success('库存更新成功')
  } catch (error) {
    ElMessage.error(error.message || '库存更新失败')
  } finally {
    updatingStock.value = false
  }
}

const handleBack = () => {
  router.push('/book')
}

// 使用base64编码的默认封面图，避免跨域问题
const defaultCoverBase64 = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjE0MCIgdmlld0JveD0iMCAwIDEwMCAxNDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxyZWN0IHdpZHRoPSIxMDAiIGhlaWdodD0iMTAwIiByeD0iNDAiIGZpbGw9IiNmNWY3ZmEiLz4KPHJlY3QgeD0iMjAiIHk9IjEwMCIgd2lkdGg9IjYwIiBoZWlnaHQ9IjQwIiBmaWxsPSIjNDU1Ii8+CjxyZWN0IHg9IjEwIiBoZWlnaHQ9IjQwIiB3aWR0aD0iODAiIGZpbGw9IiM2NzciLz4KPHJlY3QgeD0iMjAiIHk9IjE0MCIgd2lkdGg9IjYwIiBoZWlnaHQ9IjEwIiBmaWxsPSIjYWFhIi8+CjxwYXRoIGQ9Ik0zNSAxMTBoMzAgdjEwSCAzNSIgZmlsbD0iIzg5OSIvPgo8L3N2Zz4='

const handleCoverError = (event) => {
  // 封面图片加载失败时，设置为base64编码的默认封面
  event.target.src = defaultCoverBase64
  event.target.style.display = 'block'
}

const handleCoverUploadSuccess = (response) => {
  if (response.code === 200) {
    formData.coverUrl = response.data.coverUrl
    ElMessage.success('封面上传成功')
  } else {
    ElMessage.error('封面上传失败：' + (response.message || '未知错误'))
  }
}

const handleCoverUploadError = (error) => {
  ElMessage.error('封面上传失败：网络错误')
  console.error('封面上传失败', error)
}

onMounted(() => {
  loadCategories()
  if (isEdit.value) {
    loadBookData()
  } else {
    formData.stock = 5
  }
})
</script>

<style lang="scss" scoped>
.book-form {
  .page-header {
    margin-bottom: 24px;
    
    .page-title {
      font-size: 22px;
      font-weight: 600;
      color: #303133;
      margin: 0;
    }
  }

  .book-preview-card {
    margin-bottom: 24px;
    border-radius: 8px;
    
    .book-preview {
      display: flex;
      align-items: center;
      gap: 20px;
      
      .book-cover {
        width: 100px;
        height: 140px;
        background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
        border-radius: 6px;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        position: relative;
        overflow: hidden;
        
        .book-cover-image {
          width: 100%;
          height: 100%;
          object-fit: cover;
          border-radius: 6px;
        }
        
        .book-category-tag {
          position: absolute;
          top: 8px;
          right: 8px;
          background: #409EFF;
          color: #fff;
          font-size: 12px;
          padding: 2px 8px;
          border-radius: 4px;
          z-index: 1;
        }
      }
      
      .book-info-summary {
        flex: 1;
        
        .book-title {
          font-size: 20px;
          font-weight: 600;
          color: #303133;
          margin: 0 0 8px 0;
        }
        
        .book-author {
          font-size: 14px;
          color: #606266;
          margin: 0 0 12px 0;
        }
        
        .book-meta {
          display: flex;
          gap: 8px;
          flex-wrap: wrap;
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
      .el-select,
      .el-date-picker,
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
}
</style>
