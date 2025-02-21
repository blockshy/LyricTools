package utils;

import entity.LyricEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LyricMerge {

    /**
     * 合并多个歌词列表
     * @param lyrics 歌词列表
     * @param timeDifference 时间差范围（毫秒）
     * @return 合并后的歌词列表
     */
    public static List<LyricEntity> mergeLyric(List<List<LyricEntity>> lyrics, long timeDifference) {
        List<LyricEntity> mergedLyric = new ArrayList<>();
        int[] pointers = new int[lyrics.size()];
        Arrays.fill(pointers, 0);

        while (true) {
            Long minTime = null;
            // 查找当前所有指针中的最小时间
            for (int i = 0; i < lyrics.size(); i++) {
                List<LyricEntity> sublist = lyrics.get(i);
                int ptr = pointers[i];
                if (ptr < sublist.size()) {
                    long currentTime = sublist.get(ptr).getStartTimeMs();
                    if (minTime == null || currentTime < minTime) {
                        minTime = currentTime;
                    }
                }
            }
            if (minTime == null) break; // 所有指针越界，结束循环

            long baseTime = minTime;
            List<LyricEntry> entries = new ArrayList<>();

            // 收集所有时间差在偏差值内的条目，并移动指针
            for (int i = 0; i < lyrics.size(); i++) {
                List<LyricEntity> sublist = lyrics.get(i);
                int ptr = pointers[i];
                if (ptr < sublist.size()) {
                    LyricEntity entity = sublist.get(ptr);
                    if (Math.abs(entity.getStartTimeMs() - baseTime) <= timeDifference) {
                        entries.add(new LyricEntry(i, entity));
                        pointers[i]++;
                    }
                }
            }

            // 按子列表顺序合并文本并计算时间范围
            StringBuilder mergedText = new StringBuilder();
            long mergedStart = Long.MAX_VALUE;
            long mergedEnd = Long.MIN_VALUE;
            for (int i = 0; i < lyrics.size(); i++) {
                for (LyricEntry entry : entries) {
                    if (entry.sublistIndex == i) {
                        if (!mergedText.isEmpty()) {
                            mergedText.append("\n");
                        }
                        mergedText.append(entry.entity.getText());
                        mergedStart = Math.min(mergedStart, entry.entity.getStartTimeMs());
                        mergedEnd = Math.max(mergedEnd, entry.entity.getEndTimeMs());
                        break;
                    }
                }
            }

            if (!mergedText.isEmpty()) {
                mergedLyric.add(new LyricEntity(null, mergedStart, mergedEnd, mergedText.toString()));
            }
        }

        // 设置编号
        int index = 1;
        for (LyricEntity entity : mergedLyric) {
            entity.setNumber(index++);
        }

        return mergedLyric;
    }

    // 辅助类，记录子列表索引和对应的歌词实体
    private static class LyricEntry {
        int sublistIndex;
        LyricEntity entity;

        LyricEntry(int sublistIndex, LyricEntity entity) {
            this.sublistIndex = sublistIndex;
            this.entity = entity;
        }
    }

    /**
     * 校验两个startTimeMs之间的时间差是否在给定的范围内
     *
     * @param startTimeMs1 第一个startTimeMs
     * @param startTimeMs2 第二个startTimeMs
     * @param timeRangeMillis 时间差范围（毫秒）
     * @return 如果时间差在范围内，返回true，否则返回false
     */
    public static boolean isTimeDifferenceValid(Long startTimeMs1, Long startTimeMs2, long timeRangeMillis) {
        if (startTimeMs1 == null || startTimeMs2 == null) {
            throw new IllegalArgumentException("startTimeMs不能为null");
        }

        long timeDifference = Math.abs(startTimeMs1 - startTimeMs2);

        // 判断时间差是否在指定范围内
        return timeDifference <= timeRangeMillis;
    }
}

