<template>
  <div class="reader-list">
    <div class="page-header">
      <h2 class="page-title">读者管理</h2>
    </div>

    <div class="filter-container">
      <el-form :inline="true" :model="filterForm" size="default">
        <el-form-item label="学号/工号">
          <el-input v-model="filterForm.readerNo" placeholder="请输入学号/工号" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="读者姓名">
          <el-input v-model="filterForm.readerName" placeholder="请输入姓名" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item label="读者类型">
          <el-select v-model="filterForm.readerType" placeholder="请选择" clearable style="width: 140px">
            <el-option label="本科生" :value="1" />
            <el-option label="研究生" :value="2" />
            <el-option label="教师" :value="3" />
            <el-option label="教职工" :value="4" />
            <el-option label="校友" :value="5" />
            <el-option label="校外读者" :value="6" />
            <el-option label="访问学者" :value="7" />
            <el-option label="退休教师" :value="8" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="请选择" clearable style="width: 100px">
            <el-option label="正常" :value="1" />
            <el-option label="黑名单" :value="0" />
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
          注册读者
        </el-button>
        <el-button type="danger" :disabled="!selectedIds.length" @click="handleBatchDelete">
          <el-icon><Delete /></el-icon>
          批量注销
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
        <el-table-column prop="readerNo" label="学号/工号" width="140" />
        <el-table-column prop="readerName" label="读者姓名" width="100" />
        <el-table-column prop="readerType" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getReaderTypeTag(row.readerType)" size="small">
              {{ getReaderTypeName(row.readerType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="gender" label="性别" width="70" align="center">
          <template #default="{ row }">
            {{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '其他' }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="maxBorrowNum" label="可借数量" width="90" align="center" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '黑名单' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="160" />
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button
              :type="row.status === 1 ? 'warning' : 'success'"
              link
              size="small"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '拉黑' : '启用' }}
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              注销
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
import { readerApi } from '@/api'

const router = useRouter()

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

const loading = ref(false)
const tableData = ref([])
const selectedIds = ref([])

const filterForm = reactive({
  readerNo: '',
  readerName: '',
  readerType: null,
  status: null
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
    const res = await readerApi.getList(params)
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
  filterForm.readerNo = ''
  filterForm.readerName = ''
  filterForm.readerType = null
  filterForm.status = null
  handleSearch()
}

const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.readerId)
}

const handleAdd = () => {
  router.push('/reader/add')
}

const handleEdit = (row) => {
  router.push(`/reader/edit/${row.readerId}`)
}

const handleToggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const statusText = newStatus === 1 ? '启用' : '拉黑'

  try {
    await ElMessageBox.confirm(`确定要${statusText}读者"${row.readerName}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await readerApi.updateStatus(row.readerId, newStatus)
    ElMessage.success(`${statusText}成功`)
    loadData()
  } catch (e) {}
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要注销读者"${row.readerName}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await readerApi.delete(row.readerId)
    ElMessage.success('注销成功')
    loadData()
  } catch (e) {}
}

const handleBatchDelete = async () => {
  if (!selectedIds.value.length) return

  try {
    await ElMessageBox.confirm(`确定要注销选中的 ${selectedIds.value.length} 位读者吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await readerApi.batchDelete(selectedIds.value)
    ElMessage.success('批量注销成功')
    loadData()
  } catch (e) {}
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.reader-list {
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
    
    .el-tag {
      font-weight: 500;
      border-radius: 4px;
      
      &[effect="dark"] {
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
        
        &.el-tag--warning {
          background-color: rgba(230, 162, 60, 0.1);
          color: #E6A23C;
          border-color: #E6A23C;
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
        
        &.el-button--warning {
          color: #E6A23C;
          
          &:hover {
            color: #ebb563;
            text-decoration: underline;
          }
        }
      }
    }
  }
}
</style>
