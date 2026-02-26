package com.weather.exception;

import com.weather.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 【新增】专门处理登录失败（账号或密码错误）
    @ExceptionHandler({BadCredentialsException.class, InternalAuthenticationServiceException.class})
    public Result<?> handleLoginException(Exception e) {
        // 打印一条简单的日志即可，不需要打印堆栈，因为这是常见的用户操作失误
        log.warn("用户登录失败：{}", e.getMessage());
        return Result.error("账号或密码错误");
    }

    // 业务异常处理
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.error("业务异常：{}", e.getMessage());
        return Result.error(e.getMessage());
    }

    // 参数校验异常处理
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        StringBuilder errMsg = new StringBuilder("参数校验失败：");
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            errMsg.append(fieldError.getField()).append("：").append(fieldError.getDefaultMessage()).append("，");
        }
        String msg = errMsg.substring(0, errMsg.length() - 1);
        log.error(msg);
        return Result.error(msg);
    }

    // 通用异常处理（兜底）
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        // 打印完整的堆栈信息，方便排查真正的系统错误
        log.error("系统异常：", e);
        return Result.error("系统异常，请稍后重试");
    }
}