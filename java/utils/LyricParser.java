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
import java.nio.file.Paths;
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
            long startTime = CommonUtils.parseTime(times[0].trim());
            long endTime = CommonUtils.parseTime(times[1].trim());

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
                int millis = CommonUtils.parseMillis(matcher.group(4), matcher.group(3));

                long timeMs = (minutes * 60L + seconds) * 1000 + millis;
                lyrics.add(new LyricEntity(null, timeMs, null, text));
            }
        }

        lyrics.sort(Comparator.comparingLong(LyricEntity::getStartTimeMs));
        return lyrics;
    }

    /**
     * qrc,qrcRoma
     * @param filePath 文件路径
     * @return 对象集合
     */
    public static List<LyricEntity> qrcXmlParseByFile(Path filePath) throws IOException, SAXException, ParserConfigurationException {

        String xml = Files.readString(filePath, StandardCharsets.UTF_8);
        List<LyricEntity> lyrics = getLyricEntitiesQrcXml(xml);
        return lyrics;
    }

    /**
     * qrc,qrcRoma
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
            parseQrcLyricContent(content, lyrics);
        }
        lyrics.sort(Comparator.comparingLong(LyricEntity::getStartTimeMs));
        return lyrics;
    }

    // 解析歌词内容
    private static void parseQrcLyricContent(String content,
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

    /**
     * qrcts
     * @param xml xml内容
     * @return 对象集合
     */
    public static List<LyricEntity> qrcTsXmlParseByContent(String xml) throws IOException, SAXException, ParserConfigurationException {

        List<LyricEntity> lyrics = getLyricEntitiesQrcTsXml(xml);
        return lyrics;
    }

    private static List<LyricEntity> getLyricEntitiesQrcTsXml(String xml) {
        List<String> lines = Arrays.stream(xml.split("\\n")).toList();
        List<LyricEntity> lyrics = new ArrayList<>();
        Pattern pattern = Pattern.compile("^\\[(\\d{2}):(\\d{2})\\.(\\d{2})\\](.+)$");

        // 第一遍：提取所有有效歌词行
        int index = 1;
        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                int minutes = Integer.parseInt(matcher.group(1));
                int seconds = Integer.parseInt(matcher.group(2));
                int hundredths = Integer.parseInt(matcher.group(3));
                long startTimeMs = (minutes * 60L + seconds) * 1000 + hundredths * 10L;
                String text = matcher.group(4).trim();

                LyricEntity entity = new LyricEntity(index++, startTimeMs, startTimeMs + 1000, text);
                entity.setStartTimeMs(startTimeMs);
                entity.setText(text);
                lyrics.add(entity);
            }
        }

        lyrics.sort(Comparator.comparingLong(LyricEntity::getStartTimeMs));
        return lyrics;
    }

    /**
     * 解析歌词文件
     * @param filePath 文件路径
     * @return 歌词列表
     * @throws IOException 文件读取异常
     */
    private static List<LyricEntity> parseLyricFile(String filePath, String type) throws IOException, ParserConfigurationException, SAXException {
        Path path;
        path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return null;
        }

        return switch (type) {
            case "srt" -> lrcParseByFile(path);
            case "lrc" -> srtParseByFile(path);
            case "qrc" -> qrcXmlParseByContent(QrcLyricDecrypter.decryptByQrcFile(filePath));
            case "qrcRoma" -> qrcXmlParseByContent(QrcLyricDecrypter.decryptByQrcFile(filePath));
            case "qrcTs" -> qrcTsXmlParseByContent(QrcLyricDecrypter.decryptByQrcFile(filePath));
            default -> null;
        };
    }

    /**
     * 解析歌词文件
     * @param content 歌词内容
     * @param type 歌词类型（lrc, srt, qrc, qrcRoma, qrcTs）
     * @return 歌词列表
     * @throws IOException 文件读取异常
     */
    public static List<LyricEntity> parseLyricContent(String content, String type) throws IOException, ParserConfigurationException, SAXException {

        return switch (type) {
            case "srt" -> lrcParseByContent(content);
            case "lrc" -> srtParseByContent(content);
            case "qrc" -> qrcXmlParseByContent(content);
            case "qrcRoma" -> qrcXmlParseByContent(content);
            case "qrcTs" -> qrcTsXmlParseByContent(content);
            default -> null;
        };
    }

    /**
     * 解析歌词文件列表
     * @param fileListMap 文件路径列表
     * @return 歌词列表
     */
    public static List<List<LyricEntity>> parseLyricFileList(Map<String, String> fileListMap) {
        List<List<LyricEntity>> lyrics = new ArrayList<>();
        for (Map.Entry<String, String> stringStringEntry : fileListMap.entrySet()) {
            String filePath = stringStringEntry.getKey();
            String type = stringStringEntry.getValue();
            if (filePath == null || filePath.isEmpty()) {
                continue; // 跳过空路径
            }
            try {
                List<LyricEntity> lyricEntities = parseLyricFile(filePath, type);
                if (lyricEntities != null && !lyricEntities.isEmpty()) {
                    lyrics.add(lyricEntities);
                }
            } catch (IOException | ParserConfigurationException | SAXException e) {
                System.err.println("Error parsing file: " + filePath + " - " + e.getMessage());
            }
        }
        return lyrics;
    }


}
