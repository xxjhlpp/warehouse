package com.example.knowledge.controller;

import com.example.knowledge.service.AnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/analysis")
public class AnalysisController {

    @Autowired
    private AnalysisService analysisService;

    /**
     * 获取资产概况数据
     */
    @GetMapping("/summary")
    public Map<String, Object> getAssetSummary(@RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> data = analysisService.getAssetSummary(userId);
            result.put("code", 200);
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "获取资产概况失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取增长趋势数据
     */
    @GetMapping("/growth")
    public Map<String, Object> getGrowthData(
            @RequestParam Long userId,
            @RequestParam String range) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 验证时间范围参数
            if (!"week".equals(range) && !"month".equals(range) && !"year".equals(range)) {
                result.put("code", 400);
                result.put("msg", "无效的时间范围参数");
                return result;
            }

            Map<String, Object> data = analysisService.getGrowthData(userId, range);
            result.put("code", 200);
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "获取增长数据失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取行为分析数据
     */
    @GetMapping("/behavior")
    public Map<String, Object> getBehaviorData(@RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> data = analysisService.getBehaviorData(userId);
            result.put("code", 200);
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "获取行为分析数据失败: " + e.getMessage());
        }
        return result;
    }
}