// api/user.js
import request from './request'

// 用户注册
export const register = (data) => {
    return request({
        url: '/user/register',
        method: 'post',
        data
    })
}

// 用户登录
export const login = (data) => {
    return request({
        url: '/user/login',
        method: 'post',
        data
    })
}

// 获取用户信息
export const getUserInfo = () => {
    return request({
        url: '/user/info',
        method: 'get'
    })
}

// 更新用户偏好
export const updatePreference = (data) => {
    return request({
        url: '/user/preference',
        method: 'put',
        data
    })
}

export const resetPassword = (data) => {
    return request.post('/user/reset-password', data)
}