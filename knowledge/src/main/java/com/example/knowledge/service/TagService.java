package com.example.knowledge.service;

import com.example.knowledge.entity.Tag;
import java.util.List;

public interface TagService {
    int addTag(Tag tag);
    int deleteTag(Long id);
    int updateTag(Tag tag);
    Tag getTagById(Long id);
    List<Tag> getAllTags();
    // 根据用户ID查询标签
    List<Tag> getTagsByUserId(Long userId);

    int countNotesByTagId(Long tagId);

    // 新增方法：获取用户常用标签
    List<Tag> getCommonTagsByUserId(Long userId, int limit);
}
