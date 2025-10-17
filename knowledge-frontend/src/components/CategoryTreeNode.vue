<!-- src/components/CategoryTreeNode.vue -->
<template>
  <div class="tree-node" :style="{ marginLeft: level * 20 + 'px' }">
    <div class="node-content" :class="{ 'has-children': node.children && node.children.length > 0, 'search-match': isSearchMatch }">
      <div class="node-info">
        <div class="node-main">
          <el-icon v-if="node.children && node.children.length > 0" class="folder-icon">
            <FolderOpened />
          </el-icon>
          <el-icon v-else class="folder-icon">
            <Folder />
          </el-icon>
          <span class="node-name" v-html="highlightText(node.name)"></span>
          <span v-if="node.children && node.children.length > 0" class="children-count">
            {{ node.children.length }}个子分类
          </span>
        </div>
        <div class="node-actions">
          <el-button
              size="small"
              @click.stop="$emit('edit', node)"
              class="edit-btn"
          >
            <el-icon size="14"><Edit /></el-icon> 编辑
          </el-button>
          <el-button
              size="small"
              type="danger"
              @click.stop="$emit('delete', node)"
              class="delete-btn"
          >
            <el-icon size="14"><Delete /></el-icon> 删除
          </el-button>
        </div>
      </div>
    </div>

    <!-- 递归渲染子节点 -->
    <div v-if="node.children && node.children.length > 0" class="node-children">
      <CategoryTreeNode
          v-for="child in node.children"
          :key="child.id"
          :node="child"
          :level="level + 1"
          :search-term="searchTerm"
          @edit="$emit('edit', $event)"
          @delete="$emit('delete', $event)"
      />
    </div>
  </div>
</template>

<script setup>
import { FolderOpened, Folder, Edit, Delete } from '@element-plus/icons-vue'
import { computed } from 'vue'

const props = defineProps({
  node: {
    type: Object,
    required: true
  },
  level: {
    type: Number,
    default: 0
  },
  searchTerm: {
    type: String,
    default: ''
  }
})

defineEmits(['edit', 'delete'])

// 检查是否是搜索匹配项
const isSearchMatch = computed(() => {
  if (!props.searchTerm) return false
  return props.node.name.toLowerCase().includes(props.searchTerm.toLowerCase())
})

// 高亮显示搜索文本
const highlightText = (text) => {
  if (!props.searchTerm) return text

  const searchTerm = props.searchTerm.toLowerCase()
  const index = text.toLowerCase().indexOf(searchTerm)

  if (index === -1) return text

  const before = text.substring(0, index)
  const match = text.substring(index, index + searchTerm.length)
  const after = text.substring(index + searchTerm.length)

  return `${before}<span class="highlight">${match}</span>${after}`
}
</script>

<style scoped>
.tree-node {
  margin-bottom: 4px;
}

.node-content {
  padding: 12px 16px;
  border-radius: 8px;
  background-color: #fff;
  border: 1px solid #f1f5f9;
  transition: all 0.3s ease;
  margin-bottom: 4px;
}

.node-content.has-children {
  border-left: 3px solid #3b82f6;
  background-color: #f8fafc;
}

.node-content.search-match {
  border-color: #3b82f6;
  background-color: #f8fafc;
}

.node-content:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.1);
  border-color: #dbeafe;
}

.node-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.node-main {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.folder-icon {
  color: #3b82f6;
  font-size: 16px;
}

.node-content.search-match .folder-icon {
  color: #3b82f6;
}

.node-name {
  font-weight: 500;
  color: #1e293b;
  font-size: 14px;
}

.children-count {
  font-size: 12px;
  color: #64748b;
  background-color: #f1f5f9;
  padding: 2px 8px;
  border-radius: 12px;
  margin-left: 8px;
}

.node-actions {
  display: flex;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.node-content:hover .node-actions {
  opacity: 1;
}

.node-children {
  margin-top: 4px;
  border-left: 2px dashed #e2e8f0;
  margin-left: 10px;
}

/* 高亮样式 */
:deep(.highlight) {
  background-color: #fef3c7;
  color: #d97706;
  padding: 0 2px;
  border-radius: 2px;
  font-weight: 600;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .node-actions {
    opacity: 1;
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