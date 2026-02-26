package com.weather.vo;

import lombok.Data;
import lombok.AllArgsConstructor; // 新增这行

@Data
@AllArgsConstructor // 新增这行
public class Result<T> {
    private int code;    // 响应码：200-成功，500-失败，401-未授权
    private String msg;  // 响应信息
    private T data;      // 响应数据

    // 成功响应（无数据）
    public static <T> Result<T> success(String msg) {
        return new Result<>(200, msg, null);
    }

    // 成功响应（有数据）
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(200, msg, data);
    }

    // 失败响应
    public static <T> Result<T> error(String msg) {
        return new Result<>(500, msg, null);
    }
}