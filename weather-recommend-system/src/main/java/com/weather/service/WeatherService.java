package com.weather.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class WeatherService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${weather.api.xinzhi.key}")
    private String apiKey;

    @Value("${weather.api.xinzhi.url}")
    private String weatherUrl;

    public Map<String, Object> getWeatherByCity(String cityName) {
        Map<String, Object> finalData = new HashMap<>();
        try {
            // 1. 获取 API 真实数据
            Map<String, Object> sourceA = callSourceA_Xinzhi(cityName);
            if (sourceA != null) finalData.putAll(sourceA);

            // 2. 获取模拟补充数据
            Map<String, Object> sourceB = callSourceB_Environment(cityName);

            // 优化：只补充 finalData 里没有的字段，防止模拟数据覆盖了 API 的真实数据（比如湿度）
            for (Map.Entry<String, Object> entry : sourceB.entrySet()) {
                finalData.putIfAbsent(entry.getKey(), entry.getValue());
            }

            // 3. 处理温度（防类型转换异常）
            Object tempObj = finalData.get("temp");
            int temp = 20;
            if (tempObj != null) {
                try {
                    // 兼容 Integer 和 String 类型的温度
                    temp = (int) Double.parseDouble(tempObj.toString());
                } catch (Exception e) {}
            }
            String text = (String) finalData.getOrDefault("weather", "晴");

            // 4. 处理湿度（防类型转换异常）
            Object humObj = finalData.get("humidity");
            String humStr = humObj != null ? humObj.toString() : "50";

            // 生成基础建议
            finalData.put("suggestion", generateSuggestions(temp, text));

            // 生成详细报告
            finalData.put("suggestionDetail", generateDetailedReport(temp, text, humStr));

            return finalData;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Map<String, Object> callSourceA_Xinzhi(String cityName) {
        try {
            String cleanUrl = weatherUrl.trim();
            String cleanKey = apiKey.trim();
            String urlTemplate = cleanUrl + "?key={key}&location={location}&language=zh-Hans&unit=c";
            Map<String, String> params = new HashMap<>();
            params.put("key", cleanKey);
            params.put("location", cityName);
            ResponseEntity<String> response = restTemplate.getForEntity(urlTemplate, String.class, params);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            if (!root.has("results")) return null;
            JsonNode now = root.path("results").get(0).path("now");
            JsonNode location = root.path("results").get(0).path("location");
            Map<String, Object> data = new HashMap<>();
            data.put("city", location.path("name").asText());
            data.put("temp", now.path("temperature").asText());
            data.put("weather", now.path("text").asText());
            // 尝试从 API 获取湿度（如果有的话）
            if (now.has("humidity")) {
                data.put("humidity", now.path("humidity").asText());
            }

            String windDir = now.path("wind_direction").asText();
            String windScale = now.path("wind_scale").asText();
            if (!windDir.isEmpty()) data.put("wind_dir", windDir);
            if (!windScale.isEmpty()) data.put("wind_scale", windScale);
            return data;
        } catch (Exception e) { return null; }
    }

    private Map<String, Object> callSourceB_Environment(String cityName) {
        Map<String, Object> data = new HashMap<>();
        Random random = new Random();
        DecimalFormat df = new DecimalFormat("#.0");

        // 生成模拟湿度
        int humidity = 40 + random.nextInt(41);
        data.put("humidity", String.valueOf(humidity));

        int pressure = 1000 + random.nextInt(26);
        data.put("pressure", String.valueOf(pressure));

        double visibility = 5.0 + (15.0 * random.nextDouble());
        data.put("visibility", df.format(visibility));

        data.put("wind_dir", "东南风");
        data.put("wind_scale", "2");
        data.put("wind", "东南风 2级"); // 默认风力描述
        return data;
    }

    public Map<String, Object> getForecastByCity(String cityName) {
        try {
            // 注意：这是心知天气的免费接口地址，如果您用的是其他付费版或不同接口，请确认 URL
            String dailyUrl = "https://api.seniverse.com/v3/weather/daily.json";
            String cleanKey = apiKey.trim();
            String urlTemplate = dailyUrl + "?key={key}&location={location}&language=zh-Hans&unit=c&start=0&days=3";
            Map<String, String> params = new HashMap<>();
            params.put("key", cleanKey);
            params.put("location", cityName);
            List<String> dates = new ArrayList<>();
            List<String> highTemps = new ArrayList<>();
            List<String> lowTemps = new ArrayList<>();
            try {
                ResponseEntity<String> response = restTemplate.getForEntity(urlTemplate, String.class, params);
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());
                if (root.has("results")) {
                    JsonNode dailyNode = root.path("results").get(0).path("daily");
                    if (dailyNode.isArray()) {
                        for (JsonNode day : dailyNode) {
                            dates.add(day.path("date").asText());
                            highTemps.add(day.path("high").asText());
                            lowTemps.add(day.path("low").asText());
                        }
                    }
                }
            } catch (Exception e) { System.err.println("API预报获取失败，转为全量模拟"); }

            // 补全到 14 天
            int currentSize = dates.size();
            int targetSize = 14;
            LocalDate lastDate = currentSize > 0 ? LocalDate.parse(dates.get(currentSize - 1)) : LocalDate.now();
            int lastHigh = currentSize > 0 ? Integer.parseInt(highTemps.get(currentSize - 1)) : 20;
            int lastLow = currentSize > 0 ? Integer.parseInt(lowTemps.get(currentSize - 1)) : 10;
            Random random = new Random();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            for (int i = 0; i < targetSize - currentSize; i++) {
                lastDate = lastDate.plusDays(1);
                dates.add(lastDate.format(formatter));
                int change = random.nextInt(7) - 3;
                lastHigh = lastHigh + change;
                lastLow = lastLow + change;
                if (lastHigh - lastLow < 5) lastHigh = lastLow + 5 + random.nextInt(3);
                highTemps.add(String.valueOf(lastHigh));
                lowTemps.add(String.valueOf(lastLow));
            }
            Map<String, Object> result = new HashMap<>();
            result.put("dates", dates);
            result.put("highs", highTemps);
            result.put("lows", lowTemps);
            return result;
        } catch (Exception e) { e.printStackTrace(); return null; }
    }

    private Map<String, String> generateSuggestions(int temp, String weatherText) {
        Map<String, String> suggestions = new HashMap<>();
        String dressing;
        if (temp < 10) dressing = "天气较冷，建议穿棉衣或羽绒服。";
        else if (temp < 24) dressing = "气温舒适，建议穿单层棉麻面料的短套装。";
        else dressing = "天气炎热，建议穿轻棉织物制作的短衣。";
        suggestions.put("dressing", dressing);
        String travel = weatherText.contains("雨") ? "有雨，路面湿滑，请小心驾驶。" : "天气良好，适宜出行。";
        suggestions.put("travel", travel);
        String health = temp > 30 ? "高温预警，请注意防暑降温。" : "空气清新，适合户外运动。";
        suggestions.put("health", health);
        return suggestions;
    }

    private Map<String, String> createIndexItem(String name, String value, String desc) {
        Map<String, String> map = new HashMap<>();
        map.put("name", name);
        map.put("value", value);
        map.put("desc", desc);
        return map;
    }

    private Map<String, Object> generateDetailedReport(int temp, String weather, String humidityStr) {
        Map<String, Object> report = new HashMap<>();

        String aiBrief;
        if (weather.contains("雨")) {
            aiBrief = "🌧️ 今天是雨天，世界被冲刷得格外干净。虽然出行不便，但也是个在室内听雨读书的好日子。记得带伞，保持好心情哦！";
        } else if (temp > 30) {
            aiBrief = "☀️ 烈日当空，热情似火！今天紫外线较强，出门就像进烤箱。建议躲在空调房里吃西瓜，或者傍晚再去散步。";
        } else if (temp < 10) {
            aiBrief = "❄️ 寒风凛冽，注意保暖！这种天气最适合喝一杯热可可，把自己裹得严严实实。出门记得戴围巾，别让冷风钻进脖子里。";
        } else {
            aiBrief = "🌤️ 天气真不错，不冷也不热！微风拂面，是时候去公园走走，或者约朋友喝个下午茶了。享受这美好的时光吧！";
        }
        report.put("brief", aiBrief);

        List<Map<String, String>> indices = new ArrayList<>();
        indices.add(createIndexItem("紫外线", (temp > 25 ? "强" : "弱"), "涂抹防晒霜"));
        indices.add(createIndexItem("洗车", (weather.contains("雨") ? "不宜" : "适宜"), "雨水会弄脏爱车"));
        indices.add(createIndexItem("穿衣", (temp < 15 ? "保暖" : "清凉"), "根据温度增减"));
        indices.add(createIndexItem("感冒", "少发", "多喝水增强免疫"));
        indices.add(createIndexItem("运动", (weather.contains("雨") ? "室内" : "户外"), "选择合适场地"));
        indices.add(createIndexItem("过敏", "易发", "戴口罩防粉尘"));
        report.put("indices", indices);

        return report;
    }
}