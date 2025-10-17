package com.example.knowledge.controller;

import com.example.knowledge.entity.Note;
import com.example.knowledge.entity.Category;
import com.example.knowledge.service.NoteService;
import com.example.knowledge.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/knowledge") // 匹配请求路径的前缀
public class GraphController {

    @Autowired
    private NoteService noteService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 知识图谱数据接口
     */
    @GetMapping("/graph")
    public Map<String, Object> getKnowledgeGraph(@RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 1. 获取用户的分类和笔记数据
            List<Category> categories = categoryService.getUserCategories(userId);
            List<Note> notes = noteService.getUserNotes(userId);

            // 2. 构建图谱节点
            List<Map<String, Object>> nodes = new ArrayList<>();
            // 添加分类节点
            for (Category category : categories) {
                Map<String, Object> node = new HashMap<>();
                node.put("id", "category_" + category.getId());
                node.put("name", category.getName());
                node.put("type", "分类");
                node.put("size", 30);
                // 添加父ID信息用于前端参考
                node.put("parentId", category.getParentId() != null ? "category_" + category.getParentId() : null);
                nodes.add(node);
            }
            // 添加笔记节点
            for (Note note : notes) {
                Map<String, Object> node = new HashMap<>();
                node.put("id", "note_" + note.getId());
                node.put("name", note.getTitle());
                node.put("type", "笔记");
                node.put("size", 20);
                nodes.add(node);
            }

            // 3. 构建分类间的父子关系
            List<Map<String, Object>> links = new ArrayList<>();
            for (Category category : categories) {
                if (category.getParentId() != null) {
                    Map<String, Object> link = new HashMap<>();
                    link.put("source", "category_" + category.getParentId());
                    link.put("target", "category_" + category.getId());
                    link.put("label", "包含");
                    link.put("relationType", "parent-child");
                    links.add(link);
                }
            }

            // 4. 构建笔记-分类关系
            for (Note note : notes) {
                if (note.getCategoryId() != null) {
                    Map<String, Object> link = new HashMap<>();
                    link.put("source", "category_" + note.getCategoryId());
                    link.put("target", "note_" + note.getId());
                    link.put("label", "包含");
                    links.add(link);
                }
            }

            // 5. 组装返回结果
            Map<String, Object> data = new HashMap<>();
            data.put("nodes", nodes);
            data.put("links", links);

            result.put("code", 200);
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "获取知识图谱失败：" + e.getMessage());
        }
        return result;
    }
}