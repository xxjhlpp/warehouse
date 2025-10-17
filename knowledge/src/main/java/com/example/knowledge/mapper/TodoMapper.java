package com.example.knowledge.mapper;

import com.example.knowledge.entity.Todo;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface TodoMapper {
    // 新增待办事项
    @Insert("INSERT INTO todo (title, deadline, status, priority, user_id, note_id, created_at) " +
            "VALUES (#{title}, #{deadline}, #{status}, #{priority}, #{userId}, #{noteId}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Todo todo);


    // 根据ID删除待办事项
    @Delete("DELETE FROM todo WHERE id = #{id}")
    int deleteById(Long id);

    // 更新待办事项
    @Update("UPDATE todo SET title = #{title}, deadline = #{deadline}, " +
            "status = #{status}, priority = #{priority}, note_id = #{noteId} WHERE id = #{id}")
    int updateById(Todo todo);

    // 根据ID查询待办事项
    @Select("SELECT id, title, deadline, status, priority, user_id as userId, " +
            "note_id as noteId, created_at as createdAt FROM todo WHERE id = #{id}")
    Todo selectById(Long id);

    // 根据用户ID查询所有待办事项
    @Select("SELECT id, title, deadline, status, priority, user_id as userId, " +
            "note_id as noteId, created_at as createdAt FROM todo WHERE user_id = #{userId}")
    List<Todo> selectByUserId(Long userId);
}
