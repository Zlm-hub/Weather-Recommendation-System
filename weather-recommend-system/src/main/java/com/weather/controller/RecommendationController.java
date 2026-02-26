package com.weather.controller;

import com.weather.service.DataFusionService;
import com.weather.service.RecommendationEngineService;
import com.weather.vo.FusedDataVO;
import com.weather.vo.RecommendationResult;
import com.weather.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/recommend")
public class RecommendationController {

    @Autowired
    private DataFusionService dataFusionService;
    @Autowired
    private RecommendationEngineService recommendationEngineService;

    // 获取天气数据和推荐结果
    @GetMapping("/weather")
    public Result<?> getWeatherAndRecommend(@RequestHeader("Authorization") String token) {
        // 从Token中获取用户名
        String username = getUsernameFromToken(token);

        // 1. 数据融合
        FusedDataVO fusedData = dataFusionService.fuseData(username);

        // 2. 生成推荐
        RecommendationResult recommendation = recommendationEngineService.generateRecommendation(fusedData);

        // 3. 构建响应数据
        Map<String, Object> data = new HashMap<>();
        data.put("weatherData", fusedData.getWeatherData());
        data.put("recommendation", recommendation);

        return Result.success("获取成功", data);
    }

    // 简化：从Token提取用户名
    private String getUsernameFromToken(String token) {
        // 实际项目通过JwtUtil和Security上下文获取，此处省略实现
        return "testuser";
    }
}