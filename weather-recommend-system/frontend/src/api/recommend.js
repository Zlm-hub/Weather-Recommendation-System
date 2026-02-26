// api/recommend.js
import request from './request'

// 获取天气数据和推荐结果
export const getWeatherAndRecommend = () => {
    return request({
        url: '/recommend/weather',
        method: 'get'
    })
}