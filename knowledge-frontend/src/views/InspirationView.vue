<template>
  <div class="inspiration-view">
    <div class="view-header">
      <h2>我的灵感便签</h2>
      <el-button
          type="primary"
          class="add-btn"
          @click="showAddDialog = true"
      >
        <el-icon><Plus /></el-icon> 添加便签
      </el-button>
    </div>

    <div class="inspiration-filters">
      <el-input
          v-model="searchKeyword"
          placeholder="搜索便签内容..."
          prefix-icon="Search"
          class="search-input"
          @input="handleSearch"
      ></el-input>

      <el-select
          v-model="sortType"
          placeholder="排序方式"
          class="sort-select"
          @change="handleSort"
      >
        <el-option label="最新创建" value="newest"></el-option>
        <el-option label="最早创建" value="oldest"></el-option>
      </el-select>
    </div>

    <div class="inspiration-grid">
      <div
          class="inspiration-card"
          v-for="item in filteredInspirations"
          :key="item.id"
          :class="{ 'pinned': item.pinned }"
      >
        <div class="card-header">
          <el-icon
              :class="item.pinned ? 'pinned-icon active' : 'pinned-icon'"
              @click="togglePin(item)"
          >
            <Pushpin />
          </el-icon>
          <div class="actions">
            <el-icon class="action-icon edit-icon" @click="editInspiration(item)"><Edit /></el-icon>
            <el-icon class="action-icon delete-icon" @click="deleteInspiration(item)"><Delete /></el-icon>
          </div>
        </div>

        <div class="card-content">
          <p>{{ item.content }}</p>
        </div>

        <div class="card-footer">
          <span class="create-time">{{ formatDate(item.createdAt) }}</span>
        </div>
      </div>

      <div v-if="filteredInspirations.length === 0" class="empty-state">
        <el-icon class="empty-icon"><Document /></el-icon>
        <p>暂无便签内容</p>
        <el-button
            type="text"
            @click="showAddDialog = true"
        >
          立即添加
        </el-button>
      </div>
    </div>

    <!-- 添加/编辑便签对话框 -->
    <el-dialog
        v-model="showAddDialog"
        :title="isEditing ? '编辑便签' : '添加新便签'"
        :width="400"
        :close-on-click-modal="false"
    >
      <el-input
          v-model="currentContent"
          type="textarea"
          placeholder="输入你的灵感..."
          :maxlength="140"
          class="content-input"
          :rows="4"
      ></el-input>
      <div class="char-count">{{ currentContent.length }}/140</div>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button
            type="primary"
            @click="saveCurrentInspiration"
            :disabled="!currentContent.trim()"
        >
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'
import { Plus, Pushpin, Edit, Delete, Document } from '@element-plus/icons-vue'

// 状态变量
const inspirations = ref([])
const filteredInspirations = ref([])
const searchKeyword = ref('')
const sortType = ref('newest')
const showAddDialog = ref(false)
const currentContent = ref('')
const currentId = ref(null)
const isEditing = ref(false)

// 获取用户ID
const getUserId = () => {
  const userInfo = localStorage.getItem('user')
  if (userInfo) {
    return JSON.parse(userInfo).id || JSON.parse(userInfo).userId
  }
  return null
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取所有便签
const fetchInspirations = async () => {
  try {
    const userId = getUserId()
    if (!userId) {
      ElMessage.warning('请先登录')
      return
    }

    const response = await request.get('/inspiration/list', {
      params: { userId }
    })

    if (response.data.code === 200) {
      inspirations.value = response.data.data
      filterAndSortInspirations()
    }
  } catch (error) {
    console.error('获取便签失败:', error)
    ElMessage.error('获取便签失败')
  }
}

// 筛选和排序便签
const filterAndSortInspirations = () => {
  let result = [...inspirations.value]

  // 筛选
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(item =>
        item.content.toLowerCase().includes(keyword)
    )
  }

  // 排序
  result.sort((a, b) => {
    // 优先显示置顶的便签
    if (a.pinned && !b.pinned) return -1
    if (!a.pinned && b.pinned) return 1

    // 按时间排序
    const dateA = new Date(a.createdAt).getTime()
    const dateB = new Date(b.createdAt).getTime()
    return sortType.value === 'newest' ? dateB - dateA : dateA - dateB
  })

  filteredInspirations.value = result
}

// 搜索处理
const handleSearch = () => {
  filterAndSortInspirations()
}

// 排序处理
const handleSort = () => {
  filterAndSortInspirations()
}

// 添加新便签
const addInspiration = async (content) => {
  try {
    const userId = getUserId()
    if (!userId) {
      ElMessage.warning('请先登录')
      return false
    }

    const response = await request.post('/inspiration/add', {
      content
    }, {
      params: { userId }
    })

    if (response.data.code === 200) {
      ElMessage.success('便签添加成功')
      fetchInspirations()
      return true
    } else {
      ElMessage.error(response.data.msg || '添加失败')
      return false
    }
  } catch (error) {
    console.error('添加便签失败:', error)
    ElMessage.error('添加便签失败')
    return false
  }
}

// 更新便签
const updateInspiration = async (id, content) => {
  try {
    const userId = getUserId()
    if (!userId) {
      ElMessage.warning('请先登录')
      return false
    }

    const response = await request.put('/inspiration/update', {
      id,
      content
    }, {
      params: { userId }
    })

    if (response.data.code === 200) {
      ElMessage.success('便签更新成功')
      fetchInspirations()
      return true
    } else {
      ElMessage.error(response.data.msg || '更新失败')
      return false
    }
  } catch (error) {
    console.error('更新便签失败:', error)
    ElMessage.error('更新便签失败')
    return false
  }
}

// 删除便签
const deleteInspiration = async (item) => {
  try {
    await ElMessageBox.confirm(
        '确定要删除这条便签吗？',
        '确认删除',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )

    const userId = getUserId()
    if (!userId) {
      ElMessage.warning('请先登录')
      return
    }

    const response = await request.delete(`/inspiration/delete/${item.id}`, {
      params: { userId }
    })

    if (response.data.code === 200) {
      ElMessage.success('便签已删除')
      fetchInspirations()
    } else {
      ElMessage.error(response.data.msg || '删除失败')
    }
  } catch (error) {
    // 用户点击取消，不做任何操作
    if (error !== 'cancel') {
      console.error('删除便签失败:', error)
      ElMessage.error('删除便签失败')
    }
  }
}

// 切换置顶状态
const togglePin = async (item) => {
  try {
    const userId = getUserId()
    if (!userId) {
      ElMessage.warning('请先登录')
      return
    }

    // 这里假设API支持更新置顶状态
    const response = await request.put('/inspiration/update', {
      id: item.id,
      content: item.content,
      pinned: !item.pinned
    }, {
      params: { userId }
    })

    if (response.data.code === 200) {
      item.pinned = !item.pinned
      filterAndSortInspirations()
      ElMessage.success(item.pinned ? '已置顶' : '已取消置顶')
    } else {
      ElMessage.error(response.data.msg || '操作失败')
    }
  } catch (error) {
    console.error('更新置顶状态失败:', error)
    ElMessage.error('操作失败')
  }
}

// 编辑便签
const editInspiration = (item) => {
  currentId.value = item.id
  currentContent.value = item.content
  isEditing.value = true
  showAddDialog.value = true
}

// 保存当前便签（新增或编辑）
const saveCurrentInspiration = async () => {
  const content = currentContent.value.trim()
  if (!content) return

  let success = false
  if (isEditing.value && currentId.value) {
    success = await updateInspiration(currentId.value, content)
  } else {
    success = await addInspiration(content)
  }

  if (success) {
    showAddDialog.value = false
    currentContent.value = ''
    currentId.value = null
    isEditing.value = false
  }
}

// 初始化
onMounted(() => {
  fetchInspirations()
})
</script>

<style scoped>
.inspiration-view {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
  background-color: #f8fafc;
  min-height: 100vh;
}

.view-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e2e8f0;
}

.view-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 8px;
}

.view-header h2::before {
  content: '';
  display: inline-block;
  width: 4px;
  height: 24px;
  background-color: #3b82f6;
  border-radius: 2px;
}

.inspiration-filters {
  display: flex;
  gap: 16px;
  margin-bottom: 28px;
  flex-wrap: wrap;
}

.search-input {
  flex: 1;
  max-width: 400px;
  transition: all 0.3s ease;
}

.search-input:focus-within {
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.sort-select {
  width: 180px;
  transition: all 0.3s ease;
}

.sort-select:focus-within {
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.inspiration-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.inspiration-card {
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);
  padding: 20px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  border: 1px solid #f1f5f9;
  overflow: hidden;
}

.inspiration-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, #94a3b8, #cbd5e1);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.inspiration-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.08), 0 4px 6px -2px rgba(0, 0, 0, 0.04);
}

.inspiration-card:hover::before {
  opacity: 1;
}

.inspiration-card.pinned {
  border-left: 4px solid #3b82f6;
  background-color: #f8fafc;
  position: relative;
}

.inspiration-card.pinned::after {
  content: '置顶';
  position: absolute;
  top: 12px;
  right: 12px;
  font-size: 12px;
  color: #3b82f6;
  background-color: rgba(59, 130, 246, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f1f5f9;
}

.pinned-icon {
  color: #94a3b8;
  cursor: pointer;
  transition: all 0.2s ease;
  padding: 4px;
  border-radius: 4px;
}

.pinned-icon:hover {
  background-color: rgba(59, 130, 246, 0.1);
}

.pinned-icon.active {
  color: #3b82f6;
  background-color: rgba(59, 130, 246, 0.1);
}

.actions {
  display: flex;
  gap: 8px;
}

.action-icon {
  color: #94a3b8;
  cursor: pointer;
  transition: all 0.2s ease;
  padding: 4px;
  border-radius: 4px;
}

.action-icon:hover {
  background-color: rgba(59, 130, 246, 0.1);
}

.edit-icon:hover {
  color: #10b981;
}

.delete-icon:hover {
  color: #ef4444;
}

.card-content {
  margin-bottom: 20px;
  min-height: 70px;
}

.card-content p {
  margin: 0;
  line-height: 1.7;
  color: #334155;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 15px;
}

.card-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  font-size: 13px;
  color: #94a3b8;
  padding-top: 12px;
  border-top: 1px solid #f1f5f9;
}

.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 80px 20px;
  color: #64748b;
  background-color: white;
  border-radius: 12px;
  border: 1px dashed #e2e8f0;
  transition: all 0.3s ease;
}

.empty-state:hover {
  border-color: #94a3b8;
  background-color: #f8fafc;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
  color: #cbd5e1;
  transition: all 0.3s ease;
}

.empty-state:hover .empty-icon {
  color: #94a3b8;
  transform: scale(1.05);
}

.empty-state p {
  margin: 0 0 16px 0;
  font-size: 16px;
}

.empty-state .el-button {
  color: #3b82f6;
  font-weight: 500;
  transition: all 0.2s ease;
}

.empty-state .el-button:hover {
  color: #2563eb;
  background-color: rgba(59, 130, 246, 0.1);
}

.content-input {
  resize: none;
  font-size: 15px;
  line-height: 1.6;
  transition: all 0.3s ease;
}

.content-input:focus {
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
  border-color: #93c5fd;
}

.char-count {
  text-align: right;
  font-size: 12px;
  color: #94a3b8;
  margin-top: 8px;
}

.add-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.add-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.2);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .inspiration-view {
    padding: 16px;
  }

  .inspiration-grid {
    grid-template-columns: 1fr;
  }

  .inspiration-filters {
    flex-direction: column;
  }

  .search-input, .sort-select {
    width: 100%;
    max-width: none;
  }

  .view-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .add-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>
