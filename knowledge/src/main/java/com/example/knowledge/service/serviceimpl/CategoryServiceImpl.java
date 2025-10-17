package com.example.knowledge.service.serviceimpl;


import com.example.knowledge.entity.Category;
import com.example.knowledge.mapper.CategoryMapper;
import com.example.knowledge.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public Long addCategory(Category category) {
        // 参数校验
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("分类名称不能为空");
        }
        if (category.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        // 检查父分类是否存在且属于当前用户
        if (category.getParentId() != null) {
            Category parent = categoryMapper.selectById(category.getParentId());
            if (parent == null) {
                throw new IllegalArgumentException("父分类不存在");
            }
            if (!parent.getUserId().equals(category.getUserId())) {
                throw new SecurityException("无权访问该父分类");
            }
        }

        // 设置创建时间
        category.setCreatedAt(LocalDateTime.now());

        // 插入数据库
        categoryMapper.insert(category);
        return category.getId();
    }

    @Override
    public boolean deleteCategory(Long id, Long userId) {
        // 校验分类是否存在
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            return false;
        }

        // 权限校验
        if (!category.getUserId().equals(userId)) {
            throw new SecurityException("无权删除该分类");
        }

        // 检查是否有子分类
        List<Category> children = categoryMapper.selectByParentId(id, userId);
        if (!children.isEmpty()) {
            throw new IllegalStateException("该分类下存在子分类，无法删除");
        }

        // 执行删除
        return categoryMapper.deleteById(id) > 0;
    }

    @Override
    public boolean updateCategory(Category category, Long userId) {
        // 参数校验
        if (category.getId() == null) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("分类名称不能为空");
        }

        // 校验分类是否存在
        Category existing = categoryMapper.selectById(category.getId());
        if (existing == null) {
            return false;
        }

        // 权限校验
        if (!existing.getUserId().equals(userId)) {
            throw new SecurityException("无权修改该分类");
        }

        // 如果修改了父分类，需要校验新父分类
        if (category.getParentId() != null &&
                !category.getParentId().equals(existing.getParentId())) {
            Category newParent = categoryMapper.selectById(category.getParentId());
            if (newParent == null) {
                throw new IllegalArgumentException("新父分类不存在");
            }
            if (!newParent.getUserId().equals(userId)) {
                throw new SecurityException("无权访问新父分类");
            }
        }

        // 执行更新
        return categoryMapper.updateById(category) > 0;
    }

    @Override
    public Category getCategoryById(Long id, Long userId) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            return null;
        }

        // 权限校验
        if (!category.getUserId().equals(userId)) {
            throw new SecurityException("无权访问该分类");
        }

        return category;
    }

    @Override
    public List<Category> getUserCategoryTree(Long userId) {
        // 获取用户所有分类
        List<Category> allCategories = categoryMapper.selectByUserId(userId);

        // 构建树形结构
        return buildCategoryTree(allCategories, null);
    }

    @Override
    public List<Category> getChildCategories(Long parentId, Long userId) {
        return categoryMapper.selectByParentId(parentId, userId);
    }

    /**
     * 递归构建分类树形结构
     */
    private List<Category> buildCategoryTree(List<Category> allCategories, Long parentId) {
        return allCategories.stream()
                .filter(cat -> (parentId == null && cat.getParentId() == null) ||
                        (parentId != null && parentId.equals(cat.getParentId())))
                .peek(cat -> cat.setChildren(buildCategoryTree(allCategories, cat.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<Category> selectByUserId(Long userId) {
        return categoryMapper.selectByUserId(userId);
    }

    @Override
    public List<Category> getUserCategories(Long userId) {
        return categoryMapper.selectByUserId(userId); // 假设已有该Mapper方法
    }
}

