<template>
  <div class="book-list">
    <div class="page-header">
      <h2 class="page-title">图书管理</h2>
    </div>

    <div class="filter-container">
      <el-form :inline="true" :model="filterForm" size="default">
        <el-form-item label="图书编号">
          <el-input v-model="filterForm.bookNo" placeholder="请输入图书编号" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="图书名称">
          <el-input v-model="filterForm.bookName" placeholder="请输入图书名称" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="filterForm.author" placeholder="请输入作者" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="filterForm.categoryId" placeholder="请选择分类" clearable style="width: 140px">
            <el-option
              v-for="cat in categories"
              :key="cat.categoryId"
              :label="cat.categoryName"
              :value="cat.categoryId"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-container">
      <div class="table-header">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          添加图书
        </el-button>
        <el-button type="danger" :disabled="!selectedIds.length" @click="handleBatchDelete">
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
      </div>

      <el-table
        :data="tableData"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        style="width: 100%"
        stripe
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="bookNo" label="图书编号" width="120" />
        <el-table-column prop="bookName" label="图书名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="author" label="作者" width="120" />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="publisher" label="出版社" width="140" show-overflow-tooltip />
        <el-table-column prop="stock" label="库存" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.stock > 0 ? 'success' : 'danger'" size="small">
              {{ row.stock }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="borrowCount" label="借阅次数" width="100" align="center" />
        <el-table-column prop="publishTime" label="出版时间" width="110" />
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { bookApi, categoryApi } from '@/api'

const router = useRouter()

const loading = ref(false)
const tableData = ref([])
const categories = ref([])
const selectedIds = ref([])

const filterForm = reactive({
  bookNo: '',
  bookName: '',
  author: '',
  categoryId: null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...Object.fromEntries(
        Object.entries(filterForm).filter(([_, v]) => v !== '' && v !== null)
      )
    }
    const res = await bookApi.getList(params)
    tableData.value = res.data.list
    pagination.total = res.data.total
  } catch (error) {
    ElMessage.error(error.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  try {
    const res = await categoryApi.getList()
    categories.value = res.data
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  loadData()
}

const handleReset = () => {
  filterForm.bookNo = ''
  filterForm.bookName = ''
  filterForm.author = ''
  filterForm.categoryId = null
  handleSearch()
}

const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.bookId)
}

const handleAdd = () => {
  router.push('/book/add')
}

const handleEdit = (row) => {
  router.push(`/book/edit/${row.bookId}`)
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除图书《${row.bookName}》吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await bookApi.delete(row.bookId)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {}
}

const handleBatchDelete = async () => {
  if (!selectedIds.value.length) return

  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 本图书吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await bookApi.batchDelete(selectedIds.value)
    ElMessage.success('批量删除成功')
    loadData()
  } catch (e) {}
}

onMounted(() => {
  loadData()
  loadCategories()
})
</script>

<style lang="scss" scoped>
.book-list {
  .page-header {
    margin-bottom: 24px;
    
    .page-title {
      font-size: 22px;
      font-weight: 600;
      color: #303133;
      margin: 0;
    }
  }

  .filter-container {
    background-color: #fff;
    padding: 24px;
    border-radius: 8px;
    margin-bottom: 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    transition: all 0.3s ease;
    
    &:hover {
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
    }
  }

  .table-container {
    background-color: #fff;
    border-radius: 8px;
    padding: 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    transition: all 0.3s ease;
    
    &:hover {
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
    }

    .table-header {
      margin-bottom: 20px;
      display: flex;
      align-items: center;
      gap: 12px;
      
      .el-button {
        border-radius: 6px;
        font-weight: 500;
        
        &:not(:first-child) {
          margin-left: 0;
        }
      }
    }
  }

  .el-table {
    border-radius: 6px;
    overflow: hidden;
    
    .el-table__header-wrapper {
      .el-table__header {
        th {
          background-color: #fafafa;
          color: #303133;
          font-weight: 600;
          border-bottom: 2px solid #409EFF;
        }
      }
    }
    
    .el-table__body-wrapper {
      .el-table__body {
        tr {
          transition: background-color 0.2s ease;
          
          &:hover > td {
            background-color: rgba(64, 158, 255, 0.05) !important;
          }
          
          &.current-row > td {
            background-color: rgba(64, 158, 255, 0.1) !important;
          }
        }
      }
    }
    
    .el-table-column--selection {
      .el-checkbox__input.is-checked .el-checkbox__inner {
        background-color: #409EFF;
        border-color: #409EFF;
      }
    }
  }

  .pagination-container {
    margin-top: 24px;
    display: flex;
    justify-content: flex-end;
    align-items: center;
    gap: 16px;
    
    .el-pagination {
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }

  .operation-buttons {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .el-button {
      padding: 4px 12px;
      font-size: 13px;
      border-radius: 4px;
      
      &.el-button--link {
        padding: 4px 8px;
        
        &.el-button--primary {
          color: #409EFF;
          
          &:hover {
            color: #66b1ff;
            text-decoration: underline;
          }
        }
        
        &.el-button--danger {
          color: #F56C6C;
          
          &:hover {
            color: #f78989;
            text-decoration: underline;
          }
        }
      }
    }
  }
}
</style>
