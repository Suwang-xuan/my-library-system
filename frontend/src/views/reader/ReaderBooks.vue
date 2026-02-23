<template>
  <div class="reader-books">
    <div class="page-header">
      <h2 class="page-title">图书搜索</h2>
    </div>

    <div class="search-container">
      <el-form :model="searchForm" label-width="80px" size="large">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="图书名称">
              <el-input
                v-model="searchForm.bookName"
                placeholder="请输入图书名称"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="作者">
              <el-input
                v-model="searchForm.author"
                placeholder="请输入作者"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="分类">
              <el-select
                v-model="searchForm.categoryId"
                placeholder="请选择分类"
                clearable
              >
                <el-option
                  v-for="category in categories"
                  :key="category.categoryId"
                  :label="category.categoryName"
                  :value="category.categoryId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item>
              <el-button
                type="primary"
                style="margin-right: 10px;"
                @click="handleSearch"
              >
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
              <el-button @click="handleReset">
                <el-icon><RefreshRight /></el-icon>
                重置
              </el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </div>

    <div class="books-container">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="book in books" :key="book.bookId">
          <el-card class="book-card">
            <div class="book-cover">
              <img
                :src="getBookCoverUrl(book)"
                :alt="book.bookName"
                class="cover-image"
                @error="handleCoverError"
              />
            </div>
            <div class="book-info">
              <h3 class="book-title">{{ book.bookName }}</h3>
              <p class="book-author">作者：{{ book.author }}</p>
              <p class="book-publisher">出版社：{{ book.publisher }}</p>
              <p class="book-publish-time">出版时间：{{ book.publishTime }}</p>
              <div class="book-stock">
                <el-tag type="success" size="small" v-if="book.stock > 0">
                  库存：{{ book.stock }}
                </el-tag>
                <el-tag type="danger" size="small" v-else>
                  缺货
                </el-tag>
              </div>
              <div class="book-borrow">
                <el-button
                  type="primary"
                  size="small"
                  @click="handleBorrow(book)"
                  :disabled="book.stock <= 0"
                >
                  借阅
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[12, 24, 36]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, RefreshRight, Document } from '@element-plus/icons-vue'
import { bookApi, categoryApi, borrowApi } from '@/api'

const searchForm = reactive({
  bookName: '',
  author: '',
  categoryId: null
})

const categories = ref([])
const books = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)

const loadCategories = async () => {
  try {
    const res = await categoryApi.getList()
    categories.value = res.data
  } catch (error) {
    ElMessage.error('加载分类失败')
  }
}

const loadBooks = async () => {
  try {
    const res = await bookApi.getList({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      ...searchForm
    })
    console.log('图书列表数据:', res.data.list);
    books.value = res.data.list
    total.value = res.data.total
  } catch (error) {
    console.error('加载图书失败:', error);
    ElMessage.error('加载图书失败')
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadBooks()
}

const handleReset = () => {
  searchForm.bookName = ''
  searchForm.author = ''
  searchForm.categoryId = null
  currentPage.value = 1
  loadBooks()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadBooks()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadBooks()
}

const handleBorrow = async (book) => {
  try {
    // 从sessionStorage获取读者ID
    const readerInfoStr = sessionStorage.getItem('readerInfo')
    if (!readerInfoStr) {
      ElMessage.error('未找到读者信息，请重新登录')
      return
    }
    const readerInfo = JSON.parse(readerInfoStr)
    if (!readerInfo.readerId) {
      ElMessage.error('未找到读者ID，请重新登录')
      return
    }
    
    console.log('借阅请求参数:', {
      bookId: book.bookId,
      readerId: readerInfo.readerId
    })
    
    const res = await borrowApi.borrow({
      bookId: book.bookId,
      readerId: readerInfo.readerId
    })
    
    console.log('借阅响应:', res)
    
    ElMessage.success(res.message || '借阅成功')
    loadBooks() // 刷新图书列表，更新库存
  } catch (error) {
    console.error('借阅失败:', error)
    ElMessage.error(error.message || '借阅失败')
  }
}

// 添加日志，查看book对象的内容
const logBookInfo = (book) => {
  console.log('图书信息:', {
    bookId: book.bookId,
    bookName: book.bookName,
    coverUrl: book.coverUrl
  })
  // 添加更详细的日志
  console.log('完整book对象:', book)
}

// 使用base64编码的默认封面图，避免跨域问题
const defaultCoverBase64 = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjE0MCIgdmlld0JveD0iMCAwIDEwMCAxNDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxyZWN0IHdpZHRoPSIxMDAiIGhlaWdodD0iMTAwIiByeD0iNDAiIGZpbGw9IiNmNWY3ZmEiLz4KPHJlY3QgeD0iMjAiIHk9IjEwMCIgd2lkdGg9IjYwIiBoZWlnaHQ9IjQwIiBmaWxsPSIjNDU1Ii8+CjxyZWN0IHg9IjEwIiBoZWlnaHQ9IjQwIiB3aWR0aD0iODAiIGZpbGw9IiM2NzciLz4KPHJlY3QgeD0iMjAiIHk9IjE0MCIgd2lkdGg9IjYwIiBoZWlnaHQ9IjEwIiBmaWxsPSIjYWFhIi8+CjxwYXRoIGQ9Ik0zNSAxMTBoMzAgdjEwSCAzNSIgZmlsbD0iIzg5OSIvPgo8L3N2Zz4='

const handleCoverError = (event) => {
  // 图片加载失败时，设置为base64编码的默认封面
  event.target.src = defaultCoverBase64
  event.target.style.display = 'block'
}

// 获取图书封面URL
const getBookCoverUrl = (book) => {
  // 输出图书信息到控制台，查看coverUrl
  logBookInfo(book)
  
  // 使用后端返回的coverUrl
  if (book.coverUrl) {
    // 如果是相对路径，添加完整的baseURL
    if (book.coverUrl.startsWith('/')) {
      return 'http://localhost:8081' + book.coverUrl;
    }
    return book.coverUrl;
  }
  
  // 默认返回空，由handleCoverError处理
  return '';
}

// 获取图书详情URL
const getBookDetailUrl = (book) => {
  // 直接根据书名搜索，不再使用isbn
  return `https://book.douban.com/subject_search?search_text=${encodeURIComponent(book.bookName)}&cat=1001`
}

onMounted(() => {
  loadCategories()
  loadBooks()
})
</script>

<style lang="scss" scoped>
.reader-books {
  .page-header {
    margin-bottom: 24px;
    
    .page-title {
      font-size: 22px;
      font-weight: 600;
      color: #303133;
      margin: 0;
    }
  }
  
  .search-container {
    background: #fff;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    margin-bottom: 20px;
  }
  
  .books-container {
    margin-bottom: 20px;
  }
  
  .book-card {
    height: 400px;
    display: flex;
    flex-direction: column;
    transition: all 0.3s ease;
    
    &:hover {
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
      transform: translateY(-2px);
    }
    
    .book-cover {
      height: 200px;
      background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
      border-radius: 6px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 16px;
      overflow: hidden;
      
      .cover-image {
        width: 100%;
        height: 100%;
        object-fit: cover;
        border-radius: 6px;
      }
    }
    
    .book-info {
      flex: 1;
      display: flex;
      flex-direction: column;
    }
    
    .book-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin: 0 0 8px 0;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    
    .book-author,
    .book-publisher,
    .book-publish-time {
      font-size: 14px;
      color: #606266;
      margin: 0 0 4px 0;
      display: -webkit-box;
      -webkit-line-clamp: 1;
      -webkit-box-orient: vertical;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    
    .book-stock {
      margin: 8px 0;
    }
    
    .book-borrow {
      margin-top: auto;
      display: flex;
      justify-content: center;
    }
  }
  
  .pagination {
    display: flex;
    justify-content: center;
    margin-top: 20px;
  }
}
</style>
