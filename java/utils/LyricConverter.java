package utils;

import entity.LyricEntity;

import java.util.ArrayList;
import java.util.List;

public class LyricConverter {

    /**
     * 将歌词列表转换为 SRT 格式字符串
     * @param lyrics 歌词列表
     * @return SRT 格式字符串
     */
    public static String convertToSrt(List<LyricEntity> lyrics) {
        // 按开始时间排序
        List<LyricEntity> sortedLyrics = new ArrayList<>(lyrics);

        StringBuilder srt = new StringBuilder();
        int index = 1;

        for (LyricEntity lyric : sortedLyrics) {
            srt.append(index++)
               .append("\n")
               .append(CommonUtils.formatSrtTime(lyric.getStartTimeMs()))
               .append(" --> ")
               .append(CommonUtils.formatSrtTime(lyric.getEndTimeMs()))
               .append("\n")
               .append(lyric.getText())
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