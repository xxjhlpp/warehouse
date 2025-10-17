package com.example.knowledge.config;

import com.example.knowledge.mapper.UserMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.MessageDigestPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@EnableWebSecurity  // 启用Spring Security的Web安全支持
@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserMapper userMapper;

    // 使用@Lazy注解延迟注入，打破循环依赖
    public SecurityConfig(@Lazy JwtAuthenticationFilter jwtAuthenticationFilter, UserMapper userMapper) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userMapper = userMapper;
    }

    // 1. 自定义用户服务（从数据库查询用户）
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            com.example.knowledge.entity.User user = userMapper.findByUsername(username);
            if (user == null) {
                throw new UsernameNotFoundException("用户不存在");
            }
            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getUsername())
                    .password(user.getPassword())
                    .roles("USER")
                    .build();
        };
    }

    // 2. 密码加密器 - 使用MD5加密
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new MessageDigestPasswordEncoder("MD5");
    }

    // 3. 认证提供者
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // 4. 认证管理器
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // 5. 安全过滤器链（核心配置）
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .httpBasic(httpBasic -> httpBasic.disable())
                .csrf(csrf -> csrf.disable())
                // 禁用session管理，使用JWT
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 配置接口访问权限
                .authorizeHttpRequests(auth -> auth
                        // 公开访问的接口
                        .requestMatchers("/user/login", "/user/register", "/user/validate-token").permitAll()

                        // 静态资源必须放在最前面，确保优先匹配
                        .requestMatchers("/uploads/**", "/avatars/**").permitAll()  // 允许所有用户访问上传的资源

                        // 需要认证的接口
                        .requestMatchers("/user/avatar", "/user/password", "/user/update").authenticated()
                        .requestMatchers("/upload/avatar", "/upload/file", "/upload/image").authenticated()  // 上传操作需要认证
                        .requestMatchers("/category/**", "/note-tag/**", "/user/findById").authenticated()
                        .requestMatchers("/tag/**", "/note/**", "/todo/**","/inspiration/**").authenticated()
                        .requestMatchers("/statistic/user", "/tag/common", "/note/recent-visit").authenticated()
                        .requestMatchers("/analysis/**").authenticated()
                        .requestMatchers("/knowledge/graph").authenticated()
                        // Swagger文档访问权限
                        .requestMatchers(
                                "/swagger-ui/**", "/swagger-ui.html",
                                "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**"
                        ).permitAll()

                        // 其他所有请求需要认证
                        .anyRequest().authenticated()

                )
                // 添加JWT过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authenticationProvider(authenticationProvider());

        return http.build();
    }

    // CORS配置
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8081"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Set-Cookie"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
