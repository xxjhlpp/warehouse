package com.example.knowledge.service.serviceimpl;

import com.example.knowledge.entity.Note;
import com.example.knowledge.entity.RecentVisit;
import com.example.knowledge.mapper.RecentVisitMapper;
import com.example.knowledge.mapper.NoteMapper;
import com.example.knowledge.service.RecentVisitService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RecentVisitServiceImpl implements RecentVisitService {

    @Autowired
    private RecentVisitMapper recentVisitMapper;

    @Autowired // 新增注入NoteMapper
    private NoteMapper noteMapper;

    @Override
    public boolean addOrUpdateVisit(Long userId, Long noteId) {
        // 参数校验
        if (userId == null || noteId == null) {
            throw new IllegalArgumentException("用户ID和笔记ID不能为空");
        }

        RecentVisit visit = new RecentVisit();
        visit.setUserId(userId);
        visit.setNoteId(noteId);
        visit.setVisitTime(LocalDateTime.now()); // 记录当前访问时间

        // 插入或更新记录（如果已存在则更新时间）
        int rows = recentVisitMapper.insertOrUpdate(visit);
        return rows > 0;
    }

    @Override
    public boolean deleteById(Long id, Long userId) {
        if (id == null || userId == null) {
            throw new IllegalArgumentException("ID和用户ID不能为空");
        }
        // 实际项目中可先查询记录确认归属，再删除（增强安全性）
        int rows = recentVisitMapper.deleteById(id);
        return rows > 0;
    }

    @Override
    public boolean deleteByNote(Long userId, Long noteId) {
        if (userId == null || noteId == null) {
            throw new IllegalArgumentException("用户ID和笔记ID不能为空");
        }
        int rows = recentVisitMapper.deleteByUserAndNote(userId, noteId);
        return rows > 0;
    }

    @Override
    public boolean clearAll(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        int rows = recentVisitMapper.clearByUserId(userId);
        return rows > 0;
    }

    @Override
    public List<RecentVisit> getRecentVisits(Long userId) {
        return getRecentVisits(userId, 10); // 默认返回最近10条
    }

    @Override
    public List<RecentVisit> getRecentVisits(Long userId, int limit) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        // 限制最大查询数量，防止恶意请求
        int queryLimit = Math.min(limit, 100);
        return recentVisitMapper.selectByUserId(userId, queryLimit);
    }

    // 新增实现：获取用户最近访问的笔记
    @Override
    public List<Note> getRecentVisitNotes(Long userId, int limit) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        // 限制最大查询数量
        int queryLimit = Math.min(limit, 100);
        // 先查询最近访问记录，再通过noteId查询笔记详情
        List<RecentVisit> visits = recentVisitMapper.selectByUserId(userId, queryLimit);
        return noteMapper.selectByIds(visits.stream()
                .map(RecentVisit::getNoteId)
                .toList());
    }
}
