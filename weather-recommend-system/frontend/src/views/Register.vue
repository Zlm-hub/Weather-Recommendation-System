<template>
  <div class="login-container">
    <div class="login-card animate-up">
      <div class="header">
        <div class="logo">🌤️</div>
        <h2>注册账号</h2>
        <p class="subtitle">欢迎加入天气生活助手</p>
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

        <el-form-item prop="location">
          <el-input v-model="form.location" placeholder="所在城市 (例如: 上海)">
            <template #prefix><el-icon><Location /></el-icon></template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" class="login-btn" :loading="loading" @click="handleRegister">
            立即注册
          </el-button>
        </el-form-item>

        <div class="footer-links">
          <el-button link @click="$router.push('/login')">已有账号？去登录</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Location } from '@element-plus/icons-vue'
import { register } from '../api/user'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  location: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  location: [{ required: true, message: '请输入城市', trigger: 'blur' }]
}

const handleRegister = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await register(form)
        if (res.code === 200) {
          ElMessage.success('注册成功，请登录')
          router.push('/login')
        } else {
          ElMessage.error(res.msg || '注册失败')
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
.footer-links { margin-top: 20px; }
.animate-up { animation: fadeInUp 0.5s ease-out; }
@keyframes fadeInUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
</style>