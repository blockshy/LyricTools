package utils;

import org.w3c.dom.Document;
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
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CommonUtils {

    /**
     * 格式化时间为 SRT 格式
     * @param milliseconds 毫秒数
     * @return 格式化后的时间字符串
     */
    public static String formatSrtTime(Long milliseconds) {
        long hours = TimeUnit.MILLISECONDS.toHours(milliseconds);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60;
        long millis = milliseconds % 1000;

        return String.format("%02d:%02d:%02d,%03d", hours, minutes, seconds, millis);
    }

    /**
     * 格式化时间为 LRC 格式
     * @param milliseconds 毫秒数
     * @return 格式化后的时间字符串
     */
    public static String formatLrcTime(Long milliseconds) {
        long totalSeconds = milliseconds / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        long hundredths = (milliseconds % 1000) / 10;

        return String.format("%02d:%02d.%02d", minutes, seconds, hundredths);
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

    /**
     * 校验两个startTimeMs之间的时间差是否在给定的范围内
     *
     * @param startTimeMs1 第一个startTimeMs
     * @param startTimeMs2 第二个startTimeMs
     * @param timeRangeMillis 时间差范围（毫秒）
     * @return 如果时间差在范围内，返回true，否则返回false
     */
    public static boolean isTimeDifferenceValid(Long startTimeMs1, Long startTimeMs2, long timeRangeMillis) {
        if (startTimeMs1 == null || startTimeMs2 == null) {
            throw new IllegalArgumentException("startTimeMs不能为null");
        }

        long timeDifference = Math.abs(startTimeMs1 - startTimeMs2);

        // 判断时间差是否在指定范围内
        return timeDifference <= timeRangeMillis;
    }

    /**
     * 解析毫秒字符串
     * @param millisStr 毫秒字符串
     * @param separator 分隔符
     * @return 解析后的毫秒数
     */
    public static int parseMillis(String millisStr, String separator) {
        int length = millisStr.length();
        if (length == 2 && separator.equals(".")) {
            return Integer.parseInt(millisStr) * 10; // 百分秒转毫秒
        }
        return Integer.parseInt(millisStr);
    }

    /**
     * 解析时间字符串为毫秒
     * @param timeStr 时间字符串，格式为 "HH:MM:SS,mmm" 或 "HH:MM:SS.mmm"
     * @return 解析后的毫秒数
     */
    public static long parseTime(String timeStr) {
        String normalized = timeStr.replace('.', ',');
        String[] parts = normalized.split("[,:]");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);
        int millis = Integer.parseInt(parts[3]);
        return (hours * 3600L + minutes * 60L + seconds) * 1000 + millis;
    }

    /**
     * 将字节数据转换为十六进制字符串
     * @param byteData 字节数据
     * @return 十六进制字符串(大写)
     */
    public static String bytesToHex(byte[] byteData) {
        return HexFormat.of().formatHex(byteData).toUpperCase();
    }

    /**
     * 将十六进制字符串转换为字节数组
     * @param hexString 十六进制字符串
     * @return 字节数组
     */
    public static byte[] hexToBytes(String hexString) {
        // 处理奇数长度的情况
        if (hexString.length() % 2 != 0) {
            hexString = "0" + hexString;
        }
        return HexFormat.of().parseHex(hexString);
    }

    /**
     * 对两个十六进制字符串进行异或操作
     * @param hexStr1 第一个十六进制字符串
     * @param hexStr2 第二个十六进制字符串
     * @return 异或结果的十六进制字符串
     */
    public static String xorHexStrings(String hexStr1, String hexStr2) {
        byte[] bytes1 = hexToBytes(hexStr1);
        byte[] bytes2 = hexToBytes(hexStr2);
        byte[] result = new byte[bytes1.length];

        // 逐字节异或(循环使用第二个密钥)
        for (int i = 0; i < bytes1.length; i++) {
            int keyIndex = i % bytes2.length;
            result[i] = (byte) (bytes1[i] ^ bytes2[keyIndex]);
        }

        return bytesToHex(result);
    }

    /**
     * 读取文件为List<String>
     * @param filePath 文件路径
     * @return 文件内容的List<String>
     */
    public static List<String> readFileToList(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return Files.readAllLines(path);
        } catch (IOException e) {
            throw new RuntimeException("读取文件失败: " + filePath, e);
        }
    }

    public static byte[] readFileToBytes(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException("读取文件失败: " + filePath, e);
        }
    }



    /**
     * 解析XML字符串为Document对象
     * @param xml XML字符串
     * @return 解析后的Document对象
     */
    public static Document parseXml(String xml) {
        Document document = null;
        try {
            // 此处可添加安全配置（防XXE攻击）
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        }catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException("解析XML失败: " + xml, e);
        }
        return document;
    }
}
