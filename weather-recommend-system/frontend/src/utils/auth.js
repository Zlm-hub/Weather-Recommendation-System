// 存储Token
export const setToken = (token) => {
    localStorage.setItem('token', token)
}

// 获取Token
export const getToken = () => {
    return localStorage.getItem('token')
}

// 删除Token
export const removeToken = () => {
    localStorage.removeItem('token')
}