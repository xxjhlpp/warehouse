package com.example.knowledge.controller;

import com.example.knowledge.service.NoteTagService;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
@RestController
@RequestMapping("/note-tag")
public class NoteTagController {

    @Autowired
    private NoteTagService noteTagService;

    /**
     * 为笔记添加单个标签
     */
    @PostMapping("/add")
    public Map<String, Object> addNoteTag(
            @RequestParam Long noteId,
            @RequestParam Long tagId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long relationId = noteTagService.addNoteTag(noteId, tagId);
            result.put("code", 200);
            result.put("msg", "标签关联成功");
            result.put("data", relationId); // 返回关联关系ID
        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("msg", e.getMessage());
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "服务器错误：" + e.getMessage());
        }
        return result;
    }

    /**
     * 为笔记批量添加标签
     */
    @PostMapping("/batch-add")
    public Map<String, Object> batchAddNoteTags(
            @RequestParam Long noteId,
            @RequestBody List<Long> tagIds) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = noteTagService.batchAddNoteTags(noteId, tagIds);
            result.put("code", 200);
            result.put("msg", success ? "标签批量关联成功" : "标签批量关联失败");
        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("msg", e.getMessage());
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "服务器错误：" + e.getMessage());
        }
        return result;
    }

    /**
     * 移除单个关联关系
     */
    @DeleteMapping("/delete/{id}")
    public Map<String, Object> deleteNoteTag(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = noteTagService.removeNoteTagById(id);
            result.put("code", 200);
            result.put("msg", success ? "关联关系已移除" : "移除失败");
        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("msg", e.getMessage());
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "服务器错误：" + e.getMessage());
        }
        return result;
    }

    /**
     * 移除笔记的所有标签关联
     */
    @DeleteMapping("/clear-by-note/{noteId}")
    public Map<String, Object> clearTagsByNoteId(@PathVariable Long noteId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = noteTagService.removeAllTagsByNoteId(noteId);
            result.put("code", 200);
            result.put("msg", success ? "笔记的所有标签已移除" : "移除失败");
        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("msg", e.getMessage());
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "服务器错误：" + e.getMessage());
        }
        return result;
    }

    /**
     * 移除标签关联的所有笔记
     */
    @DeleteMapping("/clear-by-tag/{tagId}")
    public Map<String, Object> clearNotesByTagId(@PathVariable Long tagId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = noteTagService.removeAllNotesByTagId(tagId);
            result.put("code", 200);
            result.put("msg", success ? "标签关联的所有笔记已移除" : "移除失败");
        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("msg", e.getMessage());
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "服务器错误：" + e.getMessage());
        }
        return result;
    }

    /**
     * 为笔记批量移除指定标签
     */
    @DeleteMapping("/batch-remove")
    public Map<String, Object> batchRemoveTags(
            @RequestParam Long noteId,
            @RequestBody List<Long> tagIds) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = noteTagService.batchRemoveTagsFromNote(noteId, tagIds);
            result.put("code", 200);
            result.put("msg", success ? "标签批量移除成功" : "标签批量移除失败");
        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("msg", e.getMessage());
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "服务器错误：" + e.getMessage());
        }
        return result;
    }

    /**
     * 获取笔记关联的所有标签ID
     */
    @GetMapping("/tags/{noteId}")
    public Map<String, Object> getTagsByNoteId(@PathVariable Long noteId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Long> tagIds = noteTagService.getTagIdsByNoteId(noteId);
            result.put("code", 200);
            result.put("data", tagIds);
        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("msg", e.getMessage());
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "服务器错误：" + e.getMessage());
        }
        return result;
    }

    /**
     * 获取标签关联的所有笔记ID
     */
    @GetMapping("/notes/{tagId}")
    public Map<String, Object> getNotesByTagId(@PathVariable Long tagId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Long> noteIds = noteTagService.getNoteIdsByTagId(tagId);
            result.put("code", 200);
            result.put("data", noteIds);
        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("msg", e.getMessage());
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "服务器错误：" + e.getMessage());
        }
        return result;
    }
}
