package com.example.knowledge.entity;

import java.time.LocalDateTime;

public class Todo {
    private Long id;             // 自增主键
    private String title;        // 待办事项标题
    private LocalDateTime deadline; // 截止时间
    private Integer status;      // 状态(0:未完成,1:已完成)
    private Integer priority;    // 优先级(0:低,1:中,2:高)
    private Long userId;         // 用户ID，关联user表
    private Long noteId;         // 关联的笔记ID，可为null
    private LocalDateTime createdAt; // 创建时间

    // getter和setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getNoteId() {
        return noteId;
    }

    public void setNoteId(Long noteId) {
        this.noteId = noteId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

}
