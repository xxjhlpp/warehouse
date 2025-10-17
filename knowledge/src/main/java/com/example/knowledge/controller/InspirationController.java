package com.example.knowledge.controller;

import com.example.knowledge.entity.Inspiration;
import com.example.knowledge.service.InspirationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inspiration")
public class InspirationController {

    @Autowired
    private InspirationService inspirationService;

    /**
     * 新增灵感便签
     */
    @PostMapping("/add")
    public Map<String, Object> addInspiration(
            @RequestBody Inspiration inspiration,
            @RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long id = inspirationService.addInspiration(inspiration, userId);
            result.put("code", 200);
            result.put("msg", "新增成功");
            result.put("data", id);
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
     * 删除灵感便签
     */
    @DeleteMapping("/delete/{id}")
    public Map<String, Object> deleteInspiration(
            @PathVariable Long id,
            @RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = inspirationService.deleteInspiration(id, userId);
            result.put("code", 200);
            result.put("msg", success ? "删除成功" : "删除失败");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    /**
     * 更新灵感便签
     */
    @PutMapping("/update")
    public Map<String, Object> updateInspiration(
            @RequestBody Inspiration inspiration,
            @RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = inspirationService.updateInspiration(inspiration, userId);
            result.put("code", 200);
            result.put("msg", success ? "更新成功" : "更新失败");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    /**
     * 获取单个灵感便签
     */
    @GetMapping("/get/{id}")
    public Map<String, Object> getInspirationById(
            @PathVariable Long id,
            @RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Inspiration inspiration = inspirationService.getInspirationById(id, userId);
            result.put("code", 200);
            result.put("data", inspiration);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    /**
     * 获取用户的所有灵感便签
     */
    @GetMapping("/list")
    public Map<String, Object> getUserInspirations(@RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Inspiration> inspirations = inspirationService.getUserInspirations(userId);
            result.put("code", 200);
            result.put("data", inspirations);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
        }
        return result;
    }
}
