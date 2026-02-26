package com.weather.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@TableName("t_user_behavior")
public class UserBehavior {
    @TableId(type = IdType.AUTO)
    private Long id;                // 主键ID
    private Long userId;            // 关联用户ID
    private Map<String, Object> weatherCondition;  // 触发推荐的天气条件
    private String clickRecommend;  // 点击的推荐类型
    private Integer feedback;       // 反馈：1-好评，2-差评，0-无反馈
    private LocalDateTime createTime;  // 行为时间
}