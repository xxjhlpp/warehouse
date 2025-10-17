package com.example.knowledge.service.serviceimpl;


import com.example.knowledge.entity.NoteAttachment;
import com.example.knowledge.mapper.NoteAttachmentMapper;
import com.example.knowledge.service.NoteAttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 笔记附件服务实现类
 */
@Service
public class NoteAttachmentServiceImpl implements NoteAttachmentService {

    private final NoteAttachmentMapper noteAttachmentMapper;

    @Autowired
    public NoteAttachmentServiceImpl(NoteAttachmentMapper noteAttachmentMapper) {
        this.noteAttachmentMapper = noteAttachmentMapper;
    }

    @Override
    @Transactional
    public void saveNoteAttachments(Long noteId, List<NoteAttachment> attachments) {
        // 先删除该笔记已有的附件关联
        noteAttachmentMapper.deleteByNoteId(noteId);

        // 为每个附件设置笔记ID和创建时间
        if (attachments != null && !attachments.isEmpty()) {
            attachments.forEach(attachment -> {
                attachment.setNoteId(noteId);
                attachment.setCreatedAt(LocalDateTime.now());
            });
            noteAttachmentMapper.batchInsert(attachments);
        }
    }

    @Override
    public List<NoteAttachment> getAttachmentsByNoteId(Long noteId) {
        return noteAttachmentMapper.selectByNoteId(noteId);
    }

    @Override
    @Transactional
    public void deleteAttachmentsByNoteId(Long noteId) {
        noteAttachmentMapper.deleteByNoteId(noteId);
    }
}
