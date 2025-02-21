import utils.LyricConverter;
import entity.LyricEntity;
import utils.LrcParser;
import utils.SrtParser;
import utils.LyricMerge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 歌词合并主程序
 */
public class Main {

    private static final System.Logger logger = System.getLogger("Main");

    // 合并歌词输出路径
    public static final String OUTPUT_PATH = "./resources/result/";

    // 设置歌词合并顺序
    // 合并顺序为数组中的先后顺序，可自行修改，传入路径需要为有效路径，如果为无效路径会取消该条目的合并
    public static final String[] LYRIC_ORDER_LIST = {
            "./resources/lyric/srt/高橋李依 (たかはし りえ) - Door (门) (Single Version)_qm.srt",
            "./resources/lyric/srt/高橋李依 (たかはし りえ) - Door (门) (Single Version)_qmRoma.srt",
            "./resources/lyric/srt/高橋李依 (たかはし りえ) - Door (门) (Single Version)_qmts.srt",
            "./resources/lyric/srt/高橋李依 (たかはし りえ) - Door (门) (Single.srt"
    };

    //设置偏差值 由于存在字幕文件不同步的情况，所以设置一个偏差值，当时间轴相差在这个值内时，认为是同一句歌词
    // 100L 表示 100 毫秒 偏差值为 T+100ms 和 T-100ms
    public static final long TIME_DIFFERENCE = 100L;

    public static void main(String[] args) throws IOException {

        logger.log(System.Logger.Level.INFO, System.getProperty("user.dir"));

        // 检查路径有效性并解析
        List<List<LyricEntity>> jpSrtSubtitles = parseLyricFileList(Arrays.asList(LYRIC_ORDER_LIST));

        // 排除非法路径
        jpSrtSubtitles.removeIf(list -> list == null || list.isEmpty());

        // 合并歌词
        List<LyricEntity> mergedLyric = LyricMerge.mergeLyric(jpSrtSubtitles, TIME_DIFFERENCE);

        // 转换为 SRT 和 LRC 格式
        String srtContent = LyricConverter.convertToSrt(mergedLyric);
        String lrcContent = LyricConverter.convertToLrc(mergedLyric);

        //输出路径不存在则先创建
        Path outputPath = Paths.get(OUTPUT_PATH);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }
        // 写入文件
        Files.write(Paths.get(OUTPUT_PATH + "subtitles.srt"), srtContent.getBytes());
        Files.write(Paths.get(OUTPUT_PATH + "lyrics.lrc"), lrcContent.getBytes());
        logger.log(System.Logger.Level.INFO, "歌词合并完成");

    }

    /**
     * 解析歌词文件
     * @param filePath 文件路径
     * @return 歌词列表
     * @throws IOException 文件读取异常
     */
    private static List<LyricEntity> parseLyricFile(String filePath) throws IOException {
        Path path;
        path = Paths.get(filePath);
        if (!Files.exists(path)) {
            logger.log(System.Logger.Level.INFO, "文件不存在: " + filePath);
            return null;
        }

        // 根据文件扩展名解析文件
        return switch (getFileExtension(filePath)) {
            case "srt" -> SrtParser.parse(path);
            case "lrc" -> LrcParser.parse(path);
            default -> {
                logger.log(System.Logger.Level.INFO, "不支持的文件格式: " + filePath);
                yield null;
            }
        };
    }

    /**
     * 解析歌词文件列表
     * @param filePaths 文件路径列表
     * @return 歌词列表
     */
    private static List<List<LyricEntity>> parseLyricFileList(List<String> filePaths) {
        List<List<LyricEntity>> lyrics = new ArrayList<>();
        filePaths.forEach(path -> {
            try {
                lyrics.add(parseLyricFile(path));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return lyrics;
    }

    /**
     * 获取文件扩展名
     * @param filePath 文件路径
     * @return 文件扩展名
     */
    private static String getFileExtension(String filePath) {
        int dotIndex = filePath.lastIndexOf('.');
        return (dotIndex == -1) ? "" : filePath.substring(dotIndex + 1).toLowerCase();
    }
}
