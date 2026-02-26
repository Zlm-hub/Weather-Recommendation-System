package com.weather.service;

import com.weather.entity.SysUser;
import com.weather.entity.WeatherData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class AiAssistantService {

    @Autowired
    private WeatherApiService weatherApiService;

    @Autowired
    private SysUserService sysUserService;

    public String chat(String username, String question) {
        // 1. 获取上下文信息
        SysUser user = sysUserService.getUserByUsername(username);
        String city = (user != null && user.getLocation() != null && !user.getLocation().isEmpty())
                ? user.getLocation() : "西安"; // 默认城市

        // 2. 预处理：判断用户问的是哪一天
        int dayIndex = 0; // 0=今天, 1=明天
        String dayName = "今天";
        if (question.contains("明天") || question.contains("明日")) {
            dayIndex = 1;
            dayName = "明天";
        } else if (question.contains("后天")) {
            dayIndex = 2;
            dayName = "后天";
        }

        // 3. 获取数据源
        // (A) 未来预报 (用于获取温度趋势)
        Map<String, Object> forecast = weatherApiService.getWeatherForecast(city);

        // (B) 实时天气 (用于更精准的"今天"判断，包含天气现象如"晴"、"雨")
        WeatherData current = null;
        if (dayIndex == 0) {
            current = weatherApiService.getWeatherData(city);
        }

        // ==================================================
        // 🧠 核心意图识别 (关键词匹配)
        // ==================================================

        // 0. 闲聊/打招呼
        if (check(question, "你好", "在吗", "hi", "hello")) {
            return "你好呀！我是你的智能天气顾问。你可以问我：“" + dayName + "适合去哪玩？” 或者 “今天中午吃什么？”";
        }

        // 1. 【新增】通用活动推荐 (干什么、去哪玩、推荐)
        if (check(question, "干什么", "做什么", "去哪", "玩", "推荐", "无聊", "逛")) {
            return analyzeGeneralActivity(current, forecast, dayIndex, dayName);
        }

        // 2. 【新增】美食推荐 (吃什么)
        if (check(question, "吃", "美食", "饭", "喝")) {
            return analyzeFood(current, forecast, dayIndex, dayName);
        }

        // 3. 【新增】健康/穿衣 (冷、热、感冒、衣服)
        if (check(question, "穿", "衣服", "冷", "热", "感冒", "不舒服")) {
            return analyzeHealthAndClothing(current, forecast, dayIndex, dayName);
        }

        // 4. 专项活动：钓鱼
        if (check(question, "钓", "鱼")) {
            return analyzeFishing(forecast, dayIndex, dayName);
        }

        // 5. 专项活动：洗车
        if (check(question, "洗车", "车")) {
            return analyzeCarWash(current, forecast, dayIndex, dayName);
        }

        // 6. 专项活动：运动/跑步
        if (check(question, "跑", "运动", "球", "锻炼")) {
            return analyzeSport(current, forecast, dayIndex, dayName);
        }

        // 7. 兜底：纯查询天气
        if (check(question, "天气", "多少度", "气温", "怎么样")) {
            return analyzeWeather(forecast, dayIndex, dayName);
        }

        // 8. 无法识别
        return "这个问题难倒我了🤯。您可以试着问我：“今天适合干什么？”、“明天去钓鱼怎么样？”";
    }

    // ==================================================
    // 💡 具体的分析逻辑方法
    // ==================================================

    /**
     * 🎮 通用活动推荐 (根据天气类型 + 温度)
     */
    private String analyzeGeneralActivity(WeatherData current, Map<String, Object> forecast, int index, String dayName) {
        int temp = getTempInt(forecast, index, "highs");
        String weatherType = (index == 0 && current != null) ? current.getWeatherType() : "未知";

        // 场景 A: 下雨/雪
        if (weatherType.contains("雨") || weatherType.contains("雪")) {
            return dayName + "预报有" + weatherType + " 🌧️，户外湿滑。强烈建议您选择室内活动，比如：\n" +
                    "1. 去博物馆或艺术展陶冶情操 🎨\n" +
                    "2. 找个安静的咖啡店读本书 ☕\n" +
                    "3. 去电影院看场最新上映的大片 🎬";
        }

        // 场景 B: 晴天/多云
        if (temp > 30) {
            return dayName + "气温较高 (" + temp + "℃) 🥵，怕热的话建议去游泳馆 🏊‍♂️ 或者商场吹空调逛街。如果要出门，记得傍晚再去公园散步哦。";
        } else if (temp >= 15 && temp <= 28) {
            return dayName + "天气简直完美 (" + temp + "℃) ✨！非常适合户外活动：\n" +
                    "1. 约朋友去郊外野餐/露营 🏕️\n" +
                    "2. 爬山或去森林公园吸氧 🌲\n" +
                    "3. 城市骑行或扫街摄影 📷";
        } else if (temp < 10) {
            return dayName + "有点冷 (" + temp + "℃) 🧣，不如去泡个温泉放松一下？或者在家里追剧吃零食也是不错的选择！";
        }

        // 默认
        return dayName + "天气尚可，去附近的公园走走，或者约朋友聚个餐都很棒！";
    }

    /**
     * 🍲 美食推荐 (根据温度)
     */
    private String analyzeFood(WeatherData current, Map<String, Object> forecast, int index, String dayName) {
        int temp = getTempInt(forecast, index, "highs");

        if (temp < 8) {
            return "天这么冷 (" + temp + "℃)，当然要吃热乎的！推荐：\n🥘 重庆火锅/铜锅涮肉\n🍜 羊肉泡馍/拉面\n暖胃又暖心！";
        } else if (temp > 28) {
            return "天气太热了，没什么胃口？推荐：\n🥗 轻食沙拉\n🍣 寿司/刺身\n🍦 饭后再来个冰淇淋或冰镇西瓜！";
        } else {
            String[] foods = {"烧烤 🍢", "日料 🍣", "川菜 🌶️", "披萨 🍕", "汉堡 🍔"};
            String randomFood = foods[new Random().nextInt(foods.length)];
            return "天气不错，吃点好的奖励自己吧！\n不如今天去吃 " + randomFood + "？或者去探探附近的热门餐厅。";
        }
    }

    /**
     * 💊 健康与穿衣
     */
    private String analyzeHealthAndClothing(WeatherData current, Map<String, Object> forecast, int index, String dayName) {
        int high = getTempInt(forecast, index, "highs");
        int low = getTempInt(forecast, index, "lows");

        // 这里的逻辑可以结合 diff (温差)
        int diff = high - low;

        if (diff > 12) {
            return dayName + "温差很大 (" + low + "~" + high + "℃)，像坐过山车 🎢。请依此法穿衣：\n早晚穿外套，中午穿短袖（洋葱式穿衣法），小心感冒！";
        }
        if (high < 10) {
            return "天气寒冷，注意保暖！羽绒服、秋裤都安排上。多喝热水，预防风寒。🧣";
        }
        if (high > 32) {
            return "高温预警！出门务必涂防晒霜 🧴，多喝水防止中暑。";
        }
        return "气温舒适 (" + high + "℃)，正常穿着即可。保持好心情能提高免疫力哦！💪";
    }

    // --- 原有的专项逻辑 (保留并微调) ---

    private String analyzeFishing(Map<String, Object> forecast, int index, String dayName) {
        int high = getTempInt(forecast, index, "highs");
        int low = getTempInt(forecast, index, "lows");
        if (high == -999) return "暂无数据。";

        if (high < 5) return dayName + "太冷了 (" + low + "℃)，鱼儿都冬眠了，建议改天再去。";
        if (high - low > 15) return dayName + "温差大，建议钓早晚时段。";
        return dayName + "气温 " + low + "~" + high + "℃，非常适合钓鱼！祝您爆护！🎣";
    }

    private String analyzeCarWash(WeatherData current, Map<String, Object> forecast, int index, String dayName) {
        // 如果是今天，且实况是雨
        if (index == 0 && current != null && current.getWeatherType().contains("雨")) {
            return "外面正在下雨 🌧️，别洗车啦！等于白洗。";
        }
        // 简单逻辑：只要不是极度恶劣天气都推荐
        return dayName + "天气不错，非常适合洗车！让爱车焕然一新吧。🚗";
    }

    private String analyzeSport(WeatherData current, Map<String, Object> forecast, int index, String dayName) {
        int temp = getTempInt(forecast, index, "highs");
        if (temp > 32) return "太热了 (" + temp + "℃)，户外运动容易中暑，建议去健身房或游泳。🏊";
        if (current != null && current.getWeatherType().contains("雨") && index == 0) return "外面下雨呢，在室内做做瑜伽吧。🧘";
        return "天气给力，去跑步、打球、骑行都是很好的选择！生命在于运动！🏃‍♂️";
    }

    private String analyzeWeather(Map<String, Object> forecast, int index, String dayName) {
        String high = getTempStr(forecast, index, "highs");
        String low = getTempStr(forecast, index, "lows");
        if (high == null) return "抱歉，暂无数据。";
        return dayName + "预计气温 " + low + "℃ 到 " + high + "℃。";
    }

    // ==================================================
    // 🔧 辅助工具方法
    // ==================================================

    // 关键词匹配辅助
    private boolean check(String question, String... keywords) {
        for (String k : keywords) {
            if (question.contains(k)) return true;
        }
        return false;
    }

    // 安全获取温度 (Int)
    private int getTempInt(Map<String, Object> forecast, int index, String key) {
        String s = getTempStr(forecast, index, key);
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return -999; // 错误标记
        }
    }

    // 安全获取温度 (String)
    private String getTempStr(Map<String, Object> forecast, int index, String key) {
        if (forecast == null || forecast.get(key) == null) return null;
        List<String> list = (List<String>) forecast.get(key);
        if (index >= list.size()) return null;
        return list.get(index);
    }
}