package com.example.knowledge.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 笔记附件实体类
 */
@Data
public class NoteAttachment {
    private Long id;                 // 主键ID
    private Long noteId;             // 关联的笔记ID
    private String fileUrl;          // 文件URL
    private String fileName;         // 文件名
    private Long fileSize;           // 文件大小(字节)
    private String fileType;         // 文件类型
    private LocalDateTime createdAt; // 创建时间
}