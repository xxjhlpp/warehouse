package com.example.knowledge.mapper;

import com.example.knowledge.entity.Tag;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

public interface TagMapper {
    // 新增标签
    @Insert("INSERT INTO tag (name, user_id, created_at) VALUES (#{name}, #{userId}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id") // 自增主键回显
    int insert(Tag tag);

    // 根据 ID 删除标签
    @Delete("DELETE FROM tag WHERE id = #{id}")
    int deleteById(Long id);

    // 修改标签（根据 ID 更新 name）
    @Update("UPDATE tag SET name = #{name} WHERE id = #{id}")
    int updateById(Tag tag);

    // 根据 ID 查询标签
    @Select("SELECT id, name, user_id as userId, created_at as createdAt FROM tag WHERE id = #{id}")
    Tag selectById(Long id);

    // 查询所有标签（可选，用于拓展）
    @Select("SELECT id, name, user_id as userId, created_at as createdAt FROM tag")
    List<Tag> selectAll();

    // 新增：根据用户ID查询标签
    @Select("SELECT id, name, user_id as userId, created_at as createdAt FROM tag WHERE user_id = #{userId}")
    List<Tag> selectByUserId(Long userId);

    @Select("select count(*) from tag where user_id = #{userId}")
    int countByUserId(Long userId);

    // 在 TagMapper 接口中添加
    @Select("SELECT t.* FROM tag t " +
            "JOIN note_tag nt ON t.id = nt.tag_id " +
            "WHERE t.user_id = #{userId} " +
            "GROUP BY t.id " +
            "ORDER BY COUNT(nt.note_id) DESC " +
            "LIMIT #{limit}")
    List<Tag> selectCommonTagsByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("SELECT t.id, t.name, COUNT(nt.tag_id) as count " +
            "FROM tag t " +
            "LEFT JOIN note_tag nt ON t.id = nt.tag_id " +
            "WHERE t.user_id = #{userId} " +
            "GROUP BY t.id, t.name " +
            "ORDER BY count DESC")
    List<Map<String, Object>> selectTagsWithNoteCount(Long userId);


}
