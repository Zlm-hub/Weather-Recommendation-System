package com.weather.dto;

import lombok.Data;

@Data
public class UserResetDTO {
    private String username;
    private String location; // 用作验证身份的安全问题
    private String newPassword;
}