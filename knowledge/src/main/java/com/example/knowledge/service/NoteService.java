package com.example.knowledge.service;

import com.example.knowledge.entity.Note;
import java.util.List;

public interface NoteService {
    // 新增笔记
    Long addNote(Note note);

    // 删除笔记
    boolean deleteNote(Long id, Long userId);

    // 更新笔记
    boolean updateNote(Note note, Long userId);

    // 根据ID查询笔记（带权限校验）
    Note getNoteById(Long id, Long userId);

    // 获取用户的所有笔记
    List<Note> getUserNotes(Long userId);

    // 获取公开笔记列表
    List<Note> getPublicNotes();

    // 添加获取最近笔记的方法
    List<Note> getRecentNotes(Long userId, int limit);

    // 添加条件查询方法
    List<Note> getNotesByCondition(Long userId, Long categoryId, String keyword);

    // 更新笔记的最后访问时间
    void updateNoteVisit(Long userId, Long noteId);
}
