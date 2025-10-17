<template>
  <div class="profile-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>个人中心</h1>
      <p>管理您的个人信息和账户设置</p>
    </div>

    <div class="profile-content">
      <!-- 左侧：用户信息概览 -->
      <div class="profile-sidebar">
        <div class="avatar-section">
          <div class="avatar-container">
            <img
                :src="user.avatar || defaultAvatar"
                alt="用户头像"
                class="user-avatar"
            >
            <label class="avatar-upload-btn" @click="triggerAvatarUpload">
              <i class="fas fa-camera"></i>
              <input
                  type="file"
                  ref="avatarInput"
                  accept="image/*"
                  @change="handleAvatarUpload"
                  class="hidden"
              >
            </label>
          </div>
          <h2 class="username">{{ user.username }}</h2>
          <p class="join-date">
            注册于 {{ formatDate(user.createdAt) }}
          </p>
        </div>

        <div class="stats-section">
          <div class="stat-item">
            <div class="stat-value">{{ stats.notes }}</div>
            <div class="stat-label">我的笔记</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ stats.categories }}</div>
            <div class="stat-label">分类</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ stats.tags }}</div>
            <div class="stat-label">标签</div>
          </div>
        </div>
      </div>

      <!-- 右侧：详细信息编辑 -->
      <div class="profile-main">
        <div class="profile-card">
          <div class="card-header">
            <h2>基本信息</h2>
            <button
                class="edit-btn"
                @click="toggleEditMode"
                :class="{ 'save-btn': isEditing }"
            >
              <i class="fas" :class="isEditing ? 'fa-check' : 'fa-edit'"></i>
              <span>{{ isEditing ? '保存' : '编辑' }}</span>
            </button>
          </div>

          <div class="form-grid">
            <div class="form-group">
              <label class="form-label">用户名</label>
              <input
                  type="text"
                  v-model="formData.username"
                  class="form-control"
                  :disabled="!isEditing"
              >
            </div>

            <div class="form-group">
              <label class="form-label">邮箱</label>
              <input
                  type="email"
                  v-model="formData.email"
                  class="form-control"
                  :disabled="!isEditing"
              >
            </div>

            <div class="form-group">
              <label class="form-label">手机号码</label>
              <input
                  type="tel"
                  v-model="formData.phone"
                  class="form-control"
                  :disabled="!isEditing"
                  maxlength="11"
              >
            </div>
          </div>
        </div>

        <div class="profile-card password-card">
          <div class="card-header">
            <h2>密码修改</h2>
          </div>

          <div class="form-grid">
            <div class="form-group">
              <label class="form-label">当前密码</label>
              <input
                  type="password"
                  v-model="passwordForm.currentPassword"
                  class="form-control"
              >
            </div>

            <div class="form-group">
              <label class="form-label">新密码</label>
              <input
                  type="password"
                  v-model="passwordForm.newPassword"
                  class="form-control"
              >
            </div>

            <div class="form-group">
              <label class="form-label">确认新密码</label>
              <input
                  type="password"
                  v-model="passwordForm.confirmPassword"
                  class="form-control"
              >
            </div>

            <div class="form-actions password-actions">
              <button
                  class="btn btn-primary"
                  @click="changePassword"
              >
                <i class="fas fa-key"></i>
                <span>修改密码</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

<!--    &lt;!&ndash; 成功提示 &ndash;&gt;-->
<!--    <el-toast-->
<!--        v-if="showSuccessToast"-->
<!--        message="信息更新成功"-->
<!--        type="success"-->
<!--        :duration="3000"-->
<!--        @close="showSuccessToast = false"-->
<!--    ></el-toast>-->
  </div>
</template>

<script setup>
import { BASE_API } from '../utils/request';
import { ref, reactive, onMounted} from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import request from '../utils/request'

// 路由
const router = useRouter();

// 默认头像
const defaultAvatar = 'https://picsum.photos/200/200?random=avatar';

// 状态
const isEditing = ref(false);
const avatarInput = ref(null);
// const isAdmin = ref(false);

// 用户数据
const user = reactive({
  id: '',
  username: '',
  email: '',
  password: '',
  avatar: '',
  createdAt: '',
  updatedAt: '',
  phone: '',
  roles: []
});

// 表单数据（用于编辑）
const formData = reactive({
  username: '',
  email: '',
  phone: ''
});

// 密码表单
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// 统计数据
const stats = reactive({
  notesCount: 0,
  categoriesCount: 0,
  tagCount: 0
});

// 初始化页面数据
onMounted(async () => {
  const token = localStorage.getItem('token');
  const userInfo = localStorage.getItem('user');

  if (!token || !userInfo) {
    ElMessage.error('登录已过期，请重新登录');
    router.push('/login');
    return;
  }

  try {
    const parsedUser = JSON.parse(userInfo);
    console.log('ProfileView 解析用户信息:', parsedUser);

    if (parsedUser.id === undefined || parsedUser.id === null || parsedUser.id === '') {
      throw new Error(`用户信息不完整，id为: ${parsedUser.id}`);
    }

    await fetchUserData();
    await fetchUserStats(); // 添加此行，获取统计数据
  } catch (error) {
    console.error('ProfileView 登录状态验证失败:', error.message);
    localStorage.removeItem('user');
    localStorage.removeItem('token');
    ElMessage.error('登录已过期，请重新登录');
    router.push('/login');
  }
});
// 修改 src/views/ProfileView.vue 的 fetchUserData 函数
async function fetchUserData() {
  try {
    const token = localStorage.getItem('token');
    const userInfo = JSON.parse(localStorage.getItem('user') || '{}');
    const userId = userInfo.id;

    console.log('准备请求用户详情，userId:', userId); // 新增日志

    if (!token || !userId) {
      console.log('token或userId缺失，跳转登录'); // 新增日志
      router.push('/login');
      return;
    }

    const response = await request.get('/user/info', {
      params: { userId: userId }
    });

    if (response.data && response.data.code === 200 && response.data.data) {
      Object.assign(user, response.data.data);
      formData.username = user.username;
      formData.email = user.email;
      formData.phone = user.phone || '';
    } else {
      ElMessage.error('获取用户信息失败');
      router.push('/login');
    }
  } catch (error) {
    console.error('获取用户信息失败:', error);
    if (error.response && (error.response.status === 401 || error.response.status === 403)) {
      ElMessage.error('登录已过期，请重新登录');
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      router.push('/login');
    } else {
      ElMessage.error('网络错误，请稍后重试');
    }
  }
}

// 获取用户统计数据
const fetchUserStats = async () => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('user') || '{}');
    const userId = userInfo.id;

    if (!userId) {
      router.push('/login');
      return;
    }

    const response = await request.get('/statistic/user', {
      params: { userId: userId }
    });

    if (response.data.code === 200 && response.data.data) {
      // 更新统计数据
      stats.notes = response.data.data.notes || 0;
      stats.categories = response.data.data.categories || 0;
      stats.tags = response.data.data.tags || 0;
    }
  } catch (error) {
    console.error('获取用户统计数据失败:', error);
    ElMessage.error('获取统计数据失败');
  }
};

// 切换编辑模式
const toggleEditMode = async () => {
  if (isEditing.value) {
    // 保存修改
    await saveUserProfile();
  } else {
    // 进入编辑模式
    isEditing.value = true;
  }
};

// 保存用户信息
const saveUserProfile = async () => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('user') || '{}');
    const currentUserId = userInfo.id;

    if (!currentUserId) {
      router.push('/login');
      return;
    }

    await request.put('/user/update',
        {
          id: user.id,
          username: formData.username,
          email: formData.email,
          phone: formData.phone
        },
        {
          params: {currentUserId: currentUserId}
        }
    );

    // 更新用户数据
    user.username = formData.username;
    user.email = formData.email;
    user.phone = formData.phone;

    // 更新本地存储
    userInfo.username = formData.username;
    userInfo.email = formData.email;
    userInfo.phone = formData.phone;
    localStorage.setItem('user', JSON.stringify(userInfo));

    // 退出编辑模式
    isEditing.value = false;

    // 显示成功提示
    ElMessage.success({
      message: '信息更新成功',
      duration: 3000
    });

  }  catch (error) {
    console.error('保存用户信息失败:', error);
    ElMessage.error('保存失败，请稍后重试');
  }
};

// 触发头像上传
const triggerAvatarUpload = () => {
  if (isEditing.value) {
    avatarInput.value.click();
  }
};

// 修改头像上传相关代码（ProfileView.vue #startLine: 400附近）
const handleAvatarUpload = async (e) => {
  const file = e.target.files[0];
  if (!file) return;

  // 检查token是否存在
  const token = localStorage.getItem('token');
  if (!token) {
    ElMessage.error('请先登录');
    router.push('/login');
    return;
  }

  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请上传图片文件');
    return;
  }

  try {
    // 获取用户信息并验证
    const userInfoStr = localStorage.getItem('user');
    if (!userInfoStr) {
      ElMessage.error('用户信息不存在');
      router.push('/login');
      return;
    }

    const userInfo = JSON.parse(userInfoStr);
    const userId = userInfo.id || userInfo.userId; // 兼容不同可能的ID字段名

    if (!userId) {
      ElMessage.error('用户ID不存在');
      router.push('/login');
      return;
    }

    // 1. 先上传图片文件获取URL
    const formData = new FormData();
    formData.append('file', file);
    formData.append('userId', userId); // 确保userId正确添加到FormData

    // 打印调试信息
    console.log('上传头像 - 用户ID:', userId);
    console.log('上传头像 - Token存在:', !!token);

    // 修改1: 确保上传头像接口正确传递userId
    const uploadResponse = await request.post('/upload/avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'Authorization': `Bearer ${token}`
      },
      // 对于需要URL参数的情况添加params
      params: {
        userId: userId // 有些后端需要同时在URL参数中传递userId
      }
    });

    if (uploadResponse.data.code === 200) {
      let avatarUrl = uploadResponse.data.data;

      if (!avatarUrl.startsWith('http')) {
        avatarUrl = BASE_API + avatarUrl;
      }
      // 2. 再调用用户头像更新接口
      // 修改2: 修复403错误，确保请求格式正确
      const updateResponse = await request.put('/user/avatar', null, {
        params: {
          userId: userId,
          avatarUrl: avatarUrl  // 将avatarUrl放在params中
        }
      });

      if (updateResponse.data.code === 200) {
        // 更新用户头像
        user.avatar = avatarUrl;
        // 更新本地存储
        userInfo.avatar = avatarUrl;
        localStorage.setItem('user', JSON.stringify(userInfo));
        ElMessage.success('头像更新成功');
      }
    }
  } catch (error) {
    console.error('上传头像失败:', error);
    console.error('响应数据:', error.response?.data);
    console.error('状态码:', error.response?.status);

    // 处理Token过期情况
    if (error.response?.status === 401 || error.response?.status === 403) {
      // 更详细的错误信息
      ElMessage.error(error.response?.data?.message || '登录已过期或没有权限，请重新登录');
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      router.push('/login');
    } else {
      ElMessage.error(error.response?.data?.message || '上传头像失败');
    }
  }
};

// 修改密码
const changePassword = async () => {
  // 验证表单
  if (!passwordForm.currentPassword) {
    ElMessage.warning('请输入当前密码');
    return;
  }

  if (!passwordForm.newPassword) {
    ElMessage.warning('请输入新密码');
    return;
  }

  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致');
    return;
  }

  try {
    const userInfo = JSON.parse(localStorage.getItem('user') || '{}');
    const userId = userInfo.id;

    if (!userId) {
      router.push('/login');
      return;
    }

    // 关键修复：将参数放在params中，而不是请求体
    const response = await request.put('/user/password', null, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      params: {
        userId: userId,
        oldPassword: passwordForm.currentPassword,
        newPassword: passwordForm.newPassword
      }
    });

    if (response.data.code === 200) {
      // 清空表单
      passwordForm.currentPassword = '';
      passwordForm.newPassword = '';
      passwordForm.confirmPassword = '';

      ElMessage.success('密码修改成功，请重新登录');

      // 跳转到登录页
      setTimeout(() => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        router.push('/login');
      }, 1500);
    }
  } catch (error) {
    console.error('修改密码失败:', error);
    console.error('响应数据:', error.response?.data);

    // 处理Token过期情况
    if (error.response?.status === 401 || error.response?.status === 403) {
      ElMessage.error('登录已过期或权限不足，请重新登录');
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      router.push('/login');
    } else {
      ElMessage.error(error.response?.data?.message || '修改密码失败，可能是密码错误');
    }
  }
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';

  const date = new Date(dateString);
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });
};
</script>

<style scoped>
.profile-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
  text-align: center;
}

.page-header h1 {
  font-size: 2.2rem;
  margin-bottom: 10px;
  color: #1e293b;
}

.page-header p {
  color: #64748b;
  font-size: 1.1rem;
}

.profile-content {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 30px;
}

/* 侧边栏样式 */
.profile-sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.avatar-section {
  background: white;
  border-radius: 12px;
  padding: 25px;
  text-align: center;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.avatar-container {
  position: relative;
  width: 160px;
  height: 160px;
  margin: 0 auto 20px;
}

.user-avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
  border: 4px solid #f1f5f9;
}

.avatar-upload-btn {
  position: absolute;
  bottom: 5px;
  right: 5px;
  width: 40px;
  height: 40px;
  background: #165dff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  cursor: pointer;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.avatar-upload-btn:hover {
  background: #0d47a1;
  transform: scale(1.1);
}

.username {
  font-size: 1.4rem;
  margin-bottom: 5px;
  color: #1e293b;
}

.join-date {
  color: #64748b;
  font-size: 0.9rem;
}

.stats-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.stat-item {
  text-align: center;
  padding: 10px;
}

.stat-value {
  font-size: 1.8rem;
  font-weight: bold;
  color: #165dff;
  margin-bottom: 5px;
}

.stat-label {
  color: #64748b;
  font-size: 0.9rem;
}

/* 主内容区样式 */
.profile-main {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.profile-card {
  background: white;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f1f5f9;
}

.card-header h2 {
  font-size: 1.4rem;
  color: #1e293b;
  margin: 0;
}

.edit-btn, .save-btn {
  background: #f1f5f9;
  border: none;
  border-radius: 6px;
  padding: 8px 15px;
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s ease;
}

.edit-btn:hover {
  background: #e2e8f0;
}

.save-btn {
  background: #165dff;
  color: white;
}

.save-btn:hover {
  background: #0d47a1;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-weight: 500;
  color: #334155;
  font-size: 0.95rem;
}

.form-control {
  padding: 12px 15px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 1rem;
  transition: all 0.2s ease;
}

.form-control:focus {
  outline: none;
  border-color: #165dff;
  box-shadow: 0 0 0 3px rgba(22, 93, 255, 0.1);
}

.form-control:disabled {
  background-color: #f8fafc;
  cursor: not-allowed;
}

/* 密码修改区域 */
.password-card .form-grid {
  grid-template-columns: 1fr;
  max-width: 600px;
}

.form-actions {
  margin-top: 10px;
}

.btn {
  padding: 10px 20px;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  border: none;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.btn-primary {
  background: #165dff;
  color: white;
}

.btn-primary:hover {
  background: #0d47a1;
}

.password-actions {
  margin-top: 15px;
  text-align: right;
}

.hidden {
  display: none;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .profile-content {
    grid-template-columns: 1fr;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
