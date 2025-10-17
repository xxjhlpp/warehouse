package com.example.knowledge.controller;

import com.example.knowledge.entity.NoteAttachment;
import com.example.knowledge.service.NoteAttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 笔记附件控制器
 */
@RestController
@RequestMapping("/note")
public class NoteAttachmentController {

    private final NoteAttachmentService noteAttachmentService;

    @Autowired
    public NoteAttachmentController(NoteAttachmentService noteAttachmentService) {
        this.noteAttachmentService = noteAttachmentService;
    }

    /**
     * 保存笔记附件关联
     */
    @PostMapping("/attachments")
    public ResponseEntity<Map<String, Object>> saveAttachments(@RequestBody AttachmentRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 为每个附件设置关联的笔记ID和创建时间
            if (request.getAttachments() != null) {
                for (NoteAttachment attachment : request.getAttachments()) {
                    attachment.setNoteId(request.getNoteId());
                    attachment.setCreatedAt(LocalDateTime.now());
                }
            }

            noteAttachmentService.saveNoteAttachments(request.getNoteId(), request.getAttachments());
            response.put("success", true);
            response.put("message", "附件关联保存成功");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "附件关联保存失败: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 根据笔记ID获取附件列表
     */
    @GetMapping("/attachments/{noteId}")
    public ResponseEntity<Map<String, Object>> getAttachmentsByNoteId(@PathVariable Long noteId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<NoteAttachment> attachments = noteAttachmentService.getAttachmentsByNoteId(noteId);
            response.put("success", true);
            response.put("data", attachments);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取附件列表失败: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 删除笔记关联的所有附件
     */
    @DeleteMapping("/attachments/{noteId}")
    public ResponseEntity<Map<String, Object>> deleteAttachmentsByNoteId(@PathVariable Long noteId) {
        Map<String, Object> response = new HashMap<>();
        try {
            noteAttachmentService.deleteAttachmentsByNoteId(noteId);
            response.put("success", true);
            response.put("message", "附件关联删除成功");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "附件关联删除失败: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 附件请求参数封装类
     */
    public static class AttachmentRequest {
        private Long noteId;
        private List<NoteAttachment> attachments;

        // getter和setter
        public Long getNoteId() {
            return noteId;
        }

        public void setNoteId(Long noteId) {
            this.noteId = noteId;
        }

        public List<NoteAttachment> getAttachments() {
            return attachments;
        }

        public void setAttachments(List<NoteAttachment> attachments) {
            this.attachments = attachments;
        }
    }
}
