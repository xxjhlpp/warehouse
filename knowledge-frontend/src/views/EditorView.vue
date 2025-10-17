<template>
  <div class="input-center-container">
    <!-- 主内容区：直接作为页面起始，无顶部导航 -->
    <main class="main-content">
      <!-- 创建按钮和菜单容器 -->
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
        </div>
      </div>

      <!-- 最近新建笔记 -->
      <section class="recent-notes">
        <h2 class="section-title">最近笔记</h2>
        <div class="notes-grid">
          <div class="note-card" v-for="note in recentNotes" :key="note.id" @click="editNote(note.id)">
          <div class="note-title">{{ note.title }}</div>
            <div class="note-excerpt">{{ removeHtmlTags(note.content) }}</div>
            <div class="note-meta">
              <span>{{ formatDate(note.updatedAt) }}</span>
              <span class="tag" v-for="tag in note.tags" :key="tag">{{ tag }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 待办事项 -->
      <section class="todo-list">
        <h2 class="section-title">最近待办</h2>
        <div class="todo-items">
          <div class="todo-item" v-for="todo in recentTodos" :key="todo.id">
            <input type="checkbox" :id="'todo-' + todo.id" v-model="todo.completed" @change="updateTodoStatus(todo)">
            <label :for="'todo-' + todo.id" :class="{ 'completed': todo.completed }">{{ todo.text }}</label>
            <span class="due-date">{{ formatTodoDate(todo.deadline) }}</span>
          </div>
          <div v-if="recentTodos.length === 0" class="empty-todos">
            暂无待办事项，点击右下角按钮添加
          </div>
        </div>
        <button class="view-all-todos" @click="navigateTo('todo')">
          查看全部待办 <el-icon><ArrowRight /></el-icon>
        </button>
      </section>
    </main>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import request from '@/utils/request';
import { Plus, Document, Check, ArrowRight } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

export default {
  components: {
    Plus,
    Document,
    Check,
    ArrowRight
  },
  setup() {
    const router = useRouter();
    const showCreateMenu = ref(false);
    const createContainerRef = ref(null);
    const recentNotes = ref([]);
    const recentTodos = ref([]); // 存储最近待办事项

    // 格式化笔记日期
    const formatDate = (dateString) => {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleDateString('zh-CN', {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    };

    // 格式化待办日期
    const formatTodoDate = (dateString) => {
      if (!dateString) return '无截止日期';
      const date = new Date(dateString);
      const today = new Date();
      today.setHours(0, 0, 0, 0);

      const diffTime = date - today;
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

      if (diffDays === 0) {
        return '今天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
      } else if (diffDays === 1) {
        return '明天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
      } else if (diffDays < 7) {
        const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
        return weekdays[date.getDay()] + ' ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
      } else {
        return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' });
      }
    };

    const removeHtmlTags = (html) => {
      if (!html) return '';
      return html.replace(/<[^>]*>/g, '');
    };

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
          recentNotes.value = sortedNotes.slice(0, 3);
        }
      } catch (error) {
        console.error('获取最近笔记失败:', error);
      }
    };

    // 获取最近待办事项（最多3条）
    const fetchRecentTodos = async () => {
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
          // 转换待办数据格式并按截止日期排序
          const formattedTodos = response.data.data.map(item => ({
            id: item.id,
            text: item.title,
            completed: item.status === 1,
            deadline: item.deadline,
            priority: ['low', 'medium', 'high'][item.priority]
          }));

          // 按截止日期升序排序，最近的排在前面
          const sortedTodos = formattedTodos.sort((a, b) => {
            if (!a.deadline) return 1;
            if (!b.deadline) return -1;
            return new Date(a.deadline) - new Date(b.deadline);
          });

          // 只取前3条
          recentTodos.value = sortedTodos.slice(0, 3);
        }
      } catch (error) {
        console.error('获取最近待办失败:', error);
      }
    };

    // 更新待办状态
    const updateTodoStatus = async (todo) => {
      try {
        const userInfo = JSON.parse(localStorage.getItem('user') || '{}');
        if (!userInfo.id) {
          console.error('未获取到用户信息');
          todo.completed = !todo.completed; // 回滚状态
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
          todo.completed = !todo.completed; // 回滚状态
          ElMessage.error('更新待办状态失败');
        }
      } catch (error) {
        todo.completed = !todo.completed; // 回滚状态
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
        router.push('/home?menu=todo').then(() => {
          console.log('跳转至首页待办视图成功');
        }).catch(err => {
          console.error('待办跳转失败:', err);
        });
      }
    };

// 添加跳转方法
    const editNote = (id) => {
      router.push(`/editor/${id}`);
    }
    // 页面挂载时获取数据
    onMounted(() => {
      fetchRecentNotes();
      fetchRecentTodos(); // 获取最近待办

      // 点击外部关闭创建菜单
      const handleClickOutside = (event) => {
        if (createContainerRef.value && !createContainerRef.value.contains(event.target)) {
          showCreateMenu.value = false;
        }
      };
      document.addEventListener('click', handleClickOutside);
      return () => {
        document.removeEventListener('click', handleClickOutside);
      };
    });

    return {
      showCreateMenu,
      createContainerRef,
      recentNotes,
      recentTodos,
      formatDate,
      formatTodoDate,
      removeHtmlTags,
      navigateTo,
      updateTodoStatus,
      editNote
    };
  }
};
</script>

<style scoped>
.input-center-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 20px;
  position: relative; /* 为绝对定位子元素提供参考 */
}

/* 新增：创建按钮和菜单的容器 */
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

/* 修改：调整菜单定位 */
.create-menu {
  position: absolute;
  bottom: 70px; /* 在按钮上方 */
  right: 0;
  width: 160px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.15);
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

/* 其他样式保持不变 */
.section-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #1f2937;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-title::before {
  content: '';
  width: 4px;
  height: 20px;
  background-color: #3b82f6;
  border-radius: 2px;
}

.recent-notes {
  margin-bottom: 40px;
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
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  transition: transform 0.2s, box-shadow 0.2s;
  cursor: pointer;
}

.note-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.12);
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

.todo-list {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
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

/* 新增查看全部待办按钮样式 */
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
}

/* 添加空状态样式 */
.empty-todos {
  text-align: center;
  padding: 20px;
  color: #9ca3af;
  font-size: 14px;
}
</style>
