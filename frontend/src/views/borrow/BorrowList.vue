<template>
  <div class="borrow-list">
    <div class="page-header">
      <h2 class="page-title">借阅管理</h2>
    </div>

    <div class="filter-container">
      <el-form :inline="true" :model="filterForm" size="default">
        <el-form-item label="图书编号">
          <el-input v-model="filterForm.bookNo" placeholder="请输入图书编号" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item label="图书名称">
          <el-input v-model="filterForm.bookName" placeholder="请输入图书名称" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="读者信息">
          <el-input v-model="filterForm.readerNo" placeholder="学号/姓名/证件号" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="借阅中" :value="1" />
            <el-option label="已归还" :value="2" />
            <el-option label="超期未还" :value="3" />
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
        <el-button type="primary" @click="handleBorrow">
          <el-icon><Plus /></el-icon>
          图书借阅
        </el-button>
        <el-button type="warning" @click="handleShowOverdue">
          <el-icon><Warning /></el-icon>
          逾期记录
        </el-button>
      </div>

      <el-table
        :data="tableData"
        v-loading="loading"
        style="width: 100%"
        stripe
      >
        <el-table-column prop="bookNo" label="图书编号" width="120" />
        <el-table-column prop="bookName" label="图书名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="readerNo" label="读者学号" width="130" />
        <el-table-column prop="readerName" label="读者姓名" width="100" />
        <el-table-column prop="borrowTime" label="借阅时间" width="160" />
        <el-table-column prop="dueTime" label="应还时间" width="160" />
        <el-table-column prop="returnTime" label="实际归还时间" width="160" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="overdueDays" label="超期天数" width="90" align="center">
          <template #default="{ row }">
            <span :class="{ 'text-danger': row.overdueDays > 0 }">
              {{ row.overdueDays || '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="overdueFee" label="超期费用" width="90" align="center">
          <template #default="{ row }">
            {{ row.overdueFee ? `¥${row.overdueFee}` : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="adminName" label="操作员" width="100" />
        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 1"
              type="success"
              link
              size="small"
              @click="handleReturn(row)"
            >
              归还
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

    <el-dialog
      v-model="returnDialogVisible"
      title="图书归还"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-descriptions :column="1" border v-if="currentRecord">
        <el-descriptions-item label="图书名称">{{ currentRecord.bookName }}</el-descriptions-item>
        <el-descriptions-item label="读者姓名">{{ currentRecord.readerName }}</el-descriptions-item>
        <el-descriptions-item label="借阅时间">{{ currentRecord.borrowTime }}</el-descriptions-item>
        <el-descriptions-item label="应还时间">{{ currentRecord.dueTime }}</el-descriptions-item>
        <el-descriptions-item label="超期天数">
          <span class="text-danger">{{ currentRecord.overdueDays }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="超期费用">
          <span class="text-danger">¥{{ currentRecord.overdueFee }}</span>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="returnDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReturn" :loading="returning">
          {{ returning ? '归还中...' : '确认归还' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { borrowApi } from '@/api'

const router = useRouter()

const loading = ref(false)
const tableData = ref([])
const returnDialogVisible = ref(false)
const returning = ref(false)
const currentRecord = ref(null)

const filterForm = reactive({
  bookNo: '',
  bookName: '',
  readerNo: '',
  status: null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const getStatusType = (status) => {
  const map = { 1: 'primary', 2: 'success', 3: 'danger' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 1: '借阅中', 2: '已归还', 3: '超期未还' }
  return map[status] || '未知'
}

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
    const res = await borrowApi.getList(params)
    tableData.value = res.data.list
    pagination.total = res.data.total
  } catch (error) {
    ElMessage.error(error.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  loadData()
}

const handleReset = () => {
  filterForm.bookNo = ''
  filterForm.bookName = ''
  filterForm.readerNo = ''
  filterForm.status = null
  handleSearch()
}

const handleBorrow = () => {
  router.push('/borrow/borrow')
}

const handleReturn = (row) => {
  currentRecord.value = row
  returnDialogVisible.value = true
}

const confirmReturn = async () => {
  if (!currentRecord.value) return

  returning.value = true
  try {
    await borrowApi.return({ recordId: currentRecord.value.recordId })
    ElMessage.success('归还成功')
    returnDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '归还失败')
  } finally {
    returning.value = false
  }
}

const handleShowOverdue = async () => {
  try {
    const res = await borrowApi.getOverdueRecords()
    tableData.value = res.data
    pagination.total = res.data.length
  } catch (error) {
    ElMessage.error(error.message || '加载逾期记录失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.borrow-list {
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
    
    .el-tag {
      font-weight: 500;
      border-radius: 4px;
      
      &[effect="dark"] {
        &.el-tag--primary {
          background-color: rgba(64, 158, 255, 0.1);
          color: #409EFF;
          border-color: #409EFF;
        }
        
        &.el-tag--success {
          background-color: rgba(103, 194, 58, 0.1);
          color: #67C23A;
          border-color: #67C23A;
        }
        
        &.el-tag--danger {
          background-color: rgba(245, 108, 108, 0.1);
          color: #F56C6C;
          border-color: #F56C6C;
        }
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
        
        &.el-button--success {
          color: #67C23A;
          
          &:hover {
            color: #85ce61;
            text-decoration: underline;
          }
        }
      }
    }
  }

  .text-danger {
    color: #F56C6C;
    font-weight: bold;
  }

  .el-dialog {
    border-radius: 8px;
    overflow: hidden;
    
    .el-dialog__header {
      background-color: #fafafa;
      padding: 20px 24px;
      border-bottom: 1px solid #EBEEF5;
      
      .el-dialog__title {
        font-size: 18px;
        font-weight: 600;
        color: #303133;
      }
    }
    
    .el-dialog__body {
      padding: 24px;
    }
    
    .el-dialog__footer {
      padding: 16px 24px;
      border-top: 1px solid #EBEEF5;
      background-color: #fafafa;
      
      .el-button {
        border-radius: 6px;
        padding: 8px 20px;
        font-weight: 500;
      }
    }
  }

  .el-descriptions {
    margin: 0;
    
    .el-descriptions__table {
      width: 100%;
      
      .el-descriptions__label {
        font-weight: 500;
        color: #303133;
        background-color: #fafafa;
      }
      
      .el-descriptions__content {
        color: #606266;
      }
    }
  }
}
</style>
