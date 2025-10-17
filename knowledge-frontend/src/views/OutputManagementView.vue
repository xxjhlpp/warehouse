<template>
  <div class="output-management">
    <!-- 顶部筛选和搜索区域 -->
    <div class="filter-bar">
      <div class="filter-group">
        <label class="filter-label">分类筛选:</label>
        <select v-model="selectedCategoryId" class="category-select" @change="handleCategoryChange">
          <option value="">全部分类</option>
          <option v-for="category in categories" :key="category.id" :value="category.id">
            {{ category.displayName }}
          </option>
        </select>
      </div>

      <div class="search-group">
        <el-input
            v-model="searchKeyword"
            placeholder="搜索笔记标题或内容..."
            prefix-icon="Search"
            @keyup.enter="handleSearch"
            class="search-input"
        ></el-input>
        <el-button @click="handleSearch" class="search-btn">搜索</el-button>
        <el-button
            v-if="hasSearchParams"
            @click="resetFilter"
            class="reset-btn"
        >
          重置
        </el-button>
      </div>
    </div>

    <!-- 统计信息区 -->
    <div class="notes-stats">
      <div class="stats-info">
        <span>共 {{ allNotes.length }} 篇笔记</span>
        <span v-if="hasSearchParams" class="filtered-count">
          显示 {{ filteredNotes.length }} 个匹配结果
        </span>
      </div>
      <el-button
          v-if="hasSearchParams"
          type="text"
          @click="resetFilter"
          class="view-all-btn"
      >
        <el-icon><RefreshLeft /></el-icon> 查看全部笔记
      </el-button>
    </div>

    <!-- 笔记列表区域 -->
    <div class="notes-container">
      <div class="notes-list">
        <div
            v-for="note in filteredNotes"
            :key="note.id"
            class="note-item"
            @click="goToNoteEdit(note.id)"
        >
          <div class="note-info">
            <div class="note-title-section">
              <div class="note-title">
                <el-icon class="note-icon"><Document /></el-icon>
                <span class="title-text">{{ note.title || '无标题' }}</span>
              </div>
              <div class="note-meta">
                <span class="update-time">{{ formatDate(note.updatedAt) }}</span>
                <span v-if="getCategoryName(note.categoryId)" class="category-tag">
                  {{ getCategoryName(note.categoryId) }}
                </span>
              </div>
            </div>
            <div class="note-tags-section">
              <div class="note-tags">
                <el-tag
                    size="small"
                    v-for="tag in note.tags || []"
                    :key="tag.id"
                    class="tag-item"
                >
                  {{ tag.name }}
                </el-tag>
                <span v-if="!note.tags || note.tags.length === 0" class="no-tags">
                  无标签
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="filteredNotes.length === 0" class="empty-state">
        <div class="empty-icon">
          <el-icon><Document /></el-icon>
        </div>
        <h3>{{ hasSearchParams ? '未找到匹配的笔记' : '暂无笔记' }}</h3>
        <p>{{ hasSearchParams ? '请尝试其他搜索关键词或重置筛选条件' : '开始创建你的第一篇笔记吧' }}</p>
        <el-button
            type="primary"
            @click="$router.push('/editor')"
            class="empty-create-btn"
        >
          <el-icon><Plus /></el-icon> 新建笔记
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import request from '@/utils/request';
import { Document, RefreshLeft, Plus } from '@element-plus/icons-vue';

// 路由
const router = useRouter();

// 响应式数据
const categories = ref([]);
const allNotes = ref([]);
const selectedCategoryId = ref('');
const searchKeyword = ref('');

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

// 根据分类ID获取分类名称
const getCategoryName = (categoryId) => {
  if (!categoryId) return '';
  const category = categories.value.find(cat => cat.id === categoryId);
  return category ? category.name : '';
};

// 获取请求头信息（统一处理token）
const getAuthHeaders = () => {
  const token = localStorage.getItem('token');
  return token ? { 'Authorization': `Bearer ${token}` } : {};
};

// 验证用户登录状态
const validateUserLogin = () => {
  const userInfoStr = localStorage.getItem('user');
  const token = localStorage.getItem('token');

  if (!userInfoStr || !token) {
    ElMessage.error('请先登录');
    router.push('/login');
    return null;
  }

  const userInfo = JSON.parse(userInfoStr);
  if (!userInfo.id) {
    ElMessage.error('用户信息不完整');
    router.push('/login');
    return null;
  }

  return { userInfo, token };
};

// 获取所有标签信息
const fetchAllTags = async () => {
  try {
    const authData = validateUserLogin();
    if (!authData) return {};

    const { userInfo } = authData;
    const response = await request.get('/tag/list', {
      params: { userId: userInfo.id },
      headers: getAuthHeaders()
    });

    if (response.data.code === 200) {
      const tags = response.data.data;
      const tagMap = {};
      tags.forEach(tag => {
        tagMap[tag.id] = tag;
      });
      return tagMap;
    }
    return {};
  } catch (error) {
    console.error('获取所有标签失败:', error);
    return {};
  }
};

// 获取笔记列表
const fetchNotes = async () => {
  try {
    const authData = validateUserLogin();
    if (!authData) return;

    const {userInfo} = authData;

    const response = await request.get('/note/list', {
      params: {
        userId: userInfo.userId || userInfo.id,
        categoryId: selectedCategoryId.value || undefined,
        keyword: searchKeyword.value || undefined
      },
      headers: getAuthHeaders()
    });

    if (response.data.code === 200) {
      // 先获取所有标签信息
      const tagMap = await fetchAllTags();

      // 获取笔记数据
      const notes = response.data.data;

      // 为每个笔记获取标签信息
      const notesWithTags = await Promise.all(
          notes.map(async (note) => {
            try {
              // 获取笔记的标签ID列表
              const tagIdsResponse = await request.get(`/note-tag/tags/${note.id}`, {
                headers: getAuthHeaders()
              });

              if (tagIdsResponse.data.code === 200) {
                const tagIds = tagIdsResponse.data.data;
                // 从tagMap中获取标签详情
                note.tags = tagIds.map(tagId => tagMap[tagId]).filter(tag => tag);
              } else {
                note.tags = [];
              }
            } catch (error) {
              console.error(`获取笔记 ${note.id} 的标签失败:`, error);
              note.tags = [];
            }
            return note;
          })
      );

      allNotes.value = notesWithTags;
      console.log('获取到的笔记数据:', allNotes.value);
    } else {
      ElMessage.error('获取笔记失败: ' + (response.data.msg || '未知错误'));
    }
  } catch (error) {
    console.error('获取笔记列表失败:', error);
    // 处理401/403错误
    if (error.response && (error.response.status === 401 || error.response.status === 403)) {
      ElMessage.error('登录已过期，请重新登录');
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      router.push('/login');
    } else {
      ElMessage.error('获取笔记失败，请稍后重试');
    }
  }
};

// 过滤后的笔记列表
const filteredNotes = computed(() => {
  return allNotes.value.filter(note => {
    // 分类筛选
    const categoryMatch = !selectedCategoryId.value ||
        note.categoryId === selectedCategoryId.value;

    // 搜索筛选
    const keywordMatch = !searchKeyword.value ||
        (note.title && note.title.includes(searchKeyword.value)) ||
        (note.content && note.content.includes(searchKeyword.value));

    return categoryMatch && keywordMatch;
  });
});

// 是否有搜索参数
const hasSearchParams = computed(() => {
  return !!selectedCategoryId.value || !!searchKeyword.value;
});

// 获取分类列表
const fetchCategories = async () => {
  try {
    const authData = validateUserLogin();
    if (!authData) return;

    const {userInfo} = authData;
    const res = await request.get('/category/tree', {
      params: {userId: userInfo.id},
      headers: getAuthHeaders()
    });

    if (res.data.code === 200) {
      // 构建层级分类
      const buildHierarchicalCategories = (items, prefix = '') => {
        return items.map(item => {
          const categorizedItem = {
            ...item,
            displayName: prefix + item.name
          };

          if (item.children && item.children.length) {
            categorizedItem.children = buildHierarchicalCategories(
                item.children,
                prefix + '--'
            );
          }
          return categorizedItem;
        });
      };

      const hierarchicalCats = buildHierarchicalCategories(res.data.data);

      // 扁平化处理
      const flattenCategories = (items) => {
        let result = [];
        items.forEach(item => {
          result.push(item);
          if (item.children) {
            result = result.concat(flattenCategories(item.children));
          }
        });
        return result;
      };

      categories.value = flattenCategories(hierarchicalCats);
    }
  } catch (error) {
    console.error('获取分类失败:', error);
    ElMessage.error('获取分类失败');
  }
};

// 处理分类变更
const handleCategoryChange = () => {
  fetchNotes();
};

// 处理搜索
const handleSearch = () => {
  fetchNotes();
};

// 重置筛选条件
const resetFilter = () => {
  selectedCategoryId.value = '';
  searchKeyword.value = '';
  fetchNotes();
};

// 跳转到笔记详情
const goToNoteEdit = (id) => {
  router.push(`/editor/${id}`);
}

// 页面加载时获取数据
onMounted(() => {
  fetchCategories();
  fetchNotes();
});
</script>

<style scoped>
/* 样式保持不变 */
.output-management {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
  background-color: #f8fafc;
  min-height: calc(100vh - 160px);
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-label {
  font-weight: 500;
  color: #4b5563;
}

.category-select {
  padding: 8px 12px;
  border-radius: 4px;
  border: 1px solid #d1d5db;
  min-width: 200px;
  background: white;
}

.search-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.search-input {
  width: 300px;
  transition: all 0.3s ease;
}

.search-input:focus-within {
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
}

.search-btn, .reset-btn {
  white-space: nowrap;
  transition: all 0.2s ease;
}

.search-btn:hover, .reset-btn:hover {
  transform: translateY(-1px);
}

.notes-stats {
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
}

.filtered-count {
  color: #3b82f6;
  font-weight: 500;
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

.notes-container {
  background: #fff;
  border-radius: 12px;
  min-height: 400px;
  padding: 24px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.notes-list {
  display: grid;
  gap: 16px;
}

.note-item {
  padding: 20px;
  border-radius: 8px;
  transition: all 0.3s ease;
  background-color: #fff;
  border: 1px solid #f1f5f9;
  cursor: pointer;
}

.note-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.08);
  border-color: #dbeafe;
}

.note-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.note-title-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.note-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  flex: 1;
}

.note-icon {
  color: #3b82f6;
  font-size: 18px;
  flex-shrink: 0;
}

.title-text {
  word-break: break-word;
  line-height: 1.4;
}

.note-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.update-time {
  font-size: 13px;
  color: #64748b;
  white-space: nowrap;
}

.category-tag {
  font-size: 12px;
  color: #3b82f6;
  background-color: #eff6ff;
  padding: 4px 8px;
  border-radius: 6px;
  white-space: nowrap;
}

.note-tags-section {
  margin-top: 8px;
}

.note-tags {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.tag-item {
  transition: all 0.2s ease;
}

.tag-item:hover {
  transform: translateY(-1px);
}

.no-tags {
  font-size: 12px;
  color: #94a3b8;
  font-style: italic;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
  color: #64748b;
  background-color: #f8fafc;
  border-radius: 8px;
  margin-top: 20px;
}

.empty-icon {
  font-size: 48px;
  color: #94a3b8;
  margin-bottom: 16px;
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
  transition: all 0.2s ease;
}

.empty-create-btn:hover {
  background-color: #2563eb;
  transform: translateY(-1px);
}

@media (max-width: 768px) {
  .output-management {
    padding: 16px;
  }

  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-group {
    width: 100%;
    justify-content: space-between;
  }

  .category-select {
    min-width: 150px;
    flex: 1;
  }

  .search-group {
    width: 100%;
    justify-content: space-between;
  }

  .search-input {
    width: 100%;
    margin-right: 10px;
  }

  .note-title-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .note-meta {
    align-self: flex-start;
  }

  .notes-stats {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .note-item {
    padding: 16px;
  }

  .note-title {
    font-size: 15px;
  }

  .update-time {
    font-size: 12px;
  }
}

@media (max-width: 480px) {
  .filter-group {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .category-select {
    width: 100%;
  }

  .search-group {
    flex-direction: column;
    gap: 8px;
  }

  .search-input {
    margin-right: 0;
  }

  .note-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
}
</style>
