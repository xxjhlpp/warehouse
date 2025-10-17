package com.example.knowledge.service;

import com.example.knowledge.entity.Category;
import java.util.List;

public interface CategoryService {
    /**
     * 新增分类
     * @param category 分类信息
     * @return 新增分类的ID
     */
    Long addCategory(Category category);

    /**
     * 删除分类
     * @param id 分类ID
     * @param userId 用户ID（用于权限校验）
     * @return 是否删除成功
     */
    boolean deleteCategory(Long id, Long userId);

    /**
     * 更新分类
     * @param category 分类信息
     * @param userId 用户ID（用于权限校验）
     * @return 是否更新成功
     */
    boolean updateCategory(Category category, Long userId);

    /**
     * 根据ID查询分类
     * @param id 分类ID
     * @param userId 用户ID（用于权限校验）
     * @return 分类信息
     */
    Category getCategoryById(Long id, Long userId);

    /**
     * 获取用户的所有分类，以树形结构展示
     * @param userId 用户ID
     * @return 分类树形列表
     */
    List<Category> getUserCategoryTree(Long userId);

    /**
     * 获取指定分类的子分类
     * @param parentId 父分类ID
     * @param userId 用户ID（用于权限校验）
     * @return 子分类列表
     */
    List<Category> getChildCategories(Long parentId, Long userId);

    /**
     * 根据用户ID查询所有分类（用于父分类选择）
     */
    List<Category> selectByUserId(Long userId);


    List<Category> getUserCategories(Long userId);
}
