package demo;

import utils.LyricConverter;
import utils.LyricParser;
import utils.QrcLyricDecrypter;

public class QrcDecryptUseDemo {

    // QRC 文件路径
    private static final String FILE_PATH = "D:\\workspace\\Java\\LyricTools\\resources\\lyric\\qrc\\CRYCHIC - 春日影 - From THE FIRST TAKE - 258 - 春日影 - From THE FIRST TAKE_qmts.qrc";

    /**
     * 指定 QRC 文件路径，解密并输出 XML、LRC 和 SRT 格式的歌词
     * @param args
     */
    public static void main(String[] args) {
        String xml = QrcLyricDecrypter.decryptByQrcFile(FILE_PATH);
        System.out.println("--- Xml ---");
        System.out.println(xml);
        System.out.println("--- LRC Format ---");
        System.out.println(LyricConverter.convertToLrc(LyricParser.qrcXmlParseByContent(xml)));
        System.out.println("--- SRT Format ---");
        System.out.println(LyricConverter.convertToSrt(LyricParser.qrcXmlParseByContent(xml)));

    }
}
