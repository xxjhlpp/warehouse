package com.example.knowledge.config;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.util.Base64;

public class KeyGenerator {
    public static void main(String[] args) {
        // 生成符合HS512要求的512位密钥
        byte[] keyBytes = Keys.secretKeyFor(SignatureAlgorithm.HS512).getEncoded();
        // 转换为Base64字符串以便存储
        String base64Key = Base64.getEncoder().encodeToString(keyBytes);
        System.out.println("生成的512位密钥: " + base64Key);
    }
}