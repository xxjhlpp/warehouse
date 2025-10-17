package com.example.knowledge.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    // 统一的密钥获取方法
    private SecretKey getSigningKey() {
        try {
            // 先尝试Base64解码
            if (secret.length() > 50) { // 可能是Base64编码的长密钥
                byte[] keyBytes = Base64.getDecoder().decode(secret.trim());
                return Keys.hmacShaKeyFor(keyBytes);
            } else {
                // 如果是短字符串，直接使用
                return Keys.hmacShaKeyFor(secret.getBytes());
            }
        } catch (Exception e) {
            System.err.println("密钥处理异常，使用默认密钥: " + e.getMessage());
            // 回退方案
            String fallbackKey = "knowledge-management-system-secret-key-2024-for-jwt-token-generation";
            return Keys.hmacShaKeyFor(fallbackKey.getBytes());
        }
    }

    public String generateToken(String username) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);

        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("created", now);

        System.out.println("生成 Token 的用户名: " + username);
        System.out.println("Token 过期时间: " + expirationDate);

        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();

        System.out.println("生成的 Token 长度: " + token.length());
        return token;
    }

    // 验证JWT - 详细错误处理
    public boolean validateToken(String token) {
        try {
            System.out.println("开始验证 Token: " + token.substring(0, 20) + "...");

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            Date expiration = claims.getExpiration();

            System.out.println("Token 验证成功 - 用户名: " + username + ", 过期时间: " + expiration);
            System.out.println("当前时间: " + new Date() + ", 是否过期: " + expiration.before(new Date()));

            return !expiration.before(new Date());

        } catch (ExpiredJwtException e) {
            System.err.println("JWT Token 已过期: " + e.getMessage());
            System.err.println("过期时间: " + e.getClaims().getExpiration());
            return false;
        } catch (UnsupportedJwtException e) {
            System.err.println("不支持的 JWT Token: " + e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            System.err.println("JWT Token 格式错误: " + e.getMessage());
            return false;
        } catch (SignatureException e) {
            System.err.println("JWT Token 签名验证失败: " + e.getMessage());
            System.err.println("请检查 jwt.secret 配置是否正确");
            return false;
        } catch (IllegalArgumentException e) {
            System.err.println("JWT Token 参数错误: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("JWT Token 验证未知异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 从JWT中获取用户名
    public String getUsernameFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            System.err.println("从 Token 获取用户名失败: " + e.getMessage());
            return null;
        }
    }
}