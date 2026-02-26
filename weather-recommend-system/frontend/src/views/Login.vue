<template>
  <div class="login-container">
    <div class="login-card animate-up">
      <div class="header">
        <div class="logo">🌤️</div>
        <h2>天气生活助手</h2>
        <p class="subtitle">您的智能生活管家</p>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" size="large">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名">
            <template #prefix><el-icon><User /></el-icon></template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码">
            <template #prefix><el-icon><Lock /></el-icon></template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin">
            立即登录
          </el-button>
        </el-form-item>

        <div class="form-footer" style="display: flex; justify-content: space-between; margin-bottom: 20px;">
          <el-button link type="info" size="small" @click="$router.push('/reset-pwd')">忘记密码?</el-button>
          <el-button link type="primary" size="small" @click="$router.push('/register')">还没账号? 去注册</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
// 注意：确保你的 api/user.js 里的 login 方法是用 request.post 实现的
// 如果你之前直接用的 request，也可以把 import { login } 换成 import request from '../utils/request'
import { login } from '../api/user'
import { setToken } from '../utils/auth'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await login(form)

        if (res.code === 200) {
          // ✅ 核心修改开始
          const { token, user } = res.data

          // 1. 保存 Token
          setToken(token)

          // 2. 保存用户信息 (这是前端判断权限的关键)
          localStorage.setItem('user', JSON.stringify(user))

          ElMessage.success('登录成功')

          // 3. 根据角色进行跳转
          if (user.role === 'admin') {
            console.log("检测到管理员登录，跳转至后台")
            router.push('/admin')
          } else {
            console.log("检测到普通用户登录，跳转至首页")
            router.push('/home')
          }
          // ✅ 核心修改结束

        } else {
          ElMessage.error(res.msg || '登录失败')
        }
      } catch (error) {
        console.error(error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container { height: 100vh; display: flex; justify-content: center; align-items: center; background-color: #f2f2f7; }
.login-card { width: 400px; background: #fff; border-radius: 24px; padding: 40px; box-shadow: 0 10px 30px rgba(0,0,0,0.05); text-align: center; }
.logo { font-size: 48px; margin-bottom: 10px; }
.header h2 { margin: 0 0 10px 0; color: #1c1c1e; }
.subtitle { color: #8e8e93; margin-bottom: 30px; font-size: 14px; }
.login-btn { width: 100%; border-radius: 12px; font-weight: 600; }
.animate-up { animation: fadeInUp 0.5s ease-out; }
@keyframes fadeInUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
</style>