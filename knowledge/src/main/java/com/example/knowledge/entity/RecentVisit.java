package com.example.knowledge.entity;

import java.time.LocalDateTime;

public class RecentVisit {
    private Long id;             // 自增主键
    private Long userId;         // 用户ID（关联user表）
    private Long noteId;         // 笔记ID（关联note表）
    private LocalDateTime visitTime;  // 访问时间

    // getter和setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getVisitTime() {
        return visitTime;
    }

    public void setVisitTime(LocalDateTime visitTime) {
        this.visitTime = visitTime;
    }
}
