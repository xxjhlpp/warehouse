package com.example.knowledge.config;

import com.example.knowledge.util.JwtUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, UserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        System.out.println("=== JWT Filter 处理请求: " + requestPath + " ===");

        try {
            String jwt = parseJwt(request);

            if (jwt != null) {
                System.out.println("提取到 JWT Token: " + jwt.substring(0, 20) + "...");
                System.out.println("Token 长度: " + jwt.length());

                boolean isValid = jwtUtils.validateToken(jwt);
                System.out.println("Token 验证结果: " + isValid);

                if (isValid) {
                    String username = jwtUtils.getUsernameFromToken(jwt);
                    System.out.println("从 Token 中提取的用户名: " + username);

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    System.out.println("加载用户详情成功: " + userDetails.getUsername());

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    System.out.println("认证设置成功");
                } else {
                    System.out.println("Token 验证失败，请求将被拒绝");
                }
            } else {
                System.out.println("未找到 JWT Token");
                // 检查是否是公开接口
                if (isPublicEndpoint(requestPath)) {
                    System.out.println("公开接口，允许访问");
                } else {
                    System.out.println("需要认证的接口，但未提供Token");
                }
            }
        } catch (Exception e) {
            System.err.println("JWT 过滤器异常: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("=== JWT Filter 处理完成 ===");
        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        System.out.println("Authorization Header: " + headerAuth);

        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }

    private boolean isPublicEndpoint(String path) {
        return path.contains("/user/login") ||
                path.contains("/user/register") ||
                path.contains("/swagger") ||
                path.contains("/api-docs");
    }
}