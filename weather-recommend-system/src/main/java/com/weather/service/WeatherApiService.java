package com.weather.service;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.weather.entity.WeatherData;
import com.weather.mapper.WeatherDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class WeatherApiService {

    @Value("${weather.api.xinzhi.key}")
    private String apiKey;

    @Value("${weather.api.xinzhi.url:https://api.seniverse.com/v3/weather/now.json}")
    private String apiUrl;

    private static final String FORECAST_API_URL = "https://api.seniverse.com/v3/weather/daily.json";

    private final WeatherDataMapper weatherDataMapper;

    public WeatherApiService(WeatherDataMapper weatherDataMapper) {
        this.weatherDataMapper = weatherDataMapper;
    }

    /**
     * 1. 获取实况天气 (升级版：如果实况没湿度，就去预报里偷)
     */
    @Transactional
    public WeatherData getWeatherData(String city) {
        String url = String.format("%s?key=%s&location=%s&language=zh-Hans&unit=c", apiUrl, apiKey, city);

        try {
            // 1. 先拿实况
            String response = HttpUtil.get(url);
            log.info(">>>>>> 实况天气API响应: {}", response);

            WeatherData weatherData = parseSeniverseResponse(response, city);

            // 2. 如果实况里没湿度(为0)，尝试去请求预报接口补全数据
            if (weatherData != null && weatherData.getHumidity() == 0) {
                log.info(">>>>>> 实况缺湿度，尝试从预报获取...");
                fillDataFromForecast(weatherData, city);
            }

            if (weatherData != null) {
                try {
                    weatherDataMapper.insert(weatherData);
                } catch (Exception e) {
                    log.warn("数据入库忽略重复");
                }
                return weatherData;
            }
        } catch (Exception e) {
            log.error("获取实况天气失败", e);
        }
        return null;
    }

    /**
     * 2. 获取未来天气预报 (5天)
     */
    public Map<String, Object> getWeatherForecast(String city) {
        String url = String.format("%s?key=%s&location=%s&language=zh-Hans&unit=c&start=0&days=5", FORECAST_API_URL, apiKey, city);
        Map<String, Object> resultMap = new HashMap<>();

        try {
            String response = HttpUtil.get(url);
            log.info(">>>>>> 预报天气API响应: {}", response);

            JSONObject root = JSON.parseObject(response);
            JSONArray results = root.getJSONArray("results");

            if (results != null && !results.isEmpty()) {
                JSONArray daily = results.getJSONObject(0).getJSONArray("daily");

                List<String> dates = new ArrayList<>();
                List<String> highs = new ArrayList<>();
                List<String> lows = new ArrayList<>();

                for (int i = 0; i < daily.size(); i++) {
                    JSONObject day = daily.getJSONObject(i);
                    dates.add(day.getString("date"));
                    highs.add(day.getString("high"));
                    lows.add(day.getString("low"));
                }

                resultMap.put("dates", dates);
                resultMap.put("highs", highs);
                resultMap.put("lows", lows);
            }
        } catch (Exception e) {
            log.error("获取天气预报失败", e);
            resultMap.put("dates", new ArrayList<>());
            resultMap.put("highs", new ArrayList<>());
            resultMap.put("lows", new ArrayList<>());
        }
        return resultMap;
    }

    /**
     * 3. 🧠 智能模拟生成 24 小时逐小时预报 (新增功能)
     * 原理：利用正弦函数模拟一天气温变化（下午2点最高，凌晨3点最低）
     */
    public List<Map<String, Object>> getMockHourlyForecast(int currentTemp) {
        List<Map<String, Object>> list = new ArrayList<>();

        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();

        // 模拟未来 24 小时
        for (int i = 0; i < 24; i++) {
            LocalDateTime time = now.plusHours(i);
            int hour = time.getHour();
            Map<String, Object> item = new HashMap<>();

            // 格式化时间：例如 "14:00"
            item.put("time", String.format("%02d:00", hour));

            // 🌡️ 算法核心：模拟温度变化
            // 假设昼夜温差大概 8-10度。利用 hour 来偏移温度
            // 14:00 最热(offset=0)，02:00 最冷
            int offset = 0;
            if (hour >= 14) {
                offset = hour - 14;
            } else {
                offset = hour + 10;
            }
            // 简单的模拟公式：温度随时间缓慢下降，第二天早上回升
            // 这里为了演示，做个简单的正弦波动模拟
            double wave = Math.sin((hour - 8) * Math.PI / 12); // 模拟日照周期
            int forecastTemp = (int) (currentTemp + (wave * 3)); // 在当前温度基础上波动 +/- 3度

            item.put("temp", forecastTemp);

            // 模拟天气状况（白天晴，晚上多云）
            String type = (hour >= 6 && hour <= 18) ? "Sunny" : "Clear";
            if (i > 5 && i < 10) type = "Cloudy"; // 假装中间有段时间多云
            item.put("type", type);

            list.add(item);
        }
        return list;
    }

    /**
     * ✅ 补全数据：调用预报接口，获取今天的湿度和风力
     */
    private void fillDataFromForecast(WeatherData weather, String city) {
        String url = String.format("%s?key=%s&location=%s&language=zh-Hans&unit=c&start=0&days=1", FORECAST_API_URL, apiKey, city);
        try {
            String response = HttpUtil.get(url);
            JSONObject root = JSON.parseObject(response);
            JSONArray results = root.getJSONArray("results");

            if (results != null && !results.isEmpty()) {
                // 获取今天的预报
                JSONObject today = results.getJSONObject(0).getJSONArray("daily").getJSONObject(0);

                // 补全湿度
                if (weather.getHumidity() == 0) {
                    String h = today.getString("humidity");
                    if (h != null) weather.setHumidity(Integer.parseInt(h));
                }

                // 补全风力 (如果实况是 null null级)
                if (weather.getWindPower() == null || weather.getWindPower().contains("null")) {
                    weather.setWindPower(today.getString("wind_direction") + " " + today.getString("wind_scale") + "级");
                }
            }
        } catch (Exception e) {
            log.warn("补全天气数据失败", e);
        }
    }

    private WeatherData parseSeniverseResponse(String jsonStr, String city) {
        try {
            if (jsonStr == null || jsonStr.isEmpty()) return null;
            JSONObject root = JSON.parseObject(jsonStr);
            JSONArray results = root.getJSONArray("results");
            if (results != null && !results.isEmpty()) {
                JSONObject now = results.getJSONObject(0).getJSONObject("now");
                WeatherData weather = new WeatherData();
                weather.setCity(city);
                weather.setWeatherType(now.getString("text"));
                weather.setTemperature(now.getInteger("temperature"));

                String humidityStr = now.getString("humidity");
                weather.setHumidity(humidityStr != null ? Integer.parseInt(humidityStr) : 0);

                // 处理风力
                String windDir = now.getString("wind_direction");
                String windScale = now.getString("wind_scale");
                if (windDir != null && windScale != null) {
                    weather.setWindPower(windDir + " " + windScale + "级");
                } else {
                    weather.setWindPower("微风"); // 默认值
                }

                weather.setAirQuality("良");
                weather.setApiSource("xinzhi");
                weather.setFetchTime(LocalDateTime.now());
                weather.setExpireTime(LocalDateTime.now().plusHours(1));
                weather.setLifeIndex(generateMockLifeIndex());
                return weather;
            }
        } catch (Exception e) {
            log.error("JSON解析失败", e);
        }
        return null;
    }

    private Map<String, String> generateMockLifeIndex() {
        Map<String, String> index = new HashMap<>();
        index.put("uv", "medium");
        index.put("cold", "low");
        index.put("allergy", "low");
        index.put("comfort", "good");
        index.put("wash_car", "good");
        index.put("sport", "good");
        return index;
    }
}