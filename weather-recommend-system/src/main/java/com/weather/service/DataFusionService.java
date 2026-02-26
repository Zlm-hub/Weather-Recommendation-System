package com.weather.service;

import com.weather.entity.SysUser;
import com.weather.entity.WeatherData;
import com.weather.vo.FusedDataVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class DataFusionService {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private WeatherApiService weatherApiService;


    public FusedDataVO fuseData(String username) {
        // 1. 获取用户信息
        SysUser user = sysUserService.getUserByUsername(username);
        if (user == null) {
            // 简单的防空保护
            throw new RuntimeException("用户不存在: " + username);
        }
        String preference = user.getPreference();

        // 2. 获取天气数据
        String location = user.getLocation();
        if (location == null || location.isEmpty()) {
            location = "上海"; // 默认城市，防止报错
        }
        WeatherData weatherData = weatherApiService.getWeatherData(location);

        // 3. 构建生活指数详情
        // 【修改点】由于删除了字典表，这里不再查库，直接封装数据
        Map<String, String> lifeIndex = weatherData.getLifeIndex();
        Map<String, Map<String, String>> lifeIndexDetail = new HashMap<>();

        if (lifeIndex != null) {
            for (Map.Entry<String, String> entry : lifeIndex.entrySet()) {
                String indexType = entry.getKey();
                String indexLevel = entry.getValue();

                Map<String, String> detail = new HashMap<>();
                detail.put("level", indexLevel);
                // 这里简单生成一个描述，不再去查数据库
                detail.put("description", indexLevel);

                lifeIndexDetail.put(indexType, detail);
            }
        }

        // 4. 构建融合数据
        FusedDataVO fusedData = new FusedDataVO();
        fusedData.setUserPreference(preference);
        fusedData.setWeatherData(weatherData);
        fusedData.setLifeIndexDetail(lifeIndexDetail);
        fusedData.setFuseTime(LocalDateTime.now());

        return fusedData;
    }
}