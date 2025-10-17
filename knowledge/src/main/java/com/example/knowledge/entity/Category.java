package com.example.knowledge.entity;

import java.time.LocalDateTime;
import java.util.List;

public class Category {
    private Long id;
    private String name;
    private Long userId;
    private Long parentId;  // 父分类ID，顶级分类为null或0
    private LocalDateTime createdAt;

    // 用于树形结构展示子分类
    private List<Category> children;

    // getter和setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<Category> getChildren() { return children; }
    public void setChildren(List<Category> children) { this.children = children; }
}
