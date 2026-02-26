package com.weather.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.entity.RecommendRule;
import com.weather.entity.WeatherData;
import com.weather.mapper.RecommendRuleMapper;
import com.weather.vo.FusedDataVO;
import com.weather.vo.RecommendationResult;
import com.weather.vo.RecommendItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RecommendationEngineService {

    private final RecommendRuleMapper recommendRuleMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    // ✅ 升级缓存 Key 到 v10 (确保彻底刷新)
    private static final String RULE_CACHE_KEY = "rule:cache:v12";

    public RecommendationEngineService(RecommendRuleMapper recommendRuleMapper, RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.recommendRuleMapper = recommendRuleMapper;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public RecommendationResult generateRecommendation(FusedDataVO fusedData) {
        List<RecommendRule> ruleList = getEnabledRules();
        WeatherData weather = fusedData.getWeatherData();

        if (weather == null) return new RecommendationResult();

        // 获取数据
        Integer temperature = weather.getTemperature();
        String weatherType = weather.getWeatherType();
        String airQuality = weather.getAirQuality();
        Map<String, Object> preference = new HashMap<>();

        List<RecommendItem> clothingList = new ArrayList<>();
        List<RecommendItem> travelList = new ArrayList<>();
        List<RecommendItem> healthList = new ArrayList<>();
        List<RecommendItem> activityList = new ArrayList<>();

        if (ruleList != null) {
            for (RecommendRule rule : ruleList) {
                try {
                    // 匹配规则
                    if (matchRule(rule, temperature, weatherType, airQuality, preference)) {
                        RecommendItem item = convertToRecommendItem(rule);
                        if (item != null && rule.getRuleType() != null) {
                            // ✅ 忽略大小写判断类型
                            switch (rule.getRuleType().toLowerCase().trim()) {
                                case "clothing": clothingList.add(item); break;
                                case "travel":   travelList.add(item); break;
                                case "health":   healthList.add(item); break;
                                case "activity": activityList.add(item); break;
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("规则匹配出错 ID: {}", rule.getId(), e);
                }
            }
        }

        sortRecommendItems(clothingList);
        sortRecommendItems(travelList);
        sortRecommendItems(healthList);
        sortRecommendItems(activityList);

        RecommendationResult result = new RecommendationResult();
        result.setClothingRecommend(clothingList);
        result.setTravelRecommend(travelList);
        result.setHealthRecommend(healthList);
        result.setActivityRecommend(activityList);
        result.setGenerateTime(LocalDateTime.now());
        return result;
    }

    private boolean matchRule(RecommendRule rule, Integer temperature, String weatherType,
                              String airQuality, Map<String, Object> preference) {

        String jsonStr = parseJsonString(rule.getConditionJson());
        if (jsonStr == null || jsonStr.isEmpty()) return false;

        Map<String, Object> condition;
        try {
            condition = objectMapper.readValue(jsonStr, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return false;
        }

        if (condition == null) return false;

        // 1. 温度匹配 (使用 Double 解析，更安全)
        if (temperature != null) {
            if (condition.containsKey("temp_min")) {
                double min = Double.parseDouble(condition.get("temp_min").toString());
                if (temperature < min) return false;
            }
            if (condition.containsKey("temp_max")) {
                double max = Double.parseDouble(condition.get("temp_max").toString());
                if (temperature > max) return false;
            }
            if (condition.containsKey("temperature")) {
                if (!matchTemperatureExpression(String.valueOf(condition.get("temperature")), temperature)) return false;
            }
        }

        // 2. 天气类型匹配 (转小写 + 包含匹配)
        if (condition.containsKey("weather_type")) {
            String target = String.valueOf(condition.get("weather_type")).toLowerCase();
            String current = (weatherType != null) ? weatherType.toLowerCase() : "";
            // 只要包含就匹配 (例如: 'light rain' 包含 'rain')
            if (!current.contains(target)) return false;
        }

        // 3. 空气质量匹配 (转小写匹配)
        if (condition.containsKey("air_quality")) {
            String target = String.valueOf(condition.get("air_quality")).toLowerCase();
            String current = (airQuality != null) ? airQuality.toLowerCase() : "";
            if (!target.equals(current)) return false;
        }

        return true;
    }

    private boolean matchTemperatureExpression(String condition, Integer temperature) {
        if (condition == null) return false;
        try {
            condition = condition.trim();
            double tempVal = temperature.doubleValue(); // 转为 double 比较

            if (condition.startsWith(">=")) return tempVal >= Double.parseDouble(condition.substring(2).trim());
            if (condition.startsWith("<=")) return tempVal <= Double.parseDouble(condition.substring(2).trim());
            if (condition.startsWith(">")) return tempVal > Double.parseDouble(condition.substring(1).trim());
            if (condition.startsWith("<")) return tempVal < Double.parseDouble(condition.substring(1).trim());
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    private RecommendItem convertToRecommendItem(RecommendRule rule) {
        String jsonStr = parseJsonString(rule.getRecommendJson());
        if (jsonStr == null || jsonStr.isEmpty()) return null;

        try {
            Map<String, Object> json = objectMapper.readValue(jsonStr, new TypeReference<Map<String, Object>>() {});
            RecommendItem item = new RecommendItem();
            item.setContent((String) json.getOrDefault("content", ""));
            item.setAccessory((String) json.getOrDefault("accessory", ""));
            item.setLevel((String) json.getOrDefault("level", "info"));
            item.setPriority(rule.getPriority() == null ? 0 : rule.getPriority());
            return item;
        } catch (Exception e) {
            return null;
        }
    }

    private String parseJsonString(Object jsonField) {
        if (jsonField == null) return null;
        try {
            if (jsonField instanceof byte[]) {
                return new String((byte[]) jsonField, StandardCharsets.UTF_8);
            }
            if (jsonField instanceof String) {
                return (String) jsonField;
            }
            return jsonField.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private List<RecommendRule> getEnabledRules() {
        try {
            Object cacheObj = redisTemplate.opsForValue().get(RULE_CACHE_KEY);
            if (cacheObj != null) {
                return objectMapper.convertValue(cacheObj, new TypeReference<List<RecommendRule>>() {});
            }
        } catch (Exception e) { log.warn("Redis读取失败", e); }

        List<RecommendRule> ruleList = recommendRuleMapper.selectEnabledRules();

        if (ruleList != null && !ruleList.isEmpty()) {
            try {
                redisTemplate.opsForValue().set(RULE_CACHE_KEY, ruleList, 1, TimeUnit.HOURS);
            } catch (Exception e) { log.warn("Redis写入失败", e); }
        }
        return ruleList != null ? ruleList : new ArrayList<>();
    }

    private void sortRecommendItems(List<RecommendItem> items) {
        if (items != null && items.size() > 1) {
            items.sort(Comparator.comparingInt(RecommendItem::getPriority).reversed());
        }
    }
}