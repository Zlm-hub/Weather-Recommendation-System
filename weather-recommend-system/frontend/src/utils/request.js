import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const service = axios.create({
    // 这里使用 /api 前缀，需要配合 vite.config.js 的 proxy 转发
    baseURL: '/api',
    timeout: 5000
})

// 1. 请求拦截器：自动添加 Token
service.interceptors.request.use(
    (config) => {
        // 修改点：直接从 localStorage 获取 token，而不是去引用不存在的 auth.js
        const token = localStorage.getItem('token')

        if (token) {
            // 注意：后端 Security 通常需要 'Bearer ' 前缀
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    (error) => {
        return Promise.reject(error)
    }
)

// 2. 响应拦截器：统一处理错误
service.interceptors.response.use(
    (response) => {
        const res = response.data

        // 特殊情况：如果是文件流(blob)，直接返回
        if (response.config.responseType === 'blob') {
            return res
        }

        // 校验后端返回的状态码 (假设你后端 Result 成功是 code 200)
        if (res.code && res.code !== 200) {
            ElMessage.error(res.msg || '请求失败')
            return Promise.reject(new Error(res.msg || 'Error'))
        }

        return res
    },
    (error) => {
        // 处理 401 未授权（Token过期）
        if (error.response && error.response.status === 401) {
            ElMessage.error('登录状态已过期，请重新登录')

            // 清除脏 Token
            localStorage.removeItem('token')

            // 修改点：使用 window.location 强制跳转，不依赖 router 文件
            window.location.href = '/login'
        } else {
            ElMessage.error(error.message || '服务器连接异常')
        }
        return Promise.reject(error)
    }
)

export default service