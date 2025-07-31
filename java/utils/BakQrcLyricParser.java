package utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BakQrcLyricParser {

    public static void main(String[] args) throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<QrcInfos>\n" +
                "<QrcHeadInfo SaveTime=\"258\" Version=\"100\"/>\n" +
                "<LyricInfo LyricCount=\"1\">\n" +
                "<Lyric_1 LyricType=\"1\" LyricContent=\"[ti:ハルヒカゲ フロム ザ ファースト テイク]\n" +
                "[ar:クライシック]\n" +
                "[al:ハルヒカゲ フロム ザ ファースト テイク]\n" +
                "[by:]\n" +
                "[offset:0]\n" +
                "[kana:1はる1ひ1か(504,104)げ(608,80)1し1お1だ1き(3024,84)ょ(3108,84)く(3192,136)1ふ(3328,120)じ(3448,200)1た1じゅん1へ(3848,64)い(3912,206)1か(15302,152)じ(15454,168)か(15622,152)1こ(16198,327)こ(16525,352)ろ(16877,329)1ま(18247,152)な(18399,320)1ざ1せ1か(19879,216)い(20095,177)1ぼ(22042,349)く(22391,328)1ち1し1は(29570,416)る(29986,560)1ま(31941,376)い(32317,330)1と(32647,480)し(33127,160)1つ(33287,344)め(33631,312)1く(37459,232)ら(37691,160)1な(38803,345)か(39148,303)1いっ1ぽう1つう1こう1こ(44337,312)と(44649,344)1ば1か1な(46929,312)ぐ(47241,212)1き1た(48481,304)い(48785,263)1わ1すく1もと1つ(57337,151)づ(57488,384)1い(63548,272)ま(63820,296)1わ1き1ひ1な1ぼ(73217,689)く(73906,215)1ひ(74685,392)か(75077,656)り(75733,360)1つ1だ1くも1ま1こ(85820,368)こ(86188,160)ろ(86348,816)1み1あ(88644,185)ふ(88829,231)1ほお1あ(93387,144)つ(93531,336)1あ(94347,168)つ(94515,352)1ぬ1き(97015,160)み(97175,240)1て1あ(102527,352)た(102879,151)た(103030,392)1ね(105287,336)が(105623,256)1は(110385,264)な(110649,232)1えん1む(116172,232)す(116404,384)1だ(119444,536)れ(119980,176)1よ(123308,656)ろ(123964,312)こ(124276,600)1か(125049,160)な(125209,216)1あい1か(128756,520)ぞ(129276,200)1こ1どう1た(133463,376)し(133839,232)1い(141478,288)ま(141766,352)1わ1き1ひ1な1ぼ(151054,640)く(151694,280)1ひ(152559,344)か(152903,688)り(153591,416)1だ1て1せ1か(176205,176)い(176381,344)1さ1ほ(177261,151)こ(177412,185)1た(177885,160)い(178045,328)1せ(178373,248)つ(178621,344)1ひ(179101,272)と(179373,1600)1し1は(184405,216)る(184621,168)1ぼく1き(186061,376)み(186437,136)1な(187965,480)み(188445,200)だ(188645,176)1なが1ま(191780,176)ぶ(191956,272)1う(195624,176)つ(195800,273)く(196073,399)1くも1ま1こ(215654,192)こ(215846,192)ろ(216038,792)1み1あ(218342,328)ふ(218670,152)1ほお1あ(223177,200)つ(223377,304)1あ(224041,168)つ(224209,400)1ぬ1き(226732,160)み(226892,312)1て1あ(232396,200)た(232596,168)た(232764,360)1ね(235080,360)が(235440,184)1は(239688,480)な(240168,448)1は(247611,464)な(248075,200)]\n" +
                "[201,2215]春(201,223)日(424,80)影(504,184) - (688,80)From (768,304)THE (1072,272)FIRST (1344,216)TAKE - (1560,304)CRYCHIC(1864,552)\n" +
                "[2416,608]词(2416,144)：(2560,0)織(2560,120)田(2680,28)あ(2708,28)す(2736,136)か(2872,152)\n" +
                "[3024,1094]曲(3024,304)：(3328,0)藤(3328,320)田(3648,128)淳(3776,72)平(3848,270)\n" +
                "[15302,5641]悴(15302,472)ん(15774,152)だ(15926,272)心(16198,1008) (17206,0)ふ(17206,312)る(17518,368)え(17886,185)る(18071,176)眼(18247,472)差(18719,321)し(19040,191)世(19231,648)界(19879,393)で(20272,671)\n" +
                "\"/>\n" +
                "</LyricInfo>\n" +
                "</QrcInfos>";

        QrcLyrics parsed = parseQrcLyrics(xml);
        System.out.println("--- LRC Format ---");
        System.out.println(convertToLRC(parsed));
        System.out.println("\n--- SRT Format ---");
        System.out.println(convertToSRT(parsed));
    }

    // 解析后的数据结构
    record QrcLyrics(
            Map<String, String> metadata,
            List<LyricLine> lines
    ) {}

    record LyricLine(
            long startTime,  // 开始时间(ms)
            long duration,   // 持续时间(ms)
            String text      // 歌词文本
    ) {}

    // 解析XML文档
    public static QrcLyrics parseQrcLyrics(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        
        Map<String, String> metadata = new HashMap<>();
        List<LyricLine> lines = new ArrayList<>();

        // 提取歌词内容
        NodeList lyricNodes = doc.getElementsByTagName("Lyric_1");
        if (lyricNodes.getLength() > 0) {
            Element lyric = (Element) lyricNodes.item(0);
            String content = lyric.getAttributeNode("LyricContent").getValue();
            parseLyricContent(content, metadata, lines);
        }
        
        return new QrcLyrics(metadata, lines);
    }

    // 解析歌词内容
    private static void parseLyricContent(String content, 
                                         Map<String, String> metadata,
                                         List<LyricLine> lines) {
        String[] parts = content.replaceAll("\\[", "\n[").split("\\n");
        Pattern metadataPattern = Pattern.compile("\\[(\\w+):([^\\]]*)]");
        Pattern linePattern = Pattern.compile("\\[(\\d+),(\\d+)](.*)");
        
        for (String part : parts) {
            // 解析元数据
            Matcher metaMatcher = metadataPattern.matcher(part);
            if (metaMatcher.find()) {
                metadata.put(metaMatcher.group(1), metaMatcher.group(2));
                continue;
            }
            
            // 解析歌词行
            Matcher lineMatcher = linePattern.matcher(part);
            if (lineMatcher.find()) {
                long start = Long.parseLong(lineMatcher.group(1));
                long duration = Long.parseLong(lineMatcher.group(2));
                String text = lineMatcher.group(3).replaceAll("\\([^)]*\\)", "");
                lines.add(new LyricLine(start, duration, text));
            }
        }
    }

    // 转换为LRC格式
    public static String convertToLRC(QrcLyrics lyrics) {
        StringBuilder sb = new StringBuilder();
        
        // 添加元数据
        lyrics.metadata().forEach((k, v) -> 
            sb.append("[").append(k).append(":").append(v).append("]\n"));
        
        // 添加歌词行
        for (LyricLine line : lyrics.lines()) {
            Duration d = Duration.ofMillis(line.startTime());
            String time = String.format("[%02d:%02d.%02d]", 
                    d.toMinutesPart(), 
                    d.toSecondsPart(),
                    d.toMillisPart() / 10);
            sb.append(time).append(line.text()).append("\n");
        }
        
        return sb.toString();
    }

    // 转换为SRT格式
    public static String convertToSRT(QrcLyrics lyrics) {
        StringBuilder sb = new StringBuilder();
        int index = 1;
        
        for (LyricLine line : lyrics.lines()) {
            // 序号
            sb.append(index++).append("\n");
            
            // 时间轴
            Duration start = Duration.ofMillis(line.startTime());
            Duration end = Duration.ofMillis(line.startTime() + line.duration());
            
            String startTime = formatSrtTime(start);
            String endTime = formatSrtTime(end);
            sb.append(startTime).append(" --> ").append(endTime).append("\n");
            
            // 歌词内容
            sb.append(line.text()).append("\n\n");
        }
        
        return sb.toString();
    }
    
    private static String formatSrtTime(Duration d) {
        return String.format("%02d:%02d:%02d,%03d",
                d.toHoursPart(),
                d.toMinutesPart(),
                d.toSecondsPart(),
                d.toMillisPart());
    }
}
