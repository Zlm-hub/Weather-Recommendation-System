package com.weather.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    /**
     * 【新增】旧密码
     * exist = false 表示这个字段仅仅用于后端接收参数进行校验
     * 不会映射到数据库表中
     */
    @TableField(exist = false)
    private String oldPassword;

    private String location;

    // ✅ 新增：角色字段 (对应数据库的 role 列)
    // 值通常为: "user" (普通用户) 或 "admin" (管理员)
    private String role;

    private String avatar;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 个性签名
     */
    private String preference;

    private Integer status; // 状态：1启用 0禁用

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}