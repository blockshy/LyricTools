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

/**
 * 读取Lrc字幕文件为对象集合
 */
public class LrcParser {

    /**
     * @param filePath 文件路径
     * @return 对象集合
     */
    public static List<LyricEntity> parse(Path filePath) throws IOException {
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
}