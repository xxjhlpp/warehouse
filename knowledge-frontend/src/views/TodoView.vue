<template>
  <div class="knowledge-todo" :class="{ editing: editingTaskId }">
    <!-- 快速添加 -->
    <div class="quick-add">
      <input
          v-model="newTask"
          :placeholder="editingTaskId ? '编辑任务...' : '添加学习任务、阅读计划...'"
          @keyup.enter="addTask"
          class="task-input"
          title="编辑"
      >
      <!-- 新增：添加/更新任务按钮 -->
      <button
          @click="addTask"
          class="add-task-btn"
      >
        {{ editingTaskId ? '更新任务' : '添加任务' }}
      </button>
      <button @click="showAdvanced = !showAdvanced" class="advanced-btn">
        <i class="el-icon-more"></i>
        <span class="btn-text">高级选项</span>
      </button>
      <!-- 取消编辑按钮 -->
      <button
          v-if="editingTaskId"
          @click="resetForm"
          class="cancel-edit-btn"
          title="取消编辑"
      >
        <i class="el-icon-close"></i>
        <span class="btn-text">取消</span>
      </button>
    </div>

    <!-- 高级选项 -->
    <div v-if="showAdvanced" class="advanced-options">
      <div class="option-row">
        <label>截止时间：</label>
        <el-date-picker
            v-model="taskDeadline"
            type="datetime"
            placeholder="选择截止时间"
            :disabled-date="disabledDate"
            value-format="YYYY-MM-DDTHH:mm:ss"
            size="small"
        />
      </div>
      <div class="option-row">
        <label>关联笔记：</label>
        <el-select v-model="linkedNote" placeholder="选择关联笔记" size="small">
          <el-option
              v-for="note in recentNotes"
              :key="note.id"
              :label="note.title"
              :value="note.id"
          />
        </el-select>
      </div>
      <div class="option-row">
        <label>优先级：</label>
        <el-radio-group v-model="taskPriority" size="small">
          <el-radio-button label="low">低</el-radio-button>
          <el-radio-button label="medium">中</el-radio-button>
          <el-radio-button label="high">高</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <!-- 任务列表 -->
    <div class="task-list">
      <div
          v-for="task in filteredTasks"
          :key="task.id"
          :class="['task-item', `priority-${task.priority}`, { completed: task.completed }]"
      >
        <div class="task-main">
          <div class="task-checkbox" @click="toggleTask(task)">
            <div :class="['checkbox', { checked: task.completed }]">
              <i v-if="task.completed" class="el-icon-check"></i>
            </div>
          </div>

          <div class="task-content">
            <div class="task-text">{{ task.text }}</div>
            <div class="task-meta">
    <span v-if="task.deadline" class="deadline">
      <i class="el-icon-time"></i>
      {{ formatDate(task.deadline) }}
    </span>
              <!-- 修改关联笔记显示，添加点击事件 -->
              <span
                  v-if="task.linkedNote"
                  class="note-link"
                  @click.stop="goToNoteEditor(task.linkedNote)"
                  title="点击查看关联笔记"
              >
      <i class="el-icon-document"></i>
                <!-- 显示关联笔记的标题而非固定文本 -->
      <span class="note-title-text">{{ getLinkedNoteTitle(task.linkedNote) }}</span>
      <i class="el-icon-arrow-right el-icon--right"></i>
    </span>
              <span :class="['priority-tag', `priority-${task.priority}`]">
      {{ priorityLabels[task.priority] }}
    </span>
            </div>
          </div>
        </div>

        <!-- 编辑/删除按钮 -->
        <div class="task-actions">
          <button @click="editTask(task)" class="action-btn" title="编辑">
            <i class="el-icon-edit"></i>
            <span class="action-text">编辑</span>
          </button>
          <button @click="deleteTask(task.id)" class="action-btn delete" title="删除">
            <i class="el-icon-delete"></i>
            <span class="action-text">删除</span>
          </button>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="filteredTasks.length === 0" class="no-tasks">
        <i class="el-icon-document"></i>
        <p>暂无任务</p>
      </div>
    </div>

    <!-- 统计和筛选 -->
    <div class="todo-footer">
      <div class="stats">
        <span>{{ remainingCount }} 个待学习</span>
        <span class="divider">|</span>
        <span>{{ completedCount }} 个已完成</span>
      </div>

      <div class="filters">
        <button
            v-for="filter in filters"
            :key="filter.value"
            :class="['filter-btn', { active: activeFilter === filter.value }]"
            @click="activeFilter = filter.value"
        >
          {{ filter.label }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import dayjs from 'dayjs'
// 新增：导入路由
import { useRouter } from 'vue-router'

// 新增：初始化路由
const router = useRouter()
const newTask = ref('')
const tasks = ref([])
const activeFilter = ref('all')
const showAdvanced = ref(false)
const taskDeadline = ref('')
const linkedNote = ref('')
const taskPriority = ref('medium')
const recentNotes = ref([])
const editingTaskId = ref(null) // 编辑状态

const filters = [
  { label: '全部', value: 'all' },
  { label: '待学习', value: 'active' },
  { label: '已完成', value: 'completed' },
  { label: '高优先级', value: 'high' }
]

const priorityLabels = {
  low: '低优先级',
  medium: '中优先级',
  high: '高优先级'
}

// 计算属性
const filteredTasks = computed(() => {
  let filtered = tasks.value

  switch (activeFilter.value) {
    case 'active':
      filtered = filtered.filter(task => !task.completed)
      break
    case 'completed':
      filtered = filtered.filter(task => task.completed)
      break
    case 'high':
      filtered = filtered.filter(task => task.priority === 'high' && !task.completed)
      break
  }

  return filtered.sort((a, b) => {
    // 高优先级和临近截止时间的排前面
    const priorityOrder = { high: 3, medium: 2, low: 1 }
    return priorityOrder[b.priority] - priorityOrder[a.priority] ||
        new Date(a.deadline) - new Date(b.deadline)
  })
})

const remainingCount = computed(() =>
    tasks.value.filter(task => !task.completed).length
)

const completedCount = computed(() =>
    tasks.value.filter(task => task.completed).length
)

// 禁用过去的时间
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 24 * 60 * 60 * 1000
}

// 获取任务列表
const fetchTasks = async () => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('user') || '{}');
    if (!userInfo.id) {
      console.error('未获取到用户信息');
      ElMessage.warning('请先登录')
      return;
    }

    const response = await request.get('/todo/list', {
      params: { userId: userInfo.id },
      headers: {
        'Content-Type': 'application/json'
      }
    })

    if (response.data.code === 200) {
      tasks.value = response.data.data.map(item => ({
        id: item.id,
        text: item.title,
        completed: item.status === 1,
        priority: ['low', 'medium', 'high'][item.priority] || 'medium',
        deadline: item.deadline,
        linkedNote: item.noteId,
        createdAt: item.createdAt
      }))
    } else {
      ElMessage.error('获取任务失败：' + response.data.msg)
    }
  } catch (error) {
    ElMessage.error('获取任务列表失败')
    console.error('获取任务错误:', error)
  }
}

// 添加/更新任务
const addTask = async () => {
  if (!newTask.value.trim()) {
    ElMessage.warning('请输入任务内容')
    return
  }

  try {
    const userInfo = JSON.parse(localStorage.getItem('user') || '{}');
    if (!userInfo.id) {
      console.error('未获取到用户信息');
      ElMessage.warning('请先登录')
      return;
    }

    // 格式化截止时间为 ISO 格式
    let formattedDeadline = null
    if (taskDeadline.value) {
      formattedDeadline = dayjs(taskDeadline.value).format('YYYY-MM-DDTHH:mm:ss')
    }

    const todoDTO = {
      title: newTask.value.trim(),
      deadline: formattedDeadline,
      // 确保noteId是数字类型而非字符串，后端可能需要Long类型
      noteId: linkedNote.value ? Number(linkedNote.value) : null,
      priority: ['low', 'medium', 'high'].indexOf(taskPriority.value),
      status: 0 // 0表示未完成
    };

    let response;

    if (editingTaskId.value) {
      // 更新现有任务
      todoDTO.id = editingTaskId.value;
      response = await request.put('/todo/update', todoDTO, {
        params: {userId: userInfo.id},
        headers: {
          'Content-Type': 'application/json'
        }
      });
    } else {
      // 添加新任务
      response = await request.post('/todo/add', todoDTO, {
        params: {userId: userInfo.id},
        headers: {
          'Content-Type': 'application/json'
        }
      });
    }

    if (response.data.code === 200) {
      const message = editingTaskId.value ? '任务更新成功' : '任务添加成功';
      ElMessage.success(message);
      resetForm();
      fetchTasks(); // 重新获取任务列表
    } else {
      ElMessage.error((editingTaskId.value ? '更新' : '添加') + '失败：' + response.data.msg);
    }
  } catch (error) {
    ElMessage.error((editingTaskId.value ? '更新' : '添加') + '任务失败');
    console.error('任务操作错误详情:', error);
  }
}

// 切换任务状态
const toggleTask = async (task) => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('user') || '{}');
    if (!userInfo.id) {
      console.error('未获取到用户信息');
      ElMessage.warning('请先登录');
      return;
    }

    const newStatus = task.completed ? 0 : 1;

    const todoDTO = {
      id: task.id,
      title: task.text,
      status: newStatus,
      priority: ['low', 'medium', 'high'].indexOf(task.priority),
      deadline: task.deadline,
      noteId: task.linkedNote
    };

    const response = await request.put('/todo/update', todoDTO, {
      params: {userId: userInfo.id},
      headers: {
        'Content-Type': 'application/json'
      }
    });

    if (response.data.code === 200) {
      task.completed = !task.completed;
      ElMessage.success(`任务已标记为${task.completed ? '已完成' : '未完成'}`);
    } else {
      task.completed = !task.completed;
      ElMessage.error('更新任务状态失败：' + response.data.msg);
    }
  } catch (error) {
    task.completed = !task.completed;
    ElMessage.error('更新任务状态失败');
    console.error('更新状态错误:', error);
  }
}

// 删除任务
const deleteTask = async (id) => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('user') || '{}');
    if (!userInfo.id) {
      console.error('未获取到用户信息');
      ElMessage.warning('请先登录');
      return;
    }

    ElMessageBox.confirm('确定要删除这个任务吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      const response = await request.delete(`/todo/delete/${id}`, {
        params: {userId: userInfo.id},
        headers: {
          'Content-Type': 'application/json'
        }
      });

      if (response.data.code === 200) {
        ElMessage.success('任务已删除');
        tasks.value = tasks.value.filter(task => task.id !== id);
      } else {
        ElMessage.error('删除失败：' + response.data.msg);
      }
    }).catch(() => {
      ElMessage.info('已取消删除');
    });

  } catch (error) {
    ElMessage.error('删除任务失败');
    console.error('删除错误:', error);
  }
}

// 编辑任务
const editTask = (task) => {
  newTask.value = task.text;
  taskPriority.value = task.priority;
  taskDeadline.value = task.deadline ? dayjs(task.deadline).format('YYYY-MM-DDTHH:mm:ss') : '';
  linkedNote.value = task.linkedNote;
  editingTaskId.value = task.id;
  showAdvanced.value = true;
}

// 重置表单
const resetForm = () => {
  newTask.value = '';
  taskDeadline.value = '';
  linkedNote.value = '';
  taskPriority.value = 'medium';
  showAdvanced.value = false;
  editingTaskId.value = null;
}

const formatDate = (date) => {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

// 新增：获取关联笔记的标题
const getLinkedNoteTitle = (noteId) => {
  const note = recentNotes.value.find(note => note.id === noteId)
  return note ? note.title : '已删除笔记'
}

// 新增：跳转到笔记编辑页
const goToNoteEditor = (noteId) => {
  router.push(`/editor/${noteId}`).then(() => {
    console.log('跳转至笔记编辑页成功')
  }).catch(err => {
    console.error('笔记编辑页跳转失败:', err)
    ElMessage.error('无法打开笔记')
  })
}
// 修改：获取所有笔记并按更新时间排序
const fetchAllNotes = async () => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('user') || '{}');
    if (!userInfo.id) {
      console.error('未获取到用户信息');
      return;
    }

    const response = await request.get('/note/list', {  // 修改为获取所有笔记的接口
      params: {userId: userInfo.id},
      headers: {
        'Content-Type': 'application/json'
      }
    })
    if (response.data.code === 200) {
      // 按更新时间降序排序，最新的在前
      const sortedNotes = response.data.data.sort((a, b) => {
        return new Date(b.updateTime) - new Date(a.updateTime)
      })
      recentNotes.value = sortedNotes
    }
  } catch (error) {
    console.error('获取所有笔记失败', error)
  }
}
// 初始化
onMounted(() => {
  fetchAllNotes()
  fetchTasks()
})
</script>

<style scoped>
.knowledge-todo {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.knowledge-todo.editing {
  border: 2px solid #667eea;
}

.todo-header {
  padding: 20px 24px 16px;
  border-bottom: 1px solid #f0f0f0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.todo-header h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.subtitle {
  margin: 0;
  opacity: 0.9;
  font-size: 13px;
}

.quick-add {
  padding: 20px 24px;
  display: flex;
  gap: 12px;
  border-bottom: 1px solid #f0f0f0;
  align-items: center;
}

.task-input {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #e1e5e9;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.2s;
}

.task-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

/* 新增：添加/更新任务按钮样式 */
.add-task-btn {
  padding: 12px 20px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
}

.add-task-btn:hover {
  background: #5a6edb;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.advanced-btn {
  padding: 12px 16px;
  background: #f8f9fa;
  border: 1px solid #e1e5e9;
  border-radius: 8px;
  cursor: pointer;
  color: #667eea;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 6px;
}

.advanced-btn:hover {
  background: #667eea;
  color: white;
}

.cancel-edit-btn {
  padding: 12px 16px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 8px;
  cursor: pointer;
  color: #ef4444;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 6px;
}

.cancel-edit-btn:hover {
  background: #ef4444;
  color: white;
}

/* 按钮文本样式 */
.btn-text {
  font-size: 14px;
}

.advanced-options {
  padding: 0 24px 20px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafbfc;
}

.option-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.option-row label {
  font-size: 13px;
  color: #666;
  min-width: 70px;
}

.task-list {
  max-height: 400px;
  overflow-y: auto;
}

.task-item {
  padding: 16px 24px;
  border-bottom: 1px solid #f5f5f5;
  transition: all 0.2s;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.task-item:hover {
  background: #fafbfc;
}

.task-item.priority-high {
  border-left: 3px solid #f56565;
}

.task-item.priority-medium {
  border-left: 3px solid #ed8936;
}

.task-item.priority-low {
  border-left: 3px solid #48bb78;
}

.task-item.completed {
  opacity: 0.6;
  background: #f8f9fa;
}

.task-main {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.task-checkbox {
  margin-top: 2px;
  cursor: pointer;
  flex-shrink: 0;
}

.checkbox {
  width: 20px;
  height: 20px;
  border: 2px solid #ddd;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  font-size: 12px;
  flex-shrink: 0;
}

.checkbox.checked {
  background: #48bb78;
  border-color: #48bb78;
  color: white;
}

.task-content {
  flex: 1;
  min-width: 0;
}

.task-text {
  color: #2d3748;
  margin-bottom: 6px;
  line-height: 1.4;
  word-break: break-word;
}

.task-item.completed .task-text {
  text-decoration: line-through;
  color: #a0aec0;
}

.task-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #718096;
  flex-wrap: wrap;
  align-items: center;
}

.deadline {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* -------------- 关联笔记样式优化 -------------- */
.note-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 3px 8px;
  background-color: #f0f9ff;
  border-radius: 6px;
  color: #0284c7;
  cursor: pointer;
  transition: all 0.25s ease;
  max-width: 220px;
  white-space: nowrap;
}

/* 关联笔记图标样式 */
.note-link .el-icon-document {
  font-size: 13px;
  color: #0369a1;
}

/* 关联笔记标题文本 */
.note-title-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 12px;
  font-weight: 500;
}

/* 关联笔记右侧箭头 */
.note-link .el-icon-arrow-right {
  font-size: 11px;
  opacity: 0.7;
  transition: opacity 0.2s;
}

/* 鼠标悬浮效果 */
.note-link:hover {
  background-color: #0284c7;
  color: white;
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(2, 132, 199, 0.2);
}

.note-link:hover .el-icon-document,
.note-link:hover .el-icon-arrow-right {
  color: white;
  opacity: 1;
}

/* 任务已完成时的关联笔记样式 */
.task-item.completed .note-link {
  background-color: #f8fafc;
  color: #64748b;
}

.task-item.completed .note-link:hover {
  background-color: #64748b;
  color: white;
  box-shadow: 0 2px 6px rgba(100, 116, 139, 0.2);
}

/* -------------- 关联笔记样式优化结束 -------------- */

.priority-tag {
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 500;
}

.priority-tag.priority-high {
  background: #fed7d7;
  color: #c53030;
}

.priority-tag.priority-medium {
  background: #feebcb;
  color: #dd6b20;
}

.priority-tag.priority-low {
  background: #c6f6d5;
  color: #276749;
}

/* 任务列表操作按钮样式 */
.task-actions {
  display: flex !important;
  gap: 8px;
  opacity: 1 !important;
  visibility: visible !important;
  margin-left: auto;
  flex-shrink: 0;
}

.action-btn {
  padding: 8px 12px;
  background: #f8f9fa;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  cursor: pointer;
  color: #64748b;
  transition: all 0.3s ease;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
  width: auto;
  height: auto;
}

.action-btn:hover {
  background: #667eea;
  color: white;
  border-color: #667eea;
  transform: translateY(-1px);
}

.action-btn.delete {
  background: #fef2f2;
  border-color: #fecaca;
  color: #ef4444;
}

.action-btn.delete:hover {
  background: #ef4444;
  color: white;
  border-color: #ef4444;
}

/* 操作文本样式 */
.action-text {
  font-size: 13px;
}

/* 单独优化删除按钮文本颜色 */
.action-btn.delete .action-text {
  color: #ef4444;
}

.todo-footer {
  padding: 16px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f8f9fa;
  border-top: 1px solid #e2e8f0;
}

.stats {
  font-size: 13px;
  color: #718096;
  display: flex;
  align-items: center;
  gap: 8px;
}

.divider {
  color: #e2e8f0;
}

.filters {
  display: flex;
  gap: 8px;
}

.filter-btn {
  padding: 6px 12px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 12px;
  color: #718096;
  cursor: pointer;
  transition: all 0.2s;
}

.filter-btn.active {
  background: #667eea;
  border-color: #667eea;
  color: white;
}

.filter-btn:hover:not(.active) {
  border-color: #667eea;
  color: #667eea;
}

.empty-state {
  padding: 40px 24px;
  text-align: center;
  color: #a0aec0;
}

.empty-state i {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

/* 空状态样式 */
.no-tasks {
  padding: 40px 24px;
  text-align: center;
  color: #a0aec0;
}

.no-tasks i {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.no-tasks p {
  margin: 0;
  font-size: 14px;
}
</style>