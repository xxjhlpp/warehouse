package com.example.knowledge.mapper;

import com.example.knowledge.entity.Note;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface NoteMapper {

    // 新增笔记
    @Insert("INSERT INTO note (title, content, cover_url, user_id, category_id, " +
            "is_public, is_processed, created_at, updated_at) " +
            "VALUES (#{title}, #{content}, #{coverUrl}, #{userId}, #{categoryId}, " +
            "#{isPublic}, #{isProcessed}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Note note);

    // 根据ID删除笔记
    @Delete("DELETE FROM note WHERE id = #{id}")
    int deleteById(Long id);

    // 更新笔记
    @Update("UPDATE note SET title = #{title}, content = #{content}, cover_url = #{coverUrl}, " +
            "category_id = #{categoryId}, is_public = #{isPublic}, is_processed = #{isProcessed}, " +
            "updated_at = #{updatedAt} WHERE id = #{id}")
    int updateById(Note note);

    // 根据ID查询笔记
    @Select("SELECT * FROM note WHERE id = #{id}")
    Note selectById(Long id);

    // 根据用户ID查询笔记列表
    @Select("SELECT * FROM note WHERE user_id = #{userId} ORDER BY updated_at DESC")
    List<Note> selectByUserId(Long userId);

    // 查询公开笔记
    @Select("SELECT * FROM note WHERE is_public = 1 ORDER BY updated_at DESC")
    List<Note> selectPublicNotes();

    @Select("select count(*) from note where user_id = #{userId}")
    int countByUserId(Long userId);

    // 查询用户最近更新的笔记
    @Select("SELECT * FROM note " +
            "WHERE user_id = #{userId} " +
            "ORDER BY updated_at DESC, created_at DESC " +
            "LIMIT #{limit}")
    List<Note> findRecentByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 按条件查询笔记
     */
    @Select("<script>" +
            "SELECT * FROM note WHERE user_id = #{userId} " +
            "<if test='categoryId != null'>AND category_id = #{categoryId}</if> " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (title LIKE CONCAT('%', #{keyword}, '%') OR content LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if>" +
            "ORDER BY updated_at DESC" +
            "</script>")
    List<Note> selectByCondition(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("keyword") String keyword);

    /**
     * 修复：根据ID列表查询笔记
     * 添加了对空列表的处理，避免SQL语法错误
     */
    @Select("<script>" +
            "SELECT * FROM note " +
            "<where>" +
            "<if test='ids != null and ids.size() > 0'>" +
            "id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</if>" +
            "<if test='ids == null or ids.size() == 0'>" +
            "1 = 2  -- 当ID列表为空时，查询条件恒为假，返回空结果" +
            "</if>" +
            "</where>" +
            "</script>")
    List<Note> selectByIds(@Param("ids") List<Long> ids);

    @Select("<script>" +
            "SELECT DATE_FORMAT(created_at, #{dateFormat}) as date, COUNT(*) as count " +
            "FROM note " +
            "WHERE user_id = #{userId} " +
            "<if test='startDate != null'>AND created_at &gt;= #{startDate}</if> " + // 将 >= 改为 &gt;=
            "<if test='endDate != null'>AND created_at &lt;= #{endDate}</if> " +     // 将 <= 改为 &lt;=
            "GROUP BY DATE_FORMAT(created_at, #{dateFormat}) " +
            "ORDER BY date" +
            "</script>")
    List<Map<String, Object>> countByDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("dateFormat") String dateFormat);

    // 按用户ID和日期范围统计笔记数量
    @Select("SELECT COUNT(*) FROM note WHERE user_id = #{userId} AND created_at BETWEEN #{startDate} AND #{endDate}")
    Long countByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // 查询沉睡笔记
    @Select("SELECT id, title, updated_at as lastAccessed " +
            "FROM note " +
            "WHERE user_id = #{userId} AND updated_at < #{threshold} " +
            "ORDER BY updated_at ASC " +
            "LIMIT 3")
    List<Map<String, Object>> findSleepingNotes(
            @Param("userId") Long userId,
            @Param("threshold") LocalDateTime threshold);

    // 根据用户ID查询所有笔记
    @Select("SELECT id, title, last_accessed FROM note WHERE user_id = #{userId}")
    List<Note> findByUserId(Long userId);

    @Update("INSERT INTO recent_visit (user_id, note_id, visit_time) " +
            "VALUES (#{userId}, #{noteId}, NOW()) " +
            "ON DUPLICATE KEY UPDATE visit_time = NOW()")  // 移除多余的UPDATE
    void updateNoteVisit(@Param("userId") Long userId, @Param("noteId") Long noteId);
}


