package com.example.knowledge.entity;

public class NoteTag {
    private Long id;         // 自增主键
    private Long noteId;     // 笔记ID（关联note表）
    private Long tagId;      // 标签ID（关联tag表）

    // getter和setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNoteId() {
        return noteId;
    }

    public void setNoteId(Long noteId) {
        this.noteId = noteId;
    }

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }
}
