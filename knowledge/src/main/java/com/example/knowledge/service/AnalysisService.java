package com.example.knowledge.service;

import java.util.Map;

public interface AnalysisService {

    /**
     * 获取资产概况数据
     */
    Map<String, Object> getAssetSummary(Long userId);

    /**
     * 获取增长趋势数据
     */
    Map<String, Object> getGrowthData(Long userId, String range);

    /**
     * 获取行为分析数据
     */
    Map<String, Object> getBehaviorData(Long userId);
}