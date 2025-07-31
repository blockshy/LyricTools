package demo;

import utils.LyricConverter;
import entity.LyricEntity;
import utils.LyricMerge;

import java.util.*;

/**
 * 歌词合并主程序
 */
public class LyricMergeDemo {

    // 合并歌词输出路径
    public static final String OUTPUT_PATH = "./resources/result/";

    // 设置歌词合并顺序
    // 合并顺序为数组中的先后顺序，可自行修改，传入路径需要为有效路径，如果为无效路径会取消该条目的合并
    public static final String[] LYRIC_ORDER_LIST = {
            "D:\\剪辑\\素材\\歌曲字幕\\最后的修行.lrc",
            "D:\\剪辑\\素材\\歌曲字幕\\最后的修行-JP.lrc"
    };

    //设置偏差值 由于存在字幕文件不同步的情况，所以设置一个偏差值，当时间轴相差在这个值内时，认为是同一句歌词
    // 100L 表示 100 毫秒 偏差值为 T+100ms 和 T-100ms
    public static final long TIME_DIFFERENCE = 100L;

    public static void main(String[] args) {

        // 合并歌词
        List<LyricEntity> mergedLyric = LyricMerge.mergeLyricByFileList(LYRIC_ORDER_LIST, TIME_DIFFERENCE);

        // 转换为 SRT 和 LRC 格式
        String srtContent = LyricConverter.convertToSrt(mergedLyric);
        String lrcContent = LyricConverter.convertToLrc(mergedLyric);
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
