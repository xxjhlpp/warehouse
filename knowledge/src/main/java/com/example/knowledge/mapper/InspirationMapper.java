package com.example.knowledge.mapper;

import com.example.knowledge.entity.Inspiration;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface InspirationMapper {

    // 新增灵感便签
    @Insert("INSERT INTO inspiration (user_id, content, created_at, updated_at) " +
            "VALUES (#{userId}, #{content}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Inspiration inspiration);

    // 根据ID删除灵感便签
    @Update("UPDATE inspiration SET is_deleted = 1, updated_at = #{updatedAt} WHERE id = #{id}")
    int deleteById(@Param("id") Long id, @Param("updatedAt") LocalDateTime updatedAt);

    // 更新灵感便签
    @Update("UPDATE inspiration SET content = #{content}, " +
            "updated_at = #{updatedAt} WHERE id = #{id} AND user_id = #{userId}")
    int updateById(Inspiration inspiration);

    // 根据ID查询灵感便签
    @Select("SELECT id, user_id as userId, content, created_at as createdAt, " +
            "updated_at as updatedAt FROM inspiration " +
            "WHERE id = #{id} AND is_deleted = 0")
    Inspiration selectById(Long id);

    // 根据用户ID查询灵感便签列表
    @Select("SELECT id, user_id as userId, content, created_at as createdAt, " +
            "updated_at as updatedAt FROM inspiration " +
            "WHERE user_id = #{userId} AND is_deleted = 0 " +
            "ORDER BY created_at DESC")
    List<Inspiration> selectByUserId(Long userId);

    // 统计用户指定时间范围内的灵感便签数量
    @Select("SELECT COUNT(*) FROM inspiration " +
            "WHERE user_id = #{userId} AND is_deleted = 0 " +
            "AND created_at BETWEEN #{startDate} AND #{endDate}")
    Long countByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);


}
