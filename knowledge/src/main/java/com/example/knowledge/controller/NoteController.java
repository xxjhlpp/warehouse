package com.example.knowledge.controller;

import com.example.knowledge.entity.Note;
import com.example.knowledge.service.NoteService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/note")
public class NoteController {

    @Autowired
    private NoteService noteService;

    /**
     * 新增笔记
     */
    @PostMapping("/add")
    public Map<String, Object> addNote(@RequestBody Note note) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long noteId = noteService.addNote(note);
            result.put("code", 200);
            result.put("msg", "笔记新增成功");
            result.put("data", noteId);
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
     * 删除笔记
     */
    @DeleteMapping("/delete/{id}")
    public Map<String, Object> deleteNote(
            @PathVariable Long id,
            @RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = noteService.deleteNote(id, userId);
            result.put("code", 200);
            result.put("msg", success ? "笔记删除成功" : "笔记删除失败");
        } catch (IllegalArgumentException | SecurityException e) {
            result.put("code", 403);
            result.put("msg", e.getMessage());
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "服务器错误：" + e.getMessage());
        }
        return result;
    }

    /**
     * 更新笔记
     */
    @PutMapping("/update")
    public Map<String, Object> updateNote(
            @RequestBody Note note,
            @RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = noteService.updateNote(note, userId);
            result.put("code", 200);
            result.put("msg", success ? "笔记更新成功" : "笔记更新失败");
        } catch (IllegalArgumentException | SecurityException e) {
            result.put("code", 403);
            result.put("msg", e.getMessage());
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "服务器错误：" + e.getMessage());
        }
        return result;
    }

    /**
     * 根据ID查询笔记
     */
    @GetMapping("/get/{id}")
    public Map<String, Object> getNoteById(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Note note = noteService.getNoteById(id, userId);
            if (note != null) {
                // 当用户登录且是查看自己的笔记时，更新访问时间
                if (userId != null && note.getUserId().equals(userId)) {
                    noteService.updateNoteVisit(userId, id);
                }
                result.put("code", 200);
                result.put("data", note);
            } else {
                result.put("code", 404);
                result.put("msg", "笔记不存在");
            }
        } catch (SecurityException e) {
            result.put("code", 403);
            result.put("msg", e.getMessage());
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "服务器错误：" + e.getMessage());
        }
        return result;
    }

    /**
     * 获取当前用户的所有笔记
     */
    @GetMapping("/my-notes")
    public Map<String, Object> getUserNotes(@RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Note> notes = noteService.getUserNotes(userId);
            result.put("code", 200);
            result.put("count", notes.size());
            result.put("data", notes);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "服务器错误：" + e.getMessage());
        }
        return result;
    }

    /**
     * 获取公开笔记列表
     */
//    @GetMapping("/public")
//    public Map<String, Object> getPublicNotes() {
//        Map<String, Object> result = new HashMap<>();
//        try {
//            List<Note> notes = noteService.getPublicNotes();
//            result.put("code", 200);
//            result.put("count", note.size());
//            result.put("data", note);
//        } catch (Exception e) {
//            result.put("code", 500);
//            result.put("msg", "服务器错误：" + e.getMessage());
//        }
//        return result;
//    }

    /**
     * 获取用户最近更新的笔记（最多3条）
     */
    @GetMapping("/recent")
    public Map<String, Object> getRecentNotes(@RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 调用服务层获取最近3条笔记，按更新时间倒序
            List<Note> recentNotes = noteService.getRecentNotes(userId, 3);
            result.put("code", 200);
            result.put("data", recentNotes);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "服务器错误：" + e.getMessage());
        }
        return result;
    }

    /**
     * 按条件查询笔记列表（支持分类和关键词筛选）
     */
    @GetMapping("/list")
    public Map<String, Object> getNotesByCondition(
            @RequestParam Long userId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Note> notes = noteService.getNotesByCondition(userId, categoryId, keyword);
            result.put("code", 200);
            result.put("data", notes);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "服务器错误：" + e.getMessage());
        }
        return result;
    }
}
