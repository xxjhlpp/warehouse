package com.example.knowledge.service.serviceimpl;


import com.example.knowledge.entity.Note;
import com.example.knowledge.mapper.NoteMapper;
import com.example.knowledge.service.NoteService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NoteServiceImpl implements NoteService {

    @Autowired
    private NoteMapper noteMapper;

    @Override
    public Long addNote(Note note) {
        // 参数校验
        if (note.getTitle() == null || note.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("笔记标题不能为空");
        }
        if (note.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        // 设置默认值
        LocalDateTime now = LocalDateTime.now();
        note.setCreatedAt(now);
        note.setUpdatedAt(now);
        note.setIsPublic(note.getIsPublic() == null ? 0 : note.getIsPublic());
        note.setIsProcessed(note.getIsProcessed() == null ? 0 : note.getIsProcessed());

        // 执行插入
        int rows = noteMapper.insert(note);
        if (rows <= 0) {
            throw new RuntimeException("新增笔记失败");
        }
        return note.getId();
    }

    @Override
    public boolean deleteNote(Long id, Long userId) {
        // 校验笔记是否存在且属于当前用户
        Note note = noteMapper.selectById(id);
        if (note == null) {
            throw new IllegalArgumentException("笔记不存在");
        }
        if (!note.getUserId().equals(userId)) {
            throw new SecurityException("没有权限删除该笔记");
        }

        // 执行删除
        return noteMapper.deleteById(id) > 0;
    }

    @Override
    public boolean updateNote(Note note, Long userId) {
        // 校验参数
        if (note.getId() == null) {
            throw new IllegalArgumentException("笔记ID不能为空");
        }
        if (note.getTitle() != null && note.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("笔记标题不能为空");
        }

        // 校验权限
        Note existingNote = noteMapper.selectById(note.getId());
        if (existingNote == null) {
            throw new IllegalArgumentException("笔记不存在");
        }
        if (!existingNote.getUserId().equals(userId)) {
            throw new SecurityException("没有权限修改该笔记");
        }

        // 设置更新时间
        note.setUpdatedAt(LocalDateTime.now());
        // 确保用户ID不变（防止越权修改）
        note.setUserId(userId);

        // 执行更新
        return noteMapper.updateById(note) > 0;
    }

    @Override
    public Note getNoteById(Long id, Long userId) {
        Note note = noteMapper.selectById(id);
        if (note == null) {
            return null;
        }

        // 权限校验：公开笔记或自己的笔记才能查看
        if (note.getIsPublic() == 1 || note.getUserId().equals(userId)) {
            return note;
        }
        throw new SecurityException("没有权限查看该笔记");
    }

    @Override
    public List<Note> getUserNotes(Long userId) {
        return noteMapper.selectByUserId(userId);
    }

    @Override
    public List<Note> getPublicNotes() {
        return noteMapper.selectPublicNotes();
    }


    @Override
    public List<Note> getRecentNotes(Long userId, int limit) {
        // 调用Mapper层方法，按更新时间倒序查询并限制结果数量
        return noteMapper.findRecentByUserId(userId, limit);
    }

    @Override
    public List<Note> getNotesByCondition(Long userId, Long categoryId, String keyword) {
        // 实际实现需要在mapper中添加对应的查询方法
        // 这里只是示例逻辑，需要根据你的数据库查询方式实现
        return noteMapper.selectByCondition(userId, categoryId, keyword);
    }

    @Override
    public void updateNoteVisit(Long userId, Long noteId) {
        if (userId == null || noteId == null) {
            throw new IllegalArgumentException("用户ID和笔记ID不能为空");
        }
        noteMapper.updateNoteVisit(userId, noteId);
    }
}
