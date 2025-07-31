package utils;

import entity.LyricEntity;
import java.util.*;

public class LyricMerger {

    /**
     * 合并多个歌词列表
     * @param timeDifference 时间差范围（毫秒）
     * @param lyrics 各个歌词列表
     * @return 合并后的歌词列表
     */
    public static List<LyricEntity> mergeLyricByEntity(long timeDifference, List<List<LyricEntity>> lyrics) {
        // 排除解析失败的列表
        lyrics.removeIf(list -> list == null || list.isEmpty());

        if (lyrics.isEmpty()) {
            return Collections.emptyList(); // 如果没有有效的歌词列表，返回空列表
        }

        // 初始化指针数组，每个子列表一个指针
        List<LyricEntity> mergedLyric = new ArrayList<>();
        int[] pointers = new int[lyrics.size()];
        Arrays.fill(pointers, 0);

        // 使用指针遍历所有子列表，找到时间最小的条目
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
                        mergedEnd = Math.max(mergedEnd, Objects.isNull(entry.entity.getEndTimeMs()) ? entry.entity.getStartTimeMs() : entry.entity.getEndTimeMs());
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
}

