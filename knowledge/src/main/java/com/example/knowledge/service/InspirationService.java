package com.example.knowledge.service;

import com.example.knowledge.entity.Inspiration;
import java.time.LocalDateTime;
import java.util.List;

public interface InspirationService {

    // 新增灵感便签
    Long addInspiration(Inspiration inspiration, Long userId);

    // 删除灵感便签
    boolean deleteInspiration(Long id, Long userId);

    // 更新灵感便签
    boolean updateInspiration(Inspiration inspiration, Long userId);

    // 根据ID查询灵感便签
    Inspiration getInspirationById(Long id, Long userId);

    // 获取用户的所有灵感便签
    List<Inspiration> getUserInspirations(Long userId);

    // 统计指定时间范围内的灵感便签数量
    Long countByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate);
}
