package com.example.knowledge.entity;

import java.time.LocalDateTime;

public class Tag {
    private Long id;         // 对应数据库 bigint AI PK
    private String name;     // 对应数据库 varchar(30)
    private Long userId;     // 对应数据库 bigint（注意：数据库字段是 user_id，这里用驼峰命名 userId）
    private LocalDateTime createdAt; // 对应数据库 datetime

    // 省略 getter 和 setter 方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}