package com.weather.service;

import com.weather.entity.SysUser;
import com.weather.entity.WeatherData;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecommendationService {

    /**
     * 生成生活建议
     * 增加 Null 判断，防止天气获取失败时报错
     */
    public List<String> generateAdvice(SysUser user, WeatherData weather) {
        List<String> adviceList = new ArrayList<>();

        // 容错：如果天气获取失败，返回基础提示
        if (weather == null) {
            adviceList.add("📡 暂时无法获取天气数据，请检查网络或API配置。");
            return adviceList;
        }

        String preference = user.getPreference() != null ? user.getPreference() : "";
        int temp = weather.getTemperature();
        String type = weather.getWeatherType();

        // 1. 穿衣建议
        if (temp < 5) {
            adviceList.add("🥶 严寒预警：建议穿着羽绒服、围巾，注意防寒。");
        } else if (temp < 15) {
            adviceList.add("🧥 气温较低：建议穿着大衣或夹克，内搭毛衣。");
        } else if (temp > 28) {
            adviceList.add("👕 炎热高温：建议穿着短袖、短裤，注意防晒。");
        } else {
            adviceList.add("👔 舒适天气：衬衫或T恤搭配薄外套即可。");
        }

        // 2. 出行建议
        if (type.contains("雨")) {
            adviceList.add("☔ 下雨天：出门记得带伞，路面湿滑，开车请慢行。");
            if (preference.contains("运动") || preference.contains("跑")) {
                adviceList.add("🚫 运动建议：外面下雨了，建议改为室内器械训练。");
            }
        } else if (type.contains("晴")) {
            adviceList.add("🌞 阳光明媚：非常适合晒被子或户外活动。");
        }

        // 3. 个性化 (结合用户签名)
        if (preference.contains("钓鱼")) {
            adviceList.add("🎣 钓鱼助手：查看具体气压和风向，祝您爆护！");
        }

        return adviceList;
    }
}