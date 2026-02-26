package com.weather.vo;

import com.weather.entity.WeatherData;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class FusedDataVO {
    // 【修改点】之前是 Map<String, Object>，现在改为 String 用于存个性签名
    private String userPreference;  // 用户偏好 (个性签名)

    private WeatherData weatherData;             // 气象数据
    private Map<String, Map<String, String>> lifeIndexDetail;  // 生活指数详情
    private LocalDateTime fuseTime;              // 融合时间
}