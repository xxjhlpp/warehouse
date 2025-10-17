package com.example.knowledge.mapper;

import com.example.knowledge.entity.NoteTag;
import org.apache.ibatis.annotations.*;
import java.util.List;

public interface NoteTagMapper {

    // 新增笔记与标签的关联关系
    @Insert("INSERT into note_tag (note_id, tag_id) values (#{noteId}, #{tagId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(NoteTag noteTag);

    // 根据ID删除关联关系
    @Delete("delete from note_tag where id = #{id}")
    int deleteById(Long id);

    // 根据笔记ID删除所有关联的标签
    @Delete("delete from note_tag where note_id = #{noteId}")
    int deleteByNoteId(Long noteId);

    // 根据标签ID删除所有关联的笔记
    @Delete("delete from note_tag where tag_id = #{tagId}")
    int deleteByTagId(Long tagId);

    // 批量删除笔记与标签的关联（根据笔记ID和标签ID列表）
    @Delete("<script>" +
            "delete from note_tag where note_id = #{noteId} and tag_id in " +
            "<foreach collection='tagIds' item='tagId' open='(' separator=',' close=')'>" +
            "#{tagId}" +
            "</foreach>" +
            "</script>")
    int batchDeleteByNoteAndTags(@Param("noteId") Long noteId, @Param("tagIds") List<Long> tagIds);

    // 查询某篇笔记关联的所有标签ID
    @Select("select tag_id from note_tag where note_id = #{noteId}")
    List<Long> selectTagIdsByNoteId(Long noteId);

    // 查询某个标签关联的所有笔记ID
    @Select("select note_id from note_tag where tag_id = #{tagId}")
    List<Long> selectNoteIdsByTagId(Long tagId);

    // 检查关联关系是否已存在（避免重复关联）
    @Select("select count(1) from note_tag where note_id = #{noteId} and tag_id = #{tagId}")
    int countByNoteAndTag(@Param("noteId") Long noteId, @Param("tagId") Long tagId);


    @Select("select count(*) from note_tag where tag_id = #{tagId}")
    int countByTagId(Long tagId);
}
