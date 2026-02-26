package com.weather.controller;

import com.weather.entity.SysUser;
import com.weather.service.SysUserService;
import com.weather.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/weather")
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private SysUserService sysUserService;

    // 1. 获取实时天气 (用于首页左上角卡片)
    @GetMapping("/today")
    public Map<String, Object> getTodayWeather(@RequestParam(required = false) String city) {
        // 智能判断城市
        if (city == null || city.isEmpty()) {
            city = getCurrentUserCity();
        }
        if (city == null || city.isEmpty()) {
            city = "北京"; // 兜底
        }

        Map<String, Object> weatherData = weatherService.getWeatherByCity(city);

        Map<String, Object> response = new HashMap<>();
        if (weatherData != null) {
            response.put("code", 200);
            response.put("msg", "获取成功");
            response.put("data", weatherData);
        } else {
            response.put("code", 500);
            response.put("msg", "获取天气失败，请检查城市名");
        }
        return response;
    }

    // 2. 【新增】获取天气预报 (用于首页 ECharts 折线图)
    @GetMapping("/forecast")
    public Map<String, Object> getWeatherForecast(@RequestParam(required = false) String city) {
        // 复用智能城市判断逻辑
        if (city == null || city.isEmpty()) {
            city = getCurrentUserCity();
        }
        if (city == null || city.isEmpty()) {
            city = "北京";
        }

        // 调用 Service 新增的预报方法
        Map<String, Object> forecastData = weatherService.getForecastByCity(city);

        Map<String, Object> response = new HashMap<>();
        if (forecastData != null) {
            response.put("code", 200);
            response.put("msg", "获取预报成功");
            response.put("data", forecastData);
        } else {
            response.put("code", 500);
            response.put("msg", "获取预报失败");
        }
        return response;
    }

    /**
     * 辅助方法：获取当前登录用户的注册城市
     */
    private String getCurrentUserCity() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                String username = auth.getName();
                SysUser user = sysUserService.getUserByUsername(username);
                if (user != null) {
                    return user.getLocation();
                }
            }
        } catch (Exception e) {
            System.err.println("自动获取用户城市失败: " + e.getMessage());
        }
        return null;
    }
}