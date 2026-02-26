package com.weather.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ApiRequestUtil {

    @Value("${weather.api.timeout}")
    private int timeout;

    // 发送GET请求
    public String doGet(String url) {
        try (HttpResponse response = HttpRequest.get(url)
                .timeout(timeout)
                .execute()) {
            if (response.isOk()) {
                return response.body();
            } else {
                log.error("API请求失败，URL：{}，响应码：{}", url, response.getStatus());
                return null;
            }
        } catch (Exception e) {
            log.error("API请求异常，URL：{}", url, e);
            return null;
        }
    }
}