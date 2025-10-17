package com.example.knowledge.service.serviceimpl;

import com.example.knowledge.entity.Inspiration;
import com.example.knowledge.mapper.InspirationMapper;
import com.example.knowledge.service.InspirationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InspirationServiceImpl implements InspirationService {

    @Autowired
    private InspirationMapper inspirationMapper;

    @Override
    public Long addInspiration(Inspiration inspiration, Long userId) {
        // 参数校验
        if (inspiration.getContent() == null || inspiration.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("灵感内容不能为空");
        }
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        // 设置默认值
        LocalDateTime now = LocalDateTime.now();
        inspiration.setUserId(userId);
        inspiration.setCreatedAt(now);
        inspiration.setUpdatedAt(now);
        inspiration.setIsDeleted(0);

        // 执行插入
        int rows = inspirationMapper.insert(inspiration);
        if (rows <= 0) {
            throw new RuntimeException("新增灵感便签失败");
        }
        return inspiration.getId();
    }

    @Override
    public boolean deleteInspiration(Long id, Long userId) {
        if (id == null || userId == null) {
            throw new IllegalArgumentException("ID和用户ID不能为空");
        }

        // 检查灵感便签是否存在且属于当前用户
        Inspiration inspiration = inspirationMapper.selectById(id);
        if (inspiration == null || !inspiration.getUserId().equals(userId)) {
            throw new SecurityException("没有权限删除该灵感便签");
        }

        // 执行逻辑删除
        return inspirationMapper.deleteById(id, LocalDateTime.now()) > 0;
    }

    @Override
    public boolean updateInspiration(Inspiration inspiration, Long userId) {
        // 参数校验
        if (inspiration.getId() == null) {
            throw new IllegalArgumentException("灵感ID不能为空");
        }
        if (inspiration.getContent() != null && inspiration.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("灵感内容不能为空");
        }
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        // 检查权限
        Inspiration existing = inspirationMapper.selectById(inspiration.getId());
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new SecurityException("没有权限修改该灵感便签");
        }

        // 设置更新时间和用户ID
        inspiration.setUpdatedAt(LocalDateTime.now());
        inspiration.setUserId(userId);

        // 执行更新
        return inspirationMapper.updateById(inspiration) > 0;
    }

    @Override
    public Inspiration getInspirationById(Long id, Long userId) {
        if (id == null || userId == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        Inspiration inspiration = inspirationMapper.selectById(id);
        if (inspiration == null || !inspiration.getUserId().equals(userId)) {
            return null;
        }
        return inspiration;
    }

    @Override
    public List<Inspiration> getUserInspirations(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        return inspirationMapper.selectByUserId(userId);
    }

    @Override
    public Long countByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        if (userId == null || startDate == null || endDate == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        return inspirationMapper.countByUserIdAndDateRange(userId, startDate, endDate);
    }
}
