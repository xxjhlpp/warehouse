package com.example.knowledge.service;

import com.example.knowledge.entity.Note;
import com.example.knowledge.entity.NoteTag;
import java.util.List;

public interface NoteTagService {

    // 为笔记添加标签（单个）
    Long addNoteTag(Long noteId, Long tagId);

    // 为笔记批量添加标签
    boolean batchAddNoteTags(Long noteId, List<Long> tagIds);

    // 移除笔记与标签的关联（按关联ID）
    boolean removeNoteTagById(Long id);

    // 移除笔记的所有标签关联
    boolean removeAllTagsByNoteId(Long noteId);

    // 移除标签关联的所有笔记
    boolean removeAllNotesByTagId(Long tagId);

    // 为笔记批量移除指定标签
    boolean batchRemoveTagsFromNote(Long noteId, List<Long> tagIds);

    // 获取笔记关联的所有标签ID
    List<Long> getTagIdsByNoteId(Long noteId);

    // 获取标签关联的所有笔记ID
    List<Long> getNoteIdsByTagId(Long tagId);


}
