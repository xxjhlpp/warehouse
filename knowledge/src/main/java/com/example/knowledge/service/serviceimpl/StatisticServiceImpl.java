package com.example.knowledge.service.serviceimpl;

import com.example.knowledge.mapper.CategoryMapper;
import com.example.knowledge.mapper.NoteMapper;
import com.example.knowledge.mapper.TagMapper;
import com.example.knowledge.service.StatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StatisticServiceImpl implements StatisticService {

    @Autowired
    private NoteMapper noteMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private TagMapper tagMapper;

    @Override
    public Map<String, Integer> getUserStatistics(Long userId) {
        Map<String, Integer> stats = new HashMap<>();

        // 查询各类统计数据
        stats.put("notes", noteMapper.countByUserId(userId));
        stats.put("categories", categoryMapper.countByUserId(userId));
        stats.put("tags", tagMapper.countByUserId(userId));

        return stats;
    }
}