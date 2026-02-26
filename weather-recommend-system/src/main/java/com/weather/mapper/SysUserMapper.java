package com.weather.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.weather.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    // 关键修复点：加上 @Select 注解，告诉 MyBatis 怎么执行这个查询
    @Select("select * from sys_user where username = #{username}")
    SysUser selectByUsername(String username);
}