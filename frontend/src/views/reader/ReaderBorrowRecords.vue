<template>
  <div class="reader-borrow-records">
    <div class="page-header">
      <h2 class="page-title">我的借阅记录</h2>
    </div>

    <div class="search-container">
      <el-form :model="searchForm" label-width="80px" size="large">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="图书名称">
              <el-input
                v-model="searchForm.bookName"
                placeholder="请输入图书名称"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态">
              <el-select
                v-model="searchForm.status"
                placeholder="请选择状态"
                clearable
              >
                <el-option label="借阅中" :value="1" />
                <el-option label="已归还" :value="2" />
                <el-option label="超期未还" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
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

    <div class="records-container">
      <el-table
        :data="records"
        style="width: 100%"
        stripe
        border
        :header-cell-style="{backgroundColor: '#f5f7fa', fontWeight: '600'}"
      >
        <el-table-column prop="bookNo" label="图书编号" width="120" />
        <el-table-column prop="bookName" label="图书名称" min-width="200" />
        <el-table-column prop="borrowTime" label="借阅时间" width="180" />
        <el-table-column prop="dueTime" label="应还时间" width="180" />
        <el-table-column prop="returnTime" label="实际归还时间" width="180" />
        <el-table-column prop="overdueDays" label="超期天数" width="100" />
        <el-table-column prop="overdueFee" label="超期费用" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag
              :type="getRecordStatusTag(scope.row.status)"
              size="small"
            >
              {{ getRecordStatusName(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 1"
              type="primary"
              size="small"
              @click="handleRenew(scope.row)"
            >
              续借
            </el-button>
            <el-button
              v-if="scope.row.status === 1"
              type="success"
              size="small"
              @click="handleReturn(scope.row)"
              style="margin-left: 10px;"
            >
              归还
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 30]"
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
import { Search, RefreshRight } from '@element-plus/icons-vue'
import { borrowApi } from '@/api'

const searchForm = reactive({
  bookName: '',
  status: null
})

const records = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const getRecordStatusName = (status) => {
  const statusMap = {
    1: '借阅中',
    2: '已归还',
    3: '超期未还'
  }
  return statusMap[status] || '未知'
}

const getRecordStatusTag = (status) => {
  const tagMap = {
    1: 'info',
    2: 'success',
    3: 'danger'
  }
  return tagMap[status] || ''
}

const loadRecords = async () => {
  try {
    // 从sessionStorage获取读者ID
    const readerInfoStr = sessionStorage.getItem('readerInfo')
    if (readerInfoStr) {
      const readerInfo = JSON.parse(readerInfoStr)
      if (readerInfo && readerInfo.readerId) {
        const res = await borrowApi.getReaderHistory(readerInfo.readerId)
        if (res && res.code === 200 && res.data) {
          records.value = res.data
          total.value = res.data.length
        } else {
          records.value = []
          total.value = 0
        }
      } else {
        ElMessage.error('未找到读者信息')
        records.value = []
        total.value = 0
      }
    } else {
      ElMessage.error('未找到读者信息')
      records.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('加载借阅记录失败:', error)
    ElMessage.error(error.message || '加载借阅记录失败')
    records.value = []
    total.value = 0
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadRecords()
}

const handleReset = () => {
  searchForm.bookName = ''
  searchForm.status = null
  currentPage.value = 1
  loadRecords()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadRecords()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadRecords()
}

const handleRenew = async (record) => {
  try {
    await borrowApi.renew({
      recordId: record.recordId
    })
    ElMessage.success('续借成功')
    loadRecords()
  } catch (error) {
    ElMessage.error(error.message || '续借失败')
  }
}

const handleReturn = async (record) => {
  try {
    console.log('归还请求参数:', { recordId: record.recordId });
    const response = await borrowApi.returnBook({
      recordId: record.recordId
    });
    console.log('归还请求响应:', response);
    ElMessage.success('归还成功');
    loadRecords();
  } catch (error) {
    console.error('归还失败:', error);
    console.error('错误详情:', error.response || error.message);
    ElMessage.error(error.message || '归还失败');
  }
}

onMounted(() => {
  loadRecords()
})
</script>

<style lang="scss" scoped>
.reader-borrow-records {
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
  
  .records-container {
    background: #fff;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    margin-bottom: 20px;
  }
  
  .pagination {
    display: flex;
    justify-content: center;
    margin-bottom: 20px;
  }
}
</style>
