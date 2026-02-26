package com.weather.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RecommendationResult {
    private List<RecommendItem> clothingRecommend;  // 穿衣建议
    private List<RecommendItem> travelRecommend;    // 出行指南
    private List<RecommendItem> healthRecommend;    // 健康防护
    private List<RecommendItem> activityRecommend;  // 活动推荐
    private LocalDateTime generateTime;             // 生成时间
}

