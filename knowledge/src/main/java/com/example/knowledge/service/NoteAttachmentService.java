package com.example.knowledge.service;

import com.example.knowledge.entity.NoteAttachment;

import java.util.List;

/**
 * 笔记附件服务接口
 */
public interface NoteAttachmentService {

    /**
     * 保存笔记附件关联
     * @param noteId 笔记ID
     * @param attachments 附件列表
     */
    void saveNoteAttachments(Long noteId, List<NoteAttachment> attachments);

    /**
     * 根据笔记ID获取附件列表
     * @param noteId 笔记ID
     * @return 附件列表
     */
    List<NoteAttachment> getAttachmentsByNoteId(Long noteId);

    /**
     * 删除笔记关联的所有附件
     * @param noteId 笔记ID
     */
    void deleteAttachmentsByNoteId(Long noteId);
}