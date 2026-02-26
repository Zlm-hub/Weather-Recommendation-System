import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Home from '../views/Home.vue'
import Profile from '../views/Profile.vue'
import Suggestion from '../views/Suggestion.vue'
import { getToken } from '../utils/auth'

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        // 根路径重定向
        { path: '/', redirect: '/home' },

        // =============================================
        // 1. 不需要登录的页面 (meta: { noAuth: true })
        // =============================================
        {
            path: '/login',
            name: 'Login',
            component: Login,
            meta: { noAuth: true }
        },
        {
            path: '/register',
            name: 'Register',
            component: Register,
            meta: { noAuth: true }
        },
        {
            path: '/reset-pwd',
            name: 'ResetPwd',
            component: () => import('../views/ResetPwd.vue'),
            meta: { noAuth: true }
        },

        // =============================================
        // 2. 需要登录的页面 (默认需要验证)
        // =============================================
        {
            path: '/home',
            name: 'Home',
            component: Home
        },
        {
            path: '/profile',
            name: 'Profile',
            component: Profile
        },
        {
            path: '/suggestion',
            name: 'Suggestion',
            component: Suggestion
        },

        // ✅ 新增：管理员后台路由
        {
            path: '/admin',
            name: 'Admin',
            // 使用懒加载，只有访问时才加载 Admin 组件
            component: () => import('../views/Admin.vue')
        }
    ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
    // 判断该路由是否需要登录权限
    const requiresAuth = !to.meta.noAuth

    if (requiresAuth) {
        const token = getToken()
        if (!token) {
            // 没登录，强制跳转登录页
            next('/login')
        } else {
            // 已登录，放行
            next()
        }
    } else {
        // 不需要登录的页面，直接放行
        next()
    }
})

export default router