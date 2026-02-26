package com.weather.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
public class UserRegisterDTO {

    @NotBlank(message = "用户名不能为空")
    // 🚨【关键修改】将 min=4 改为 min=2，以支持两个字的中文名
    @Length(min = 2, max = 20, message = "用户名长度需在2-20个字符之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Length(min = 6, max = 20, message = "密码长度需在6-20个字符之间")
    private String password;

    @NotBlank(message = "地理位置不能为空")
    private String location;
}