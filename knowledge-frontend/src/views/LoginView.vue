<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="login-header">
          <h2>知识管理系统</h2>
          <p>请登录您的账号</p>
        </div>
      </template>

      <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
      >
        <el-form-item prop="username">
          <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              prefix-icon="User"
          ></el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
          ></el-input>
        </el-form-item>

        <el-form-item>
          <el-button
              type="primary"
              class="login-btn"
              @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>

        <div class="register-link">
          还没有账号?
          <el-link type="primary" @click="$router.push('/register')">
            立即注册
          </el-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios';
import { ElMessage } from 'element-plus'
import {debugToken} from "@/utils/request";
import store from "@/store";

const router = useRouter()

const loginFormRef = ref(null)

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
})


// src/views/LoginView.vue 中的 handleLogin 函数
// LoginView.vue - 修复登录处理
const handleLogin = async () => {
  try {
    const response = await axios.post('http://localhost:8080/user/login', {
      username: loginForm.username,
      password: loginForm.password
    });

    if (response.data.code === 200) {
      const { token, user } = response.data.data;

      if (token && user && user.id) {
        localStorage.setItem('token', token);
        localStorage.setItem('user', JSON.stringify(user));
        // 新增：同步用户状态到 Vuex
        store.dispatch('setUser', user);
        // 调试：立即验证token
        console.log('登录成功，存储的token:', token);

        // 立即验证token是否有效
        setTimeout(async () => {
          const validation = await debugToken();
          if (validation && validation.valid) {
            console.log('Token验证成功，跳转到首页');
            ElMessage.success('登录成功');
            router.push('/home');
          } else {
            console.error('Token验证失败，请检查后端配置');
            ElMessage.error('登录状态异常，请联系管理员');
          }
        }, 100);
      }
    } else {
      ElMessage.error(response.data.msg || '登录失败');
    }
  } catch (error) {
    console.error('登录错误:', error);
    const errorMsg = error.response?.data?.msg || '登录失败，请稍后重试';
    ElMessage.error(errorMsg);
  }
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.login-card {
  width: 400px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
}

.login-form {
  padding: 20px;
}

.login-btn {
  width: 100%;
}

.register-link {
  text-align: center;
  margin-top: 15px;
}
</style>
