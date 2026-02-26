import axios from 'axios'
import { ElMessage } from 'element-plus'
// 如果你有路由文件，可以保留 router 引用；如果没有，下面我会用 window.location 跳转
// import router from '../router'

// 创建axios实例
const service = axios.create({
    // 改成相对路径，配合 vite.config.js 的 proxy 解决跨域
    baseURL: '/api',
    timeout: 5000
})

// 请求拦截器：添加Token
service.interceptors.request.use(
    (config) => {
        // 1. 修改点：直接从 localStorage 获取 token，不依赖额外的 auth.js 文件
        const token = localStorage.getItem('token')

        if (token) {
            // 2. 这里的 Bearer 前缀非常重要，后端 Security 需要它
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    (error) => {
        return Promise.reject(error)
    }
)

// 响应拦截器：统一处理响应
service.interceptors.response.use(
    (response) => {
        const res = response.data

        // 特殊处理：如果是下载文件（blob），直接返回
        if (response.config.responseType === 'blob') {
            return res
        }

        // 3. 校验后端返回的状态码
        // 假设你后端 Result.success() 返回的是 code: 200
        if (res.code && res.code !== 200) {
            ElMessage.error(res.msg || '请求失败')
            return Promise.reject(new Error(res.msg || 'Error'))
        }

        return res
    },
    (error) => {
        // 4. 处理 401 未授权（Token过期或无效）
        if (error.response && error.response.status === 401) {
            ElMessage.error('登录状态已过期，请重新登录')

            // 清除本地缓存的 token
            localStorage.removeItem('token')

            // 强制跳转回登录页 (使用 window.location 最稳妥，防止路由循环依赖)
            window.location.href = '/login'
        } else {
            ElMessage.error(error.message || '服务器连接异常')
        }
        return Promise.reject(error)
    }
)

export default service