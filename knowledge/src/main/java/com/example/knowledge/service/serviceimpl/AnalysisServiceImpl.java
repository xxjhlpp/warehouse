package com.example.knowledge.service.serviceimpl;

import com.example.knowledge.entity.Note;
import com.example.knowledge.entity.NoteTag;
import com.example.knowledge.mapper.*;
import com.example.knowledge.service.AnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalysisServiceImpl implements AnalysisService {

    @Autowired
    private NoteMapper noteMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private InspirationMapper inspirationMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private NoteTagMapper noteTagMapper;

    @Override
    public Map<String, Object> getAssetSummary(Long userId) {
        Map<String, Object> summary = new HashMap<>();

        // 获取各类统计数据
        summary.put("notesCount", noteMapper.countByUserId(userId));
        summary.put("tagsCount", tagMapper.countByUserId(userId));
        summary.put("categoriesCount", categoryMapper.countByUserId(userId));

        return summary;
    }

    @Override
    public Map<String, Object> getGrowthData(Long userId, String range) {
        List<Map<String, Object>> growthData = new ArrayList<>();
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime startDate = null;
        String dateFormat = "";

        // 根据时间范围计算起始日期和日期格式化方式
        switch (range) {
            case "week":
                // 最近7天，格式为MM-dd
                startDate = today.minusDays(6);
                dateFormat = "%m-%d";
                break;
            case "month":
                // 最近6个月，格式为MM月
                startDate = today.minusMonths(5);
                dateFormat = "%c月";
                break;
            case "year":
                // 全年，格式为MM月
                startDate = today.withMonth(1).withDayOfMonth(1);
                dateFormat = "%c月";
                break;
            default:
                throw new IllegalArgumentException("无效的时间范围: " + range);
        }

        // 查询数据库获取真实数据
        List<Map<String, Object>> dbData = noteMapper.countByDateRange(
                userId, startDate, today, dateFormat);

        // 处理查询结果，确保所有日期都有数据（包括没有创建笔记的日期）
        growthData = fillMissingDates(dbData, range, startDate, today);

        return Collections.singletonMap("growthData", growthData);
    }

    // 填充缺失的日期，确保时间序列完整
    private List<Map<String, Object>> fillMissingDates(
            List<Map<String, Object>> dbData,
            String range,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        List<Map<String, Object>> fullData = new ArrayList<>();
        Map<String, Long> dateCountMap = new HashMap<>();

        // 将数据库数据转换为Map便于查询
        for (Map<String, Object> item : dbData) {
            String date = (String) item.get("date");
            Long count = ((Number) item.get("count")).longValue();
            dateCountMap.put(date, count);
        }

        // 生成完整的日期序列
        LocalDateTime current = startDate;
        DateTimeFormatter formatter;

        switch (range) {
            case "week":
                formatter = DateTimeFormatter.ofPattern("MM-dd");
                while (!current.isAfter(endDate)) {
                    String dateStr = current.format(formatter);
                    Map<String, Object> item = new HashMap<>();
                    item.put("date", dateStr);
                    item.put("count", dateCountMap.getOrDefault(dateStr, 0L));
                    fullData.add(item);
                    current = current.plusDays(1);
                }
                break;
            case "month":
            case "year":
                formatter = DateTimeFormatter.ofPattern("M月");
                while (!current.isAfter(endDate)) {
                    String dateStr = current.format(formatter);
                    // 每月只添加一次
                    if (fullData.isEmpty() || !fullData.get(fullData.size()-1).get("date").equals(dateStr)) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("date", dateStr);
                        item.put("count", dateCountMap.getOrDefault(dateStr, 0L));
                        fullData.add(item);
                    }
                    current = current.plusMonths(1);
                }
                break;
        }

        return fullData;
    }

    @Override
    public Map<String, Object> getBehaviorData(Long userId) {
        Map<String, Object> behaviorData = new HashMap<>();

        // 获取高频标签TOP5
        List<Map<String, Object>> topTags = getTopTags(userId, 5);
        behaviorData.put("topTags", topTags);

        // 近期录入量统计（真实数据）
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        Long recentNotes = noteMapper.countByUserIdAndDateRange(userId, thirtyDaysAgo, LocalDateTime.now());
        behaviorData.put("recentNotes", recentNotes);

        // 灵感便签统计（新增部分）
        Long recentInspirations = inspirationMapper.countByUserIdAndDateRange(userId, thirtyDaysAgo, LocalDateTime.now());
        behaviorData.put("recentInspirations", recentInspirations);

        // 计算趋势（与上一个30天比较）
        LocalDateTime sixtyDaysAgo = LocalDateTime.now().minusDays(60);
        Long previousNotes = noteMapper.countByUserIdAndDateRange(userId, sixtyDaysAgo, thirtyDaysAgo);
        int notesTrend = calculateTrend(recentNotes, previousNotes);
        behaviorData.put("notesTrend", notesTrend);
        behaviorData.put("inspirationsTrend", 0); // 暂时设为0

        // 计算便签趋势（新增部分）
        Long previousInspirations = inspirationMapper.countByUserIdAndDateRange(userId, sixtyDaysAgo, thirtyDaysAgo);
        int inspirationsTrend = calculateTrend(recentInspirations, previousInspirations);
        behaviorData.put("inspirationsTrend", inspirationsTrend);

        // 沉睡笔记提醒（真实数据）
        behaviorData.put("sleepingNotes", getSleepingNotes(userId));

        return behaviorData;
    }

    // 计算趋势百分比
    private int calculateTrend(Long current, Long previous) {
        if (previous == 0) {
            return current > 0 ? 100 : 0;
        }
        return (int) ((current - previous) * 100 / previous);
    }


    /**
     * 获取用户高频标签TOP N
     */
// 修改后
    private List<Map<String, Object>> getTopTags(Long userId, int limit) {
        // 查询用户所有标签
        List<Map<String, Object>> tags = tagMapper.selectTagsWithNoteCount(userId);

        // 按笔记数量排序并取前N - 使用Long处理
        List<Map<String, Object>> sortedTags = tags.stream()
                .sorted((t1, t2) -> {
                    Long c1 = (Long) t1.get("count");
                    Long c2 = (Long) t2.get("count");
                    return Long.compare(c2, c1); // 使用Long的比较方法
                })
                .limit(limit)
                .collect(Collectors.toList());

        // 计算百分比（相对最大值）
        if (!sortedTags.isEmpty()) {
            Long maxCount = (Long) sortedTags.get(0).get("count");
            sortedTags.forEach(tag -> {
                Long count = (Long) tag.get("count");
                // 先转为double计算，再转为int
                tag.put("percentage", maxCount > 0 ? (int) (((double) count / maxCount) * 100) : 0);
            });
        }

        return sortedTags;
    }

    /**
     * 获取沉睡笔记列表（真实数据）
     */
    private List<Map<String, Object>> getSleepingNotes(Long userId) {
        // 定义沉睡笔记的时间阈值（例如：30天未访问）
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);

        // 查询用户超过30天未访问的笔记
        List<Map<String, Object>> sleepingNotes = noteMapper.findSleepingNotes(userId, threshold);

        // 计算沉睡天数并补充到结果中
        sleepingNotes.forEach(note -> {
            LocalDateTime lastAccessed = (LocalDateTime) note.get("lastAccessed");
            long sleepingDays = ChronoUnit.DAYS.between(lastAccessed, LocalDateTime.now());
            note.put("sleepingDays", sleepingDays);
            // 格式化日期显示
            note.put("lastAccessed", lastAccessed.toLocalDate().toString());
        });

        return sleepingNotes;
    }
}