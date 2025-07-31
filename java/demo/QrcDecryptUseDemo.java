package demo;

import utils.LyricConverter;
import utils.LyricParser;
import utils.QrcLyricDecrypter;

public class QrcDecryptUseDemo {

    /**
     * 指定 QRC 文件路径，解密并输出 XML、LRC 和 SRT 格式的歌词
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String xml = QrcLyricDecrypter.decryptByQrcFile("D:\\workspace\\Java\\LyricTools\\resources\\lyric\\qrc\\CRYCHIC - 春日影 - From THE FIRST TAKE - 258 - 春日影 - From THE FIRST TAKE_qmts.qrc");
        System.out.println("--- Xml ---");
        System.out.println(xml);
        System.out.println("--- LRC Format ---");
        System.out.println(LyricConverter.convertToLrc(LyricParser.qrcXmlParseByContent(xml)));
        System.out.println("--- SRT Format ---");
        System.out.println(LyricConverter.convertToSrt(LyricParser.qrcXmlParseByContent(xml)));

    }
}
