package com.weather.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.weather.dto.UserLoginDTO;
import com.weather.dto.UserResetDTO;
import com.weather.dto.UserRegisterDTO;
import com.weather.entity.SysUser;
import com.weather.exception.BusinessException;
import com.weather.mapper.SysUserMapper;
import com.weather.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
// import java.util.HashMap; // 不再需要
// import java.util.Map;     // 不再需要

@Service
public class SysUserService extends ServiceImpl<SysUserMapper, SysUser> {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    @Lazy
    private AuthenticationManager authenticationManager;

    @Autowired
    @Lazy
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    // 用户注册
    @Transactional
    public void register(UserRegisterDTO dto) {
        // 检查用户名是否已存在
        SysUser existingUser = sysUserMapper.selectByUsername(dto.getUsername());
        if (existingUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 创建用户
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));  // 密码加密
        user.setLocation(dto.getLocation());

        // 【修改点】不再创建 Map，直接设置默认的个性签名字符串
        user.setPreference("这个人很懒，什么都没有写...");

        // 之前的 Map 代码已删除...

        user.setStatus(1);  // 正常状态
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        sysUserMapper.insert(user);
    }

    // 用户登录（生成JWT令牌）
    public String login(UserLoginDTO dto) {
        // 验证用户名密码
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
        );

        // 生成Token
        UserDetails userDetails = userDetailsService.loadUserByUsername(dto.getUsername());
        return jwtUtil.generateToken(userDetails);
    }

    // 更新用户偏好 (个性签名)
    @Transactional
    public void updatePreference(String username, String preference) { // 【修改点】参数类型改为 String
        SysUser user = sysUserMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPreference(preference);
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(user);
    }

    // 根据用户名获取用户信息
    public SysUser getUserByUsername(String username) {
        return sysUserMapper.selectByUsername(username);
    }

    @Transactional
    public void resetPassword(UserResetDTO dto) {
        // 1. 查询用户
        SysUser user = sysUserMapper.selectByUsername(dto.getUsername());
        if (user == null) {
            throw new BusinessException("用户名不存在");
        }

        // 2. 验证身份（比对城市是否匹配）
        // 注意：这里为了严谨，处理了空指针情况
        String dbLocation = user.getLocation() == null ? "" : user.getLocation();
        String inputLocation = dto.getLocation() == null ? "" : dto.getLocation();

        // 这里的逻辑是：必须包含用户输入的城市名 (比如用户填"上海"，数据库是"上海,北京"，算通过)
        if (!dbLocation.contains(inputLocation) || inputLocation.isEmpty()) {
            throw new BusinessException("身份验证失败：所在城市不匹配");
        }

        // 3. 更新密码
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());

        sysUserMapper.updateById(user);
    }
}