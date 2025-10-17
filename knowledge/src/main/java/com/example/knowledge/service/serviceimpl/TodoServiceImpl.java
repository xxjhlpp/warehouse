package com.example.knowledge.service.serviceimpl;

import com.example.knowledge.dto.TodoDTO;
import com.example.knowledge.entity.Todo;
import com.example.knowledge.mapper.TodoMapper;
import com.example.knowledge.service.TodoService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TodoServiceImpl implements TodoService {

    @Autowired
    private TodoMapper todoMapper;

    @Override
    public Long addTodo(TodoDTO todoDTO, Long userId) {
        // 参数校验
        if (todoDTO.getTitle() == null || todoDTO.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("待办事项标题不能为空");
        }

        // 转换DTO为实体
        Todo todo = new Todo();
        todo.setTitle(todoDTO.getTitle());
        todo.setDeadline(todoDTO.getDeadline());
        // 默认状态为未完成(0)
        todo.setStatus(todoDTO.getStatus() != null ? todoDTO.getStatus() : 0);
        // 设置优先级，默认0(低)
        todo.setPriority(todoDTO.getPriority() != null ? todoDTO.getPriority() : 0);
        todo.setUserId(userId);
        todo.setNoteId(todoDTO.getNoteId());
        todo.setCreatedAt(LocalDateTime.now());

        // 执行插入
        todoMapper.insert(todo);
        return todo.getId();
    }

    @Override
    public boolean deleteTodo(Long id, Long userId) {
        // 先查询待办事项，验证是否属于当前用户
        Todo todo = todoMapper.selectById(id);
        if (todo == null) {
            throw new IllegalArgumentException("待办事项不存在");
        }
        if (!todo.getUserId().equals(userId)) {
            throw new SecurityException("没有权限删除该待办事项");
        }

        // 执行删除
        return todoMapper.deleteById(id) > 0;
    }

    @Override
    public boolean updateTodo(TodoDTO todoDTO, Long userId) {
        if (todoDTO.getId() == null) {
            throw new IllegalArgumentException("待办事项ID不能为空");
        }

        // 验证权限
        Todo existingTodo = todoMapper.selectById(todoDTO.getId());
        if (existingTodo == null) {
            throw new IllegalArgumentException("待办事项不存在");
        }
        if (!existingTodo.getUserId().equals(userId)) {
            throw new SecurityException("没有权限修改该待办事项");
        }

        // 更新字段
        Todo todo = new Todo();
        todo.setId(todoDTO.getId());
        todo.setTitle(todoDTO.getTitle());
        todo.setDeadline(todoDTO.getDeadline());
        todo.setStatus(todoDTO.getStatus());
        todo.setPriority(todoDTO.getPriority()); // 新增设置优先级
        todo.setNoteId(todoDTO.getNoteId());
        todo.setUserId(userId);

        // 执行更新
        return todoMapper.updateById(todo) > 0;
    }

    @Override
    public Todo getTodoById(Long id, Long userId) {
        Todo todo = todoMapper.selectById(id);
        if (todo == null) {
            return null;
        }

        // 验证权限
        if (!todo.getUserId().equals(userId)) {
            throw new SecurityException("没有权限查看该待办事项");
        }

        return todo;
    }

    @Override
    public List<Todo> getTodosByUserId(Long userId) {
        return todoMapper.selectByUserId(userId);
    }
}
