package com.example.knowledge.controller;

import com.example.knowledge.entity.Category;
import com.example.knowledge.service.CategoryService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类
     */
    @PostMapping("/add")
    public Map<String, Object> addCategory(
            @RequestParam String name,
            @RequestParam Long userId,
            @RequestParam(required = false) Long parentId) {
        Map<String, Object> result = new HashMap<>();

        // 参数验证
        if (name == null || name.trim().isEmpty()) {
            result.put("code", 400);
            result.put("msg", "分类名称不能为空");
            return result;
        }

        if (userId == null) {
            result.put("code", 400);
            result.put("msg", "用户ID不能为空");
            return result;
        }

        try {
            Category category = new Category();
            category.setName(name);
            category.setUserId(userId);
            category.setParentId(parentId);

            Long categoryId = categoryService.addCategory(category);
            result.put("code", 200);
            result.put("msg", "分类创建成功");
            result.put("data", categoryId);
        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("msg", "参数错误：" + e.getMessage());
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "服务器错误：" + e.getMessage());
            // 打印完整异常堆栈
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 删除分类
     */
    @DeleteMapping("/delete/{id}")
    public Map<String, Object> deleteCategory(
            @PathVariable Long id,
            @RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = categoryService.deleteCategory(id, userId);
            result.put("code", 200);
            result.put("msg", success ? "分类删除成功" : "分类删除失败");
        } catch (SecurityException e) {
            result.put("code", 403);
            result.put("msg", e.getMessage());
        } catch (IllegalStateException e) {
            result.put("code", 400);
            result.put("msg", e.getMessage());
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "服务器错误：" + e.getMessage());
        }
        return result;
    }

    /**
     * 更新分类
     */
    @PutMapping("/update")
    public Map<String, Object> updateCategory(
            @RequestBody Category category,
            @RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = categoryService.updateCategory(category, userId);
            result.put("code", 200);
            result.put("msg", success ? "分类更新成功" : "分类更新失败");
        } catch (SecurityException e) {
            result.put("code", 403);
            result.put("msg", e.getMessage());
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
     * 根据ID查询分类
     */
    @GetMapping("/get/{id}")
    public Map<String, Object> getCategoryById(
            @PathVariable Long id,
            @RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Category category = categoryService.getCategoryById(id, userId);
            if (category != null) {
                result.put("code", 200);
                result.put("data", category);
            } else {
                result.put("code", 404);
                result.put("msg", "分类不存在");
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
     * 获取用户的所有分类（树形结构）
     */
    @GetMapping("/tree")
    public Map<String, Object> getUserCategoryTree(@RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Category> categoryTree = categoryService.getUserCategoryTree(userId);
            result.put("code", 200);
            result.put("data", categoryTree);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "服务器错误：" + e.getMessage());
        }
        return result;
    }

    /**
     * 获取指定分类的子分类
     */
    @GetMapping("/children/{parentId}")
    public Map<String, Object> getChildCategories(
            @PathVariable Long parentId,
            @RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Category> children = categoryService.getChildCategories(parentId, userId);
            result.put("code", 200);
            result.put("data", children);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "服务器错误：" + e.getMessage());
        }
        return result;
    }

    /**
     * 获取所有可用的父分类（用于前端选择）
     */
    @GetMapping("/list")
    public Map<String, Object> getUserCategories(@RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 获取用户所有分类，用于父分类选择
            List<Category> categories = categoryService.selectByUserId(userId);
            result.put("code", 200);
            result.put("data", categories);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "服务器错误：" + e.getMessage());
        }
        return result;
    }
}
