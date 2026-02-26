package com.weather.controller;

import com.weather.entity.SysUser;
import com.weather.entity.WeatherData;
import com.weather.service.RecommendationEngineService;
import com.weather.service.SysUserService;
import com.weather.service.WeatherApiService;
import com.weather.vo.FusedDataVO;
import com.weather.vo.HomeDataVO;
import com.weather.vo.RecommendationResult;
import com.weather.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List; // ✅ 新增导入
import java.util.Map;

@RestController
@RequestMapping("/home")
public class HomeController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private WeatherApiService weatherApiService;

    @Autowired
    private RecommendationEngineService recommendationEngineService;

    /**
     * 获取首页仪表盘所需的所有数据
     * @return Result<HomeDataVO>
     */
    @GetMapping("/dashboard")
    public Result<HomeDataVO> getDashboardData() {
        // 1. 获取当前登录用户
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        SysUser user = sysUserService.getUserByUsername(username);

        // 默认城市兜底
        String city = (user.getLocation() != null && !user.getLocation().isEmpty()) ? user.getLocation() : "上海";

        // 2. 获取天气数据
        // 2.1 实时天气 (Now)
        WeatherData currentWeather = weatherApiService.getWeatherData(city);

        // 2.2 未来预报 (Forecast - 给图表用)
        Map<String, Object> forecastData = weatherApiService.getWeatherForecast(city);

        // 3. 准备推荐引擎所需的数据 (FusedDataVO)
        // 【安全检查】如果天气API挂了返回null，这里手动补一个空对象，防止空指针报错
        if (currentWeather == null) {
            currentWeather = new WeatherData();
            currentWeather.setTemperature(20); // 默认兜底数据
            currentWeather.setWeatherType("cloudy");
        }

        // ✅✅✅ 新增关键代码：生成 24 小时逐小时预报
        // 利用当前的实时温度作为基准，生成未来的模拟曲线
        List<Map<String, Object>> hourlyData = weatherApiService.getMockHourlyForecast(currentWeather.getTemperature());

        FusedDataVO fusedData = new FusedDataVO();
        fusedData.setWeatherData(currentWeather);
        fusedData.setUserPreference(user.getPreference());
        fusedData.setFuseTime(LocalDateTime.now());

        // 构建生活指数详情 (用于前端展示具体指数)
        Map<String, Map<String, String>> lifeIndexDetail = buildLifeIndexDetail(currentWeather);
        fusedData.setLifeIndexDetail(lifeIndexDetail);

        // 4. 调用推荐引擎生成建议
        RecommendationResult recommendation = recommendationEngineService.generateRecommendation(fusedData);

        // 5. 组装最终的大对象 HomeDataVO
        HomeDataVO homeData = new HomeDataVO();

        // 填入用户信息
        homeData.setUsername(user.getUsername());
        homeData.setAvatar(user.getAvatar());
        homeData.setLocation(city);

        // 填入天气信息
        homeData.setCurrentWeather(currentWeather);
        homeData.setForecast(forecastData);

        // ✅✅✅ 将小时级数据填入返回对象
        homeData.setHourlyForecast(hourlyData);

        // 填入推荐和指数
        homeData.setRecommendation(recommendation);
        homeData.setLifeIndexDetail(lifeIndexDetail);

        return Result.success("获取首页数据成功", homeData);
    }

    /**
     * 辅助方法：将 WeatherData 里的 lifeIndex Map 转为前端易读的格式
     */
    private Map<String, Map<String, String>> buildLifeIndexDetail(WeatherData weatherData) {
        Map<String, Map<String, String>> detailMap = new HashMap<>();

        // 防空判断
        if (weatherData == null || weatherData.getLifeIndex() == null) {
            return detailMap;
        }

        Map<String, String> rawIndex = weatherData.getLifeIndex(); // {"uv": "high", "cold": "low"}

        for (Map.Entry<String, String> entry : rawIndex.entrySet()) {
            Map<String, String> item = new HashMap<>();
            item.put("level", entry.getValue());
            // 这里可以扩展更多描述，目前简单处理
            item.put("description", getIndexDescription(entry.getKey(), entry.getValue()));
            detailMap.put(entry.getKey(), item);
        }

        return detailMap;
    }

    // 简单的指数描述映射 (可选)
    private String getIndexDescription(String type, String level) {
        if ("uv".equals(type) && "high".equals(level)) return "紫外线强，注意防晒";
        if ("cold".equals(type) && "high".equals(level)) return "极易感冒，注意保暖";
        return level; // 默认直接返回等级
    }
}