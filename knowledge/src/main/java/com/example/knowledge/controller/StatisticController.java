package com.example.knowledge.controller;

import com.example.knowledge.common.Result;
import com.example.knowledge.service.StatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/statistic")
public class StatisticController {

    @Autowired
    private StatisticService statisticService;

    @GetMapping("/user")
    public ResponseEntity<Result<Map<String, Integer>>> getUserStatistics(@RequestParam Long userId) {
        try {
            Map<String, Integer> stats = statisticService.getUserStatistics(userId);
            return ResponseEntity.ok(Result.success(stats));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Result.error("获取统计数据失败：" + e.getMessage()));
        }
    }
}