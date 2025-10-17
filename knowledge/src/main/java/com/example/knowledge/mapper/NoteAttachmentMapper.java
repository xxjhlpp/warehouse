package com.example.knowledge.mapper;

import com.example.knowledge.entity.NoteAttachment;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.List;

/**
 * 笔记附件数据访问接口
 */
@Mapper
public interface NoteAttachmentMapper {

    /**
     * 批量添加笔记附件关联
     * @param attachments 附件列表
     * @return 影响行数
     */
    @Insert({
            "<script>",
            "INSERT INTO note_attachment (note_id, file_url, file_name, file_size, file_type, created_at) VALUES ",
            "<foreach collection='attachments' item='item' separator=','>",
            "(#{item.noteId}, #{item.fileUrl}, #{item.fileName}, #{item.fileSize}, #{item.fileType}, #{item.createdAt})",
            "</foreach>",
            "</script>"
    })
    int batchInsert(@Param("attachments") List<NoteAttachment> attachments);

    /**
     * 根据笔记ID查询附件列表
     * @param noteId 笔记ID
     * @return 附件列表
     */
    @Select("SELECT id, note_id, file_url, file_name, file_size, file_type, created_at " +
            "FROM note_attachment WHERE note_id = #{noteId} ORDER BY created_at DESC")
    @Results({
            @Result(column = "id", property = "id", jdbcType = JdbcType.BIGINT, id = true),
            @Result(column = "note_id", property = "noteId", jdbcType = JdbcType.BIGINT),
            @Result(column = "file_url", property = "fileUrl", jdbcType = JdbcType.VARCHAR),
            @Result(column = "file_name", property = "fileName", jdbcType = JdbcType.VARCHAR),
            @Result(column = "file_size", property = "fileSize", jdbcType = JdbcType.BIGINT),
            @Result(column = "file_type", property = "fileType", jdbcType = JdbcType.VARCHAR),
            @Result(column = "created_at", property = "createdAt", jdbcType = JdbcType.TIMESTAMP)
    })
    List<NoteAttachment> selectByNoteId(Long noteId);

    /**
     * 根据笔记ID删除所有关联的附件
     * @param noteId 笔记ID
     * @return 影响行数
     */
    @Delete("DELETE FROM note_attachment WHERE note_id = #{noteId}")
    int deleteByNoteId(Long noteId);
}
