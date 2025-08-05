package demo;

import constant.SupportFileTypeEnum;
import entity.LyricEntity;
import utils.CommonUtils;
import utils.LyricConverter;
import utils.OpenLyricUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LyricMergeUseDemo {

    // 合并歌词输出路径
    public static final String OUTPUT_PATH = "./resources/result/";

    // key 为 QRC 文件路径，value 为对应的类型(SupportFileTypeEnum)
    // 合并顺序为数组中的先后顺序，可自行修改，传入路径需要为有效路径，如果为无效路径会取消该条目的合并
    // 需要注意qrc相关类型应为QQ音乐的歌词文件，不预先有修改（否则可能导致解密失败）
    public static final Map<String, SupportFileTypeEnum> LYRIC_QRC_ORDER_LIST = new HashMap<>(){{
            put("D:\\workspace\\Java\\LyricTools\\resources\\lyric\\qrc\\奶油苏打和冕形灯\\神野メイ - クリームソーダとシャンデリア (feat_ねんね) - 221 - _qm.qrc", SupportFileTypeEnum.QRC);
            put("D:\\workspace\\Java\\LyricTools\\resources\\lyric\\qrc\\奶油苏打和冕形灯\\神野メイ - クリームソーダとシャンデリア (feat_ねんね) - 221 - _qmRoma.qrc", SupportFileTypeEnum.QRC_ROMA);
            put("D:\\workspace\\Java\\LyricTools\\resources\\lyric\\qrc\\奶油苏打和冕形灯\\神野メイ - クリームソーダとシャンデリア (feat_ねんね) - 221 - _qmts.qrc", SupportFileTypeEnum.QRC_TS);
    }};

    // 设置偏差值 由于存在字幕文件不同步的情况，所以设置一个偏差值，当时间轴相差在这个值内时，认为是同一句歌词
    // 100L 表示 100 毫秒 偏差值为 T+100ms 和 T-100ms
    public static final long TIME_DIFFERENCE = 100L;

    public static void main(String[] args) {

        // 文件列表转换
        List<LyricEntity> lyricEntities = OpenLyricUtils.mergeLyricByFileList(LYRIC_QRC_ORDER_LIST, TIME_DIFFERENCE);
        // 转换为 SRT 和 LRC 格式 输出
        String srtContent = LyricConverter.convertToSrt(lyricEntities);
        String lrcContent = LyricConverter.convertToLrc(lyricEntities);
        System.out.println("--- LRC Format ---");
        System.out.println(lrcContent);
        System.out.println("--- SRT Format ---");
        System.out.println(srtContent);


        // 写入文件
        System.out.println("Writing files to: " + OUTPUT_PATH);
        CommonUtils.writeFiles(OUTPUT_PATH, "lyrics.lrc", lrcContent);
        CommonUtils.writeFiles(OUTPUT_PATH, "lyrics.srt", srtContent);
        System.out.println("Files written successfully.");
    }


}
