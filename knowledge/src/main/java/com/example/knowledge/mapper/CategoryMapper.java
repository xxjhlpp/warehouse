package com.example.knowledge.mapper;

import com.example.knowledge.entity.Category;
import org.apache.ibatis.annotations.*;
import java.util.List;

public interface CategoryMapper {

    /**
     * 新增分类
     */
    @Insert("INSERT INTO category (name, user_id, parent_id, created_at) " +
            "VALUES (#{name}, #{userId}, #{parentId}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Category category);

    /**
     * 根据ID删除分类
     */
    @Delete("DELETE FROM category WHERE id = #{id}")
    int deleteById(Long id);

    /**
     * 根据ID更新分类
     */
    @Update("UPDATE category SET name = #{name}, parent_id = #{parentId} " +
            "WHERE id = #{id}")
    int updateById(Category category);

    /**
     * 根据ID查询分类
     */
    @Select("SELECT id, name, user_id as userId, parent_id as parentId, created_at as createdAt " +
            "FROM category WHERE id = #{id}")
    Category selectById(Long id);

    /**
     * 根据用户ID查询所有分类
     */
    @Select("SELECT id, name, user_id as userId, parent_id as parentId, created_at as createdAt " +
            "FROM category WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<Category> selectByUserId(Long userId);

    /**
     * 根据父分类ID和用户ID查询子分类
     */
    @Select("SELECT id, name, user_id as userId, parent_id as parentId, created_at as createdAt " +
            "FROM category WHERE parent_id = #{parentId} AND user_id = #{userId} " +
            "ORDER BY created_at DESC")
    List<Category> selectByParentId(Long parentId, Long userId);

    @Select("select count(*) from category where user_id = #{userId}")
    int countByUserId(Long userId);


}

