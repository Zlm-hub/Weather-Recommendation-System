package com.weather.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.weather.dto.UserLoginDTO;
import com.weather.dto.UserRegisterDTO;
import com.weather.dto.UserPreferenceDTO;
import com.weather.entity.SysUser;
import com.weather.service.SysUserService;
import com.weather.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 用户注册
    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody UserRegisterDTO dto) {
        sysUserService.register(dto);
        return Result.success("注册成功");
    }

    // 用户登录
    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody UserLoginDTO dto) {
        String token = sysUserService.login(dto);

        // 获取用户信息
        SysUser user = sysUserService.getUserByUsername(dto.getUsername());
        if(user != null) {
            user.setPassword(null); // 安全处理
        }

        // 返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user); // 把用户信息传回去

        return Result.success("登录成功", data);
    }

    // 获取用户信息
    @GetMapping("/info")
    public Result<?> getUserInfo() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        SysUser user = sysUserService.getUserByUsername(username);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(null); // 隐藏密码
        return Result.success("获取成功", user);
    }

    // 更新用户信息
    @PostMapping("/update")
    public Result<?> updateUser(@RequestBody SysUser user) {
        // 1. 获取当前登录用户名
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        SysUser currentUser = sysUserService.getUserByUsername(currentUsername);

        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        // 2. 更新逻辑
        boolean isChanged = false;

        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            currentUser.setAvatar(user.getAvatar());
            isChanged = true;
        }

        if (user.getLocation() != null && !user.getLocation().isEmpty()) {
            currentUser.setLocation(user.getLocation());
            isChanged = true;
        }

        // 修改密码逻辑
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            if (user.getOldPassword() == null || user.getOldPassword().isEmpty()) {
                return Result.error("为了安全，修改密码请提供旧密码");
            }
            if (!passwordEncoder.matches(user.getOldPassword(), currentUser.getPassword())) {
                return Result.error("旧密码输入错误，无法修改");
            }
            currentUser.setPassword(passwordEncoder.encode(user.getPassword()));
            isChanged = true;
        }

        if (user.getPreference() != null) {
            currentUser.setPreference(user.getPreference());
            isChanged = true;
        }

        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            currentUser.setPhone(user.getPhone());
            isChanged = true;
        }

        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            currentUser.setEmail(user.getEmail());
            isChanged = true;
        }

        // 3. 保存
        if (isChanged) {
            sysUserService.updateById(currentUser);
            return Result.success("修改成功");
        } else {
            return Result.success("无修改");
        }
    }

    // 单独更新偏好
    @PutMapping("/preference")
    public Result<?> updatePreference(@RequestBody UserPreferenceDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        sysUserService.updatePreference(username, dto.getPreference());
        return Result.success("偏好更新成功");
    }

    // 重置密码接口
    @PostMapping("/reset-password")
    public Result<?> resetPassword(@RequestBody com.weather.dto.UserResetDTO dto) {
        sysUserService.resetPassword(dto);
        // ✅ 修正：之前可能写错参数顺序了，这里改成只传消息
        return Result.success("密码重置成功，请重新登录");
    }

    // =======================================================
    // 👑 管理员功能接口
    // =======================================================

    /**
     * 【管理员功能】获取所有用户列表
     */
    @GetMapping("/list")
    public Result<List<SysUser>> getUserList(@RequestParam(required = false) String username) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        // 如果传了用户名，就进行模糊查询
        if (username != null && !username.isEmpty()) {
            queryWrapper.like(SysUser::getUsername, username);
        }
        queryWrapper.orderByDesc(SysUser::getCreateTime); // 按注册时间倒序
        List<SysUser> list = sysUserService.list(queryWrapper);

        // 脱敏处理：把密码清空，不返回给前端
        list.forEach(u -> u.setPassword(null));

        // ✅✅✅ 修复点：增加第一个参数 "获取成功"
        return Result.success("获取成功", list);
    }

    /**
     * 【管理员功能】删除用户
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteUser(@PathVariable Long id) {
        sysUserService.removeById(id);
        return Result.success("用户删除成功");
    }
}