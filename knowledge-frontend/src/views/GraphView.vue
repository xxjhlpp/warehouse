<template>
  <div class="graph-view-container">
    <div class="graph-header">
      <h2>知识图谱</h2>
      <div class="graph-actions">
        <el-button type="primary" @click="handleRefresh">
          <el-icon><Refresh /></el-icon>
          刷新图谱
        </el-button>
        <el-button @click="handleExport">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </div>
    </div>

    <div class="graph-content">
      <!-- 图谱可视化区域 -->
      <div class="graph-visualization" ref="graphContainer">
        <div v-if="loading" class="graph-loading">
          <el-icon class="loading-icon"><Loading /></el-icon>
          <span>正在加载知识图谱...</span>
        </div>
        <div v-else-if="graphData.nodes.length === 0" class="graph-empty">
          <el-icon class="empty-icon"><PieChart /></el-icon>
          <h3>暂无知识图谱数据</h3>
          <p>开始创建分类和笔记来构建您的知识图谱吧</p>
          <el-button type="primary" @click="handleCreateData">
            创建示例数据
          </el-button>
        </div>
        <div v-else class="graph-canvas" id="knowledge-graph" ref="graphCanvas"></div>
      </div>

      <!-- 右侧信息面板 -->
      <div class="graph-sidebar">
        <el-card class="node-info-panel">
          <template #header>
            <div class="panel-header">
              <span>节点信息</span>
            </div>
          </template>
          <!-- 添加滚动容器 -->
          <div class="scrollable-content">
            <div v-if="selectedNode" class="node-details">
              <div class="node-type">{{ selectedNode.type }}</div>
              <h4 class="node-name">{{ selectedNode.name }}</h4>
              <div v-if="selectedNode.description" class="node-description">
                {{ selectedNode.description }}
              </div>
              <!-- 显示分类关系信息 -->
              <div v-if="selectedNode.type === '分类' && relatedCategoryRelations.length > 0" class="category-relations">
                <div class="relations-header" @click="isRelationsExpanded = !isRelationsExpanded">
                  <h5>相关分类关系 ({{ relatedCategoryRelations.length }}个)</h5>
                  <el-icon :class="['expand-icon', { expanded: isRelationsExpanded }]">
                    <ArrowDown />
                  </el-icon>
                </div>
                <div v-show="isRelationsExpanded" class="relations-list">
                  <div
                      v-for="rel in relatedCategoryRelations"
                      :key="rel.id"
                      class="relation-item"
                  >
                    <span class="relation-source">{{ rel.sourceName }}</span>
                    <span class="relation-text">{{ rel.relationText }}</span>
                    <span class="relation-target">{{ rel.targetName }}</span>
                  </div>
                </div>
              </div>
              <div class="node-stats">
                <div class="stat-item">
                  <span class="stat-label">关联数量:</span>
                  <span class="stat-value">{{ selectedNode.linkCount || 0 }}</span>
                </div>
              </div>
              <div class="node-actions">
                <el-button size="small" @click="handleExploreRelated">
                  探索关联
                </el-button>
              </div>
            </div>
            <div v-else class="no-selection">
              <el-icon><InfoFilled /></el-icon>
              <p>点击图谱中的节点查看详细信息</p>
            </div>
          </div>
        </el-card>

        <el-card class="graph-controls">
          <template #header>
            <div class="panel-header">
              <span>图谱控制</span>
            </div>
          </template>
          <div class="control-content">
            <div class="control-group">
              <label>布局算法:</label>
              <el-select v-model="layoutAlgorithm" size="small" @change="handleLayoutChange">
                <el-option label="力导向布局" value="force"></el-option>
                <el-option label="圆形布局" value="circular"></el-option>
              </el-select>
            </div>
            <div class="control-group">
              <label>节点大小:</label>
              <el-slider v-model="nodeSize" :min="10" :max="50" @change="handleNodeSizeChange"></el-slider>
            </div>
            <div class="control-group">
              <label>连线长度:</label>
              <el-slider v-model="linkDistance" :min="50" :max="300" @change="handleLinkDistanceChange"></el-slider>
            </div>
            <!-- 添加关系显示控制 -->
            <div class="control-group">
              <el-checkbox v-model="showRelationLabels" @change="handleRelationLabelChange">
                显示关系标签
              </el-checkbox>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, onMounted, onUnmounted, nextTick, computed, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {Refresh, Download, Loading, PieChart, InfoFilled, ArrowDown} from '@element-plus/icons-vue'
import request from '@/utils/request'
import * as echarts from 'echarts'

// 状态管理
const loading = ref(false)
const graphData = ref({
  nodes: [],
  links: []
})
const selectedNode = ref(null)
const layoutAlgorithm = ref('force')
const nodeSize = ref(20)
const linkDistance = ref(150)
const graphInstance = ref(null)
const showRelationLabels = ref(true)
const relatedCategoryRelations = ref([])
const isRelationsExpanded = ref(true)

// 图谱容器引用
const graphContainer = ref(null)
const graphCanvas = ref(null)

// 计算最高级父级分类
const topLevelCategories = computed(() => {
  const categories = graphData.value.nodes.filter(node => node.type === '分类')
  const parentChildLinks = graphData.value.links.filter(link =>
      link.relationType === 'parent-child'
  )

  const hasParent = new Set()
  parentChildLinks.forEach(link => {
    hasParent.add(String(link.target))
  })

  return categories.filter(category => !hasParent.has(String(category.id)))
})

// 生命周期
onMounted(() => {
  fetchGraphData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (graphInstance.value) {
    graphInstance.value.dispose()
  }
})

// 监听数据变化，确保图谱正确初始化
watch(() => graphData.value.nodes, (newNodes) => {
  if (newNodes && newNodes.length > 0) {
    nextTick(() => {
      setTimeout(() => {
        initGraphVisualization()
      }, 100)
    })
  }
})

// 获取图谱数据
const fetchGraphData = async () => {
  loading.value = true
  try {
    const userInfo = JSON.parse(localStorage.getItem('user') || '{}')
    if (!userInfo.id) {
      ElMessage.warning('请先登录')
      return
    }

    const res = await request.get('/knowledge/graph', {
      params: {
        userId: userInfo.id,
        includeCategoryRelations: true
      }
    })

    if (res.data && res.data.code === 200) {
      graphData.value = res.data.data || {nodes: [], links: []}
      console.log('原始数据:', JSON.parse(JSON.stringify(graphData.value)))
      validateAndCleanData()
      // 不在这里初始化图谱，通过watch监听数据变化来初始化
    } else {
      ElMessage.error(res.data?.msg || '获取知识图谱失败')
    }
  } catch (error) {
    console.error('获取知识图谱错误:', error)
    ElMessage.error('获取知识图谱失败')
  } finally {
    loading.value = false
  }
}

// 彻底修复数据格式问题
const validateAndCleanData = () => {
  if (!graphData.value.nodes) graphData.value.nodes = []
  if (!graphData.value.links) graphData.value.links = []

  // 为每个节点添加唯一ID和必要的属性
  graphData.value.nodes = graphData.value.nodes.map((node, index) => {
    const nodeId = String(node.id || `node_${index}`)
    const isTopLevel = topLevelCategories.value.some(topCat =>
        String(topCat.id) === nodeId
    )

    return {
      ...node,
      id: nodeId,
      name: node.name || `节点${index}`,
      symbolSize: nodeSize.value,
      itemStyle: {
        color: getNodeColorByType(node.type, isTopLevel)
      },
      label: {
        show: true,
        position: 'right',
        formatter: '{b}',
        fontSize: 12
      },
      category: node.type,
      isTopLevel: isTopLevel
    }
  })

  // 创建节点ID映射
  const nodeIdMap = new Map()
  graphData.value.nodes.forEach(node => {
    nodeIdMap.set(node.id, node)
  })

  // 彻底清理连线数据
  const validLinks = []
  graphData.value.links.forEach((link, index) => {
    const sourceId = String(link.source)
    const targetId = String(link.target)

    // 检查source和target是否存在
    if (nodeIdMap.has(sourceId) && nodeIdMap.has(targetId)) {
      validLinks.push({
        ...link,
        id: `link_${index}`,
        source: sourceId,
        target: targetId
      })
    } else {
      console.warn('过滤无效连线:', link, '原因: 节点不存在')
    }
  })

  graphData.value.links = validLinks

  console.log('清理后的数据:', {
    nodes: graphData.value.nodes,
    links: graphData.value.links,
    topLevelCategories: topLevelCategories.value
  })
}

// 重新设计图谱可视化初始化
const initGraphVisualization = () => {
  if (!graphCanvas.value) {
    console.error('图谱容器未找到')
    return
  }

  // 确保容器有正确的尺寸
  setGraphSize()

  if (graphInstance.value) {
    graphInstance.value.dispose()
  }

  graphInstance.value = echarts.init(graphCanvas.value)

  const nodes = JSON.parse(JSON.stringify(graphData.value.nodes))
  const links = JSON.parse(JSON.stringify(graphData.value.links))

  if (nodes.length === 0) {
    return
  }

  // 处理连线数据
  const processedLinks = processLinks(nodes, links)

  // 使用最简化的配置
  const option = getSimplifiedGraphOption(nodes, processedLinks)

  try {
    graphInstance.value.setOption(option, true)

    // 绑定事件
    graphInstance.value.off('click')
    graphInstance.value.on('click', function (params) {
      if (params.dataType === 'node') {
        handleNodeClick(params.data, nodes, processedLinks)
      }
    })

    // 确保图表正确渲染
    setTimeout(() => {
      graphInstance.value.resize()
    }, 50)

  } catch (error) {
    console.error('图表渲染错误:', error)
    ElMessage.error('图表渲染失败，请检查数据格式')

    // 使用最基本的配置重试
    try {
      const basicOption = getBasicGraphOption(nodes, processedLinks)
      graphInstance.value.setOption(basicOption, true)
    } catch (basicError) {
      console.error('基础图表渲染也失败:', basicError)
    }
  }
}

// 处理连线数据 - 简化版本
const processLinks = (nodes, links) => {
  const nodeIdMap = new Map()
  nodes.forEach(node => nodeIdMap.set(node.id, node))

  return links.map((link, index) => {
    const sourceNode = nodeIdMap.get(link.source)
    const targetNode = nodeIdMap.get(link.target)

    if (!sourceNode || !targetNode) {
      return null
    }

    // 基本连线样式
    const lineStyle = {
      width: 1,
      color: '#ccc',
      curveness: 0.1
    }

    // 分类间关系特殊样式
    if (sourceNode.type === '分类' && targetNode.type === '分类') {
      const isParentChild = link.relationType === 'parent-child'
      Object.assign(lineStyle, {
        width: isParentChild ? 3 : 2,
        color: isParentChild ? '#3b82f6' : '#8b5cf6',
        type: isParentChild ? 'solid' : 'dashed'
      })
    }

    const safeLink = {
      id: link.id || `link_${index}`,
      source: link.source,
      target: link.target,
      lineStyle: lineStyle
    }

    // 添加标签
    if (showRelationLabels.value) {
      safeLink.label = {
        show: true,
        formatter: link.label || (link.relationType === 'parent-child' ? '父子关系' : '关联'),
        fontSize: 10
      }
    }

    return safeLink
  }).filter(link => link !== null)
}

// 最简化的图表配置
const getSimplifiedGraphOption = (nodes, links) => {
  const baseOption = {
    animation: true,
    tooltip: {
      trigger: 'item',
      formatter: function (params) {
        if (params.dataType === 'node') {
          const topLevelText = params.data.isTopLevel ? ' (顶级分类)' : ''
          return `${params.data.name} - ${params.data.type}${topLevelText}`
        }
        return params.data.label?.formatter || '关联关系'
      }
    },
    series: [{
      type: 'graph',
      layout: layoutAlgorithm.value,
      data: nodes,
      links: links,
      roam: true,
      focusNodeAdjacency: true,
      label: {
        show: true,
        position: 'right',
        fontSize: 12,
        color: '#333'
      },
      lineStyle: {
        color: 'source',
        curveness: 0.1,
        opacity: 0.6
      },
      emphasis: {
        focus: 'adjacency',
        lineStyle: {
          width: 3
        }
      }
    }]
  }

  // 添加布局配置
  const layoutOptions = getLayoutOptions()
  return {
    ...baseOption,
    series: [{
      ...baseOption.series[0],
      ...layoutOptions
    }]
  }
}

// 基础图表配置（备用）
const getBasicGraphOption = (nodes, links) => {
  return {
    series: [{
      type: 'graph',
      layout: 'force',
      data: nodes,
      links: links,
      roam: true,
      label: {
        show: true,
        position: 'right'
      },
      force: {
        repulsion: 100,
        edgeLength: 100
      }
    }]
  }
}

// 获取布局选项
const getLayoutOptions = () => {
  const baseConfig = {
    force: {
      repulsion: 200,
      gravity: 0.1,
      edgeLength: linkDistance.value
    }
  }

  switch (layoutAlgorithm.value) {
    case 'force':
      return baseConfig

    case 'circular':
      return {
        circular: {
          rotateLabel: true
        },
        force: {
          repulsion: 100,
          gravity: 0.3,
          edgeLength: linkDistance.value
        }
      }

    default:
      return baseConfig
  }
}

// 处理节点点击事件
const handleNodeClick = (nodeData, nodes, links) => {
  selectedNode.value = nodeData

  const relatedLinks = links.filter(link =>
      link.source === nodeData.id || link.target === nodeData.id
  )
  selectedNode.value.linkCount = relatedLinks.length

  if (nodeData.type === '分类') {
    relatedCategoryRelations.value = relatedLinks
        .filter(link => {
          const sourceNode = nodes.find(n => n.id === link.source)
          const targetNode = nodes.find(n => n.id === link.target)
          return sourceNode?.type === '分类' && targetNode?.type === '分类'
        })
        .map(link => {
          const sourceNode = nodes.find(n => n.id === link.source)
          const targetNode = nodes.find(n => n.id === link.target)

          // 简化关系文本显示
          let relationText = '关联'
          if (link.relationType === 'parent-child') {
            relationText = link.source === nodeData.id ? '包含子分类' : '属于父分类'
          }

          return {
            ...link,
            sourceName: sourceNode?.name || '未知',
            targetName: targetNode?.name || '未知',
            relationText: relationText
          }
        })
  } else {
    relatedCategoryRelations.value = []
  }
}

// 根据节点类型和是否顶级分类获取颜色
const getNodeColorByType = (type, isTopLevel = false) => {
  const colorMap = {
    '分类': isTopLevel ? '#ef4444' : '#3b82f6',
    '笔记': '#10b981',
    '标签': '#f59e0b',
    '项目': '#8b5cf6',
    '默认': '#64748b'
  }
  return colorMap[type] || colorMap['默认']
}

// 设置图谱尺寸
const setGraphSize = () => {
  if (graphCanvas.value && graphContainer.value) {
    const container = graphContainer.value
    const canvas = graphCanvas.value

    // 确保容器有正确的高度
    const containerHeight = container.clientHeight || 500
    const containerWidth = container.clientWidth || 800

    canvas.style.height = containerHeight + 'px'
    canvas.style.width = containerWidth + 'px'

    if (graphInstance.value) {
      setTimeout(() => {
        graphInstance.value.resize()
      }, 100)
    }
  }
}

const handleRefresh = () => {
  fetchGraphData()
}

const handleExport = () => {
  if (!graphInstance.value) return

  try {
    const imgUrl = graphInstance.value.getDataURL({
      type: 'png',
      pixelRatio: 2,
      backgroundColor: '#fff'
    })

    const link = document.createElement('a')
    link.href = imgUrl
    link.download = '知识图谱_' + new Date().getTime() + '.png'
    link.click()
  } catch (error) {
    console.error('导出图谱失败:', error)
    ElMessage.error('导出图谱失败')
  }
}

const handleCreateData = async () => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('user') || '{}')
    if (!userInfo.id) {
      ElMessage.warning('请先登录')
      return
    }

    const res = await request.post('/knowledge/create-example', {
      userId: userInfo.id,
      includeCategoryRelations: true
    })

    if (res.data && res.data.code === 200) {
      ElMessage.success('示例数据创建成功')
      fetchGraphData()
    } else {
      ElMessage.error(res.data?.msg || '创建示例数据失败')
    }
  } catch (error) {
    console.error('创建示例数据错误:', error)
    ElMessage.error('创建示例数据失败')
  }
}

const handleExploreRelated = () => {
  if (!selectedNode.value) return

  const relatedNodeIds = new Set([selectedNode.value.id])
  const relatedLinks = graphData.value.links.filter(link => {
    if (link.source === selectedNode.value.id) {
      relatedNodeIds.add(link.target)
      return true
    }
    if (link.target === selectedNode.value.id) {
      relatedNodeIds.add(link.source)
      return true
    }
    return false
  })

  const relatedNodes = graphData.value.nodes.filter(node =>
      relatedNodeIds.has(node.id)
  )

  const tempData = {
    nodes: relatedNodes,
    links: relatedLinks
  }

  const originalData = {...graphData.value}
  graphData.value = tempData
  validateAndCleanData()
  initGraphVisualization()

  setTimeout(() => {
    graphData.value = originalData
  }, 5000)
}

const handleRelationLabelChange = () => {
  initGraphVisualization()
}

const handleLayoutChange = () => {
  initGraphVisualization()
}

const handleNodeSizeChange = () => {
  graphData.value.nodes.forEach(node => {
    node.symbolSize = nodeSize.value
  })
  initGraphVisualization()
}

const handleLinkDistanceChange = () => {
  initGraphVisualization()
}

const handleResize = () => {
  setGraphSize()
}
</script>

<style scoped>
.graph-view-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f8fafc;
}

.graph-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: white;
  border-bottom: 1px solid #e2e8f0;
  flex-shrink: 0;
}

.graph-header h2 {
  margin: 0;
  color: #1e293b;
  font-size: 20px;
  font-weight: 600;
}

.graph-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.graph-content {
  flex: 1;
  display: flex;
  overflow: hidden;
  min-height: 0;
}

.graph-visualization {
  flex: 1;
  background: white;
  margin: 16px;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
  min-height: 500px;
}

.graph-sidebar {
  width: 320px;
  min-width: 320px;
  padding: 16px;
  background: #f8fafc;
  border-left: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
  overflow: hidden;
}

.graph-canvas {
  width: 100%;
  height: 100%;
  background: #fafafa;
}

.graph-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #64748b;
}

.loading-icon {
  font-size: 32px;
  margin-bottom: 16px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.graph-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #64748b;
  text-align: center;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.7;
}

.graph-empty h3 {
  margin: 0 0 8px 0;
  color: #334155;
}

.graph-empty p {
  margin: 0 0 16px 0;
  max-width: 300px;
}

.panel-header {
  font-weight: 600;
  color: #1e293b;
}

/* 节点信息面板滚动区域 */
.node-info-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.node-info-panel :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  padding: 0;
}

.scrollable-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  max-height: 400px;
}

.node-details {
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.node-type {
  display: inline-block;
  padding: 4px 10px;
  background: #e0f2fe;
  color: #0369a1;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  align-self: flex-start;
}

.node-name {
  margin: 0;
  color: #1e293b;
  font-size: 18px;
  font-weight: 600;
  line-height: 1.3;
  word-break: break-word;
}

.node-description {
  color: #64748b;
  font-size: 14px;
  margin-bottom: 12px;
  line-height: 1.4;
  max-height: 60px;
  overflow-y: auto;
  padding: 8px;
  background: #f8fafc;
  border-radius: 4px;
  border: 1px solid #f1f5f9;
}

/* 分类关系样式优化 */
.category-relations {
  margin: 10px 0;
  padding: 12px;
  background-color: #f0f9ff;
  border-radius: 6px;
  border: 1px solid #bae6fd;
}

.relations-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  padding: 4px 0;
}

.relations-header h5 {
  margin: 0;
  font-size: 14px;
  color: #0284c7;
  font-weight: 600;
}

.expand-icon {
  transition: transform 0.3s ease;
  color: #64748b;
}

.expand-icon.expanded {
  transform: rotate(180deg);
}

/* 关系列表独立滚动 */
.relations-list {
  max-height: 120px;
  overflow-y: auto;
  margin-top: 8px;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  padding: 8px;
  background: #fafafa;
}

.relation-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 4px 0;
  font-size: 12px;
  border-bottom: 1px solid #f1f5f9;
}

.relation-item:last-child {
  border-bottom: none;
}

.relation-source, .relation-target {
  flex: 1;
  text-align: center;
  padding: 0 4px;
  font-weight: 500;
  color: #334155;
}

.relation-text {
  flex: 0 0 auto;
  padding: 2px 6px;
  background: #3b82f6;
  color: white;
  border-radius: 10px;
  font-size: 10px;
  margin: 0 4px;
}

.node-stats {
  margin: 8px 0;
  padding: 12px;
  background: #f8fafc;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0;
}

.stat-label {
  color: #64748b;
  font-size: 14px;
}

.stat-value {
  color: #1e293b;
  font-weight: 600;
  font-size: 16px;
}

.node-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.no-selection {
  text-align: center;
  color: #94a3b8;
  padding: 40px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.no-selection .el-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.no-selection p {
  margin: 0;
  font-size: 14px;
  line-height: 1.5;
}

/* 图谱控制面板 */
.graph-controls {
  margin-top: auto;
  flex-shrink: 0;
}

.graph-controls :deep(.el-card__body) {
  padding: 16px;
}

.control-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.control-group {
  margin-bottom: 0;
}

.control-group label {
  display: block;
  margin-bottom: 8px;
  color: #475569;
  font-size: 14px;
  font-weight: 500;
}

/* 滚动条样式优化 */
.scrollable-content::-webkit-scrollbar,
.relations-list::-webkit-scrollbar,
.node-description::-webkit-scrollbar {
  width: 6px;
}

.scrollable-content::-webkit-scrollbar-track,
.relations-list::-webkit-scrollbar-track,
.node-description::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 3px;
}

.scrollable-content::-webkit-scrollbar-thumb,
.relations-list::-webkit-scrollbar-thumb,
.node-description::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

.scrollable-content::-webkit-scrollbar-thumb:hover,
.relations-list::-webkit-scrollbar-thumb:hover,
.node-description::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .graph-sidebar {
    width: 280px;
    min-width: 280px;
  }
}

@media (max-width: 768px) {
  .graph-content {
    flex-direction: column;
  }

  .graph-sidebar {
    width: auto;
    min-width: auto;
    border-left: none;
    border-top: 1px solid #e2e8f0;
    height: auto;
    max-height: 50vh;
  }

  .scrollable-content {
    max-height: 200px;
  }
}
</style>