package utils;

import entity.LyricEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LyricConverter {

    public static String convertToSrt(List<LyricEntity> lyrics) {
        // 按开始时间排序
        List<LyricEntity> sortedLyrics = new ArrayList<>(lyrics);

        StringBuilder srt = new StringBuilder();
        int index = 1;

        for (LyricEntity lyric : sortedLyrics) {
            srt.append(index++)
               .append("\n")
               .append(formatSrtTime(lyric.getStartTimeMs()))
               .append(" --> ")
               .append(formatSrtTime(lyric.getEndTimeMs()))
               .append("\n")
               .append(lyric.getText())
               .append("\n\n");
        }

        return srt.toString().trim();
    }

    private static String formatSrtTime(Long milliseconds) {
        long hours = TimeUnit.MILLISECONDS.toHours(milliseconds);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60;
        long millis = milliseconds % 1000;

        return String.format("%02d:%02d:%02d,%03d", hours, minutes, seconds, millis);
    }

    public static String convertToLrc(List<LyricEntity> lyrics) {
        // 按开始时间排序
        List<LyricEntity> sortedLyrics = new ArrayList<>(lyrics);

        StringBuilder lrc = new StringBuilder();

        for (LyricEntity lyric : sortedLyrics) {
            //对于单个时间段的多行字幕使用相同的时间轴+文本行
            for (String lyricLine : lyric.getText().split("\n")) {
                lrc.append("[")
                    .append(formatLrcTime(lyric.getStartTimeMs()))
                    .append("]")
                    .append(lyricLine)
                    .append("\n");
            }

        }

        return lrc.toString().trim();
    }

    private static String formatLrcTime(Long milliseconds) {
        long totalSeconds = milliseconds / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        long hundredths = (milliseconds % 1000) / 10;

        return String.format("%02d:%02d.%02d", minutes, seconds, hundredths);
    }
}