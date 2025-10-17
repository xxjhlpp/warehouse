package com.example.knowledge.service.serviceimpl;
import com.example.knowledge.entity.Tag;
import com.example.knowledge.mapper.TagMapper;
import com.example.knowledge.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.knowledge.mapper.NoteTagMapper;

import java.util.List;

@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private TagMapper tagMapper;

    @Autowired  // 新增注入
    private NoteTagMapper noteTagMapper;
    @Override
    public int addTag(Tag tag) {
        return tagMapper.insert(tag);
    }

    @Override
    public int deleteTag(Long id) {
        return tagMapper.deleteById(id);
    }

    @Override
    public int updateTag(Tag tag) {
        return tagMapper.updateById(tag);
    }

    @Override
    public Tag getTagById(Long id) {
        return tagMapper.selectById(id);
    }

    @Override
    public List<Tag> getAllTags() {
        return tagMapper.selectAll();
    }

    // 关键实现：根据用户ID查询标签
    @Override
    public List<Tag> getTagsByUserId(Long userId) {
        // 调用新增的mapper方法查询该用户的标签
        return tagMapper.selectByUserId(userId);
    }

    // 在TagServiceImpl中添加
    @Override
    public int countNotesByTagId(Long tagId) {
        return noteTagMapper.countByTagId(tagId);
    }

    @Override
    public List<Tag> getCommonTagsByUserId(Long userId, int limit) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        // 限制最大查询数量
        int queryLimit = Math.min(limit, 50);
        return tagMapper.selectCommonTagsByUserId(userId, queryLimit);
    }

}
