package com.weather.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler; // 必须导入这个
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
// 1. autoResultMap = true 是必须的，否则读取时 Map 会是 null
@TableName(value = "t_weather_data", autoResultMap = true)
public class WeatherData {

    @TableId(type = IdType.AUTO)
    private Long id;

    // 建议统一叫 city，因为天气通常是基于城市的
    private String city;

    // 温度用 Integer 足够 (例如 26)
    private Integer temperature;

    // 湿度 (%)
    private Integer humidity;

    // 天气类型 (晴/雨/多云)
    private String weatherType;

    // 风力/风速 (建议存字符串，例如 "3级" 或 "东南风2级")
    private String windPower;

    // 空气质量 (优/良)
    private String airQuality;

    /**
     * 关键修改点：
     * 1. 数据库里存的是 JSON 字符串，Java 里是 Map
     * 2. 必须加 typeHandler 注解
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, String> lifeIndex;

    // 数据来源 (hefeng/xinzhi)
    private String apiSource;

    // 获取时间
    private LocalDateTime fetchTime;

    // 过期时间 (用于判断是否需要重新请求 API)
    private LocalDateTime expireTime;
}