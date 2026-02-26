package com.weather.vo;

import com.weather.entity.WeatherData;
import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 首页仪表盘数据对象
 * 包含：用户信息、实时天气、未来预报、推荐结果、24小时预报
 */
@Data
public class HomeDataVO {
    // 1. 用户基本信息 (用于显示 "你好, 小张")
    private String username;
    private String avatar;
    private String location;

    // 2. 实时天气 (用于 Header 显示温度、天气图标)
    private WeatherData currentWeather;

    // 3. 未来天气预报 (用于 ECharts 折线图)
    // 格式: { "dates": [...], "highs": [...], "lows": [...] }
    private Map<String, Object> forecast;

    // 4. 智能推荐结果 (用于显示卡片)
    // 注意：确保 RecommendationResult 类也在 com.weather.vo 包下
    private RecommendationResult recommendation;

    // 5. 生活指数详情 (用于显示具体指数，如紫外线等级)
    private Map<String, Map<String, String>> lifeIndexDetail;

    // 6. ✅ 新增：24小时逐小时预报数据 (用于 ECharts 平滑曲线)
    // 格式: [{ "time": "14:00", "temp": 24, "type": "Sunny" }, ...]
    private List<Map<String, Object>> hourlyForecast;
}