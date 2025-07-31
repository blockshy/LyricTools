package demo;

import entity.LyricEntity;
import org.xml.sax.SAXException;
import utils.LyricConverter;
import utils.LyricMerge;
import utils.OpenLyricUtils;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 歌词合并主程序
 */
public class LyricMergeUseDemo {

    // 合并歌词输出路径
    public static final String OUTPUT_PATH = "./resources/result/";

    //key 为 QRC 文件路径，value 为对应的类型(lrc/srt/qrc/qrcRoma/qrcTs)
    // 合并顺序为数组中的先后顺序，可自行修改，传入路径需要为有效路径，如果为无效路径会取消该条目的合并
    // 需要注意qrc应为QQ音乐的歌词文件，不应对其进行预先修改
    public static final Map<String, String> LYRIC_QRC_ORDER_LIST = new HashMap<>(){{
            put("D:\\workspace\\Java\\LyricTools\\resources\\lyric\\qrc\\ClariS (クラリス) - irony (讽刺) - 259 - irony (反话)_qm.qrc", "qrc");
            put("D:\\workspace\\Java\\LyricTools\\resources\\lyric\\qrc\\ClariS (クラリス) - irony (讽刺) - 259 - irony (反话)_qmRoma.qrc", "qrcRoma");
            put("D:\\workspace\\Java\\LyricTools\\resources\\lyric\\qrc\\ClariS (クラリス) - irony (讽刺) - 259 - irony (反话)_qmts.qrc", "qrcTs");
    }};

    //设置偏差值 由于存在字幕文件不同步的情况，所以设置一个偏差值，当时间轴相差在这个值内时，认为是同一句歌词
    // 100L 表示 100 毫秒 偏差值为 T+100ms 和 T-100ms
    public static final long TIME_DIFFERENCE = 100L;

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {

        //文件列表转换
        List<LyricEntity> lyricEntities = OpenLyricUtils.mergeLyricByFileList(LYRIC_QRC_ORDER_LIST, TIME_DIFFERENCE);
        // 转换为 SRT 和 LRC 格式 输出
        String srtContent = LyricConverter.convertToSrt(lyricEntities);
        String lrcContent = LyricConverter.convertToLrc(lyricEntities);
        System.out.println("--- LRC Format ---");
        System.out.println(lrcContent);
        System.out.println("--- SRT Format ---");
        System.out.println(srtContent);

        /*//输出路径不存在则先创建
        Path outputPath = Paths.get(OUTPUT_PATH);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }
        // 写入文件
        Files.write(Paths.get(OUTPUT_PATH + "subtitles.srt"), srtContent.getBytes());
        Files.write(Paths.get(OUTPUT_PATH + "lyrics.lrc"), lrcContent.getBytes());*/
    }


}
