package com.example.knowledge.mapper;

import com.example.knowledge.entity.RecentVisit;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface RecentVisitMapper {

    // 新增访问记录（如果用户已访问过该笔记，则更新访问时间）
    @Insert("INSERT INTO recent_visit (user_id, note_id, visit_time) " +
            "VALUES (#{userId}, #{noteId}, #{visitTime}) " +
            "ON DUPLICATE KEY UPDATE visit_time = #{visitTime}")
    int insertOrUpdate(RecentVisit recentVisit);

    // 根据ID删除访问记录
    @Delete("DELETE FROM recent_visit WHERE id = #{id}")
    int deleteById(Long id);

    // 根据用户ID和笔记ID删除访问记录
    @Delete("DELETE FROM recent_visit WHERE user_id = #{userId} AND note_id = #{noteId}")
    int deleteByUserAndNote(@Param("userId") Long userId, @Param("noteId") Long noteId);

    // 清空用户的所有访问记录
    @Delete("DELETE FROM recent_visit WHERE user_id = #{userId}")
    int clearByUserId(Long userId);

    // 查询用户的最近访问记录（按时间倒序）
    @Select("SELECT id, user_id AS userId, note_id AS noteId, visit_time AS visitTime " +
            "FROM recent_visit WHERE user_id = #{userId} " +
            "ORDER BY visit_time DESC LIMIT #{limit}")
    List<RecentVisit> selectByUserId(@Param("userId") Long userId, @Param("limit") int limit);
}
