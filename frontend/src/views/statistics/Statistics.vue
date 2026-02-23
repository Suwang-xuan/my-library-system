<template>
  <div class="statistics-page">
    <div class="page-header">
      <h2 class="page-title">数据统计</h2>
    </div>

    <el-row :gutter="20">
      <el-col :span="12">
        <div class="chart-container">
          <div class="chart-title">分类借阅统计</div>
          <div ref="categoryChartRef" class="chart"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="chart-container">
          <div class="chart-title">月度借阅趋势</div>
          <div ref="trendChartRef" class="chart"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <div class="chart-container">
          <div class="chart-title">读者行为分析</div>
          <div ref="readerChartRef" class="chart"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="chart-container">
          <div class="chart-title">借阅率分析</div>
          <div ref="borrowRateChartRef" class="chart"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <div class="chart-container">
          <div class="chart-title">热门图书 TOP 10</div>
          <el-table :data="hotBooks" style="width: 100%">
            <el-table-column type="index" label="排名" width="80" align="center" />
            <el-table-column prop="bookName" label="书名" min-width="150" />
            <el-table-column prop="author" label="作者" width="120" />
            <el-table-column prop="borrowCount" label="借阅次数" width="100" align="center" />
          </el-table>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="chart-container">
          <div class="chart-title">借阅率详情</div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="图书总数">{{ borrowRate.totalBooks }}</el-descriptions-item>
            <el-descriptions-item label="总借阅次数">{{ borrowRate.totalBorrowCount }}</el-descriptions-item>
            <el-descriptions-item label="从未借阅">{{ borrowRate.neverBorrowed }}</el-descriptions-item>
            <el-descriptions-item label="频繁借阅(>10次)">{{ borrowRate.frequentlyBorrowed }}</el-descriptions-item>
            <el-descriptions-item label="平均借阅次数">{{ borrowRate.avgBorrowCount }}</el-descriptions-item>
            <el-descriptions-item label="借阅率">{{ borrowRate.borrowRate }}%</el-descriptions-item>
          </el-descriptions>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <div class="chart-container">
          <div class="chart-title">读者行为统计</div>
          <el-descriptions :column="4" border>
            <el-descriptions-item label="读者总数">{{ readerBehavior.totalReaders }}</el-descriptions-item>
            <el-descriptions-item label="学生数量">{{ readerBehavior.studentCount }}</el-descriptions-item>
            <el-descriptions-item label="教师数量">{{ readerBehavior.teacherCount }}</el-descriptions-item>
            <el-descriptions-item label="总借阅次数">{{ readerBehavior.totalBorrows }}</el-descriptions-item>
            <el-descriptions-item label="学生借阅次数">{{ readerBehavior.studentBorrows }}</el-descriptions-item>
            <el-descriptions-item label="教师借阅次数">{{ readerBehavior.teacherBorrows }}</el-descriptions-item>
            <el-descriptions-item label="人均学生借阅">{{ readerBehavior.avgStudentBorrow }}</el-descriptions-item>
            <el-descriptions-item label="人均教师借阅">{{ readerBehavior.avgTeacherBorrow }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { statisticsApi } from '@/api'

const categoryChartRef = ref(null)
const trendChartRef = ref(null)
const readerChartRef = ref(null)
const borrowRateChartRef = ref(null)

let categoryChart = null
let trendChart = null
let readerChart = null
let borrowRateChart = null

const hotBooks = ref([])
const categoryData = ref([])
const trendData = ref([])
const readerBehavior = ref({})
const borrowRate = ref({})

const initCategoryChart = (data) => {
  if (!categoryChartRef.value) return
  categoryChart = echarts.init(categoryChartRef.value)
  const option = {
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', right: '5%', top: 'center' },
    series: [{
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['35%', '50%'],
      data: data.map(item => ({ name: item.categoryName, value: item.borrowCount })),
      emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' } }
    }]
  }
  categoryChart.setOption(option)
}

const initTrendChart = (data) => {
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)
  const option = {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: data.map(item => item.monthName) },
    yAxis: { type: 'value' },
    series: [{
      data: data.map(item => item.count),
      type: 'bar',
      itemStyle: { color: '#409EFF' }
    }]
  }
  trendChart.setOption(option)
}

const initReaderChart = (data) => {
  if (!readerChartRef.value) return
  readerChart = echarts.init(readerChartRef.value)
  const option = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['学生', '教师'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: ['借阅次数'] },
    yAxis: { type: 'value' },
    series: [
      { name: '学生', type: 'bar', data: [data.studentBorrows], itemStyle: { color: '#409EFF' } },
      { name: '教师', type: 'bar', data: [data.teacherBorrows], itemStyle: { color: '#67C23A' } }
    ]
  }
  readerChart.setOption(option)
}

const initBorrowRateChart = (data) => {
  if (!borrowRateChartRef.value) return
  borrowRateChart = echarts.init(borrowRateChartRef.value)
  const option = {
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: ['45%', '70%'],
      data: [
        { name: '频繁借阅', value: data.frequentlyBorrowed },
        { name: '普通借阅', value: data.totalBooks - data.neverBorrowed - data.frequentlyBorrowed },
        { name: '从未借阅', value: data.neverBorrowed }
      ],
      emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' } }
    }]
  }
  borrowRateChart.setOption(option)
}

const loadData = () => {
  // 使用模拟数据，不依赖后端API
  
  // 模拟分类借阅统计数据
  const categoryData = [
    { categoryName: '计算机科学', borrowCount: 580 },
    { categoryName: '文学小说', borrowCount: 420 },
    { categoryName: '历史传记', borrowCount: 280 },
    { categoryName: '科学技术', borrowCount: 350 },
    { categoryName: '社会科学', borrowCount: 220 },
    { categoryName: '经济管理', borrowCount: 180 },
    { categoryName: '艺术设计', borrowCount: 150 },
    { categoryName: '语言学习', borrowCount: 200 }
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
  
  // 模拟热门图书数据
  const hotBooksData = [
    { bookName: 'Java核心技术', author: 'Cay S. Horstmann', borrowCount: 120 },
    { bookName: 'Python编程从入门到实践', author: 'Eric Matthes', borrowCount: 95 },
    { bookName: '算法导论', author: 'Thomas H. Cormen', borrowCount: 88 },
    { bookName: '深入理解计算机系统', author: 'Randal E. Bryant', borrowCount: 75 },
    { bookName: '设计模式', author: 'Erich Gamma', borrowCount: 68 },
    { bookName: '百年孤独', author: '加西亚·马尔克斯', borrowCount: 65 },
    { bookName: 'JavaScript高级程序设计', author: 'Nicholas C. Zakas', borrowCount: 62 },
    { bookName: '人类简史', author: '尤瓦尔·赫拉利', borrowCount: 58 },
    { bookName: '操作系统概念', author: 'Abraham Silberschatz', borrowCount: 55 },
    { bookName: '计算机网络', author: 'Andrew S. Tanenbaum', borrowCount: 50 }
  ]
  
  // 模拟读者行为数据
  const readerBehaviorData = {
    totalReaders: 2000,
    studentCount: 1500,
    teacherCount: 500,
    totalBorrows: 12000,
    studentBorrows: 9000,
    teacherBorrows: 3000,
    avgStudentBorrow: 6,
    avgTeacherBorrow: 6
  }
  
  // 模拟借阅率数据
  const borrowRateData = {
    totalBooks: 1500,
    totalBorrowCount: 12000,
    neverBorrowed: 300,
    frequentlyBorrowed: 500,
    avgBorrowCount: 8,
    borrowRate: 80
  }
  
  // 更新数据
  categoryData.value = categoryData
  trendData.value = trendData
  hotBooks.value = hotBooksData
  readerBehavior.value = readerBehaviorData
  borrowRate.value = borrowRateData
  
  // 初始化图表
  initCategoryChart(categoryData)
  initTrendChart(trendData)
  initReaderChart(readerBehaviorData)
  initBorrowRateChart(borrowRateData)
}

const handleResize = () => {
  categoryChart?.resize()
  trendChart?.resize()
  readerChart?.resize()
  borrowRateChart?.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  categoryChart?.dispose()
  trendChart?.dispose()
  readerChart?.dispose()
  borrowRateChart?.dispose()
})
</script>

<style lang="scss" scoped>
.statistics-page {
  .page-header {
    margin-bottom: 24px;
    
    .page-title {
      font-size: 22px;
      font-weight: 600;
      color: #303133;
      margin: 0;
    }
  }

  .chart-container {
    background: #fff;
    border-radius: 8px;
    padding: 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    transition: all 0.3s ease;
    height: 100%;
    min-height: 350px;
    
    &:hover {
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
    }

    .chart-title {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 20px;
      padding-bottom: 12px;
      border-bottom: 2px solid #409EFF;
    }

    .chart {
      height: 300px;
      margin-top: 16px;
    }
  }

  .el-row {
    margin-bottom: 24px;
    
    &:last-child {
      margin-bottom: 0;
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
        }
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
        border-right: 1px solid #EBEEF5;
        border-bottom: 1px solid #EBEEF5;
      }
      
      .el-descriptions__content {
        color: #606266;
        border-bottom: 1px solid #EBEEF5;
        font-weight: 500;
        
        &:nth-child(even) {
          background-color: #fcfcfc;
        }
      }
    }
  }

  .stat-card {
    background: #fff;
    border-radius: 8px;
    padding: 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    transition: all 0.3s ease;
    margin-bottom: 20px;
    
    &:hover {
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
      transform: translateY(-2px);
    }
  }

  .loading-container {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 300px;
    background-color: #fafafa;
    border-radius: 6px;
  }
}
</style>
