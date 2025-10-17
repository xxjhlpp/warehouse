<template>
  <div class="category-management">
    <!-- 顶部操作栏 -->
    <div class="category-header">
      <div class="filter-section">
        <el-input
            v-model="searchCategory"
            placeholder="搜索分类"
            prefix-icon="Search"
            @keyup.enter="handleSearch"
            class="category-search"
        ></el-input>

        <!-- 父类筛选框 -->
        <el-select
            v-model="selectedParentId"
            placeholder="选择父分类筛选"
            clearable
            class="parent-filter"
            @change="handleParentFilterChange"
        >
          <el-option label="全部分类" value=""></el-option>
          <el-option
              v-for="parent in availableParents"
              :key="parent.id"
              :label="parent.name"
              :value="parent.id"
          ></el-option>
        </el-select>
      </div>

      <div class="header-actions">
        <el-button
            v-if="searchCategory || selectedParentId"
            type="default"
            @click="clearAllFilters"
            class="clear-btn"
        >
          清除筛选
        </el-button>
        <el-button
            type="primary"
            @click="handleAdd"
            class="create-btn"
        >
          <el-icon><Plus /></el-icon> 新建分类
        </el-button>
      </div>
    </div>

    <!-- 分类统计与控制区 -->
    <div class="category-stats">
      <div class="stats-info">
        <span>共 {{ totalCategoriesCount }} 个分类</span>
        <span v-if="searchCategory" class="filtered-count">
          搜索匹配: {{ displayedCategories.length }} 个
        </span>
        <span v-if="selectedParentId && selectedParentName" class="filtered-count">
          父类筛选: {{ selectedParentName }}
        </span>
        <span v-if="selectedParentId && !searchCategory" class="children-count">
          显示 {{ getSubtreeCount(selectedParentId) }} 个相关分类
        </span>
      </div>
      <el-button
          v-if="searchCategory || selectedParentId"
          type="text"
          @click="handleViewAll"
          class="view-all-btn"
      >
        <el-icon><RefreshLeft /></el-icon> 查看全部分类
      </el-button>
    </div>

    <!-- 树形分类展示区 -->
    <div class="category-tree-container">
      <!-- 空状态处理 -->
      <div v-if="displayedCategories.length === 0 && !searchCategory && !selectedParentId" class="empty-state">
        <div class="empty-icon">
          <el-icon><FolderOpened /></el-icon>
        </div>
        <h3>暂无分类</h3>
        <p>点击"新建分类"按钮创建你的第一个分类吧</p>
        <el-button
            type="primary"
            @click="handleAdd"
            class="empty-create-btn"
        >
          <el-icon><Plus /></el-icon> 新建分类
        </el-button>
      </div>

      <!-- 筛选空状态 -->
      <div v-else-if="displayedCategories.length === 0" class="empty-state">
        <div class="empty-icon">
          <el-icon><Search /></el-icon>
        </div>
        <h3>未找到匹配的分类</h3>
        <p v-if="searchCategory && selectedParentId">在当前父分类下未找到匹配的搜索内容</p>
        <p v-else-if="searchCategory">请尝试其他搜索关键词</p>
        <p v-else-if="selectedParentId">该父分类下暂无子分类</p>
        <el-button
            type="default"
            @click="clearAllFilters"
            class="empty-create-btn"
        >
          清除筛选
        </el-button>
      </div>

      <!-- 分类列表 -->
      <div v-else class="category-tree">
        <!-- 统一使用树形结构显示 -->
        <div class="tree-view">
          <CategoryTreeNode
              v-for="node in displayedCategories"
              :key="node.id"
              :node="node"
              :level="0"
              :search-term="searchCategory"
              @edit="handleEdit"
              @delete="handleDelete"
          />
        </div>
      </div>
    </div>

    <!-- 新建/编辑分类弹窗 -->
    <el-dialog
        v-model="showCreateDialog"
        :title="isEditing ? '编辑分类' : '新建分类'"
        :width="400"
        :close-on-click-modal="false"
        class="category-dialog"
    >
      <el-form :model="categoryForm" :rules="categoryRules" ref="categoryFormRef">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称"></el-input>
        </el-form-item>

        <el-form-item label="父分类">
          <el-select
              v-model="categoryForm.parentId"
              placeholder="请选择父分类（可选）"
              clearable
              style="width: 100%"
          >
            <el-option label="无（顶级分类）" value=""></el-option>
            <el-option
                v-for="cat in availableParentCategories"
                :key="cat.id"
                :label="cat.name"
                :value="cat.id"
                :disabled="isEditing && (cat.id === categoryForm.id || isDescendant(cat, categoryForm.id))"
            ></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="submitCategory">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed} from 'vue'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { Plus, RefreshLeft, FolderOpened,  Search } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { useRouter } from 'vue-router'

// 导入树节点组件
import CategoryTreeNode from '@/components/CategoryTreeNode.vue'

// 引入路由
const router = useRouter()
const loading = ref(null)

// 状态管理
const categories = ref([])
const treeData = ref([])
const searchCategory = ref('')
const selectedParentId = ref('') // 选中的父分类ID
const selectedParentName = ref('') // 选中的父分类名称
const showCreateDialog = ref(false)
const isEditing = ref(false)
const categoryFormRef = ref(null)
const availableParentCategories = ref([])

// 计算属性
const totalCategoriesCount = computed(() => {
  return categories.value.length
})

// 可用的父分类（用于筛选框）
const availableParents = computed(() => {
  return categories.value.filter(cat => !cat.parentId) // 只显示顶级分类作为筛选选项
})

// 根据筛选条件显示的分类
const displayedCategories = computed(() => {
  let result = []

  // 如果有搜索关键词
  if (searchCategory.value) {
    const searchTerm = searchCategory.value.toLowerCase()

    // 找到所有匹配的分类
    const matchedCategories = categories.value.filter(item =>
        item.name.toLowerCase().includes(searchTerm) ||
        (item.fullPath && item.fullPath.toLowerCase().includes(searchTerm))
    )

    // 构建包含匹配分类及其祖先的树形结构
    result = buildSearchResultTree(matchedCategories)
  } else {
    // 没有搜索关键词时，显示树形结构或筛选结果
    if (selectedParentId.value) {
      // 父类筛选模式下，找到选中的父分类并返回其子树
      const parentCategory = findCategoryById(selectedParentId.value)
      if (parentCategory) {
        result = [parentCategory] // 返回包含父分类的数组，树形组件会递归显示其子节点
      } else {
        result = []
      }
    } else {
      // 没有筛选时显示完整的树形结构
      result = [...treeData.value]
    }
  }

  return result
})

// 构建搜索结果的树形结构
const buildSearchResultTree = (matchedCategories) => {
  if (matchedCategories.length === 0) return []

  // 获取所有匹配分类及其祖先
  const allNodes = new Map()

  matchedCategories.forEach(category => {
    // 添加当前匹配的分类
    if (!allNodes.has(category.id)) {
      allNodes.set(category.id, { ...category, children: [] })
    }

    // 递归添加所有祖先
    let current = category
    while (current.parentId) {
      const parent = categories.value.find(cat => cat.id === current.parentId)
      if (parent && !allNodes.has(parent.id)) {
        allNodes.set(parent.id, { ...parent, children: [] })
      }
      current = parent
    }
  })

  // 构建树形结构
  const tree = []
  allNodes.forEach(node => {
    if (!node.parentId) {
      // 顶级节点
      tree.push(node)
    } else {
      // 子节点，找到父节点并添加
      const parent = allNodes.get(node.parentId)
      if (parent) {
        parent.children.push(node)
      } else {
        // 如果父节点不在当前集合中，将其作为顶级节点
        tree.push(node)
      }
    }
  })

  return tree
}

// 根据ID查找分类（包括子节点）
const findCategoryById = (id) => {
  const findInTree = (nodes) => {
    for (const node of nodes) {
      if (node.id === id) {
        return node
      }
      if (node.children && node.children.length > 0) {
        const found = findInTree(node.children)
        if (found) return found
      }
    }
    return null
  }
  return findInTree(treeData.value)
}

// 获取子树节点数量
const getSubtreeCount = (parentId) => {
  const parentCategory = findCategoryById(parentId)
  if (!parentCategory) return 0

  const countNodes = (node) => {
    let count = 1 // 当前节点
    if (node.children && node.children.length > 0) {
      node.children.forEach(child => {
        count += countNodes(child)
      })
    }
    return count
  }

  return countNodes(parentCategory)
}

// 表单数据
const categoryForm = reactive({
  id: '',
  name: '',
  parentId: null
})

// 表单验证规则
const categoryRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { max: 50, message: '分类名称不能超过50个字符', trigger: 'blur' }
  ]
}

// 生命周期 - 加载分类
onMounted(() => {
  fetchCategories()
})

// 获取用户认证信息
const getAuthInfo = () => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('user') || '{}')
    const token = localStorage.getItem('token') || ''
    if (!userInfo.id || !token) return null
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

// 创建请求配置
const createRequestConfig = (params = {}) => {
  const authInfo = getAuthInfo()
  if (!authInfo) return {}

  const { userInfo, token } = authInfo
  return {
    params: {
      userId: userInfo?.id,
      ...params
    },
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  }
}

// 搜索分类
const handleSearch = () => {
  // 搜索逻辑已在计算属性中处理
}

// 父分类筛选变化
const handleParentFilterChange = (parentId) => {
  if (parentId) {
    const parent = categories.value.find(cat => cat.id === parentId)
    selectedParentName.value = parent ? parent.name : ''
  } else {
    selectedParentName.value = ''
  }
}

// 清除所有筛选
const clearAllFilters = () => {
  searchCategory.value = ''
  selectedParentId.value = ''
  selectedParentName.value = ''
}

// 查看全部分类
const handleViewAll = () => {
  clearAllFilters()
}

// 构建树形结构
const buildTree = (categories) => {
  const categoryMap = new Map()
  const tree = []

  // 创建映射
  categories.forEach(category => {
    categoryMap.set(category.id, { ...category, children: [] })
  })

  // 构建树
  categories.forEach(category => {
    const node = categoryMap.get(category.id)
    if (category.parentId && categoryMap.has(category.parentId)) {
      const parent = categoryMap.get(category.parentId)
      parent.children.push(node)
    } else {
      tree.push(node)
    }
  })

  return tree
}

// 获取所有分类
const fetchCategories = async () => {
  if (!checkLoginStatus()) return

  try {
    loading.value = ElLoading.service({ text: '加载分类中...' })
    const config = createRequestConfig()
    const res = await request.get('/category/tree', config)

    if (res.data && res.data.code === 200) {
      const categoriesData = Array.isArray(res.data.data) ? res.data.data : []

      // 扁平化所有分类用于搜索
      const flattenCategories = (nodes, parentPath = '') => {
        let result = []
        nodes.forEach(node => {
          const fullPath = parentPath ? `${parentPath} / ${node.name}` : node.name
          const flatNode = {
            ...node,
            fullPath,
            parentName: parentPath || null
          }
          result.push(flatNode)
          if (node.children && node.children.length) {
            result = result.concat(flattenCategories(node.children, fullPath))
          }
        })
        return result
      }

      const allCategories = flattenCategories(categoriesData)
      categories.value = allCategories
      treeData.value = buildTree(allCategories)

      // 更新可用的父分类列表
      availableParentCategories.value = allCategories
    } else {
      ElMessage.error(res.data?.msg || '获取分类失败')
      categories.value = []
      treeData.value = []
    }
  } catch (error) {
    console.error('分类获取错误:', error)
    handleRequestError(error)
    categories.value = []
    treeData.value = []
  } finally {
    if (loading.value) loading.value.close()
  }
}

// 检查是否后代节点
const isDescendant = (parent, childId) => {
  const checkChildren = (node, targetId) => {
    if (node.id === targetId) return true
    if (node.children) {
      for (let child of node.children) {
        if (checkChildren(child, targetId)) return true
      }
    }
    return false
  }

  return checkChildren(parent, childId)
}

// 处理请求错误
const handleRequestError = (error) => {
  if (error.response && error.response.status === 401) {
    ElMessage.error('登录已过期，请重新登录')
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    router.push('/login')
  }
}

// 新建分类
const handleAdd = () => {
  if (!checkLoginStatus()) return
  isEditing.value = false
  resetForm()
  showCreateDialog.value = true
}

// 编辑分类
const handleEdit = (category) => {
  if (!checkLoginStatus()) return
  isEditing.value = true
  categoryForm.id = category.id
  categoryForm.name = category.name
  categoryForm.parentId = category.parentId || ''
  showCreateDialog.value = true
}

// 重置表单
const resetForm = () => {
  categoryForm.id = ''
  categoryForm.name = ''
  categoryForm.parentId = ''
  if (categoryFormRef.value) {
    categoryFormRef.value.resetFields()
  }
}

// 提交分类
const submitCategory = async () => {
  if (!checkLoginStatus()) {
    showCreateDialog.value = false
    return
  }

  try {
    await categoryFormRef.value.validate()
    loading.value = ElLoading.service({ text: '提交中...' })
    const config = createRequestConfig()
    const { userInfo } = getAuthInfo() || {}

    if (isEditing.value) {
      await request.put('/category/update', {
        id: categoryForm.id,
        name: categoryForm.name,
        parentId: categoryForm.parentId || null
      }, config)
      ElMessage.success('分类更新成功')
    } else {
      await request.post('/category/add', null, {
        ...config,
        params: {
          name: categoryForm.name,
          parentId: categoryForm.parentId || null,
          userId: userInfo?.id
        }
      })
      ElMessage.success('分类创建成功')
    }

    showCreateDialog.value = false
    await fetchCategories()
    resetForm()
  } catch (error) {
    console.error('分类提交错误:', error)
    if (error === 'cancel') return
    handleRequestError(error)
    ElMessage.error(isEditing.value ?
        (error.response?.data?.msg || '分类更新失败') :
        (error.response?.data?.msg || '分类创建失败'))
  } finally {
    if (loading.value) loading.value.close()
  }
}

// 删除分类
const handleDelete = async (category) => {
  if (!checkLoginStatus()) return

  try {
    // 检查是否有子分类
    if (category.children && category.children.length > 0) {
      ElMessage.warning('请先删除该分类下的子分类')
      return
    }

    await ElMessageBox.confirm(
        `确定要删除分类"${category.name}"吗？`,
        '删除确认',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning',
        }
    )

    const config = createRequestConfig()
    await request.delete(`/category/delete/${category.id}`, config)

    ElMessage.success('分类删除成功')
    await fetchCategories()
  } catch (error) {
    if (error === 'cancel') return
    console.error('删除错误:', error)
    handleRequestError(error)
    ElMessage.error('分类删除失败: ' + (error.response?.data?.msg || error.message))
  }
}
</script>

<style scoped>
.category-management {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
  background-color: #f8fafc;
  min-height: calc(100vh - 160px);
}

/* 头部搜索与操作区 */
.category-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.filter-section {
  display: flex;
  gap: 12px;
  align-items: center;
  flex: 1;
}

.category-search {
  width: 280px;
  transition: all 0.3s ease;
}

.category-search:focus-within {
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
}

.parent-filter {
  width: 200px;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.clear-btn {
  transition: all 0.2s ease;
}

.clear-btn:hover {
  background-color: #f1f5f9;
}

.create-btn {
  background-color: #3b82f6;
  border-color: #3b82f6;
  transition: all 0.2s ease;
}

.create-btn:hover {
  background-color: #2563eb;
  transform: translateY(-1px);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.create-btn:active {
  transform: translateY(0);
}

/* 统计信息区 */
.category-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 0 4px;
}

.stats-info {
  color: #64748b;
  font-size: 14px;
  display: flex;
  gap: 16px;
  align-items: center;
  flex-wrap: wrap;
}

.filtered-count {
  color: #3b82f6;
  font-weight: 500;
  background-color: #f0f5ff;
  padding: 4px 8px;
  border-radius: 6px;
}

.children-count {
  color: #10b981;
  font-weight: 500;
  background-color: #f0fdf4;
  padding: 4px 8px;
  border-radius: 6px;
}

.view-all-btn {
  color: #3b82f6;
  padding: 4px 8px;
  transition: all 0.2s ease;
}

.view-all-btn:hover {
  color: #2563eb;
  background-color: rgba(59, 130, 246, 0.1);
}

.view-all-btn .el-icon {
  margin-right: 4px;
}

/* 分类列表容器 */
.category-tree-container {
  background: #fff;
  border-radius: 12px;
  min-height: 400px;
  padding: 24px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

/* 空状态样式 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  text-align: center;
  color: #64748b;
}

.empty-icon {
  font-size: 64px;
  color: #94a3b8;
  margin-bottom: 20px;
  opacity: 0.7;
}

.empty-state h3 {
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 8px;
  color: #334155;
}

.empty-state p {
  font-size: 14px;
  margin-bottom: 24px;
  max-width: 400px;
  line-height: 1.6;
}

.empty-create-btn {
  background-color: #3b82f6;
  border-color: #3b82f6;
}

.empty-create-btn:hover {
  background-color: #2563eb;
}

/* 操作按钮样式 */
.edit-btn {
  color: #64748b;
  padding: 4px 10px;
  transition: all 0.2s ease;
}

.edit-btn:hover {
  color: #3b82f6;
  background-color: rgba(59, 130, 246, 0.1);
}

.edit-btn .el-icon {
  margin-right: 4px;
}

.delete-btn {
  color: #ef4444;
  background-color: rgba(239, 68, 68, 0.05);
  border-color: transparent;
  padding: 4px 10px;
  transition: all 0.2s ease;
}

.delete-btn:hover {
  color: #dc2626;
  background-color: rgba(239, 68, 68, 0.1);
}

.delete-btn .el-icon {
  margin-right: 4px;
}

/* 弹窗样式优化 */
.category-dialog .el-dialog__header {
  border-bottom: 1px solid #f1f5f9;
  padding-bottom: 16px;
}

.category-dialog .el-dialog__title {
  font-size: 16px;
  font-weight: 500;
  color: #1e293b;
}

.category-dialog .el-dialog__body {
  padding: 24px;
}

.category-dialog .el-form-item {
  margin-bottom: 20px;
}

.category-dialog .el-form-item__label {
  color: #475569;
  font-weight: 500;
}

.category-dialog .el-dialog__footer {
  border-top: 1px solid #f1f5f9;
  padding: 16px 24px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .category-management {
    padding: 16px;
  }

  .category-header {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-section {
    flex-direction: column;
    width: 100%;
  }

  .category-search,
  .parent-filter {
    width: 100%;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .stats-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .edit-btn, .delete-btn {
    padding: 2px 8px;
    font-size: 12px;
  }

  .edit-btn .el-icon,
  .delete-btn .el-icon {
    display: none;
  }
}
</style>