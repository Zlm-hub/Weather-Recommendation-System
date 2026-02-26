package com.weather.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("life_index_rule")
public class LifeIndexRule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String type;    // 规则类型：temp, uv, etc.
    private Integer minVal; // 最小值
    private Integer maxVal; // 最大值
    private String level;   // 等级
    private String suggestion; // 建议内容
}