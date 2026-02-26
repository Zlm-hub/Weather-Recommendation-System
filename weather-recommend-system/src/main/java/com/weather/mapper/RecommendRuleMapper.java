package com.weather.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.weather.entity.RecommendRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface RecommendRuleMapper extends BaseMapper<RecommendRule> {

    // ✅ 回归最简单的查询，不需要 AS 别名了，因为 Entity 用了 @TableField 映射
    @Select("SELECT * FROM t_recommend_rule WHERE status = 1")
    List<RecommendRule> selectEnabledRules();
}