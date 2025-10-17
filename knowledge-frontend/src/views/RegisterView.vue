<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <div class="register-header">
          <h2>账号注册</h2>
          <p>创建您的知识管理系统账号</p>
        </div>
      </template>

      <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          class="register-form"
      >
        <el-form-item prop="username">
          <el-input
              v-model="registerForm.username"
              placeholder="请输入用户名"
              prefix-icon="User"
          ></el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
          ></el-input>
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请确认密码"
              prefix-icon="Lock"
          ></el-input>
        </el-form-item>

        <el-form-item prop="email">
          <el-input
              v-model="registerForm.email"
              placeholder="请输入邮箱"
              prefix-icon="Message"
          ></el-input>
        </el-form-item>

        <el-form-item>
          <el-button
              type="primary"
              class="register-btn"
              @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>

        <div class="login-link">
          已有账号?
          <el-link type="primary" @click="$router.push('/login')">
            立即登录
          </el-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const registerFormRef = ref(null)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: ''
})

const registerRules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: ['blur', 'change'] }
  ]
})

const handleRegister = async () => {
  try {
    // 表单验证
    await registerFormRef.value.validate()

    // 发送注册请求
    const response = await request.post('/user/register', registerForm)

    ElMessage.success(response || '注册成功')
    // 注册成功后跳转到登录页
    router.push('/login')
  } catch (error) {
    if (error.response) {
      ElMessage.error(error.response.data || '注册失败')
    } else {
      ElMessage.error('注册失败，请稍后重试')
    }
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.register-card {
  width: 500px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.register-header {
  text-align: center;
}

.register-form {
  padding: 20px;
}

.register-btn {
  width: 100%;
}

.login-link {
  text-align: center;
  margin-top: 15px;
}
</style>
