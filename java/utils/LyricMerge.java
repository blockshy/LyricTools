package utils;

import entity.LyricEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class LyricMerge {

    /**
     * 合并多个歌词列表
     * @param fileList 文件列表
     * @param timeDifference 时间差范围（毫秒）
     * @return 合并后的歌词列表
     */
    public static List<LyricEntity> mergeLyricByFileList(String[] fileList, long timeDifference) {

        // 检查路径有效性并解析
        List<List<LyricEntity>> lyrics = LyricMerge.parseLyricFileList(Arrays.asList(fileList));

        // 排除非法路径
        lyrics.removeIf(list -> list == null || list.isEmpty());

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

    /**
     * 解析歌词文件
     * @param filePath 文件路径
     * @return 歌词列表
     * @throws IOException 文件读取异常
     */
    private static List<LyricEntity> parseLyricFile(String filePath) throws IOException {
        Path path;
        path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return null;
        }

        // 根据文件扩展名解析文件
        return switch (getFileExtension(filePath)) {
            case "srt" -> LyricParser.srtParseByFile(path);
            case "lrc" -> LyricParser.lrcParseByFile(path);
            default -> null;
        };
    }

    /**
     * 解析歌词文件列表
     * @param filePaths 文件路径列表
     * @return 歌词列表
     */
    private static List<List<LyricEntity>> parseLyricFileList(List<String> filePaths) {
        List<List<LyricEntity>> lyrics = new ArrayList<>();
        filePaths.forEach(path -> {
            try {
                lyrics.add(parseLyricFile(path));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return lyrics;
    }

    /**
     * 获取文件扩展名
     * @param filePath 文件路径
     * @return 文件扩展名
     */
    private static String getFileExtension(String filePath) {
        int dotIndex = filePath.lastIndexOf('.');
        return (dotIndex == -1) ? "" : filePath.substring(dotIndex + 1).toLowerCase();
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

