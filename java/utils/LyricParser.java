package utils;

import entity.LyricEntity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LyricParser {

    public static List<LyricEntity> srtParse(Path filePath) throws IOException {
        List<LyricEntity> subtitles = new ArrayList<>();
        List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
        int index = 0;

        while (index < lines.size()) {
            String line = lines.get(index++).trim();
            if (line.isEmpty()) continue;

            // 解析序号
            int number = Integer.parseInt(line);

            // 解析时间轴
            String timeLine = lines.get(index++).trim();
            String[] times = timeLine.split("-->");
            long startTime = parseTime(times[0].trim());
            long endTime = parseTime(times[1].trim());

            // 收集文本
            StringBuilder text = new StringBuilder();
            while (index < lines.size() && !(line = lines.get(index).trim()).isEmpty()) {
                text.append(line).append("\n");
                index++;
            }

            subtitles.add(new LyricEntity(number, startTime, endTime, text.toString().trim()));
        }

        return subtitles;
    }

    /**
     * @param filePath 文件路径
     * @return 对象集合
     */
    public static List<LyricEntity> lrcParse(Path filePath) throws IOException {
        List<LyricEntity> lyrics = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\[(\\d+):(\\d+)([.:])(\\d+)]");
        List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);

        for (String line : lines) {
            line = line.trim();
            Matcher matcher = pattern.matcher(line);
            String text = line.replaceAll("\\[\\d+[:.]\\d+[:.]\\d+]", "").trim();

            while (matcher.find()) {
                int minutes = Integer.parseInt(matcher.group(1));
                int seconds = Integer.parseInt(matcher.group(2));
                int millis = parseMillis(matcher.group(4), matcher.group(3));

                long timeMs = (minutes * 60L + seconds) * 1000 + millis;
                lyrics.add(new LyricEntity(null, timeMs, null, text));
            }
        }

        lyrics.sort(Comparator.comparingLong(LyricEntity::getStartTimeMs));
        return lyrics;
    }

    private static int parseMillis(String millisStr, String separator) {
        int length = millisStr.length();
        if (length == 2 && separator.equals(".")) {
            return Integer.parseInt(millisStr) * 10; // 百分秒转毫秒
        }
        return Integer.parseInt(millisStr);
    }

    private static long parseTime(String timeStr) {
        String normalized = timeStr.replace('.', ',');
        String[] parts = normalized.split("[,:]");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);
        int millis = Integer.parseInt(parts[3]);
        return (hours * 3600L + minutes * 60L + seconds) * 1000 + millis;
    }
}
