package com.weather.service;

import com.weather.entity.SysUser;
import com.weather.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private SysUserService sysUserService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 从数据库查询用户
        SysUser user = sysUserService.getUserByUsername(username);
        if (user == null) {
            throw new BusinessException("用户名不存在");
        }

        // 封装用户信息
        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(Collections.emptyList())
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                // 【修改点】disabled(true) 代表禁用。所以状态 != 1 时才设为 true (禁用)
                .disabled(user.getStatus() != 1)
                .build();
    }
}