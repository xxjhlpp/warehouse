package com.example.knowledge.service;

import com.example.knowledge.entity.Note;
import com.example.knowledge.entity.RecentVisit;
import java.util.List;

public interface RecentVisitService {

    // 新增或更新访问记录（用户访问笔记时调用）
    boolean addOrUpdateVisit(Long userId, Long noteId);

    // 根据ID删除单条访问记录
    boolean deleteById(Long id, Long userId);

    // 删除用户对某篇笔记的访问记录
    boolean deleteByNote(Long userId, Long noteId);

    // 清空用户的所有访问记录
    boolean clearAll(Long userId);

    // 获取用户的最近访问记录（默认取前10条）
    List<RecentVisit> getRecentVisits(Long userId);

    // 获取用户的最近访问记录（自定义数量）
    List<RecentVisit> getRecentVisits(Long userId, int limit);

    // 新增方法：获取用户最近访问的笔记
    List<Note> getRecentVisitNotes(Long userId, int limit);
}

