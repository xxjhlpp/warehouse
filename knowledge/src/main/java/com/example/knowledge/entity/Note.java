package com.example.knowledge.entity;

import java.time.LocalDateTime;

public class Note {
    private Long id;
    private String title;
    private String content;
    private String coverUrl;
    private Long userId;
    private Long categoryId;
    private Integer isPublic; // 0-私有，1-公开
    private Integer isProcessed; // 0-未处理，1-已处理
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // getter和setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public Integer getIsPublic() { return isPublic; }
    public void setIsPublic(Integer isPublic) { this.isPublic = isPublic; }
    public Integer getIsProcessed() { return isProcessed; }
    public void setIsProcessed(Integer isProcessed) { this.isProcessed = isProcessed; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
