package com.example.knowledge.service.serviceimpl;

import com.example.knowledge.entity.NoteTag;
import com.example.knowledge.mapper.NoteTagMapper;
import com.example.knowledge.service.NoteTagService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Service
public class NoteTagServiceImpl implements NoteTagService {

    @Autowired
    private NoteTagMapper noteTagMapper;

    @Override
    public Long addNoteTag(Long noteId, Long tagId) {
        // 校验参数
        if (noteId == null || tagId == null) {
            throw new IllegalArgumentException("笔记ID和标签ID不能为空");
        }

        // 检查是否已存在关联关系（避免重复添加）
        int count = noteTagMapper.countByNoteAndTag(noteId, tagId);
        if (count > 0) {
            throw new IllegalArgumentException("该笔记已关联此标签");
        }

        // 新增关联关系
        NoteTag noteTag = new NoteTag();
        noteTag.setNoteId(noteId);
        noteTag.setTagId(tagId);
        noteTagMapper.insert(noteTag);
        return noteTag.getId();
    }

    @Override
    public boolean batchAddNoteTags(Long noteId, List<Long> tagIds) {
        if (noteId == null || tagIds == null || tagIds.isEmpty()) {
            throw new IllegalArgumentException("参数不能为空");
        }

        // 批量添加（过滤已存在的关联）
        for (Long tagId : tagIds) {
            int count = noteTagMapper.countByNoteAndTag(noteId, tagId);
            if (count == 0) {
                NoteTag noteTag = new NoteTag();
                noteTag.setNoteId(noteId);
                noteTag.setTagId(tagId);
                noteTagMapper.insert(noteTag);
            }
        }
        return true;
    }

    @Override
    public boolean removeNoteTagById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("关联ID不能为空");
        }
        return noteTagMapper.deleteById(id) > 0;
    }

    @Override
    public boolean removeAllTagsByNoteId(Long noteId) {
        if (noteId == null) {
            throw new IllegalArgumentException("笔记ID不能为空");
        }
        return noteTagMapper.deleteByNoteId(noteId) > 0;
    }

    @Override
    public boolean removeAllNotesByTagId(Long tagId) {
        if (tagId == null) {
            throw new IllegalArgumentException("标签ID不能为空");
        }
        return noteTagMapper.deleteByTagId(tagId) > 0;
    }

    @Override
    public boolean batchRemoveTagsFromNote(Long noteId, List<Long> tagIds) {
        if (noteId == null || tagIds == null || tagIds.isEmpty()) {
            throw new IllegalArgumentException("参数不能为空");
        }
        return noteTagMapper.batchDeleteByNoteAndTags(noteId, tagIds) > 0;
    }

    @Override
    public List<Long> getTagIdsByNoteId(Long noteId) {
        if (noteId == null) {
            throw new IllegalArgumentException("笔记ID不能为空");
        }
        return noteTagMapper.selectTagIdsByNoteId(noteId);
    }

    @Override
    public List<Long> getNoteIdsByTagId(Long tagId) {
        if (tagId == null) {
            throw new IllegalArgumentException("标签ID不能为空");
        }
        return noteTagMapper.selectNoteIdsByTagId(tagId);
    }


}
