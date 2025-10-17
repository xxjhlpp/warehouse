package com.example.knowledge.controller;

import com.example.knowledge.entity.Note;
import com.example.knowledge.entity.RecentVisit;
import com.example.knowledge.service.RecentVisitService;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/recent-visit")
public class RecentVisitController {

    @Autowired
    private RecentVisitService recentVisitService;

    /**
     * 记录用户访问笔记（新增或更新访问时间）
     */
    @PostMapping("/record")
    public Map<String, Object> recordVisit(
            @RequestParam Long userId,
            @RequestParam Long noteId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = recentVisitService.addOrUpdateVisit(userId, noteId);
            result.put("code", 200);
            result.put("msg", success ? "记录成功" : "记录失败");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    /**
     * 删除单条访问记录
     */
    @DeleteMapping("/delete/{id}")
    public Map<String, Object> deleteById(
            @PathVariable Long id,
            @RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = recentVisitService.deleteById(id, userId);
            result.put("code", 200);
            result.put("msg", success ? "删除成功" : "删除失败");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    /**
     * 删除用户对某篇笔记的访问记录
     */
    @DeleteMapping("/delete/note")
    public Map<String, Object> deleteByNote(
            @RequestParam Long userId,
            @RequestParam Long noteId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = recentVisitService.deleteByNote(userId, noteId);
            result.put("code", 200);
            result.put("msg", success ? "删除成功" : "删除失败");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    /**
     * 清空用户的所有访问记录
     */
    @DeleteMapping("/clear")
    public Map<String, Object> clearAll(@RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = recentVisitService.clearAll(userId);
            result.put("code", 200);
            result.put("msg", success ? "清空成功" : "清空失败");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    /**
     * 获取用户的最近访问记录
     */
    @GetMapping("/list")
    public Map<String, Object> getRecentVisits(
            @RequestParam Long userId,
            @RequestParam(required = false, defaultValue = "10") int limit) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<RecentVisit> visits = recentVisitService.getRecentVisits(userId, limit);
            result.put("code", 200);
            result.put("data", visits);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    @GetMapping("/note")
    public Map<String, Object> getRecentVisitNotes(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "3") int limit) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Note> notes = recentVisitService.getRecentVisitNotes(userId, limit);
            result.put("code", 200);
            result.put("data", notes);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
        }
        return result;
    }
}
