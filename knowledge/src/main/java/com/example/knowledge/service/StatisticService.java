package com.example.knowledge.service;

import java.util.Map;

public interface StatisticService {
    // 获取用户的各类统计数据
    Map<String, Integer> getUserStatistics(Long userId);
}