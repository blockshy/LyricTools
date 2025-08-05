package utils;

import constant.SupportFileTypeEnum;
import entity.LyricEntity;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LyricParser {

    public static List<LyricEntity> srtParseByFile(String filePath) {

        return getLyricEntitiesSrt(CommonUtils.readFileToList(filePath));
    }

    public static List<LyricEntity> srtParseByContent(String content) {

        List<String> lines = Arrays.stream(content.split("\\n")).toList();

        return getLyricEntitiesSrt(lines);
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
    public static List<LyricEntity> lrcParseByFile(String filePath) {
        List<LyricEntity> lyrics = getLyricEntitiesLrc(CommonUtils.readFileToList(filePath));
        return lyrics;
    }

    /**
     * @param content lrc内容
     * @return 对象集合
     */
    public static List<LyricEntity> lrcParseByContent(String content) {
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
    public static List<LyricEntity> qrcXmlParseByFile(Path filePath) throws IOException {

        String xml = Files.readString(filePath, StandardCharsets.UTF_8);
        return qrcXmlParseByContent(xml);
    }

    /**
     * qrc,qrcRoma
     * @param xml xml内容
     * @return 对象集合
     */
    public static List<LyricEntity> qrcXmlParseByContent(String xml) {

        return getLyricEntitiesQrcXml(xml);
    }

    /**
     * 解析QRC XML歌词内容
     * @param xml XML内容
     * @return 对象集合
     */
    private static List<LyricEntity> getLyricEntitiesQrcXml(String xml) {

        List<LyricEntity> lyrics = new ArrayList<>();

        // 提取歌词内容
        NodeList lyricNodes = CommonUtils.parseXml(xml).getElementsByTagName("Lyric_1");
        if (lyricNodes.getLength() > 0) {
            Element lyric = (Element) lyricNodes.item(0);
            String content = lyric.getAttributeNode("LyricContent").getValue();
            parseQrcLyricContent(content, lyrics);
        }

        lyrics.sort(Comparator.comparingLong(LyricEntity::getStartTimeMs));
        return lyrics;
    }

    /**
     * 解析QRC歌词内容
     * @param content QRC歌词内容
     * @param lyrics 歌词列表
     */
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
    public static List<LyricEntity> qrcTsXmlParseByContent(String xml) {

        List<LyricEntity> lyrics = getLyricEntitiesQrcTsXml(xml);
        return lyrics;
    }

    /**
     * 解析QRC TS歌词内容
     * @param xml XML内容
     * @return 对象集合
     */
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
     */
    private static List<LyricEntity> parseLyricFile(String filePath, SupportFileTypeEnum type) {

        return switch (type) {
            case SupportFileTypeEnum.SRT -> lrcParseByFile(filePath);
            case SupportFileTypeEnum.LRC -> srtParseByFile(filePath);
            case SupportFileTypeEnum.QRC -> qrcXmlParseByContent(QrcLyricDecrypter.decryptByQrcFile(filePath));
            case SupportFileTypeEnum.QRC_ROMA -> qrcXmlParseByContent(QrcLyricDecrypter.decryptByQrcFile(filePath));
            case SupportFileTypeEnum.QRC_TS -> qrcTsXmlParseByContent(QrcLyricDecrypter.decryptByQrcFile(filePath));
            default -> null;
        };
    }

    /**
     * 解析歌词文件
     * @param content 歌词内容
     * @param type 歌词类型（SupportFileTypeEnum）
     * @return 歌词列表
     */
    public static List<LyricEntity> parseLyricContent(String content, SupportFileTypeEnum type) {

        return switch (type) {
            case SupportFileTypeEnum.SRT -> lrcParseByContent(content);
            case SupportFileTypeEnum.LRC -> srtParseByContent(content);
            case SupportFileTypeEnum.QRC -> qrcXmlParseByContent(content);
            case SupportFileTypeEnum.QRC_ROMA -> qrcXmlParseByContent(content);
            case SupportFileTypeEnum.QRC_TS -> qrcTsXmlParseByContent(content);
            default -> null;
        };
    }

    /**
     * 解析歌词文件列表
     * @param fileListMap 文件路径列表
     * @return 歌词列表
     */
    public static List<List<LyricEntity>> parseLyricFileList(Map<String, SupportFileTypeEnum> fileListMap) {
        List<List<LyricEntity>> lyrics = new ArrayList<>();
        for (Map.Entry<String, SupportFileTypeEnum> fileListMapEntry : fileListMap.entrySet()) {
            String filePath = fileListMapEntry.getKey();
            SupportFileTypeEnum type = fileListMapEntry.getValue();
            if (filePath == null || filePath.isEmpty()) {
                continue; // 跳过空路径
            }
            List<LyricEntity> lyricEntities = parseLyricFile(filePath, type);
            if (lyricEntities != null && !lyricEntities.isEmpty()) {
                lyrics.add(lyricEntities);
            }
        }
        return lyrics;
    }


}
