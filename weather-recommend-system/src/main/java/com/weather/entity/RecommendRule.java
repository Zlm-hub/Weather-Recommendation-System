package com.weather.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("t_recommend_rule")
public class RecommendRule implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("rule_type")
    private String ruleType;

    /**
     * ✅ 核心修改：改为 Object 类型
     * 这样无论数据库返回的是 String 还是 byte[] (BLOB)，都不会报错，都能接住！
     */
    @TableField("condition_json")
    private Object conditionJson;

    @TableField("recommend_json")
    private Object recommendJson;

    private Integer priority;
    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}