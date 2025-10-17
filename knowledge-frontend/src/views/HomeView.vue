<template>
  <div class="home-container">
    <!-- 顶部导航栏 -->
    <el-header class="top-nav">
      <div class="logo">
        <el-icon class="logo-icon"><Book /></el-icon>
        <span class="logo-text">知识管理系统</span>
      </div>

      <!-- 用户信息区域（删除搜索框后调整布局） -->
      <div class="user-info">
        <div class="avatar-container"
             @mouseenter="showUserCard = true; isMouseOverCard = false"
             @mouseleave="handleAvatarLeave">
          <el-badge :value="unreadCount" class="user-badge" is-dot>
            <el-avatar class="user-avatar">
              <img :src="user.avatar || 'https://picsum.photos/200/200'" alt="用户头像" />
            </el-avatar>
          </el-badge>

          <!-- 用户信息悬浮框 -->
          <div v-if="showUserCard"
               class="user-card"
               @mouseenter="isMouseOverCard = true; showUserCard = true"
               @mouseleave="handleCardLeave">
            <div class="user-card-header">
              <div class="avatar-large">
                <img :src="user.avatar || 'https://picsum.photos/200/200'" alt="用户头像" />
              </div>
              <div class="user-info-main">
                <h3>{{ user.name || '未知用户' }}</h3>
                <button class="profile-btn" @click.stop="handleProfile">
                  <el-icon><User /></el-icon>
                  <span>个人中心</span>
                </button>
              </div>
            </div>
            <div class="user-card-body">
              <div class="info-item">
                <span class="info-label">邮箱：</span>
                <span class="info-value">{{ user.email || '未设置' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">手机：</span>
                <span class="info-value">{{ user.phone || '未设置' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">注册时间：</span>
                <span class="info-value">{{ formatDate(user.created_at) || '未知' }}</span>
              </div>
            </div>
            <div class="user-card-footer">
              <el-dropdown
                  v-model:visible="userMenuVisible"
                  placement="bottom-end"
                  @visible-change="handleMenuVisibleChange"
                  trigger="click"
              >
                <el-button type="text" class="dropdown-trigger">
                  <el-icon><ChevronDown /></el-icon>
                  <span>更多选项</span>
                </el-button>

                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="handleSetting">
                      <el-icon><Setting /></el-icon>
                      <span>设置</span>
                    </el-dropdown-item>
                    <el-dropdown-item @click="handleHelp">
                      <el-icon><Help /></el-icon>
                      <span>帮助</span>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>

              <el-button
                  type="text"
                  class="logout-btn"
                  @click="handleLogout"
              >
                <el-icon><Logout /></el-icon>
                <span>退出登录</span>
              </el-button>

            </div>
          </div>
        </div>
      </div>
    </el-header>

    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 左侧功能栏 - 添加便签菜单 -->
      <el-aside class="sidebar">
        <el-menu
            default-active="home"
            class="sidebar-menu"
            :collapse="false"
            @select="handleMenuSelect"
        >
          <el-menu-item index="home">
            <el-icon><Home /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-sub-menu index="knowledge">
            <template #title>
              <el-icon><FolderOpened /></el-icon>
              <span>知识库</span>
            </template>
            <el-menu-item index="knowledge-category" @click="handleCategoryClick">
              <el-icon><Grid /></el-icon>
              <span>分类</span>
            </el-menu-item>
            <el-menu-item index="inspiration" @click="handleInspirationClick">
              <el-icon><Lightning /></el-icon>
              <span>便签</span>
            </el-menu-item>
            <el-menu-item index="tags" @click="handleTagClick">
              <el-icon><Tickets /></el-icon>
              <span>标签</span>
            </el-menu-item>
            <el-menu-item index="knowledge-map">
              <el-icon><Link /></el-icon>
              <span>图谱</span>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item index="todo">
            <el-icon><Check /></el-icon>
            <span>待办事项</span>
          </el-menu-item>
          <el-menu-item index="output">
            <el-icon><Document /></el-icon>
            <span>我的笔记</span>
          </el-menu-item>
          <el-menu-item index="analysis">
            <el-icon><PieChart /></el-icon>
            <span>统计分析</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 中间内容区 -->
      <el-main class="content-area">
        <!-- 便签视图 - 新增内容 -->
        <template v-if="activeMenu === 'inspiration'">
          <InspirationView />
        </template>

        <template v-if="activeMenu === 'home'">
          <!-- 模块1：快速捕获 - 修改为添加便签 -->
          <el-card class="content-module quick-capture">
            <div class="module-header">
              <h3>快速捕获</h3>
            </div>
            <div class="capture-content">
              <el-button
                  type="primary"
                  class="new-inspiration-btn"
                  @click="goToInspiration"
              >
                <el-icon><Plus /></el-icon>
                添加便签
              </el-button>
              <el-input
                  v-model="inspirationNote"
                  type="textarea"
                  placeholder="灵感便签（限140字）"
                  :maxlength="140"
                  class="inspiration-input"
                  @change="saveInspiration"
              ></el-input>
            </div>
          </el-card>

          <!-- 模块2：最近笔记 -->
          <el-card class="content-module recent-notes">
            <div class="module-header">
              <h3>最近笔记</h3>
            </div>
            <div class="notes-grid">
              <div class="note-card" v-for="note in recentNotes" :key="note.id" @click="editNote(note.id)">
                <div class="note-title">{{ note.title }}</div>
                <div class="note-excerpt">{{ removeHtmlTags(note.content) }}</div>
                <div class="note-meta">
                  <span>{{ formatNoteDate(note.updatedAt) }}</span>
                  <span class="tag" v-for="tag in note.tags" :key="tag">{{ tag }}</span>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 模块3：场景模板（已注释） -->
          <!--          <el-card class="content-module scene-templates">-->
          <!--            <div class="module-header">-->
          <!--              <h3>场景模板</h3>-->
          <!--            </div>-->
          <!--            <div class="template-list">-->
          <!--              <el-card-->
          <!--                  class="template-card"-->
          <!--                  v-for="template in templates"-->
          <!--                  :key="template.id"-->
          <!--                  @click="useTemplate(template.id)"-->
          <!--              >-->
          <!--                <div class="template-icon">{{ template.icon }}</div>-->
          <!--                <h4>{{ template.name }}</h4>-->
          <!--                <p>{{ template.desc }}</p>-->
          <!--              </el-card>-->
          <!--            </div>-->
          <!--          </el-card>-->
        </template>

        <!-- 标签视图 -->
        <template v-if="activeMenu === 'tags'">
          <TagManagement />
        </template>
        <!-- 分类内容区域 -->
        <template v-if="activeMenu === 'knowledge-category'">
          <CategoryView />
        </template>

        <!-- 待办视图 -->
        <template v-if="activeMenu === 'todo'">
          <TodoView />
        </template>

        <!-- 输出管理视图 -->
        <template v-if="activeMenu === 'output'">
          <OutputManagement />
        </template>

        <!-- 图谱视图 -->
        <template v-if="activeMenu === 'knowledge-map'">
          <GraphView />
        </template>

        <!-- 统计分析视图 -->
        <template v-if="activeMenu === 'analysis'">
          <AnalysisView />
        </template>
      </el-main>

      <!-- 右侧快捷区 -->
      <el-aside
          v-if="activeMenu === 'home'"
          class="right-sidebar"
      >
        <!-- 今日待办 -->
        <el-card class="sidebar-module todo-list">
          <div class="module-header">
            <h3>今日待办</h3>
          </div>
          <div class="todo-items">
            <div class="todo-item" v-for="todo in todayTodos" :key="todo.id">
              <input type="checkbox" :id="'todo-' + todo.id" v-model="todo.completed" @change="updateTodoStatus(todo)">
              <label :for="'todo-' + todo.id" :class="{ 'completed': todo.completed }">{{ todo.text }}</label>
              <span class="due-date">{{ formatTodayTodoTime(todo.deadline) }}</span>
            </div>
            <div v-if="todayTodos.length === 0" class="empty-state">
              今日暂无待办事项
            </div>
          </div>
          <button class="view-all-todos" @click="navigateTo('todo')">
            查看全部待办 <el-icon><ArrowRight /></el-icon>
          </button>
        </el-card>

        <!-- 常用标签 -->
        <el-card class="sidebar-module common-tags">
          <div class="module-header">
            <h3>常用标签</h3>
          </div>
          <div class="tags-container">
            <el-tag
                v-for="tag in commonTags"
                :key="tag.id"
                @click="filterByTag(tag.name)"
                class="common-tag"
            >
              <el-icon size="14"><Tag /></el-icon>
              <span>{{ tag.name }}</span>
              <span class="tag-count">({{ tag.count }})</span>
            </el-tag>
          </div>
          <div v-if="commonTags.length === 0" class="empty-state">
            暂无常用标签
          </div>
        </el-card>
      </el-aside>
    </div>

    <!-- 创建按钮 -->
    <div class="create-container" ref="createContainerRef">
      <button class="create-btn" @click.stop="showCreateMenu = !showCreateMenu">
        <el-icon><Plus /></el-icon>
      </button>

      <div v-if="showCreateMenu" class="create-menu">
        <button class="menu-item" @click="navigateTo('note')">
          <el-icon><Document /></el-icon> 新建笔记
        </button>
        <button class="menu-item" @click="navigateTo('todo')">
          <el-icon><Check /></el-icon> 新建待办
        </button>
        <button class="menu-item" @click="goToInspiration">
          <el-icon><Lightning /></el-icon> 添加便签
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import AnalysisView from './AnalysisView.vue'
import GraphView from './GraphView.vue'
import OutputManagement from './OutputManagementView.vue'
import TodoView from './TodoView.vue'
import { useStore } from 'vuex'
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
import CategoryView from './CategoryView.vue'
import TagManagement from '../components/TagManagement.vue';
// 导入新增的便签组件
import InspirationView from './InspirationView.vue';
// 导入所需图标（已移除Search图标）
import {
  Book, Setting, Help, Logout, Home, FolderOpened,
  Grid, Link, Document, PieChart, Plus,
  User, ChevronDown, Tag, Tickets, Check, ArrowRight,
  Lightning
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
// 悬浮框控制变量
const showUserCard = ref(false)
const showCreateMenu = ref(false)
const createContainerRef = ref(null)

// 日期格式化函数
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });
};

const activeMenu = ref('home')
// 监听路由参数变化
watch(() => route.query.menu, (newMenu) => {
  if (newMenu && ['home', 'knowledge-category', 'tags', 'todo', 'inspiration'].includes(newMenu)) {
    activeMenu.value = newMenu
  }
}, { immediate: true })

const user = ref({
  name: '',
  avatar: 'https://picsum.photos/200/200',
  email: ''
})

// 用户菜单相关（已移除搜索相关变量）
const userMenuVisible = ref(false)
const unreadCount = ref(2)

// 内容区数据
const inspirationNote = ref('') // 快速捕获的便签内容
const recentNotes = ref([])
const todayTodos = ref([])

// 右侧快捷区数据
const commonTags = ref([
  { name: 'JavaScript', count: 12 },
  { name: 'Vue3', count: 8 },
  { name: 'Node.js', count: 6 },
  { name: 'MySQL', count: 5 },
  { name: '算法', count: 4 }
])

// 在setup函数中获取store实例
const store = useStore()

// 初始化
onMounted(() => {
  fetchRecentNotes();
  fetchTodayTodos();
  fetchCommonTags();

  const token = localStorage.getItem('token');
  const userInfo = localStorage.getItem('user');

  if (!token || !userInfo) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  if (route.query.menu) {
    activeMenu.value = route.query.menu;
  }
  try {
    const parsedUser = JSON.parse(userInfo);
    console.log('解析用户信息成功:', parsedUser);

    user.value = {
      ...user.value,
      id: parsedUser.id || parsedUser.userId,
      name: parsedUser.username || '用户',
      avatar: parsedUser.avatar || 'https://picsum.photos/200/200',
      email: parsedUser.email || '',
      phone: parsedUser.phone || '',
      created_at: parsedUser.createdAt || parsedUser.created_at || ''
    };

  } catch (e) {
    console.error('用户信息解析失败:', e);
    localStorage.removeItem('user');
    localStorage.removeItem('token');
    ElMessage.error('登录状态异常，请重新登录');
    router.push('/login');
  }

  // 点击外部关闭创建菜单
  const handleClickOutside = (event) => {
    if (createContainerRef.value && !createContainerRef.value.contains(event.target)) {
      showCreateMenu.value = false;
    }
  };
  document.addEventListener('click', handleClickOutside);
});

// 事件处理（已移除handleSearch函数）
const handleMenuVisibleChange = (visible) => {
  userMenuVisible.value = visible
}

const handleSetting = () => {
  userMenuVisible.value = false
  ElMessage.info('进入设置页面')
  router.push('/settings')
}

const handleHelp = () => {
  userMenuVisible.value = false
  ElMessage.info('打开帮助中心')
  router.push('/help')
}

const handleLogout = async () => {
  try {
    await request.post('/user/logout', {}, {
      validateStatus: () => true
    })
  } catch (error) {
    console.log('退出登录接口调用失败:', error)
  } finally {
    localStorage.removeItem('user')
    localStorage.removeItem('token')
    store.dispatch('logout')
    ElMessage.success('退出登录成功')
    router.push('/login')
  }
};

// 修改菜单选择处理，同时更新URL
const handleMenuSelect = (index) => {
  activeMenu.value = index
  if (index === 'home') {
    router.push('/home')
  } else {
    router.push(`/home?menu=${index}`)
  }
}

// 跳转到便签页面
const goToInspiration = () => {
  activeMenu.value = 'inspiration'
  router.push('/home?menu=inspiration')
  showCreateMenu.value = false
}

// 保存快速捕获的便签
const saveInspiration = async () => {
  if (inspirationNote.value.trim()) {
    try {
      const userId = user.value.id
      if (!userId) {
        ElMessage.warning('请先登录')
        return
      }

      const response = await request.post('/inspiration/add', {
        content: inspirationNote.value.trim()
      }, {
        params: { userId }
      })

      if (response.data.code === 200) {
        ElMessage.success('灵感便签已保存')
        inspirationNote.value = ''
      } else {
        ElMessage.error(response.data.msg || '保存失败')
      }
    } catch (error) {
      console.error('保存便签失败:', error)
      ElMessage.error('保存便签失败')
    }
  }
}

// 标签筛选事件
const filterByTag = (tagName) => {
  router.push({
    path: '/home',
    query: {
      menu: 'tags',
      tag: tagName
    }
  });
  ElMessage.info(`筛选标签: ${tagName}`);
};

const editNote = (id) => {
  router.push(`/editor/${id}`)
}

const handleCategoryClick = () => {
  activeMenu.value = 'knowledge-category'
}

// 便签菜单点击事件
const handleInspirationClick = () => {
  activeMenu.value = 'inspiration'
}

// 标签菜单点击事件
const handleTagClick = () => {
  activeMenu.value = 'tags'
}

// 个人中心跳转
const handleProfile = () => {
  const token = localStorage.getItem('token');
  const userInfo = localStorage.getItem('user');

  if (!token || !userInfo) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }

  try {
    const parsedUser = JSON.parse(userInfo);
    const userId = parsedUser.id || parsedUser.userId;
    if (!userId) {
      throw new Error('用户信息不完整，缺少ID');
    }

    setTimeout(() => {
      router.push('/profile')
          .then(() => {
            console.log('跳转至个人中心成功');
            showUserCard.value = false;
          })
          .catch(err => {
            console.error('跳转个人中心失败:', err);
          });
    }, 100);

  } catch (e) {
    console.error('用户信息解析失败:', e.message);
    localStorage.removeItem('user');
    localStorage.removeItem('token');
    ElMessage.error('登录状态异常，请重新登录');
    router.push('/login');
  }
};

const handleCardLeave = () => {
  isMouseOverCard.value = false;
  showUserCard.value = false;
};

// 跟踪鼠标是否在悬浮窗上
const isMouseOverCard = ref(false);

// 处理头像区域鼠标离开
const handleAvatarLeave = () => {
  setTimeout(() => {
    if (!isMouseOverCard.value) {
      showUserCard.value = false;
    }
  }, 200);
};

// 格式化笔记日期
const formatNoteDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('zh-CN', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
};

// 今日待办时间格式化
const formatTodayTodoTime = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  });
};

// 移除HTML标签
const removeHtmlTags = (html) => {
  if (!html) return '';
  return html.replace(/<[^>]*>/g, '');
};

// 获取最近笔记
const fetchRecentNotes = async () => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('user') || '{}');
    if (!userInfo.id) {
      console.error('未获取到用户信息');
      return;
    }

    const response = await request.get('/note/recent', {
      params: { userId: userInfo.id }
    });

    if (response.data.code === 200) {
      const sortedNotes = response.data.data.sort((a, b) => {
        return new Date(b.updateTime || b.createTime) - new Date(a.updateTime || a.createTime);
      });
      recentNotes.value = sortedNotes.slice(0, 6);
    }
  } catch (error) {
    console.error('获取最近笔记失败:', error);
  }
};

// 获取今日待办事项
const fetchTodayTodos = async () => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('user') || '{}');
    if (!userInfo.id) {
      console.error('未获取到用户信息');
      return;
    }

    const response = await request.get('/todo/list', {
      params: { userId: userInfo.id }
    });

    if (response.data.code === 200) {
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      const tomorrow = new Date(today);
      tomorrow.setDate(today.getDate() + 1);

      // 筛选出今天的待办事项
      const todayTodosData = response.data.data.filter(item => {
        if (!item.deadline) return false;
        const todoDate = new Date(item.deadline);
        return todoDate >= today && todoDate < tomorrow;
      });

      const formattedTodos = todayTodosData.map(item => ({
        id: item.id,
        text: item.title,
        completed: item.status === 1,
        deadline: item.deadline,
        priority: ['low', 'medium', 'high'][item.priority]
      }));

      // 按截止时间排序
      todayTodos.value = formattedTodos.sort((a, b) => {
        if (!a.deadline) return 1;
        if (!b.deadline) return -1;
        return new Date(a.deadline) - new Date(b.deadline);
      });
    }
  } catch (error) {
    console.error('获取今日待办失败:', error);
  }
};

// 更新待办状态
const updateTodoStatus = async (todo) => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('user') || '{}');
    if (!userInfo.id) {
      console.error('未获取到用户信息');
      todo.completed = !todo.completed;
      ElMessage.warning('请先登录');
      return;
    }

    const response = await request.put('/todo/update', {
      id: todo.id,
      title: todo.text,
      status: todo.completed ? 1 : 0,
      priority: ['low', 'medium', 'high'].indexOf(todo.priority),
      deadline: todo.deadline
    }, {
      params: { userId: userInfo.id }
    });

    if (response.data.code !== 200) {
      todo.completed = !todo.completed;
      ElMessage.error('更新待办状态失败');
    } else {
      fetchTodayTodos();
    }
  } catch (error) {
    todo.completed = !todo.completed;
    console.error('更新待办状态出错:', error);
    ElMessage.error('更新待办状态失败');
  }
};

// 页面导航
const navigateTo = (type) => {
  showCreateMenu.value = false;
  if (type === 'note') {
    router.push('/editor').then(() => {
      console.log('跳转至新建笔记页面成功');
    }).catch(err => {
      console.error('笔记跳转失败:', err);
    });
  } else if (type === 'todo') {
    activeMenu.value = 'todo';
    router.push('/home?menu=todo').then(() => {
      console.log('切换至首页待办视图成功');
    }).catch(err => {
      console.error('待办跳转失败:', err);
    });
  }
};

// 获取常用标签
const fetchCommonTags = async () => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('user') || '{}');
    if (!userInfo.id) {
      console.error('未获取到用户信息');
      return;
    }

    const response = await request.get('/tag/common', {
      params: {userId: userInfo.id, limit: 5}
    });

    if (response.data.code === 200) {
      commonTags.value = response.data.data;
    } else if (response.data.code === 403) {
      ElMessage.error('没有获取标签的权限');
    } else {
      ElMessage.error(`获取标签失败: ${response.data.msg || '未知错误'}`);
    }
  } catch (error) {
    console.error('获取常用标签失败:', error);
  }
};
</script>

<style scoped>
/* 原有样式保持不变，已移除搜索框相关样式 */
.home-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}

/* 顶部导航栏（优化用户信息区域布局） */
.top-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #165DFF;
  color: white;
  padding: 0 20px;
  height: 60px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-icon {
  font-size: 24px;
}

.logo-text {
  font-size: 18px;
  font-weight: bold;
}

/* 用户信息区域（新增margin-left: auto，确保靠右对齐） */
.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-left: auto;
}

.user-avatar {
  cursor: pointer;
  transition: transform 0.2s;
}

.user-avatar:hover {
  transform: scale(1.05);
}

/* 主内容区 */
.main-content {
  display: flex;
  flex: 1;
  overflow: hidden;
}

/* 左侧功能栏 */
.sidebar {
  width: 220px;
  background-color: #f8fafc;
  border-right: 1px solid #e2e8f0;
}

.sidebar-menu {
  border-right: none;
  height: 100%;
}

/* 中间内容区 */
.content-area {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background-color: #f1f5f9;
}

.content-module {
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
}

.module-header {
  padding: 12px 20px;
  border-bottom: 1px solid #e2e8f0;
}

.module-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

/* 快速捕获模块 - 便签样式 */
.quick-capture .capture-content {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.new-inspiration-btn {
  align-self: flex-start;
}

.inspiration-input {
  resize: none;
  min-height: 80px;
}

/* 其余样式保持不变 */
.recent-notes {
  padding: 20px;
}

.notes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.note-card {
  background-color: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: transform 0.2s, box-shadow 0.2s;
  cursor: pointer;
}

.note-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.note-title {
  font-weight: 600;
  margin-bottom: 8px;
  color: #1f2937;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.note-excerpt {
  font-size: 14px;
  color: #4b5563;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.note-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: #9ca3af;
}

.tag {
  background-color: #eff6ff;
  color: #3b82f6;
  padding: 2px 6px;
  border-radius: 4px;
  margin-left: 4px;
}

/* 模板相关样式（已注释，保留代码防止误删） */
/* .template-list {
  padding: 20px;
  display: flex;
  gap: 20px;
}

.template-card {
  flex: 1;
  cursor: pointer;
  text-align: center;
  padding: 20px;
  transition: all 0.2s;
}

.template-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.template-icon {
  font-size: 36px;
  margin-bottom: 10px;
}

.template-card h4 {
  margin: 0 0 5px 0;
}

.template-card p {
  font-size: 12px;
  color: #64748b;
  margin: 0;
} */

.right-sidebar {
  width: 300px;
  border-left: 1px solid #e2e8f0;
  background-color: #f8fafc;
  overflow-y: auto;
  padding: 20px;
}

.sidebar-module {
  margin-bottom: 20px;
}

.todo-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.todo-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f3f4f6;
  gap: 12px;
}

.todo-item:last-child {
  border-bottom: none;
}

.todo-item input[type="checkbox"] {
  width: 18px;
  height: 18px;
  accent-color: #3b82f6;
}

.todo-item label {
  flex: 1;
  color: #1f2937;
  transition: color 0.2s, text-decoration 0.2s;
}

.todo-item label.completed {
  color: #9ca3af;
  text-decoration: line-through;
}

.due-date {
  font-size: 12px;
  color: #6b7280;
  background-color: #f3f4f6;
  padding: 2px 8px;
  border-radius: 12px;
}

.view-all-todos {
  margin-top: 16px;
  padding: 8px 16px;
  background-color: #f0f5ff;
  color: #3b82f6;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  transition: background-color 0.2s;
  width: 100%;
  justify-content: center;
}

.view-all-todos:hover {
  background-color: #e1ebff;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 10px 0;
}

.common-tag {
  cursor: pointer;
  transition: all 0.2s;
}

.common-tag:hover {
  transform: scale(1.05);
}

.tag-count {
  font-size: 12px;
  margin-left: 4px;
}

.empty-state {
  text-align: center;
  padding: 20px 0;
  color: #9ca3af;
  font-size: 14px;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
}

.dropdown-trigger {
  color: white;
  padding: 0;
  margin-left: 5px;
}

.dropdown-trigger .el-icon {
  font-size: 18px;
}

.avatar-container {
  position: relative;
  display: inline-block;
}

.user-card {
  position: absolute;
  top: 100%;
  right: 0;
  width: 300px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  padding: 15px;
  margin-top: 10px;
  z-index: 1000;
  color: #333;
}

.user-card-header {
  display: flex;
  align-items: center;
  gap: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
  margin-bottom: 10px;
}

.avatar-large img {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
}

.profile-btn {
  background: #165DFF;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 5px 10px;
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
  margin-top: 5px;
}

.profile-btn:hover {
  background: #0d47a1;
}

.user-card-body {
  padding: 5px 0;
}

.info-item {
  display: flex;
  margin-bottom: 8px;
  font-size: 14px;
}

.info-label {
  color: #666;
  width: 80px;
  flex-shrink: 0;
}

.info-value {
  flex: 1;
  word-break: break-all;
}

.user-card-footer {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #eee;
  text-align: right;
}

.user-card-footer {
  display: flex;
  justify-content: space-between;
  padding: 15px 20px;
  border-top: 1px solid #f1f5f9;
}

.logout-btn {
  color: #ff4d4f;
}

.create-container {
  position: fixed;
  right: 40px;
  bottom: 40px;
  z-index: 1000;
}

.create-btn {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background-color: #3b82f6;
  color: white;
  font-size: 16px;
  border: none;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.create-btn:hover {
  background-color: #2563eb;
  transform: scale(1.05);
}

.create-menu {
  position: absolute;
  bottom: 70px;
  right: 0;
  width: 160px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  padding: 8px 0;
  z-index: 1001;
}

.menu-item {
  width: 100%;
  padding: 10px 16px;
  text-align: left;
  background: none;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #333;
}

.menu-item:hover {
  background-color: #f0f5ff;
  color: #3b82f6;
}
</style>