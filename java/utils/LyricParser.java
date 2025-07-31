package utils;

import entity.LyricEntity;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LyricParser {

    public static List<LyricEntity> srtParseByFile(Path filePath) throws IOException {
        List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
        List<LyricEntity> subtitles = getLyricEntitiesSrt(lines);

        return subtitles;
    }

    public static List<LyricEntity> srtParseByContent(String content) throws IOException {
        List<String> lines = Arrays.stream(content.split("\\n")).toList();
        List<LyricEntity> subtitles = getLyricEntitiesSrt(lines);

        return subtitles;
    }

    private static List<LyricEntity> getLyricEntitiesSrt(List<String> lines) {
        List<LyricEntity> subtitles = new ArrayList<>();
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
    public static List<LyricEntity> lrcParseByFile(Path filePath) throws IOException {
        List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
        List<LyricEntity> lyrics = getLyricEntitiesLrc(lines);
        return lyrics;
    }

    /**
     * @param content lrc内容
     * @return 对象集合
     */
    public static List<LyricEntity> lrcParseByContent(String content) throws IOException {
        List<String> lines = Arrays.stream(content.split("\\n")).toList();
        List<LyricEntity> lyrics = getLyricEntitiesLrc(lines);
        return lyrics;
    }

    private static List<LyricEntity> getLyricEntitiesLrc(List<String> lines) {
        List<LyricEntity> lyrics = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\[(\\d+):(\\d+)([.:])(\\d+)]");

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

    /**
     * @param filePath 文件路径
     * @return 对象集合
     */
    public static List<LyricEntity> qrcXmlParseByFile(Path filePath) throws IOException, SAXException, ParserConfigurationException {

        String xml = Files.readString(filePath, StandardCharsets.UTF_8);
        List<LyricEntity> lyrics = getLyricEntitiesQrcXml(xml);
        return lyrics;
    }

    /**
     * @param xml xml内容
     * @return 对象集合
     */
    public static List<LyricEntity> qrcXmlParseByContent(String xml) throws IOException, SAXException, ParserConfigurationException {

        List<LyricEntity> lyrics = getLyricEntitiesQrcXml(xml);
        return lyrics;
    }

    private static List<LyricEntity> getLyricEntitiesQrcXml(String xml) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        List<LyricEntity> lyrics = new ArrayList<>();

        // 提取歌词内容
        NodeList lyricNodes = doc.getElementsByTagName("Lyric_1");
        if (lyricNodes.getLength() > 0) {
            Element lyric = (Element) lyricNodes.item(0);
            String content = lyric.getAttributeNode("LyricContent").getValue();
            parseLyricContent(content, lyrics);
        }
        lyrics.sort(Comparator.comparingLong(LyricEntity::getStartTimeMs));
        return lyrics;
    }

    // 解析歌词内容
    private static void parseLyricContent(String content,
                                          List<LyricEntity> lyrics) {
        String[] parts = content.replaceAll("\\[", "\n[").split("\\n");
        Pattern metadataPattern = Pattern.compile("\\[(\\w+):([^\\]]*)]");
        Pattern linePattern = Pattern.compile("\\[(\\d+),(\\d+)](.*)");

        int index = 1;
        for (String part : parts) {
            // 解析元数据
            Matcher metaMatcher = metadataPattern.matcher(part);
            if (metaMatcher.find()) {
                //metadata.put(metaMatcher.group(1), metaMatcher.group(2));
                continue;
            }

            // 解析歌词行
            Matcher lineMatcher = linePattern.matcher(part);
            if (lineMatcher.find()) {
                long start = Long.parseLong(lineMatcher.group(1));
                long duration = Long.parseLong(lineMatcher.group(2));
                String text = lineMatcher.group(3).replaceAll("\\([^)]*\\)", "");
                lyrics.add(new LyricEntity(index++, start, start + duration, text));
            }
        }
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
