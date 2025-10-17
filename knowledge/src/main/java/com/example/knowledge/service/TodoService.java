package com.example.knowledge.service;

import com.example.knowledge.dto.TodoDTO;
import com.example.knowledge.entity.Todo;
import java.util.List;

public interface TodoService {
    /**
     * 新增待办事项
     * @param todoDTO 待办事项信息
     * @param userId 用户ID
     * @return 新增的待办事项ID
     */
    Long addTodo(TodoDTO todoDTO, Long userId);

    /**
     * 根据ID删除待办事项
     * @param id 待办事项ID
     * @param userId 用户ID(用于权限验证)
     * @return 是否删除成功
     */
    boolean deleteTodo(Long id, Long userId);

    /**
     * 更新待办事项
     * @param todoDTO 待办事项信息
     * @param userId 用户ID(用于权限验证)
     * @return 是否更新成功
     */
    boolean updateTodo(TodoDTO todoDTO, Long userId);

    /**
     * 根据ID查询待办事项
     * @param id 待办事项ID
     * @param userId 用户ID(用于权限验证)
     * @return 待办事项详情
     */
    Todo getTodoById(Long id, Long userId);

    /**
     * 查询当前用户的所有待办事项
     * @param userId 用户ID
     * @return 待办事项列表
     */
    List<Todo> getTodosByUserId(Long userId);
}
