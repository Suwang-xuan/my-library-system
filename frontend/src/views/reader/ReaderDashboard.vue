<template>
  <div class="reader-dashboard">
    <div class="page-header">
      <h2 class="page-title">读者中心</h2>
    </div>

    <div class="dashboard-content">
      <!-- 读者信息卡片 -->
      <el-row :gutter="20">
        <el-col :span="24">
          <el-card class="info-card reader-info-card">
            <div class="reader-info">
              <div class="reader-avatar-wrapper">
                <el-avatar :size="100" class="reader-avatar">
                  {{ readerInfo.readerName?.charAt(0) }}
                </el-avatar>
              </div>
              <div class="reader-details">
                <h3 class="reader-name">{{ readerInfo.readerName }}</h3>
                <p class="reader-meta">
                  <el-tag :type="getReaderTypeTag(readerInfo.readerType)" size="small">
                    {{ getReaderTypeName(readerInfo.readerType) }}
                  </el-tag>
                  <el-tag type="info" size="small">
                    {{ readerInfo.readerNo }}
                  </el-tag>
                  <el-tag :type="readerInfo.status === 1 ? 'success' : 'danger'" size="small">
                    {{ readerInfo.status === 1 ? '正常' : '黑名单' }}
                  </el-tag>
                </p>
                <div class="reader-contact">
                  <el-icon><Phone /></el-icon>
                  <span>{{ readerInfo.phone }}</span>
                  <el-icon><Message /></el-icon>
                  <span>{{ readerInfo.email || '未设置' }}</span>
                </div>
              </div>
              <!-- 数据模块 -->
              <div class="reader-stats">
                <div class="stat-item">
                  <div class="stat-label">
                    <el-icon><Document /></el-icon>
                    <span>当前借阅</span>
                  </div>
                  <div class="stat-value">{{ readerInfo.borrowedCount || 0 }}</div>
                  <div class="stat-progress">
                    <el-progress 
                      :percentage="calculateBorrowPercentage()" 
                      :stroke-width="8" 
                      :show-text="false"
                    />
                  </div>
                </div>
                <div class="stat-item">
                  <div class="stat-label">
                    <el-icon><Reading /></el-icon>
                    <span>可借数量</span>
                  </div>
                  <div class="stat-value">{{ readerInfo.maxBorrowNum || 5 }}</div>
                </div>
                <div class="stat-item">
                  <div class="stat-label">
                    <el-icon><Clock /></el-icon>
                    <span>累计借阅</span>
                  </div>
                  <div class="stat-value">{{ readerInfo.totalBorrowed || 0 }}</div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 功能卡片 -->
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="8">
          <el-card class="function-card" @click="navigateTo('/reader/books')">
            <div class="function-content">
              <div class="function-icon blue">
                <el-icon :size="48"><Reading /></el-icon>
              </div>
              <h3 class="function-title">图书搜索</h3>
              <p class="function-desc">查找并借阅图书</p>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="function-card" @click="navigateTo('/reader/borrow-records')">
            <div class="function-content">
              <div class="function-icon green">
                <el-icon :size="48"><Document /></el-icon>
              </div>
              <h3 class="function-title">借阅记录</h3>
              <p class="function-desc">查看我的借阅历史</p>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="function-card" @click="navigateTo('/reader/profile')">
            <div class="function-content">
              <div class="function-icon orange">
                <el-icon :size="48"><User /></el-icon>
              </div>
              <h3 class="function-title">个人设置</h3>
              <p class="function-desc">修改个人信息</p>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 借阅规则提示 -->
      <div class="borrow-rules-card" style="margin-top: 20px;">
        <div class="rules-title">
          <el-icon :size="24"><Document /></el-icon>
          <span>借阅规则</span>
        </div>
        <div class="rule-content">
          <div class="rule-item">
            <el-icon class="rule-icon"><Reading /></el-icon>
            <div class="rule-content">
              <span class="rule-label">最大可借数量：</span>
              <span class="rule-value">{{ borrowRule.maxBorrowNum }}本</span>
            </div>
          </div>
          <div class="rule-item">
            <el-icon class="rule-icon"><Calendar /></el-icon>
            <div class="rule-content">
              <span class="rule-label">借阅期限：</span>
              <span class="rule-value">{{ borrowRule.borrowDays }}天</span>
            </div>
          </div>
          <div class="rule-item">
            <el-icon class="rule-icon"><RefreshRight /></el-icon>
            <div class="rule-content">
              <span class="rule-label">最大续借次数：</span>
              <span class="rule-value">{{ borrowRule.maxRenewTimes }}次</span>
            </div>
          </div>
          <div class="rule-item">
            <el-icon class="rule-icon"><Clock /></el-icon>
            <div class="rule-content">
              <span class="rule-label">每次续借天数：</span>
              <span class="rule-value">{{ borrowRule.renewDays }}天</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Phone, Message, Reading, Document, User, 
  Calendar, RefreshRight, Clock, 
  Document as DocumentIcon
} from '@element-plus/icons-vue'
import { readerApi, borrowRuleApi } from '@/api'

const router = useRouter()

const readerInfo = reactive({
  readerId: null,
  readerNo: '',
  readerName: '',
  readerType: null,
  gender: null,
  phone: '',
  email: '',
  maxBorrowNum: 0,
  status: 1,
  borrowedCount: 0,
  totalBorrowed: 0
})

const borrowRule = reactive({
  maxBorrowNum: 5,
  borrowDays: 30,
  maxRenewTimes: 2,
  renewDays: 15
})

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

const getReaderTypeName = (type) => {
  const readerType = readerTypes.find(item => item.value === type)
  return readerType ? readerType.label : '未知'
}

const getReaderTypeTag = (type) => {
  const readerType = readerTypes.find(item => item.value === type)
  return readerType ? readerType.tag : ''
}

// 计算当前借阅占比
const calculateBorrowPercentage = () => {
  const borrowed = readerInfo.borrowedCount || 0
  const max = readerInfo.maxBorrowNum || 5
  return max > 0 ? Math.round((borrowed / max) * 100) : 0
}

const navigateTo = (path) => {
  router.push(path)
}

const loadReaderInfo = async () => {
  try {
    // 从sessionStorage获取读者信息
    const readerInfoStr = sessionStorage.getItem('readerInfo')
    if (readerInfoStr) {
      const storedInfo = JSON.parse(readerInfoStr)
      if (storedInfo && storedInfo.readerId) {
        // 使用存储的读者ID获取详细信息
        const res = await readerApi.getById(storedInfo.readerId)
        if (res.data) {
          Object.assign(readerInfo, res.data)
          // 更新sessionStorage中的读者信息
          sessionStorage.setItem('readerInfo', JSON.stringify(readerInfo))
        } else {
          // 如果API返回为空，使用存储的信息
          Object.assign(readerInfo, storedInfo)
        }
      } else {
        // 使用存储的基本信息
        Object.assign(readerInfo, storedInfo)
      }
      
      // 加载借阅规则
      const ruleRes = await borrowRuleApi.getByReaderType(readerInfo.readerType)
      if (ruleRes.data) {
        Object.assign(borrowRule, ruleRes.data)
      }
    } else {
      ElMessage.error('未找到读者信息')
    }
  } catch (error) {
    console.error('加载读者信息失败:', error)
    ElMessage.error('加载读者信息失败')
  }
}

onMounted(() => {
  loadReaderInfo()
})
</script>

<style lang="scss" scoped>
// 导入全局变量
@import "@/styles/variables.scss";

.reader-dashboard {
  .page-header {
    margin-bottom: 24px;
    
    .page-title {
      font-size: 24px;
      font-weight: 700;
      color: $text-primary;
      margin: 0;
    }
  }
  
  .dashboard-content {
    max-width: 1200px;
  }
  
  .info-card {
    .reader-info {
      display: flex;
      align-items: center;
      gap: 24px;
      padding: 16px 0;
    }
    
    .reader-avatar-wrapper {
      position: relative;
    }
    
    .reader-avatar {
      background: linear-gradient(135deg, #6B90C6 0%, #8CB4E6 100%);
      color: #fff;
      font-size: 40px;
      font-weight: 600;
      box-shadow: 0 4px 12px rgba(107, 144, 198, 0.3);
      transition: all 0.3s ease;
      
      &:hover {
        transform: scale(1.05);
        box-shadow: 0 6px 16px rgba(107, 144, 198, 0.4);
      }
      
      &::after {
        content: "📚";
        position: absolute;
        bottom: -4px;
        right: -4px;
        background: white;
        border-radius: 50%;
        width: 28px;
        height: 28px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 16px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
        border: 2px solid #6B90C6;
      }
    }
    
    .reader-details {
      flex: 1;
      
      .reader-name {
        font-size: 26px;
        font-weight: 700;
        color: $text-primary;
        margin: 0 0 12px 0;
      }
      
      .reader-meta {
        display: flex;
        gap: 10px;
        flex-wrap: wrap;
        margin-bottom: 12px;
      }
      
      .reader-contact {
        display: flex;
        gap: 20px;
        align-items: center;
        color: $text-secondary;
        font-size: 14px;
        
        .el-icon {
          margin-right: 6px;
          color: $primary-color;
        }
      }
    }
    
    .reader-stats {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 20px;
      min-width: 400px;
    }
    
    .stat-item {
      background: $background-secondary;
      padding: 20px;
      border-radius: $border-radius-md;
      box-shadow: $shadow-sm;
      transition: all 0.3s ease;
      text-align: center;
      
      &:hover {
        box-shadow: $shadow-md;
        transform: translateY(-2px);
      }
      
      .stat-label {
        font-size: 14px;
        color: $text-secondary;
        margin-bottom: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 8px;
        
        .el-icon {
          font-size: 16px;
          color: $primary-color;
        }
      }
      
      .stat-value {
        display: block;
        font-size: 28px;
        font-weight: 600;
        color: $primary-color;
        margin-bottom: 12px;
      }
      
      .stat-progress {
        margin-top: 8px;
      }
    }
  }
  
  .function-card {
    cursor: pointer;
    transition: all 0.3s ease;
    border-radius: $border-radius-md;
    background: $background-secondary;
    overflow: hidden;
    
    &:hover {
      transform: translateY(-4px);
      box-shadow: $shadow-lg;
    }
    
    .function-content {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 28px 20px;
    }
    
    .function-icon {
      width: 80px;
      height: 80px;
      border-radius: $border-radius-lg;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 16px;
      transition: all 0.3s ease;
      
      &.blue {
        background: rgba(107, 144, 198, 0.1);
        color: $primary-color;
      }
      
      &.green {
        background: rgba(139, 195, 74, 0.1);
        color: $success-color;
      }
      
      &.orange {
        background: rgba(255, 193, 7, 0.1);
        color: $warning-color;
      }
      
      &.red {
        background: rgba(255, 112, 67, 0.1);
        color: $danger-color;
      }
      
      &:hover {
        transform: scale(1.1);
      }
    }
    
    .function-title {
      margin: 0 0 8px 0;
      font-size: 18px;
      font-weight: 600;
      color: $text-primary;
    }
    
    .function-desc {
      color: $text-secondary;
      font-size: 14px;
      margin: 0;
      text-align: center;
    }
  }
  
  // 借阅规则卡片样式已在custom.scss中定义
}
</style>
