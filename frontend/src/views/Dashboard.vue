<template>
  <div class="dashboard">
    <div class="page-header">
      <h2 class="page-title">数据概览</h2>
    </div>

    <!-- 数据统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6" v-for="stat in stats" :key="stat.id">
        <div class="stat-card" :class="`stat-card-${stat.color}`" @mouseenter="stat.hovered = true" @mouseleave="stat.hovered = false">
          <el-row align="middle">
            <el-col :span="16">
              <div class="stat-value">{{ stat.value }}</div>
              <div v-if="stat.trend" class="stat-trend" :class="`stat-trend-${stat.trend.type}`">
                <el-icon>
                  <component :is="stat.trend.icon" />
                </el-icon>
                <span>{{ stat.trend.text }}</span>
              </div>
              <div class="stat-label">
                {{ stat.label }}
                <el-button 
                  v-if="stat.label === '逾期数量'" 
                  type="primary" 
                  size="small" 
                  class="remind-btn" 
                  @click="showRemindDialog = true"
                >
                  催还
                </el-button>
              </div>
            </el-col>
            <el-col :span="8" style="text-align: right;">
              <div :class="['stat-icon', stat.color]">
                <el-icon>
                  <Reading v-if="stat.icon === 'Reading'" />
                  <User v-else-if="stat.icon === 'User'" />
                  <Tickets v-else-if="stat.icon === 'Tickets'" />
                  <Warning v-else-if="stat.icon === 'Warning'" />
                  <Reading v-else />
                </el-icon>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-col>
    </el-row>
    
    <!-- 一键催还弹窗 -->
    <el-dialog
      v-model="showRemindDialog"
      title="批量催还"
      width="400px"
      :border-radius="8"
      :custom-class="'remind-dialog'"
    >
      <div class="dialog-content">
        <div class="dialog-section">
          <el-checkbox v-model="selectAll" @change="handleSelectAll">
            全选逾期读者 ({{ overdueReaders.length }})
          </el-checkbox>
        </div>
        
        <div class="dialog-section">
          <h4>催还模板</h4>
          <div class="template-buttons">
            <el-button type="default" size="small" @click="selectTemplate(1)">模板一</el-button>
            <el-button type="default" size="small" @click="selectTemplate(2)">模板二</el-button>
          </div>
        </div>
        
        <div class="dialog-section">
          <h4>预览</h4>
          <div class="preview-content">
            {{ selectedTemplate }}
          </div>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showRemindDialog = false">取消</el-button>
          <el-button type="primary" @click="sendReminder" style="background-color: #4080FF;">发送</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 月度趋势图 -->
    <el-row :gutter="25" style="margin-top: 25px;">
      <el-col :span="14">
        <div class="chart-container">
          <div class="chart-title">
            月度借阅趋势
            <el-button type="text" size="small" style="float: right; color: #64748B;" @click="resetAllCharts">
              重置
            </el-button>
          </div>
          <div ref="trendChartRef" class="chart"></div>
        </div>
      </el-col>
      <!-- 分类借阅统计 -->
      <el-col :span="10">
        <div class="chart-container">
          <div class="chart-title">分类借阅统计</div>
          <div ref="categoryChartRef" class="chart"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 热门图书 -->
    <el-row :gutter="25" style="margin-top: 25px;">
      <el-col :span="12">
        <div class="chart-container">
          <div class="chart-title">热门图书 TOP 10</div>
          <el-table :data="hotBooks" style="width: 100%" max-height="300">
            <el-table-column prop="bookName" label="书名" width="auto">
              <template #default="{ row }">
                <span>{{ row.bookName }}</span>
                <el-tag :type="'info'" size="mini" :class="['borrow-ratio', `borrow-ratio-${row.category}`]">{{ row.borrowRatio }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="author" label="作者" width="120" />
            <el-table-column prop="borrowCount" label="借阅次数" width="100" align="center" />
          </el-table>
        </div>
      </el-col>
      <!-- 库存预警 -->
      <el-col :span="12">
        <div class="chart-container">
          <div class="chart-title">库存预警</div>
          <el-table :data="lowStockBooks" style="width: 100%" max-height="300">
            <el-table-column prop="bookName" label="书名" width="auto">
              <template #default="{ row }">
                <div>
                  <span class="book-name-link" @click="navigateToPurchase(row)">{{ row.bookName }}</span>
                  <div class="recent-demand" v-if="row.recentDemand">
                    近7天被预约 {{ row.recentDemand }} 次
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="bookNo" label="图书编号" width="120" />
            <el-table-column prop="stock" label="库存" width="100" align="center">
              <template #default="{ row }">
                <el-tag 
                  :type="'danger'" 
                  size="small" 
                  :class="[
                    { 'stock-warning': row.stock === 1 },
                    { 'stock-warning-low': row.stock === 2 }
                  ]">
                  {{ row.stock }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { statisticsApi } from '@/api'
import { Reading, User, Tickets, Warning, ArrowUp, ArrowDown, Minus } from '@element-plus/icons-vue'

const trendChartRef = ref(null)
const categoryChartRef = ref(null)

let trendChart = null
let categoryChart = null

const stats = ref([
  { 
    label: '图书总数', 
    value: 0, 
    displayValue: 0, 
    icon: 'Reading', 
    color: 'blue', 
    hovered: false, 
    id: 'stat-1',
    trend: { type: 'up', icon: ArrowUp, text: '较上月 + 20 本' }
  },
  { 
    label: '读者总数', 
    value: 0, 
    displayValue: 0, 
    icon: 'User', 
    color: 'green', 
    hovered: false, 
    id: 'stat-2',
    trend: { type: 'up', icon: ArrowUp, text: '较上月 + 15 人' }
  },
  { 
    label: '当前借阅', 
    value: 0, 
    displayValue: 0, 
    icon: 'Tickets', 
    color: 'orange', 
    hovered: false, 
    id: 'stat-3',
    trend: { type: 'down', icon: ArrowDown, text: '较上月 - 5 本' }
  },
  { 
    label: '逾期数量', 
    value: 0, 
    displayValue: 0, 
    icon: 'Warning', 
    color: 'red', 
    hovered: false, 
    id: 'stat-4',
    trend: { type: 'up', icon: ArrowUp, text: '较上月 + 3 本' }
  }
])

const hotBooks = ref([])
const lowStockBooks = ref([])
const categoryData = ref([])

// 一键催还弹窗状态
const showRemindDialog = ref(false)
const selectAll = ref(true)
const overdueReaders = ref([
  { id: 1, name: '张三', bookName: 'Java核心技术', overdueDays: 5 },
  { id: 2, name: '李四', bookName: 'Python编程从入门到实践', overdueDays: 3 },
  { id: 3, name: '王五', bookName: '算法导论', overdueDays: 8 }
])

// 催还模板
const templates = [
  '尊敬的读者，您借阅的《书名》已逾期X天，请尽快归还。',
  '您好，您有一本图书《书名》已逾期X天，为了不影响您的信用，请及时归还。'
]

const selectedTemplate = ref(templates[0])

// 选择全选
const handleSelectAll = () => {
  // 这里可以添加全选/取消全选逻辑
}

// 选择模板
const selectTemplate = (index) => {
  selectedTemplate.value = templates[index - 1]
}

// 发送催还提醒
const sendReminder = () => {
  // 这里可以添加发送催还提醒的逻辑
  showRemindDialog.value = false
  ElMessage.success('催还提醒已发送')
}

// 跳转到采购页面
const navigateToPurchase = (book) => {
  // 实际项目中应跳转到采购页面，并传递图书信息
  console.log('跳转到采购页面，图书信息:', book)
  // 这里使用模拟跳转，实际项目中应使用router.push
  // router.push({ 
  //   path: '/purchase', 
  //   query: { 
  //     bookName: book.bookName, 
  //     author: book.author, 
  //     bookNo: book.bookNo 
  //   } 
  // })
  ElMessage.info(`跳转到《${book.bookName}》的采购页面`)
}

const initTrendChart = (data, color = '#4080FF') => {
  if (!trendChartRef.value) return

  trendChart = echarts.init(trendChartRef.value)
  
  // 计算环比数据
  const calculateGrowth = (current, previous) => {
    if (!previous) return { symbol: '', value: '0%', text: '持平' }
    const growth = ((current - previous) / previous * 100).toFixed(1)
    const symbol = growth >= 0 ? '↑' : '↓'
    return { symbol, value: `${symbol}${Math.abs(growth)}%`, text: `${symbol}${Math.abs(growth)}%` }
  }
  
  const option = {
    backgroundColor: 'rgba(64,128,255,0.05)',
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#ffffff',
      borderColor: '#e5e5e5',
      borderWidth: 1,
      borderRadius: 8,
      padding: 10,
      textStyle: {
        fontFamily: '微软雅黑',
        fontSize: 12,
        color: '#333333',
        lineHeight: 1.2
      },
      formatter: (params) => {
        const current = params[0]
        const currentIndex = data.findIndex(item => item.monthName === current.name)
        const previousIndex = currentIndex - 1
        const previous = data[previousIndex]
        const growth = calculateGrowth(current.value, previous?.count)
        
        // 确保previousMonth有值，避免显示"环 undefined"
        const previousMonth = previous?.monthName || '上月'
        
        return `
          <div style="line-height: 1.2;">
            <div>${current.name}：${current.value} 本</div>
            <div style="color: #666666; font-size: 11px;">环比${previousMonth}${growth.value}</div>
          </div>
        `
      }
    },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: data.map(item => item.monthName)
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      data: data.map(item => item.count),
      type: 'line',
      smooth: true,
      // 加粗折线图线条到2.5px
      lineStyle: {
        width: 2.5,
        color: color
      },
      // 加粗数据点
      itemStyle: {
        color: color,
        borderWidth: 3,
        borderColor: color
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: `${color}4D` }, // 64透明度
          { offset: 1, color: `${color}1A` }  // 25透明度
        ])
      }
    }]
  }
  trendChart.setOption(option)
}

const initCategoryChart = (data) => {
  if (!categoryChartRef.value) return

  categoryChart = echarts.init(categoryChartRef.value)
  
  // 保存原始数据用于联动
  categoryData.value = data
  
  const option = {
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      icon: 'circle',
      itemWidth: 8,
      itemHeight: 8,
      itemGap: 12,
      textStyle: {
        fontSize: 12,
        padding: [0, 0, 0, 5],
        color: '#666'
      },
      formatter: (name) => {
        return name;
      }
    },
    series: [{
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: false,
      // 提高饼图色块对比度
      itemStyle: {
        borderRadius: 6,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 14, fontWeight: 'bold' },
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      },
      data: data.map(item => ({
        name: item.categoryName,
        value: item.borrowCount,
        itemStyle: {
          color: item.color
        }
      }))
    }]
  }
  
  // 添加点击事件处理
  categoryChart.on('click', (params) => {
    handleCategoryClick(params)
  })
  
  // 使用ECharts默认的hover效果，自动处理扇区高亮
  // 移除了自定义hover事件，避免导致其他板块消失
  
  categoryChart.setOption(option)
}

// 保存原始数据以便恢复
const originalStats = ref([])
const originalHotBooks = ref([])

// 分类点击联动处理
const handleCategoryClick = (params) => {
  const categoryName = params.name
  const categoryColor = params.data.color
  
  // 保存原始数据（首次点击时）
  if (originalStats.value.length === 0) {
    originalStats.value = JSON.parse(JSON.stringify(stats.value))
    originalHotBooks.value = [...hotBooks.value]
  }
  
  // 1. 联动月度趋势图 - 使用模拟数据
  const categoryTrendData = [
    { monthName: '1月', count: Math.floor(Math.random() * 100) + 50 },
    { monthName: '2月', count: Math.floor(Math.random() * 100) + 50 },
    { monthName: '3月', count: Math.floor(Math.random() * 100) + 50 },
    { monthName: '4月', count: Math.floor(Math.random() * 100) + 50 },
    { monthName: '5月', count: Math.floor(Math.random() * 100) + 50 },
    { monthName: '6月', count: Math.floor(Math.random() * 100) + 50 },
    { monthName: '7月', count: Math.floor(Math.random() * 100) + 50 },
    { monthName: '8月', count: Math.floor(Math.random() * 100) + 50 },
    { monthName: '9月', count: Math.floor(Math.random() * 100) + 50 },
    { monthName: '10月', count: Math.floor(Math.random() * 100) + 50 },
    { monthName: '11月', count: Math.floor(Math.random() * 100) + 50 },
    { monthName: '12月', count: Math.floor(Math.random() * 100) + 50 }
  ]
  initTrendChart(categoryTrendData, categoryColor)
  
  // 2. 联动热门图书 - 筛选对应分类的图书
  const filteredBooks = originalHotBooks.value.filter(book => book.category === categoryName)
  hotBooks.value = filteredBooks.length > 0 ? filteredBooks : originalHotBooks.value
  
  // 3. 联动当前借阅卡片 - 显示对应分类的当前借阅数
  const categoryBorrowCount = Math.floor(Math.random() * 100) + 20
  stats.value[2] = {
    ...stats.value[2],
    value: categoryBorrowCount,
    label: `${categoryName}当前借阅`,
    displayValue: categoryBorrowCount
  }
  
  // 重新执行数字动画
  animateStats()
}

// 重置所有图表数据
const resetAllCharts = () => {
  loadData()
  // 清空原始数据缓存
  originalStats.value = []
  originalHotBooks.value = []
}

// 数字滚动动画函数
const animateNumber = (element, start, end, duration) => {
  let startTime = null;
  const step = (timestamp) => {
    if (!startTime) startTime = timestamp;
    const progress = Math.min((timestamp - startTime) / duration, 1);
    const current = Math.floor(progress * (end - start) + start);
    element.textContent = current;
    if (progress < 1) {
      requestAnimationFrame(step);
    }
  };
  requestAnimationFrame(step);
};

// 加载数据后执行数字动画
const animateStats = () => {
  const statValueElements = document.querySelectorAll('.stat-value');
  statValueElements.forEach((element, index) => {
    animateNumber(element, 0, stats.value[index].value, 1500);
  });
};

const loadData = () => {
  // 使用模拟数据，不依赖后端API
  stats.value[0].value = 1500
  stats.value[1].value = 2000
  stats.value[2].value = 300
  stats.value[3].value = 15
  
  hotBooks.value = [
    { bookName: 'Java核心技术', author: 'Cay S. Horstmann', borrowCount: 120, borrowRatio: '18%', category: '计算机科学' },
    { bookName: 'Python编程从入门到实践', author: 'Eric Matthes', borrowCount: 95, borrowRatio: '15%', category: '计算机科学' },
    { bookName: '算法导论', author: 'Thomas H. Cormen', borrowCount: 88, borrowRatio: '13%', category: '科学技术' },
    { bookName: '深入理解计算机系统', author: 'Randal E. Bryant', borrowCount: 75, borrowRatio: '11%', category: '科学技术' },
    { bookName: '设计模式', author: 'Erich Gamma', borrowCount: 68, borrowRatio: '10%', category: '计算机科学' },
    { bookName: '百年孤独', author: '加西亚·马尔克斯', borrowCount: 65, borrowRatio: '9%', category: '文学小说' },
    { bookName: 'JavaScript高级程序设计', author: 'Nicholas C. Zakas', borrowCount: 62, borrowRatio: '9%', category: '计算机科学' },
    { bookName: '人类简史', author: '尤瓦尔·赫拉利', borrowCount: 58, borrowRatio: '8%', category: '历史传记' },
    { bookName: '操作系统概念', author: 'Abraham Silberschatz', borrowCount: 55, borrowRatio: '8%', category: '科学技术' },
    { bookName: '计算机网络', author: 'Andrew S. Tanenbaum', borrowCount: 50, borrowRatio: '7%', category: '计算机科学' }
  ]
  
  lowStockBooks.value = [
    { bookName: '深入理解Java虚拟机', author: '周志明', bookNo: 'ISBN-9787111421900', stock: 1, recentDemand: 3 },
    { bookName: 'Redis设计与实现', author: '黄健宏', bookNo: 'ISBN-9787115379501', stock: 1, recentDemand: 2 },
    { bookName: 'MongoDB权威指南', author: 'Kristina Chodorow', bookNo: 'ISBN-9787111421917', stock: 2, recentDemand: 1 },
    { bookName: 'Elasticsearch权威指南', author: 'Clinton Gormley', bookNo: 'ISBN-9787115379518', stock: 2, recentDemand: 1 }
  ]
  
  // 模拟月度借阅趋势数据
  const trendData = [
    { monthName: '1月', count: 120 },
    { monthName: '2月', count: 95 },
    { monthName: '3月', count: 130 },
    { monthName: '4月', count: 155 },
    { monthName: '5月', count: 180 },
    { monthName: '6月', count: 165 },
    { monthName: '7月', count: 100 },
    { monthName: '8月', count: 90 },
    { monthName: '9月', count: 145 },
    { monthName: '10月', count: 170 },
    { monthName: '11月', count: 190 },
    { monthName: '12月', count: 210 }
  ]
  initTrendChart(trendData)
  
  // 模拟分类借阅统计数据 - 统一全局色彩，与导航栏对应
  const categoryData = [
    { categoryName: '计算机科学', borrowCount: 580, color: '#4080FF' }, // 对应导航栏首页
    { categoryName: '文学小说', borrowCount: 420, color: '#87D293' }, // 对应图书管理模块
    { categoryName: '历史传记', borrowCount: 280, color: '#F59E0B' }, // 保持橙色系
    { categoryName: '科学技术', borrowCount: 350, color: '#EF4444' }, // 保持红色系
    { categoryName: '社会科学', borrowCount: 220, color: '#8B5CF6' }, // 保持紫色系
    { categoryName: '经济管理', borrowCount: 180, color: '#06B6D4' }, // 保持青色系
    { categoryName: '艺术设计', borrowCount: 150, color: '#EC4899' }, // 保持粉色系
    { categoryName: '语言学习', borrowCount: 200, color: '#10B981' }  // 保持绿色系
  ]
  initCategoryChart(categoryData)
  
  // 等待DOM更新后执行动画
  setTimeout(() => {
    animateStats();
  }, 100);
}

const handleResize = () => {
  trendChart?.resize()
  categoryChart?.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  categoryChart?.dispose()
})
</script>

<style lang="scss" scoped>
.dashboard {
  // 页面头部布局
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }
  
  // 页面标题样式
  .page-title {
    font-size: 20px;
    font-weight: bold;
    color: #2D3748;
    position: relative;
    padding-bottom: 8px;
    
    &::after {
      content: '';
      position: absolute;
      left: 0;
      bottom: 0;
      width: 40px;
      height: 3px;
      background-color: #3B82F6;
      border-radius: 2px;
    }
  }
  
  // 页面操作按钮样式
  .page-actions {
    display: flex;
    gap: 8px;
  }
  
  // 自定义模式下的样式
  .custom-mode {
    .stat-card,
    .chart-container {
      cursor: move;
      border: 2px dashed #CCCCCC;
      
      &:hover {
        background-color: rgba(59, 130, 246, 0.05);
      }
    }
  }
  
  // 拖拽提示样式
  .drag-tip {
    position: absolute;
    top: 10px;
    right: 10px;
    background-color: rgba(0, 0, 0, 0.6);
    color: #FFFFFF;
    padding: 4px 8px;
    border-radius: 4px;
    font-size: 12px;
    z-index: 10;
    pointer-events: none;
  }
  
  // 拖拽中样式
  .dragging {
    opacity: 0.5;
    transform: rotate(2deg);
    transition: all 0.2s ease;
  }
  
  // 模块容器样式（添加相对定位，让拖拽提示能绝对定位）
  .el-row,
  .el-col {
    position: relative;
  }
  
  // 图书名称链接样式
  .book-name-link {
    color: #4080FF;
    cursor: pointer;
    text-decoration: none;
    transition: all 0.2s ease;
    
    &:hover {
      color: #66B1FF;
      text-decoration: underline;
    }
  }
  
  // 一键催还弹窗样式
  .remind-dialog {
    .el-dialog {
      border-radius: 8px;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
      
      .el-dialog__header {
        padding: 20px 20px 10px;
      }
      
      .el-dialog__title {
        font-size: 16px;
        font-weight: 600;
        color: #333;
      }
      
      .el-dialog__body {
        padding: 10px 20px 20px;
        height: 280px;
        overflow-y: auto;
      }
      
      .el-dialog__footer {
        padding: 10px 20px 20px;
        border-top: none;
      }
    }
    
    // 弹窗内容样式
    .dialog-content {
      .dialog-section {
        margin-bottom: 20px;
        
        h4 {
          font-size: 14px;
          font-weight: 600;
          color: #333;
          margin-bottom: 10px;
        }
      }
      
      // 模板按钮样式
      .template-buttons {
        display: flex;
        gap: 10px;
        
        .el-button {
          width: 80px;
          height: 32px;
          border-radius: 4px;
          font-size: 12px;
        }
      }
      
      // 预览内容样式
      .preview-content {
        background-color: #F5F5F5;
        border-radius: 4px;
        padding: 12px;
        min-height: 60px;
        font-size: 12px;
        color: #333;
        line-height: 1.5;
      }
      
      // 复选框样式
      .el-checkbox {
        font-size: 13px;
        color: #333;
      }
    }
  }

  // 统计卡片样式
  .stat-card {
    background: #FFFFFF;
    border-radius: 5px;
    padding: 20px;
    transition: all 0.3s ease;
    position: relative;
    overflow: hidden;
    border: 1px solid transparent;
    background-clip: padding-box;
    
    // 卡片hover效果
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    }
  }
  
  // 卡片识别色 - 使用渐变边框和同色系背景
  .stat-card-blue {
    background-color: rgba(134, 168, 255, 0.1);
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      border-radius: 5px;
      padding: 1px;
      background: linear-gradient(135deg, #4080FF, #69b1ff);
      -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
      -webkit-mask-composite: xor;
      mask-composite: exclude;
      pointer-events: none;
    }
  }
  
  .stat-card-green {
    background-color: rgba(135, 210, 147, 0.1);
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      border-radius: 5px;
      padding: 1px;
      background: linear-gradient(135deg, #10B981, #34D399);
      -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
      -webkit-mask-composite: xor;
      mask-composite: exclude;
      pointer-events: none;
    }
  }
  
  .stat-card-orange {
    background-color: rgba(255, 196, 123, 0.1);
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      border-radius: 5px;
      padding: 1px;
      background: linear-gradient(135deg, #F59E0B, #FBBF24);
      -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
      -webkit-mask-composite: xor;
      mask-composite: exclude;
      pointer-events: none;
    }
  }
  
  .stat-card-red {
    background-color: rgba(255, 102, 102, 0.1);
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      border-radius: 5px;
      padding: 1px;
      background: linear-gradient(135deg, #EF4444, #F87171);
      -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
      -webkit-mask-composite: xor;
      mask-composite: exclude;
      pointer-events: none;
    }
  }

  // 统计头部样式（包含数值和趋势）
  .stat-header {
    display: flex;
    align-items: baseline;
    gap: 8px;
    margin-bottom: 8px;
  }

  // 统计值样式
  .stat-value {
    font-size: 24px;
    font-weight: bold;
    color: #1E293B;
    line-height: 1.2;
    margin-bottom: 4px;
  }

  // 统计趋势样式
  .stat-trend {
    display: flex;
    align-items: center;
    gap: 2px;
    font-size: 12px;
    font-weight: 500;
    color: #666;
    margin-bottom: 8px;
  }
  
  // 统计标签样式
  .stat-label {
    font-size: 14px;
    color: #64748B;
    font-weight: 500;
    display: flex;
    align-items: center;
    gap: 8px;
  }
  
  // 上升趋势样式
  .stat-trend-up {
    color: #10B981;
  }
  
  // 下降趋势样式
  .stat-trend-down {
    color: #EF4444;
  }
  
  // 持平趋势样式
  .stat-trend-flat {
    color: #6B7280;
  }

  // 统计标签样式
  .stat-label {
    font-size: 14px;
    color: #64748B;
    font-weight: 500;
  }
  
  // 统计操作按钮样式
  .stat-action {
    margin-top: 12px;
  }

  // 统计图标样式
  .stat-icon {
    width: 50px;
    height: 50px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    
    // 图标颜色
    &.blue {
      background-color: rgba(30, 64, 175, 0.1);
      color: #1E40AF;
    }
    
    &.green {
      background-color: rgba(22, 163, 74, 0.1);
      color: #16A34A;
    }
    
    &.orange {
      background-color: rgba(245, 158, 11, 0.1);
      color: #F59E0B;
    }
    
    &.red {
      background-color: rgba(239, 68, 68, 0.1);
      color: #EF4444;
    }
  }

  // 图表容器样式
  .chart-container {
    background: #FFFFFF;
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    margin-bottom: 20px;
  }
  
  // 图表标题样式
  .chart-title {
    font-size: 16px;
    font-weight: bold;
    color: #2D3748;
    margin-bottom: 16px;
    padding-bottom: 8px;
    border-bottom: 1px solid #E2E8F0;
  }
  
  // 图表样式
  .chart {
    height: 300px;
    width: 100%;
  }
  
  // 表格样式
  .el-table {
    background: #FFFFFF;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    
    .el-table__header-wrapper {
      .el-table__header {
        th {
          background-color: #F8FAFC;
          font-weight: bold;
          color: #2D3748;
        }
      }
    }
    
    .el-table__body-wrapper {
      max-height: 300px;
      
      // 显示更多行
      .el-table__row {
        &:nth-child(-n+5) {
          display: table-row;
        }
      }
    }
  }
  
  // 库存预警数字跳动动画（库存=1）
  .stock-warning {
    animation: bounce 2s infinite;
    font-weight: 600;
    // 渐变字体效果
    background: linear-gradient(135deg, #FF4500, #FF6347);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
  
  @keyframes bounce {
    0%, 100% {
      transform: translateY(0);
      opacity: 1;
    }
    50% {
      transform: translateY(-3px);
      opacity: 0.8;
    }
  }
  
  // 库存预警数字渐隐渐现+边框闪烁动画（库存=2）
  .stock-warning-low {
    animation: pulseFade 2.5s infinite;
    font-weight: 600;
    color: #FFA500;
    position: relative;
  }
  
  @keyframes pulseFade {
    0%, 100% {
      opacity: 1;
      filter: brightness(100%);
      box-shadow: 0 0 0 0 rgba(255, 165, 0, 0.4);
      border: 1px solid rgba(255, 165, 0, 1);
    }
    50% {
      opacity: 0.8;
      filter: brightness(80%);
      box-shadow: 0 0 0 3px rgba(255, 165, 0, 0);
      border: 1px solid rgba(255, 165, 0, 0.5);
    }
  }
  
  // 图书名称链接样式
  .book-name-link {
    color: #4080FF;
    cursor: pointer;
    text-decoration: none;
    transition: all 0.2s ease;
    
    &:hover {
      color: #66B1FF;
      text-decoration: underline;
    }
  }
  
  // 借阅占比标签样式
  .borrow-ratio {
    background-color: #F5F5F5;
    border-radius: 2px;
    padding: 2px 4px;
    font-size: 10px;
    color: #666666;
    margin-left: 8px;
    line-height: 1;
    border: none;
  }
  
  // 借阅占比标签分类色
  .borrow-ratio-计算机科学 {
    background-color: rgba(64, 128, 255, 0.1);
    color: #4080FF;
  }
  
  .borrow-ratio-文学小说 {
    background-color: rgba(135, 210, 147, 0.1);
    color: #87D293;
  }
  
  .borrow-ratio-历史传记 {
    background-color: rgba(245, 158, 11, 0.1);
    color: #F59E0B;
  }
  
  .borrow-ratio-科学技术 {
    background-color: rgba(239, 68, 68, 0.1);
    color: #EF4444;
  }
  
  .borrow-ratio-社会科学 {
    background-color: rgba(139, 92, 246, 0.1);
    color: #8B5CF6;
  }
  
  .borrow-ratio-经济管理 {
    background-color: rgba(6, 182, 212, 0.1);
    color: #06B6D4;
  }
  
  .borrow-ratio-艺术设计 {
    background-color: rgba(236, 72, 153, 0.1);
    color: #EC4899;
  }
  
  .borrow-ratio-语言学习 {
    background-color: rgba(16, 185, 129, 0.1);
    color: #10B981;
  }
  
  // 表格行高和列间距调整
  .el-table {
    .el-table__row {
      line-height: 22px;
    }
    
    .el-table__cell {
      padding: 10px 13px; // 加宽列间距 3px
    }
  }
  
  // 近7天借阅需求提示
  .recent-demand {
    background-color: rgba(255, 165, 0, 0.1);
    font-size: 11px;
    color: #FF8C00;
    margin-top: 4px;
    padding: 2px 6px;
    border-radius: 3px;
    display: inline-block;
  }
  
  // 布局间距调整
  .el-row {
    margin-bottom: 15px;
  }
  
  // 统计行样式
  .stat-row {
    margin-top: 0;
    margin-bottom: 15px;
  }
  
  // 图表标题样式 - 添加下边框
  .chart-title {
    font-size: 16px;
    font-weight: bold;
    color: #2D3748;
    margin-bottom: 16px;
    padding-bottom: 8px;
    border-bottom: 1px solid #f0f0f0;
  }
  
  // 催还按钮样式
  .remind-btn {
    background-color: rgba(255, 102, 102, 0.1);
    border-color: rgba(255, 102, 102, 0.3);
    border-radius: 4px;
    color: #EF4444;
    font-weight: bold;
    transition: all 0.3s ease;
    padding: 4px 8px;
    height: auto;
    font-size: 12px;
  }
  
  .remind-btn:hover {
    background-color: rgba(255, 102, 102, 0.2);
    border-color: rgba(255, 102, 102, 0.5);
    color: #EF4444;
  }
  
  // 库存预警表格行hover效果
  .el-table {
    .el-table__body-wrapper {
      .el-table__row {
        &:hover {
          background-color: rgba(0, 0, 0, 0.02);
        }
      }
    }
  }
}
</style>
