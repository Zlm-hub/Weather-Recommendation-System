package com.weather.config;

import com.weather.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy; // ✅ 1. 必须导入这个包
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    @Lazy // ✅ 2. 关键修改：加上 @Lazy 打破循环依赖
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String requestHeader = request.getHeader("Authorization");

        String username = null;
        String authToken = null;

        // 1. 检查 Header 是否存在且以 Bearer 开头
        if (requestHeader != null && requestHeader.startsWith("Bearer ")) {
            authToken = requestHeader.substring(7);
            try {
                // 2. 尝试解析 Token
                username = jwtUtil.getUsernameFromToken(authToken);
            } catch (IllegalArgumentException e) {
                logger.warn("无法获取 JWT Token");
            } catch (ExpiredJwtException e) {
                logger.warn("JWT Token 已过期");
            } catch (MalformedJwtException e) {
                // 捕获格式错误
                logger.warn("JWT Token 格式错误，已忽略");
            } catch (Exception e) {
                logger.warn("Token 解析失败: " + e.getMessage());
            }
        }

        // 3. 验证 Token 并设置上下文
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 因为用了 @Lazy，这里真正调用时才会去初始化 UserDetailsService
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 如果 token 有效
            if (jwtUtil.validateToken(authToken, userDetails)) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 设置当前用户已登录
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        chain.doFilter(request, response);
    }
}