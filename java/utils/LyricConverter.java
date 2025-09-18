package utils;

import entity.LyricEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class LyricConverter {

    /**
     * 将歌词列表转换为 SRT 格式字符串
     * @param lyrics 歌词列表
     * @return SRT 格式字符串
     */
    public static String convertToSrt(List<LyricEntity> lyrics) {
        // 按开始时间排序
        List<LyricEntity> sortedLyrics = new ArrayList<>(lyrics);
        sortedLyrics.sort(Comparator.comparingLong(LyricEntity::getStartTimeMs));
        StringBuilder srt = new StringBuilder();
        int index = 1;
        for (int i = 0; i < sortedLyrics.size(); i++) {
            LyricEntity current = sortedLyrics.get(i);
            Long endTimeMs = current.getEndTimeMs();

            // 如果结束时间为空，则使用下一行的开始时间减去100ms（若非最后一行）
            if (endTimeMs == null) {
                if (i < sortedLyrics.size() - 1) {
                    endTimeMs = sortedLyrics.get(i + 1).getStartTimeMs() - 100;
                } else {
                    // 最后一行处理：若无结束时间，则设置为开始时间 + 默认持续时间（例如5秒）
                    endTimeMs = current.getStartTimeMs() + 5000; // 默认5秒
                }
            }
            srt.append(index++)
                    .append("\n")
                    .append(CommonUtils.formatSrtTime(current.getStartTimeMs()))
                    .append(" --> ")
                    .append(CommonUtils.formatSrtTime(endTimeMs))
                    .append("\n")
                    .append(current.getText())
                    .append("\n\n");
        }

        return srt.toString().trim();
    }

    /**
     * 将歌词列表转换为 LRC 格式字符串
     * @param lyrics 歌词列表
     * @return LRC 格式字符串
     */
    public static String convertToLrc(List<LyricEntity> lyrics) {
        // 按开始时间排序
        List<LyricEntity> sortedLyrics = new ArrayList<>(lyrics);

        StringBuilder lrc = new StringBuilder();

        for (LyricEntity lyric : sortedLyrics) {
            // 对于单个时间段的多行字幕使用相同的时间轴+文本行
            for (String lyricLine : lyric.getText().split("\n")) {
                lrc.append("[")
                    .append(CommonUtils.formatLrcTime(lyric.getStartTimeMs()))
                    .append("]")
                    .append(lyricLine)
                    .append("\n");
            }

        }

        return lrc.toString().trim();
    }
}