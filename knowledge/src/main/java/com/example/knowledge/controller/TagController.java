package com.example.knowledge.controller;

import com.example.knowledge.entity.Tag;
import com.example.knowledge.service.TagService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tag")
public class TagController {
    @Autowired
    private TagService tagService;

    // 新增标签 - 自动关联当前用户
    @PostMapping("/add")
    public Result<Integer> addTag(@RequestBody Tag tag, @RequestParam Long userId) {
        // 强制设置标签所属用户
        tag.setUserId(userId);
        int result = tagService.addTag(tag);
        return result > 0 ? Result.success(result) : Result.fail("新增失败");
    }

    // 删除标签 - 验证所有权
    @DeleteMapping("/delete/{id}")
    public Result<Integer> deleteTag(@PathVariable Long id, @RequestParam Long userId) {
        // 先验证标签是否属于当前用户
        Tag tag = tagService.getTagById(id);
        if (tag == null) {
            return Result.fail("标签不存在");
        }
        if (!tag.getUserId().equals(userId)) {
            return Result.fail("没有权限删除该标签");
        }
        int result = tagService.deleteTag(id);
        return result > 0 ? Result.success(result) : Result.fail("删除失败");
    }

    // 修改标签 - 验证所有权
    @PutMapping("/update")
    public Result<Integer> updateTag(@RequestBody Tag tag, @RequestParam Long userId) {
        // 验证标签是否属于当前用户
        Tag existingTag = tagService.getTagById(tag.getId());
        if (existingTag == null) {
            return Result.fail("标签不存在");
        }
        if (!existingTag.getUserId().equals(userId)) {
            return Result.fail("没有权限修改该标签");
        }
        // 确保不能修改用户ID
        tag.setUserId(userId);
        int result = tagService.updateTag(tag);
        return result > 0 ? Result.success(result) : Result.fail("修改失败");
    }

    // 查询单个标签 - 验证所有权
    @GetMapping("/get/{id}")
    public Result<Tag> getTagById(@PathVariable Long id, @RequestParam Long userId) {
        Tag tag = tagService.getTagById(id);
        if (tag == null) {
            return Result.fail("标签不存在");
        }
        // 验证标签是否属于当前用户
        if (!tag.getUserId().equals(userId)) {
            return Result.fail("没有权限查看该标签");
        }
        return Result.success(tag);
    }

    // 查询当前用户的所有标签
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getTagsByUserId(@RequestParam Long userId) {
        List<Tag> tags = tagService.getTagsByUserId(userId);
        // 转换为包含笔记数量的Map列表
        List<Map<String, Object>> resultList = tags.stream().map(tag -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", tag.getId());
            map.put("name", tag.getName());
            map.put("userId", tag.getUserId());
            map.put("createdAt", tag.getCreatedAt());
            // 查询关联的笔记数量
            map.put("count", tagService.countNotesByTagId(tag.getId()));
            return map;
        }).collect(Collectors.toList());
        return Result.success(resultList);
    }

    @GetMapping("/common")
    public Result<List<Map<String, Object>>> getCommonTags(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "5") int limit) {
        List<Tag> tags = tagService.getCommonTagsByUserId(userId, limit);
        List<Map<String, Object>> resultList = tags.stream().map(tag -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", tag.getId());
            map.put("name", tag.getName());
            map.put("count", tagService.countNotesByTagId(tag.getId()));
            return map;
        }).collect(Collectors.toList());
        return Result.success(resultList);
    }

    // 关键修复：将Result改为静态内部类
    public static class Result<T> {
        private int code;
        private String msg;
        private T data;

        public static <T> Result<T> success(T data) {
            Result<T> result = new Result<>();
            result.setCode(200);
            result.setMsg("成功");
            result.setData(data);
            return result;
        }

        public static <T> Result<T> fail(String msg) {
            Result<T> result = new Result<>();
            result.setCode(500);
            result.setMsg(msg);
            return result;
        }

        // getter 和 setter
        public int getCode() { return code; }
        public void setCode(int code) { this.code = code; }
        public String getMsg() { return msg; }
        public void setMsg(String msg) { this.msg = msg; }
        public T getData() { return data; }
        public void setData(T data) { this.data = data; }
    }
}
