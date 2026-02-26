package com.weather.vo;

import lombok.Data;

// 推荐子项
@Data
public class RecommendItem { // 关键：添加public修饰符
    private String content;   // 推荐内容
    private String accessory; // 必备物品
    private String level;     // 推荐等级（强推荐/推荐/必须）
    private Integer priority; // 优先级
}