package utils;

import constant.SupportFileTypeEnum;
import entity.LyricEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpenLyricUtils {

    /**
     * 合并多个歌词列表
     * @param fileListMap 键为文件路径，值为文件类型 SupportFileTypeEnum
     * @param timeDifference 时间差范围（毫秒）
     * @return 合并后的歌词列表
     */
    public static List<LyricEntity> mergeLyricByFileList(Map<String, SupportFileTypeEnum> fileListMap, long timeDifference) {

        // 直接读取所谓文件的十六进制字符串，然后走文件内容合并方法
        Map<String, SupportFileTypeEnum> mergeContentMap = new HashMap<>();
        for (Map.Entry<String, SupportFileTypeEnum> fileListMapEntry : fileListMap.entrySet()) {
            String hex = CommonUtils.bytesToHex(CommonUtils.readFileToBytes(fileListMapEntry.getKey()));
            mergeContentMap.put(hex, fileListMapEntry.getValue());
        }

        return mergeLyricByContentList(mergeContentMap, timeDifference);
    }



    /**
     * 合并多个歌词列表
     * @param contentMap 歌词内容映射，键为歌词内容，值为歌词类型 SupportFileTypeEnum
     * @param timeDifference 时间差范围（毫秒）
     * @return 合并后的歌词列表
     */
    public static List<LyricEntity> mergeLyricByContentList(Map<String, SupportFileTypeEnum> contentMap, long timeDifference) {

        // 对传入的文件内容，先判断类型，qrc相关类型需要走解密流程后再进入相应的解析方法
        Map<String, SupportFileTypeEnum> mergeContentMap = new HashMap<>();
        for (Map.Entry<String, SupportFileTypeEnum> contentMapEntry : contentMap.entrySet()) {
            String content = contentMapEntry.getKey();
            if(contentMapEntry.getValue().isNeedDecrypt()) {
                // 如果是 QRC 文件，先解密
                content = QrcLyricDecrypter.decryptByQrcHexContent(content);
                if (content.isEmpty()) {
                    System.err.println("Failed to decrypt QRC file: " + contentMapEntry.getKey());
                }
            }
            mergeContentMap.put(content, contentMapEntry.getValue());
        }

        // 走解析器解析成对象
        List<List<LyricEntity>> lyrics = new ArrayList<>();
        for (Map.Entry<String, SupportFileTypeEnum> mergeContentMapEntry : mergeContentMap.entrySet()) {
            List<LyricEntity> lyricEntities = LyricParser.parseLyricContent(mergeContentMapEntry.getKey(), mergeContentMapEntry.getValue());
            lyrics.add(lyricEntities);
        }

        // 合并
        List<LyricEntity> mergedLyric = LyricMerger.mergeLyricByEntity(timeDifference, lyrics);

        return mergedLyric;
    }
}
