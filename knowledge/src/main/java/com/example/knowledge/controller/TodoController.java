package com.example.knowledge.controller;


import com.example.knowledge.dto.TodoDTO;
import com.example.knowledge.entity.Todo;
import com.example.knowledge.service.TodoService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/todo")
public class TodoController {

    @Autowired
    private TodoService todoService;

    /**
     * 新增待办事项
     */
    @PostMapping("/add")
    public Map<String, Object> addTodo(@RequestBody TodoDTO todoDTO,
                                       @RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long todoId = todoService.addTodo(todoDTO, userId);
            result.put("code", 200);
            result.put("msg", "新增成功");
            result.put("data", todoId);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    /**
     * 删除待办事项
     */
    @DeleteMapping("/delete/{id}")
    public Map<String, Object> deleteTodo(@PathVariable Long id,
                                          @RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = todoService.deleteTodo(id, userId);
            if (success) {
                result.put("code", 200);
                result.put("msg", "删除成功");
            } else {
                result.put("code", 500);
                result.put("msg", "删除失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    /**
     * 更新待办事项
     */
    @PutMapping("/update")
    public Map<String, Object> updateTodo(@RequestBody TodoDTO todoDTO,
                                          @RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = todoService.updateTodo(todoDTO, userId);
            if (success) {
                result.put("code", 200);
                result.put("msg", "更新成功");
            } else {
                result.put("code", 500);
                result.put("msg", "更新失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    /**
     * 获取单个待办事项详情
     */
    @GetMapping("/get/{id}")
    public Map<String, Object> getTodoById(@PathVariable Long id,
                                           @RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Todo todo = todoService.getTodoById(id, userId);
            result.put("code", 200);
            result.put("data", todo);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
        }
        return result;
    }

    /**
     * 获取当前用户的所有待办事项
     */
    @GetMapping("/list")
    public Map<String, Object> getTodosByUserId(@RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Todo> todos = todoService.getTodosByUserId(userId);
            result.put("code", 200);
            result.put("data", todos);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
        }
        return result;
    }
}
