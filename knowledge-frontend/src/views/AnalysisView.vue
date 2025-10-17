<template>
  <div class="analysis-container">
    <!-- 核心展示内容：知识资产数据 -->
    <div class="core-section">
      <h2 class="section-title">知识资产概况</h2>

      <!-- 基础资产总量 -->
      <el-row :gutter="20" class="asset-summary">
        <el-col :span="8">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-item">
              <div class="stat-icon notes">
                <el-icon><Document /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ assetData.notesCount }}</div>
                <div class="stat-label">笔记总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-item">
              <div class="stat-icon tags">
                <el-icon><Tickets /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ assetData.tagsCount }}</div>
                <div class="stat-label">标签总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-item">
              <div class="stat-icon categories">
                <el-icon><Grid /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ assetData.categoriesCount }}</div>
                <div class="stat-label">分类总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 资产增长趋势 -->
      <el-card class="trend-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span class="card-title">资产增长趋势</span>
            <div class="time-filter">
              <el-radio-group v-model="timeRange" @change="handleTimeRangeChange">
                <el-radio-button label="week">周度</el-radio-button>
                <el-radio-button label="month">月度</el-radio-button>
                <el-radio-button label="year">年度</el-radio-button>
              </el-radio-group>
            </div>
          </div>
        </template>
        <div class="chart-container">
          <!-- 简化：直接渲染图表容器 -->
          <div ref="chartContainer" class="chart" style="height: 300px;"></div>
          <div v-if="!chartLoaded" class="chart-loading">
            <el-icon class="loading-icon"><Loading /></el-icon>
            <span>图表加载中...</span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 关键展示内容：用户使用行为数据 -->
    <div class="behavior-section">
      <h2 class="section-title">使用行为分析</h2>

      <el-row :gutter="20">
        <!-- 高频互动标签 -->
        <el-col :span="12">
          <el-card class="behavior-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span class="card-title">高频互动标签 TOP5</span>
              </div>
            </template>
            <div class="tags-list">
              <div
                  v-for="(tag, index) in behaviorData.topTags"
                  :key="tag.name"
                  class="tag-item"
              >
                <div class="tag-rank">
                  <span class="rank-number">{{ index + 1 }}</span>
                </div>
                <div class="tag-info">
                  <span class="tag-name">{{ tag.name }}</span>
                  <span class="tag-count">{{ tag.count }} 篇笔记</span>
                </div>
                <div class="tag-progress">
                  <el-progress
                      :percentage="tag.percentage"
                      :show-text="false"
                      :color="getProgressColor(index)"
                  />
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 近期录入量统计 -->
        <el-col :span="12">
          <el-card class="behavior-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span class="card-title">近期录入量统计</span>
              </div>
            </template>
            <div class="recent-stats">
              <div class="recent-item">
                <div class="recent-icon">
                  <el-icon><EditPen /></el-icon>
                </div>
                <div class="recent-content">
                  <div class="recent-value">{{ behaviorData.recentNotes }}</div>
                  <div class="recent-label">近30天录入笔记</div>
                  <div
                      class="recent-trend"
                      :class="behaviorData.notesTrend >= 0 ? 'positive' : 'negative'"
                  >
                    <el-icon>
                      <CaretTop v-if="behaviorData.notesTrend >= 0" />
                      <CaretBottom v-else />
                    </el-icon>
                    {{ Math.abs(behaviorData.notesTrend) }}%
                  </div>
                </div>
              </div>
              <div class="recent-item">
                <div class="recent-icon">
                  <el-icon><Lightning /></el-icon>
                </div>
                <div class="recent-content">
                  <div class="recent-value">{{ behaviorData.recentInspirations }}</div>
                  <div class="recent-label">近30天灵感便签</div>
                  <div
                      class="recent-trend"
                      :class="behaviorData.inspirationsTrend >= 0 ? 'positive' : 'negative'"
                  >
                    <el-icon>
                      <CaretTop v-if="behaviorData.inspirationsTrend >= 0" />
                      <CaretBottom v-else />
                    </el-icon>
                    {{ Math.abs(behaviorData.inspirationsTrend) }}%
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 沉睡笔记提醒 -->
      <el-card class="sleeping-notes-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span class="card-title">沉睡笔记提醒</span>
            <span class="sleeping-count">{{ behaviorData.sleepingNotes.length }} 篇沉睡笔记</span>
          </div>
        </template>
        <div class="sleeping-notes-list">
          <div
              v-for="note in behaviorData.sleepingNotes"
              :key="note.id"
              class="sleeping-note-item"
          >
            <div class="note-info">
              <div class="note-title">{{ note.title }}</div>
              <div class="note-meta">
                <span>最后访问：{{ formatSleepingDate(note.lastAccessed) }}</span>
                <span>沉睡：{{ note.sleepingDays }} 天</span>
              </div>
            </div>
            <el-button
                type="primary"
                size="small"
                @click="viewNote(note.id)"
            >
              查看
            </el-button>
          </div>
          <div v-if="behaviorData.sleepingNotes.length === 0" class="empty-sleeping">
            <el-icon><SuccessFilled /></el-icon>
            <span>恭喜！当前没有沉睡笔记</span>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import request from '../utils/request'
import {
  Document,
  Tickets,
  Grid,
  EditPen,
  Lightning,
  CaretTop,
  CaretBottom,
  SuccessFilled,
  Loading
} from '@element-plus/icons-vue'

const router = useRouter()

// 时间范围选择
const timeRange = ref('month')

// 知识资产数据
const assetData = ref({
  notesCount: 0,
  tagsCount: 0,
  categoriesCount: 0,
  growthData: []
})

// 用户行为数据
const behaviorData = ref({
  topTags: [],
  recentNotes: 0,
  recentInspirations: 0,
  notesTrend: 0,
  inspirationsTrend: 0,
  sleepingNotes: []
})

// 图表相关状态 - 简化版本
const chartContainer = ref(null)
const chartInstance = ref(null)
const chartLoaded = ref(false)

// 监听 growthData 变化
watch(() => assetData.value.growthData, (newData) => {
  if (newData && newData.length > 0) {
    console.log('数据变化，准备更新图表...')
    nextTick(() => {
      updateChart()
    })
  }
}, { deep: true })

// 初始化图表 - 简化版本
const initChart = () => {
  console.log('开始初始化图表...')

  if (!chartContainer.value) {
    console.error('图表容器不存在')
    return false
  }

  try {
    // 销毁现有实例
    if (chartInstance.value) {
      chartInstance.value.dispose()
    }

    // 初始化图表
    chartInstance.value = echarts.init(chartContainer.value)
    console.log('图表初始化成功')

    // 如果有数据，立即渲染
    if (assetData.value.growthData && assetData.value.growthData.length > 0) {
      updateChart()
    }

    return true
  } catch (error) {
    console.error('图表初始化失败:', error)
    return false
  }
}

// 更新图表数据 - 简化版本
const updateChart = () => {
  console.log('更新图表数据...')

  if (!chartInstance.value) {
    console.warn('图表实例不存在，尝试初始化...')
    initChart()
    return
  }

  const growthData = assetData.value.growthData
  if (!growthData || growthData.length === 0) {
    console.warn('没有可用的增长数据')
    return
  }

  try {
    const option = {
      tooltip: {
        trigger: 'axis',
        formatter: function(params) {
          const date = params[0].axisValue
          const value = params[0].value
          return `${date}<br/>新增笔记: ${value} 篇`
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: growthData.map(item => item.date)
      },
      yAxis: {
        type: 'value',
        name: '新增笔记数'
      },
      series: [
        {
          name: '新增笔记',
          type: 'line',
          smooth: true,
          lineStyle: {
            color: '#165DFF',
            width: 3
          },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [{
                offset: 0,
                color: 'rgba(22, 93, 255, 0.3)'
              }, {
                offset: 1,
                color: 'rgba(22, 93, 255, 0.05)'
              }]
            }
          },
          data: growthData.map(item => item.count)
        }
      ]
    }

    chartInstance.value.setOption(option)
    chartLoaded.value = true
    console.log('图表数据更新成功')

    // 触发重绘
    nextTick(() => {
      chartInstance.value && chartInstance.value.resize()
    })
  } catch (error) {
    console.error('图表数据更新失败:', error)
  }
}

// 备选方案：使用 v-chart 的方式（如果上述方法仍不行）
const initChartWithTimeout = () => {
  // 使用 setTimeout 确保 DOM 完全渲染
  setTimeout(() => {
    if (!chartContainer.value) {
      console.error('图表容器仍未找到')
      return
    }

    try {
      if (chartInstance.value) {
        chartInstance.value.dispose()
      }

      chartInstance.value = echarts.init(chartContainer.value)
      console.log('延迟初始化图表成功')

      if (assetData.value.growthData && assetData.value.growthData.length > 0) {
        updateChart()
      }
    } catch (error) {
      console.error('延迟初始化图表失败:', error)
    }
  }, 100)
}

// 获取进度条颜色
const getProgressColor = (index) => {
  const colors = ['#165DFF', '#0FC6C2', '#722ED1', '#F7BA1E', '#F53F3F']
  return colors[index] || '#165DFF'
}

// 格式化沉睡日期
const formatSleepingDate = (dateString) => {
  if (!dateString) return '未知'
  // 处理后端返回的LocalDateTime格式（如：2024-01-15T10:30:00）
  const date = new Date(dateString.replace('T', ' '))
  return date.toLocaleDateString('zh-CN')
}


// 查看笔记
const viewNote = (noteId) => {
  router.push(`/editor/${noteId}`)
}

// 处理时间范围变化
const handleTimeRangeChange = () => {
  fetchGrowthData()
}

// 获取增长数据
const fetchGrowthData = async () => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('user') || '{}')
    if (!userInfo.id) {
      console.error('未获取到用户信息')
      return
    }

    const response = await request.get('/analysis/growth', {
      params: {
        userId: userInfo.id,
        range: timeRange.value
      }
    })

    if (response.data.code === 200) {
      // 直接使用后端返回的真实数据
      assetData.value.growthData = response.data.data.growthData
      console.log('获取增长数据成功:', assetData.value.growthData)

      // 数据获取后确保图表更新
      nextTick(() => {
        if (!chartInstance.value) {
          initChart()
        } else {
          updateChart()
        }
      })
    }
  } catch (error) {
    console.error('获取增长数据失败:', error)
    // 出错时仍使用模拟数据
    assetData.value.growthData = generateMockGrowthData()
    nextTick(() => {
      if (!chartInstance.value) {
        initChart()
      } else {
        updateChart()
      }
    })
  }
}


// 获取资产概况
const fetchAssetSummary = async () => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('user') || '{}')
    if (!userInfo.id) {
      console.error('未获取到用户信息')
      return
    }

    const response = await request.get('/analysis/summary', {
      params: { userId: userInfo.id }
    })

    if (response.data.code === 200) {
      const data = response.data.data
      assetData.value.notesCount = data.notesCount || 0
      assetData.value.tagsCount = data.tagsCount || 0
      assetData.value.categoriesCount = data.categoriesCount || 0
    }
  } catch (error) {
    console.error('获取资产概况失败:', error)
    // 使用模拟数据
    assetData.value.notesCount = 120
    assetData.value.tagsCount = 35
    assetData.value.categoriesCount = 12
  }
}

// 获取行为数据
const fetchBehaviorData = async () => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('user') || '{}')
    if (!userInfo.id) {
      console.error('未获取到用户信息')
      return
    }

    const response = await request.get('/analysis/behavior', {
      params: { userId: userInfo.id }
    })

    if (response.data.code === 200) {
      // 处理沉睡笔记的日期和天数
      const sleepingNotes = response.data.data.sleepingNotes.map(note => {
        // 计算沉睡天数
        const lastAccessed = new Date(note.lastAccessed.replace('T', ' '))
        const today = new Date()
        const sleepingDays = Math.floor((today - lastAccessed) / (1000 * 60 * 60 * 24))

        return {
          ...note,
          sleepingDays,
          lastAccessed: note.lastAccessed.split('T')[0] // 只保留日期部分
        }
      })

      behaviorData.value = {
        ...response.data.data,
        sleepingNotes
      }
    }
  } catch (error) {
    console.error('获取行为数据失败:', error)

  }
}

// 生成模拟增长数据
const generateMockGrowthData = () => {
  const ranges = {
    week: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
    month: ['1月', '2月', '3月', '4月', '5月', '6月'],
    year: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
  }

  const data = ranges[timeRange.value] || ranges.month
  return data.map((date) => ({
    date,
    count: Math.floor(Math.random() * 20) + 10
  }))
}

// 窗口大小变化时重绘图表
const handleResize = () => {
  if (chartInstance.value) {
    chartInstance.value.resize()
  }
}

onMounted(async () => {
  console.log('组件挂载，开始初始化...')

  // 先获取数据
  await Promise.all([
    fetchAssetSummary(),
    fetchGrowthData(),
    fetchBehaviorData()
  ])

  console.log('数据获取完成，准备初始化图表...')

  // 使用双重保障：立即初始化和延迟初始化
  initChart() // 立即尝试

  // 如果立即初始化失败，延迟再次尝试
  setTimeout(() => {
    if (!chartInstance.value) {
      console.log('立即初始化失败，尝试延迟初始化...')
      initChartWithTimeout()
    }
  }, 500)

  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (chartInstance.value) {
    chartInstance.value.dispose()
    chartInstance.value = null
  }
  window.removeEventListener('resize', handleResize)
})
</script>


<style scoped>
.analysis-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 20px;
  padding-left: 8px;
  border-left: 4px solid #165DFF;
}

/* 资产概况样式 */
.asset-summary {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 12px;
  border: none;
}

.stat-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 24px;
  color: white;
}

.stat-icon.notes {
  background: linear-gradient(135deg, #165DFF, #0E42D2);
}

.stat-icon.tags {
  background: linear-gradient(135deg, #00B42A, #009A29);
}

.stat-icon.categories {
  background: linear-gradient(135deg, #722ED1, #531DAB);
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
  margin-top: 4px;
}

/* 趋势图表样式 */
.trend-card {
  border-radius: 12px;
  margin-bottom: 32px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.chart-container {
  width: 100%;
  min-height: 300px;
  position: relative;
}

.chart {
  width: 100%;
  height: 300px;
}

.chart-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  color: #909399;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: white;
  z-index: 10;
}

.loading-icon {
  font-size: 32px;
  margin-bottom: 8px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}


/* 行为分析样式 */
.behavior-section {
  margin-top: 32px;
}

.behavior-card {
  border-radius: 12px;
  margin-bottom: 20px;
}

/* 高频标签样式 */
.tags-list {
  padding: 8px 0;
}

.tag-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f3f4f6;
}

.tag-item:last-child {
  border-bottom: none;
}

.tag-rank {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background-color: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
}

.rank-number {
  font-size: 14px;
  font-weight: 600;
  color: #6b7280;
}

.tag-info {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-right: 12px;
}

.tag-name {
  font-weight: 500;
  color: #1f2937;
}

.tag-count {
  font-size: 12px;
  color: #9ca3af;
}

.tag-progress {
  width: 80px;
}

/* 近期统计样式 */
.recent-stats {
  padding: 8px 0;
}

.recent-item {
  display: flex;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #f3f4f6;
}

.recent-item:last-child {
  border-bottom: none;
}

.recent-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background-color: #f0f5ff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  color: #165DFF;
  font-size: 20px;
}

.recent-content {
  flex: 1;
}

.recent-value {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.2;
}

.recent-label {
  font-size: 14px;
  color: #6b7280;
  margin: 4px 0;
}

.recent-trend {
  display: inline-flex;
  align-items: center;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
}

.recent-trend.positive {
  background-color: #f6ffed;
  color: #52c41a;
}

.recent-trend.negative {
  background-color: #fff2f0;
  color: #ff4d4f;
}

/* 沉睡笔记样式 */
.sleeping-notes-card {
  border-radius: 12px;
}

.sleeping-count {
  font-size: 14px;
  color: #f59e0b;
  font-weight: 500;
}

.sleeping-notes-list {
  padding: 8px 0;
}

.sleeping-note-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border: 1px solid #f3f4f6;
  border-radius: 8px;
  margin-bottom: 12px;
  transition: all 0.2s;
}

.sleeping-note-item:hover {
  border-color: #165DFF;
  box-shadow: 0 2px 8px rgba(22, 93, 255, 0.1);
}

.sleeping-note-item:last-child {
  margin-bottom: 0;
}

.note-title {
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 4px;
}

.note-meta {
  font-size: 12px;
  color: #9ca3af;
}

.note-meta span {
  margin-right: 16px;
}

.empty-sleeping {
  text-align: center;
  padding: 40px 20px;
  color: #9ca3af;
}

.empty-sleeping .el-icon {
  font-size: 48px;
  margin-bottom: 12px;
  display: block;
  margin: 0 auto 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .analysis-container {
    padding: 16px;
  }

  .asset-summary .el-col {
    margin-bottom: 16px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .time-filter {
    align-self: flex-end;
  }

  .sleeping-note-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .sleeping-note-item .el-button {
    align-self: flex-end;
  }
}

.chart-container {
  width: 100%;
  height: 300px;
  position: relative;
}

.chart {
  width: 100% !important;
  height: 100% !important;
}

.chart-loading {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: rgba(255, 255, 255, 0.9);
  z-index: 10;
}

.chart-error {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #f53f3f;
}

.error-icon {
  font-size: 32px;
  margin-bottom: 8px;
}
</style>