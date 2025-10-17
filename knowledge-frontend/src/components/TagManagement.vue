<template>
  <div class="tag-management">
    <!-- 顶部操作栏 -->
    <div class="tag-header">
      <el-input
          v-model="searchTag"
          placeholder="搜索标签"
          prefix-icon="Search"
          @keyup.enter="handleSearch"
          class="tag-search"
      ></el-input>
      <div class="header-actions">
        <el-button
            v-if="searchTag"
            type="default"
            @click="clearSearch"
            class="clear-btn"
        >
          清除搜索
        </el-button>
        <el-button
            type="primary"
            @click="handleAdd"
            class="create-btn"
        >
          <el-icon><Plus /></el-icon> 新建标签
        </el-button>
      </div>
    </div>

    <!-- 标签统计与控制区 -->
    <div class="tag-stats">
      <div class="stats-info">
        <span>共 {{ tagsCount }} 个标签</span>
        <span v-if="searchTag" class="filtered-count">
          显示 {{ filteredTags.length }} 个匹配结果
        </span>
      </div>
      <el-button
          v-if="searchTag && filteredTags.length !== tags.length"
          type="text"
          @click="handleViewAll"
          class="view-all-btn"
      >
        <el-icon><RefreshLeft /></el-icon> 查看全部标签
      </el-button>
    </div>

    <!-- 标签展示区 -->
    <div class="tag-grid">
      <!-- 空状态处理 -->
      <div v-if="filteredTags.length === 0" class="empty-state">
        <div class="empty-icon">
          <el-icon><Tag /></el-icon>
        </div>
        <h3>{{ searchTag ? '未找到匹配的标签' : '暂无标签' }}</h3>
        <p>{{ searchTag ? '请尝试其他搜索关键词' : '点击"新建标签"按钮创建你的第一个标签吧' }}</p>
        <el-button
            type="primary"
            @click="handleAdd"
            class="empty-create-btn"
        >
          <el-icon><Plus /></el-icon> 新建标签
        </el-button>
      </div>

      <!-- 标签卡片 -->
      <el-card
          class="tag-card"
          v-for="tag in filteredTags"
          :key="tag.id"
      >
        <div class="tag-info">
          <div class="tag-icon">
            <el-icon><Tag /></el-icon>
          </div>
          <div class="tag-details">
            <h3>{{ tag.name }}</h3>
            <p>关联笔记: {{ tag.count || 0 }} 篇</p>
          </div>
        </div>
        <div class="tag-actions">
          <el-button
              size="small"
              @click="handleEdit(tag)"
              class="edit-btn"
          >
            <el-icon size="14"><Edit /></el-icon> 编辑
          </el-button>
          <el-button
              size="small"
              type="danger"
              @click="handleDelete(tag)"
              class="delete-btn"
          >
            <el-icon size="14"><Delete /></el-icon> 删除
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 新建/编辑标签弹窗 -->
    <el-dialog
        v-model="showCreateDialog"
        :title="isEditing ? '编辑标签' : '新建标签'"
        :width="350"
        :close-on-click-modal="false"
    >
      <el-form :model="tagForm" :rules="tagRules" ref="tagFormRef">
        <el-form-item label="标签名称" prop="name">
          <el-input v-model="tagForm.name" placeholder="请输入标签名称"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="submitTag">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { Plus, Tag, Edit, Delete, RefreshLeft } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { useRouter } from 'vue-router'

// 引入路由和加载组件
const router = useRouter()
const loading = ref(null)

// 状态管理
const tags = ref([])
const filteredTags = ref([])
const searchTag = ref('')
const showCreateDialog = ref(false)
const isEditing = ref(false)
const tagFormRef = ref(null)

// 计算属性 - 标签总数
const tagsCount = computed(() => tags.value.length)

// 表单数据
const tagForm = reactive({
  id: '',
  name: ''
})

// 表单验证规则
const tagRules = {
  name: [
    { required: true, message: '请输入标签名称', trigger: 'blur' },
    { max: 20, message: '标签名称不能超过20个字符', trigger: 'blur' }
  ]
}

// 生命周期 - 加载标签
onMounted(() => {
  fetchTags()
})

// 获取用户认证信息的通用方法
const getAuthInfo = () => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('user') || '{}')
    const token = localStorage.getItem('token') || ''

    // 验证用户信息有效性
    if (!userInfo.id || !token) {
      return null
    }

    return { userInfo, token }
  } catch (error) {
    console.error('解析用户信息失败:', error)
    return null
  }
}

// 检查登录状态
const checkLoginStatus = () => {
  const authInfo = getAuthInfo()
  if (!authInfo) {
    ElMessage.error('请先登录')
    router.push('/login')
    return false
  }
  return true
}

// 创建一个通用的请求配置函数，确保所有请求都有正确的头信息
const createRequestConfig = (params = {}) => {
  const { userInfo, token } = getAuthInfo() || {}

  // 将用户ID添加到所有请求参数中
  const requestParams = {
    userId: userInfo?.id,
    ...params
  }

  return {
    params: requestParams,
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
      'X-Requested-With': 'XMLHttpRequest'
    }
  }
}

// 搜索标签
const handleSearch = () => {
  if (!searchTag.value) {
    filteredTags.value = [...tags.value]
    return
  }
  filteredTags.value = tags.value.filter(tag =>
      tag.name.toLowerCase().includes(searchTag.value.toLowerCase())
  )
}

// 清除搜索
const clearSearch = () => {
  searchTag.value = ''
  handleSearch()
}

// 查看全部标签
const handleViewAll = () => {
  searchTag.value = ''
  filteredTags.value = [...tags.value]
}

// 获取所有标签
const fetchTags = async () => {
  // 检查登录状态
  if (!checkLoginStatus()) return

  try {
    loading.value = ElLoading.service({ text: '加载标签中...' })

    // 使用通用请求配置
    const config = createRequestConfig()
    const res = await request.get('/tag/list', config)

    if (res.data.code === 200) {
      tags.value = res.data.data || []
      filteredTags.value = [...tags.value]
    } else {
      ElMessage.error(res.data.msg || '获取标签失败')
      tags.value = []
      filteredTags.value = []
    }
  } catch (error) {
    console.error('标签获取错误:', error)
    handleRequestError(error)
  } finally {
    if (loading.value) {
      loading.value.close()
    }
  }
}

// 新建标签
const handleAdd = () => {
  // 打开弹窗前检查登录状态
  if (!checkLoginStatus()) return

  isEditing.value = false
  resetForm()
  showCreateDialog.value = true
}

// 编辑标签
const handleEdit = (tag) => {
  if (!checkLoginStatus()) return

  isEditing.value = true
  tagForm.id = tag.id
  tagForm.name = tag.name
  showCreateDialog.value = true
}

// 新建/编辑标签提交
const submitTag = async () => {
  // 提交前再次检查登录状态
  if (!checkLoginStatus()) {
    showCreateDialog.value = false
    return
  }

  try {
    // 验证表单
    const valid = await tagFormRef.value.validate()
    if (!valid) return

    loading.value = ElLoading.service({ text: '提交中...' })
    const config = createRequestConfig()
    // 生成当前时间，用于创建新标签
    const currentTime = new Date().toISOString()

    if (isEditing.value) {
      // 编辑标签
      await request.put('/tag/update', {
        id: tagForm.id,
        name: tagForm.name
      }, config)
      ElMessage.success('标签更新成功')
    } else {
      // 新建标签 - 添加创建时间参数
      await request.post('/tag/add',
          {
            name: tagForm.name,
            createdAt: currentTime
          },
          config
      )
      ElMessage.success('标签创建成功')
    }

    showCreateDialog.value = false
    await fetchTags() // 重新加载标签列表
    resetForm()
  } catch (error) {
    console.error('标签提交错误:', error)
    if (error !== 'cancel') {
      handleRequestError(error)
      ElMessage.error(isEditing.value ?
          (error.response?.data?.msg || '标签更新失败') :
          (error.response?.data?.msg || '标签创建失败'))
    }
  } finally {
    if (loading.value) {
      loading.value.close()
    }
  }
}

// 删除标签
const handleDelete = async (tag) => {
  if (!checkLoginStatus()) return

  try {
    await ElMessageBox.confirm(
        `确定要删除标签"${tag.name}"吗？`,
        '删除确认',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning',
        }
    )

    const config = createRequestConfig()
    await request.delete(`/tag/delete/${tag.id}`, config)

    ElMessage.success('标签删除成功')
    await fetchTags()
  } catch (error) {
    if (error === 'cancel') {
      return // 用户取消删除
    }

    console.error('删除错误:', error)
    handleRequestError(error)
    ElMessage.error('标签删除失败: ' + (error.response?.data?.msg || error.message))
  }
}

// 处理请求错误的通用方法
const handleRequestError = (error) => {
  // 处理401未授权错误
  if (error.response && error.response.status === 401) {
    ElMessage.error('登录已过期，请重新登录')
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    router.push('/login')
    return
  }

  // 处理403禁止访问错误
  if (error.response && error.response.status === 403) {
    ElMessage.error('没有权限执行此操作，请重新登录')
    setTimeout(() => {
      router.push('/login')
    }, 1500)
    return
  }
}

// 重置表单
const resetForm = () => {
  if (tagFormRef.value) {
    tagFormRef.value.resetFields()
  }
  tagForm.id = ''
  tagForm.name = ''
  isEditing.value = false
}
</script>

<style scoped>
/* 样式保持不变 */
.tag-management {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.tag-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 10px 0;
}

.tag-search {
  width: 300px;
  transition: width 0.3s;
}

.tag-search:focus-within {
  width: 350px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.clear-btn {
  background-color: #f5f7fa;
}

.create-btn {
  background-color: #165dff;
}

.tag-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  color: #64748b;
  font-size: 14px;
  padding: 8px 0;
  border-bottom: 1px solid #e2e8f0;
}

.stats-info {
  display: flex;
  gap: 15px;
}

.filtered-count {
  color: #165dff;
  padding-left: 10px;
  border-left: 1px solid #e2e8f0;
}

.view-all-btn {
  color: #165dff;
  padding: 5px 10px;
}

.view-all-btn:hover {
  color: #0d47a1;
  background-color: #f0f5ff;
}

.tag-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.tag-card {
  transition: all 0.3s ease;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
}

.tag-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(22, 93, 255, 0.1);
  border-color: #dbeafe;
}

.tag-info {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 10px 0;
}

.tag-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background-color: #f0f5ff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #165dff;
  font-size: 24px;
  flex-shrink: 0;
}

.tag-details h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
}

.tag-details p {
  margin: 0;
  font-size: 14px;
  color: #64748b;
}

.tag-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 10px;
  border-top: 1px dashed #e2e8f0;
}

.edit-btn {
  color: #165dff;
  border-color: #dbeafe;
}

.edit-btn:hover {
  background-color: #f0f5ff;
}

.delete-btn {
  color: #ffffff !important;
  background-color: #ff4d4f !important;
  border-color: #ff4d4f !important;
  transition: all 0.2s ease;
  padding: 4px 12px;
}

.delete-btn:hover {
  background-color: #d93f3f !important;
  transform: translateY(-1px);
  box-shadow: 0 2px 5px rgba(255, 77, 79, 0.3);
}

.delete-btn .el-icon {
  margin-right: 4px;
}

.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px 20px;
  background-color: #f8fafc;
  border-radius: 12px;
  border: 1px dashed #e2e8f0;
}

.empty-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 20px;
  border-radius: 50%;
  background-color: #f0f5ff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #165dff;
  font-size: 32px;
}

.empty-state h3 {
  margin: 0 0 10px 0;
  font-size: 18px;
  color: #1e293b;
}

.empty-state p {
  margin: 0 0 20px 0;
  color: #64748b;
  max-width: 400px;
  margin-left: auto;
  margin-right: auto;
}

.empty-create-btn {
  margin-top: 10px;
}

@media (max-width: 768px) {
  .tag-header {
    flex-direction: column;
    align-items: stretch;
    gap: 15px;
  }

  .tag-search {
    width: 100%;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .tag-grid {
    grid-template-columns: 1fr;
  }
}
</style>