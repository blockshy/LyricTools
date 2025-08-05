package utils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * 使用三重DES算法进行数据解密
 */
public class QrcLyricDecrypter {

    // 读取本地QRC转换为可以decrypt的字节流所需异或数据
    private static final String XOR_KEY = "629F5B0900C35E95239F13117ED8923FBC90BB740EC347743D90AA3F51D8F411849FDE951DC3C609D59FFA66F9D8F0F7A090A1D6F3C3F3D6A190A0F7F0D8F966FA9FD509C6C31D95DE9F8411F4D8513FAA903D7447C30E74BB90BC3F92D87E11139F23955EC300095B9F6266A1D852F76790CAD64AC34AD6CA9067F752D8A166";

    // 三重DES算法使用的三个密钥
    private static final byte[] KEY1 = "!@#)(NHLiuy*$%^&".getBytes(StandardCharsets.UTF_8);
    private static final byte[] KEY2 = "123ZXC!@#)(*$%^&".getBytes(StandardCharsets.UTF_8);
    private static final byte[] KEY3 = "!@#)(*$%^&abcDEF".getBytes(StandardCharsets.UTF_8);
    
    // 加密/解密模式标识
    private static final int ENCRYPT_MODE = 1;
    private static final int DECRYPT_MODE = 0;
    
    // DES算法使用的8个S盒
    private static final int[][] SBOXES = {
        // S盒1
        {
            14, 4, 13, 1, 2, 15, 11, 8, 3, 10, 6, 12, 5, 9, 0, 7,
            0, 15, 7, 4, 14, 2, 13, 1, 10, 6, 12, 11, 9, 5, 3, 8,
            4, 1, 14, 8, 13, 6, 2, 11, 15, 12, 9, 7, 3, 10, 5, 0,
            15, 12, 8, 2, 4, 9, 1, 7, 5, 11, 3, 14, 10, 0, 6, 13
        },
        // S盒2
        {
            15, 1, 8, 14, 6, 11, 3, 4, 9, 7, 2, 13, 12, 0, 5, 10,
            3, 13, 4, 7, 15, 2, 8, 15, 12, 0, 1, 10, 6, 9, 11, 5,
            0, 14, 7, 11, 10, 4, 13, 1, 5, 8, 12, 6, 9, 3, 2, 15,
            13, 8, 10, 1, 3, 15, 4, 2, 11, 6, 7, 12, 0, 5, 14, 9
        },
        // S盒3
        {
            10, 0, 9, 14, 6, 3, 15, 5, 1, 13, 12, 7, 11, 4, 2, 8,
            13, 7, 0, 9, 3, 4, 6, 10, 2, 8, 5, 14, 12, 11, 15, 1,
            13, 6, 4, 9, 8, 15, 3, 0, 11, 1, 2, 12, 5, 10, 14, 7,
            1, 10, 13, 0, 6, 9, 8, 7, 4, 15, 14, 3, 11, 5, 2, 12
        },
        // S盒4
        {
            7, 13, 14, 3, 0, 6, 9, 10, 1, 2, 8, 5, 11, 12, 4, 15,
            13, 8, 11, 5, 6, 15, 0, 3, 4, 7, 2, 12, 1, 10, 14, 9,
            10, 6, 9, 0, 12, 11, 7, 13, 15, 1, 3, 14, 5, 2, 8, 4,
            3, 15, 0, 6, 10, 10, 13, 8, 9, 4, 5, 11, 12, 7, 2, 14
        },
        // S盒5
        {
            2, 12, 4, 1, 7, 10, 11, 6, 8, 5, 3, 15, 13, 0, 14, 9,
            14, 11, 2, 12, 4, 7, 13, 1, 5, 0, 15, 10, 3, 9, 8, 6,
            4, 2, 1, 11, 10, 13, 7, 8, 15, 9, 12, 5, 6, 3, 0, 14,
            11, 8, 12, 7, 1, 14, 2, 13, 6, 15, 0, 9, 10, 4, 5, 3
        },
        // S盒6
        {
            12, 1, 10, 15, 9, 2, 6, 8, 0, 13, 3, 4, 14, 7, 5, 11,
            10, 15, 4, 2, 7, 12, 9, 5, 6, 1, 13, 14, 0, 11, 3, 8,
            9, 14, 15, 5, 2, 8, 12, 3, 7, 0, 4, 10, 1, 13, 11, 6,
            4, 3, 2, 12, 9, 5, 15, 10, 11, 14, 1, 7, 6, 0, 8, 13
        },
        // S盒7
        {
            4, 11, 2, 14, 15, 0, 8, 13, 3, 12, 9, 7, 5, 10, 6, 1,
            13, 0, 11, 7, 4, 9, 1, 10, 14, 3, 5, 12, 2, 15, 8, 6,
            1, 4, 11, 13, 12, 3, 7, 14, 10, 15, 6, 8, 0, 5, 9, 2,
            6, 11, 13, 8, 1, 4, 10, 7, 9, 5, 0, 15, 14, 2, 3, 12
        },
        // S盒8
        {
            13, 2, 8, 4, 6, 15, 11, 1, 10, 9, 3, 14, 5, 0, 12, 7,
            1, 15, 13, 8, 10, 3, 7, 4, 12, 5, 6, 11, 0, 14, 9, 2,
            7, 11, 4, 1, 9, 12, 14, 2, 0, 6, 10, 13, 15, 3, 5, 8,
            2, 1, 14, 7, 4, 10, 8, 13, 15, 12, 9, 0, 3, 5, 6, 11
        }
    };

    /**
     * 从字节数组中提取特定位
     * @param dataBytes 输入的字节数组
     * @param bitPosition 要提取的位位置(0-63)
     * @param shiftAmount 结果左移的位数
     * @return 提取并移位后的位值
     */
    private static int extractBitFromBytes(byte[] dataBytes, int bitPosition, int shiftAmount) {
        // 计算字节索引: 每32位为一组，每组4字节
        int byteIndex = (bitPosition / 32) * 4 + 3 - (bitPosition % 32) / 8;
        // 计算字节内的位位置(从最高位开始)
        int bitInByte = 7 - (bitPosition % 8);
        return ((dataBytes[byteIndex] >> bitInByte) & 0x01) << shiftAmount;
    }

    /**
     * 从32位整数中提取特定位
     * @param dataInt 输入的32位整数
     * @param bitPosition 要提取的位位置(0-31)
     * @param shiftAmount 结果左移的位数
     * @return 提取并移位后的位值
     */
    private static int extractBitFromInt(int dataInt, int bitPosition, int shiftAmount) {
        return ((dataInt >> (31 - bitPosition)) & 0x00000001) << shiftAmount;
    }

    /**
     * 从32位整数中提取特定位（左移版本）
     * @param dataInt 输入的32位整数
     * @param bitPosition 要提取的位位置
     * @param shiftAmount 结果右移的位数
     * @return 提取并移位后的位值
     */
    private static int extractBitLeftShift(int dataInt, int bitPosition, int shiftAmount) {
        return ((dataInt << bitPosition) & 0x80000000) >>> shiftAmount;
    }

    /**
     * 准备S盒查找的索引
     * @param inputByte 输入的6位值
     * @return 用于S盒查找的5位索引
     */
    private static int prepareSboxIndex(int inputByte) {
        return (inputByte & 0x20) | ((inputByte & 0x1f) >> 1) | ((inputByte & 0x01) << 4);
    }

    /**
     * 执行DES算法的初始置换(IP)
     * @param state 存储置换结果的数组(两个32位整数)
     * @param inputData 64位输入数据(8字节)
     */
    private static void initialPermutation(int[] state, byte[] inputData) {
        // 高位32位
        state[0] = (
                extractBitFromBytes(inputData, 57, 31) |
                extractBitFromBytes(inputData, 49, 30) |
                extractBitFromBytes(inputData, 41, 29) |
                extractBitFromBytes(inputData, 33, 28) |
                extractBitFromBytes(inputData, 25, 27) |
                extractBitFromBytes(inputData, 17, 26) |
                extractBitFromBytes(inputData, 9, 25) |
                extractBitFromBytes(inputData, 1, 24) |
                extractBitFromBytes(inputData, 59, 23) |
                extractBitFromBytes(inputData, 51, 22) |
                extractBitFromBytes(inputData, 43, 21) |
                extractBitFromBytes(inputData, 35, 20) |
                extractBitFromBytes(inputData, 27, 19) |
                extractBitFromBytes(inputData, 19, 18) |
                extractBitFromBytes(inputData, 11, 17) |
                extractBitFromBytes(inputData, 3, 16) |
                extractBitFromBytes(inputData, 61, 15) |
                extractBitFromBytes(inputData, 53, 14) |
                extractBitFromBytes(inputData, 45, 13) |
                extractBitFromBytes(inputData, 37, 12) |
                extractBitFromBytes(inputData, 29, 11) |
                extractBitFromBytes(inputData, 21, 10) |
                extractBitFromBytes(inputData, 13, 9) |
                extractBitFromBytes(inputData, 5, 8) |
                extractBitFromBytes(inputData, 63, 7) |
                extractBitFromBytes(inputData, 55, 6) |
                extractBitFromBytes(inputData, 47, 5) |
                extractBitFromBytes(inputData, 39, 4) |
                extractBitFromBytes(inputData, 31, 3) |
                extractBitFromBytes(inputData, 23, 2) |
                extractBitFromBytes(inputData, 15, 1) |
                extractBitFromBytes(inputData, 7, 0)
        );

        // 低位32位
        state[1] = (
                extractBitFromBytes(inputData, 56, 31) |
                extractBitFromBytes(inputData, 48, 30) |
                extractBitFromBytes(inputData, 40, 29) |
                extractBitFromBytes(inputData, 32, 28) |
                extractBitFromBytes(inputData, 24, 27) |
                extractBitFromBytes(inputData, 16, 26) |
                extractBitFromBytes(inputData, 8, 25) |
                extractBitFromBytes(inputData, 0, 24) |
                extractBitFromBytes(inputData, 58, 23) |
                extractBitFromBytes(inputData, 50, 22) |
                extractBitFromBytes(inputData, 42, 21) |
                extractBitFromBytes(inputData, 34, 20) |
                extractBitFromBytes(inputData, 26, 19) |
                extractBitFromBytes(inputData, 18, 18) |
                extractBitFromBytes(inputData, 10, 17) |
                extractBitFromBytes(inputData, 2, 16) |
                extractBitFromBytes(inputData, 60, 15) |
                extractBitFromBytes(inputData, 52, 14) |
                extractBitFromBytes(inputData, 44, 13) |
                extractBitFromBytes(inputData, 36, 12) |
                extractBitFromBytes(inputData, 28, 11) |
                extractBitFromBytes(inputData, 20, 10) |
                extractBitFromBytes(inputData, 12, 9) |
                extractBitFromBytes(inputData, 4, 8) |
                extractBitFromBytes(inputData, 62, 7) |
                extractBitFromBytes(inputData, 54, 6) |
                extractBitFromBytes(inputData, 46, 5) |
                extractBitFromBytes(inputData, 38, 4) |
                extractBitFromBytes(inputData, 30, 3) |
                extractBitFromBytes(inputData, 22, 2) |
                extractBitFromBytes(inputData, 14, 1) |
                extractBitFromBytes(inputData, 6, 0)
        );
    }

    /**
     * 执行DES算法的逆初始置换(InvIP)
     * @param state 输入状态(两个32位整数)
     * @param output 存储置换结果的字节数组(8字节)
     */
    private static void inversePermutation(int[] state, byte[] output) {
        // 将两个32位整数转换为8字节数组
        output[3] = (byte) (
                extractBitFromInt(state[1], 7, 7) |
                extractBitFromInt(state[0], 7, 6) |
                extractBitFromInt(state[1], 15, 5) |
                extractBitFromInt(state[0], 15, 4) |
                extractBitFromInt(state[1], 23, 3) |
                extractBitFromInt(state[0], 23, 2) |
                extractBitFromInt(state[1], 31, 1) |
                extractBitFromInt(state[0], 31, 0)
        );

        output[2] = (byte) (
                extractBitFromInt(state[1], 6, 7) |
                extractBitFromInt(state[0], 6, 6) |
                extractBitFromInt(state[1], 14, 5) |
                extractBitFromInt(state[0], 14, 4) |
                extractBitFromInt(state[1], 22, 3) |
                extractBitFromInt(state[0], 22, 2) |
                extractBitFromInt(state[1], 30, 1) |
                extractBitFromInt(state[0], 30, 0)
        );

        output[1] = (byte) (
                extractBitFromInt(state[1], 5, 7) |
                extractBitFromInt(state[0], 5, 6) |
                extractBitFromInt(state[1], 13, 5) |
                extractBitFromInt(state[0], 13, 4) |
                extractBitFromInt(state[1], 21, 3) |
                extractBitFromInt(state[0], 21, 2) |
                extractBitFromInt(state[1], 29, 1) |
                extractBitFromInt(state[0], 29, 0)
        );

        output[0] = (byte) (
                extractBitFromInt(state[1], 4, 7) |
                extractBitFromInt(state[0], 4, 6) |
                extractBitFromInt(state[1], 12, 5) |
                extractBitFromInt(state[0], 12, 4) |
                extractBitFromInt(state[1], 20, 3) |
                extractBitFromInt(state[0], 20, 2) |
                extractBitFromInt(state[1], 28, 1) |
                extractBitFromInt(state[0], 28, 0)
        );

        output[7] = (byte) (
                extractBitFromInt(state[1], 3, 7) |
                extractBitFromInt(state[0], 3, 6) |
                extractBitFromInt(state[1], 11, 5) |
                extractBitFromInt(state[0], 11, 4) |
                extractBitFromInt(state[1], 19, 3) |
                extractBitFromInt(state[0], 19, 2) |
                extractBitFromInt(state[1], 27, 1) |
                extractBitFromInt(state[0], 27, 0)
        );

        output[6] = (byte) (
                extractBitFromInt(state[1], 2, 7) |
                extractBitFromInt(state[0], 2, 6) |
                extractBitFromInt(state[1], 10, 5) |
                extractBitFromInt(state[0], 10, 4) |
                extractBitFromInt(state[1], 18, 3) |
                extractBitFromInt(state[0], 18, 2) |
                extractBitFromInt(state[1], 26, 1) |
                extractBitFromInt(state[0], 26, 0)
        );

        output[5] = (byte) (
                extractBitFromInt(state[1], 1, 7) |
                extractBitFromInt(state[0], 1, 6) |
                extractBitFromInt(state[1], 9, 5) |
                extractBitFromInt(state[0], 9, 4) |
                extractBitFromInt(state[1], 17, 3) |
                extractBitFromInt(state[0], 17, 2) |
                extractBitFromInt(state[1], 25, 1) |
                extractBitFromInt(state[0], 25, 0)
        );

        output[4] = (byte) (
                extractBitFromInt(state[1], 0, 7) |
                extractBitFromInt(state[0], 0, 6) |
                extractBitFromInt(state[1], 8, 5) |
                extractBitFromInt(state[0], 8, 4) |
                extractBitFromInt(state[1], 16, 3) |
                extractBitFromInt(state[0], 16, 2) |
                extractBitFromInt(state[1], 24, 1) |
                extractBitFromInt(state[0], 24, 0)
        );
    }

    /**
     * DES算法的Feistel函数
     * @param state 32位输入状态
     * @param roundKey 48位轮密钥(6字节)
     * @return 32位输出
     */
    private static int feistelFunction(int state, byte[] roundKey) {
        // 扩展置换：将32位扩展为48位
        int[] expanded = new int[6];
        // 第一部分扩展
        int t1 = (
                extractBitLeftShift(state, 31, 0) |
                ((state & 0xf0000000) >>> 1) |
                extractBitLeftShift(state, 4, 5) |
                extractBitLeftShift(state, 3, 6) |
                ((state & 0x0f000000) >>> 3) |
                extractBitLeftShift(state, 8, 11) |
                extractBitLeftShift(state, 7, 12) |
                ((state & 0x00f00000) >>> 5) |
                extractBitLeftShift(state, 12, 17) |
                extractBitLeftShift(state, 11, 18) |
                ((state & 0x000f0000) >>> 7) |
                extractBitLeftShift(state, 16, 23)
        );
        // 第二部分扩展
        int t2 = (
                extractBitLeftShift(state, 15, 0) |
                ((state & 0x0000f000) << 15) |
                extractBitLeftShift(state, 20, 5) |
                extractBitLeftShift(state, 19, 6) |
                ((state & 0x00000f00) << 13) |
                extractBitLeftShift(state, 24, 11) |
                extractBitLeftShift(state, 23, 12) |
                ((state & 0x000000f0) << 11) |
                extractBitLeftShift(state, 28, 17) |
                extractBitLeftShift(state, 27, 18) |
                ((state & 0x0000000f) << 9) |
                extractBitLeftShift(state, 0, 23)
        );

        // 将扩展结果分割为6字节
        expanded[0] = (t1 >> 24) & 0xFF;
        expanded[1] = (t1 >> 16) & 0xFF;
        expanded[2] = (t1 >> 8) & 0xFF;
        expanded[3] = (t2 >> 24) & 0xFF;
        expanded[4] = (t2 >> 16) & 0xFF;
        expanded[5] = (t2 >> 8) & 0xFF;

        // 与轮密钥进行异或
        for (int i = 0; i < 6; i++) {
            expanded[i] ^= (roundKey[i] & 0xFF);
        }

        // 将6字节转换为8个6位组
        int[] sboxInputs = new int[8];
        sboxInputs[0] = (expanded[0] >> 2);
        sboxInputs[1] = ((expanded[0] & 0x03) << 4) | (expanded[1] >> 4);
        sboxInputs[2] = ((expanded[1] & 0x0F) << 2) | (expanded[2] >> 6);
        sboxInputs[3] = expanded[2] & 0x3F;
        sboxInputs[4] = (expanded[3] >> 2);
        sboxInputs[5] = ((expanded[3] & 0x03) << 4) | (expanded[4] >> 4);
        sboxInputs[6] = ((expanded[4] & 0x0F) << 2) | (expanded[5] >> 6);
        sboxInputs[7] = expanded[5] & 0x3F;

        // S盒替换 (6位输入 -> 4位输出)
        int sboxOutput = 0;
        sboxOutput |= SBOXES[0][prepareSboxIndex(sboxInputs[0])] << 28;
        sboxOutput |= SBOXES[1][prepareSboxIndex(sboxInputs[1])] << 24;
        sboxOutput |= SBOXES[2][prepareSboxIndex(sboxInputs[2])] << 20;
        sboxOutput |= SBOXES[3][prepareSboxIndex(sboxInputs[3])] << 16;
        sboxOutput |= SBOXES[4][prepareSboxIndex(sboxInputs[4])] << 12;
        sboxOutput |= SBOXES[5][prepareSboxIndex(sboxInputs[5])] << 8;
        sboxOutput |= SBOXES[6][prepareSboxIndex(sboxInputs[6])] << 4;
        sboxOutput |= SBOXES[7][prepareSboxIndex(sboxInputs[7])];

        // P盒置换
        return (
                extractBitLeftShift(sboxOutput, 15, 0) |
                extractBitLeftShift(sboxOutput, 6, 1) |
                extractBitLeftShift(sboxOutput, 19, 2) |
                extractBitLeftShift(sboxOutput, 20, 3) |
                extractBitLeftShift(sboxOutput, 28, 4) |
                extractBitLeftShift(sboxOutput, 11, 5) |
                extractBitLeftShift(sboxOutput, 27, 6) |
                extractBitLeftShift(sboxOutput, 16, 7) |
                extractBitLeftShift(sboxOutput, 0, 8) |
                extractBitLeftShift(sboxOutput, 14, 9) |
                extractBitLeftShift(sboxOutput, 22, 10) |
                extractBitLeftShift(sboxOutput, 25, 11) |
                extractBitLeftShift(sboxOutput, 4, 12) |
                extractBitLeftShift(sboxOutput, 17, 13) |
                extractBitLeftShift(sboxOutput, 30, 14) |
                extractBitLeftShift(sboxOutput, 9, 15) |
                extractBitLeftShift(sboxOutput, 1, 16) |
                extractBitLeftShift(sboxOutput, 7, 17) |
                extractBitLeftShift(sboxOutput, 23, 18) |
                extractBitLeftShift(sboxOutput, 13, 19) |
                extractBitLeftShift(sboxOutput, 31, 20) |
                extractBitLeftShift(sboxOutput, 26, 21) |
                extractBitLeftShift(sboxOutput, 2, 22) |
                extractBitLeftShift(sboxOutput, 8, 23) |
                extractBitLeftShift(sboxOutput, 18, 24) |
                extractBitLeftShift(sboxOutput, 12, 25) |
                extractBitLeftShift(sboxOutput, 29, 26) |
                extractBitLeftShift(sboxOutput, 5, 27) |
                extractBitLeftShift(sboxOutput, 21, 28) |
                extractBitLeftShift(sboxOutput, 10, 29) |
                extractBitLeftShift(sboxOutput, 3, 30) |
                extractBitLeftShift(sboxOutput, 24, 31)
        );
    }

    /**
     * 生成DES算法的轮密钥
     * @param masterKey 64位主密钥(8字节)
     * @param keySchedule 存储生成的轮密钥(16轮，每轮6字节)
     * @param mode 加密或解密模式
     */
    private static void generateKeySchedule(byte[] masterKey, byte[][] keySchedule, int mode) {
        // 密钥置换参数
        int[] keyRotation = {1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1};
        int[] keyPermC = {
            56, 48, 40, 32, 24, 16, 8, 0, 57, 49, 41, 33, 25, 17, 9, 1,
            58, 50, 42, 34, 26, 18, 10, 2, 59, 51, 43, 35
        };
        int[] keyPermD = {
            62, 54, 46, 38, 30, 22, 14, 6, 61, 53, 45, 37, 29, 21, 13, 5,
            60, 52, 44, 36, 28, 20, 12, 4, 27, 19, 11, 3
        };
        int[] keyCompression = {
            13, 16, 10, 23, 0, 4, 2, 27, 14, 5, 20, 9, 22, 18, 11, 3,
            25, 7, 15, 6, 26, 19, 12, 1, 40, 51, 30, 36, 46, 54, 29, 39,
            50, 44, 32, 47, 43, 48, 38, 55, 33, 52, 45, 41, 49, 35, 28, 31
        };

        // 初始置换选择1
        int leftHalf = 0;
        int rightHalf = 0;
        int j = 31;

        // 生成左半部分28位
        for (int i = 0; i < 28; i++) {
            leftHalf |= extractBitFromBytes(masterKey, keyPermC[i], j);
            j--;
        }

        // 生成右半部分28位
        j = 31;
        for (int i = 0; i < 28; i++) {
            rightHalf |= extractBitFromBytes(masterKey, keyPermD[i], j);
            j--;
        }

        // 生成16轮子密钥
        for (int roundNum = 0; roundNum < 16; roundNum++) {
            // 循环左移
            int shift = keyRotation[roundNum];
            leftHalf = ((leftHalf << shift) | (leftHalf >>> (28 - shift))) & 0xfffffff0;
            rightHalf = ((rightHalf << shift) | (rightHalf >>> (28 - shift))) & 0xfffffff0;

            // 确定轮密钥的存储位置
            int scheduleIndex;
            if (mode == DECRYPT_MODE) {
                scheduleIndex = 15 - roundNum;
            } else {
                scheduleIndex = roundNum;
            }

            // 初始化轮密钥
            for (int k = 0; k < 6; k++) {
                keySchedule[scheduleIndex][k] = 0;
            }

            // 压缩置换生成48位轮密钥
            for (int k = 0; k < 24; k++) {
                keySchedule[scheduleIndex][k / 8] |= (byte) extractBitFromInt(
                        leftHalf, keyCompression[k], 7 - (k % 8)
                );
            }

            for (int k = 24; k < 48; k++) {
                keySchedule[scheduleIndex][k / 8] |= (byte) extractBitFromInt(
                        rightHalf, keyCompression[k] - 27, 7 - (k % 8)
                );
            }
        }
    }

    /**
     * 处理单个DES块(加密或解密)
     * @param inputBlock 输入数据块(8字节)
     * @param keySchedule 轮密钥表
     * @return 处理后的数据块(8字节)
     */
    private static byte[] processDesBlock(byte[] inputBlock, byte[][] keySchedule) {
        int[] state = new int[2];
        // 初始置换
        initialPermutation(state, inputBlock);
        // 15轮Feistel网络(带交换)
        for (int roundIdx = 0; roundIdx < 15; roundIdx++) {
            int temp = state[1];
            state[1] = feistelFunction(state[1], keySchedule[roundIdx]) ^ state[0];
            state[0] = temp;
        }

        // 最后一轮(不交换左右部分)
        state[0] = feistelFunction(state[1], keySchedule[15]) ^ state[0];

        // 逆初始置换
        byte[] outputBlock = new byte[8];
        inversePermutation(state, outputBlock);
        return outputBlock;
    }

    /**
     * DES加密函数
     * @param data 待加密数据
     * @param key 64位密钥(8字节)
     * @return 加密后的数据
     */
    private static byte[] desEncrypt(byte[] data, byte[] key) {
        byte[][] keySchedule = new byte[16][6];
        generateKeySchedule(key, keySchedule, ENCRYPT_MODE);
        return processInBlocks(data, keySchedule);
    }

    /**
     * DES解密函数
     * @param data 待解密数据
     * @param key 64位密钥(8字节)
     * @return 解密后的数据
     */
    private static byte[] desDecrypt(byte[] data, byte[] key) {
        byte[][] keySchedule = new byte[16][6];
        generateKeySchedule(key, keySchedule, DECRYPT_MODE);
        return processInBlocks(data, keySchedule);
    }

    /**
     * 分块处理数据
     * @param data 输入数据
     * @param keySchedule 轮密钥表
     * @return 处理后的数据
     */
    private static byte[] processInBlocks(byte[] data, byte[][] keySchedule) {
        int length = data.length;
        byte[] result = new byte[length];
        
        // 分块处理(每块8字节)
        for (int i = 0; i < length; i += 8) {
            int blockSize = Math.min(8, length - i);
            byte[] inputBlock = Arrays.copyOfRange(data, i, i + blockSize);
            // 如果不足8字节，填充0
            if (inputBlock.length < 8) {
                inputBlock = Arrays.copyOf(inputBlock, 8);
            }
            byte[] outputBlock = processDesBlock(inputBlock, keySchedule);
            // 复制结果
            System.arraycopy(outputBlock, 0, result, i, Math.min(8, length - i));
        }
        return result;
    }

    /**
     * 解密QRC数据
     * @param hexString 十六进制格式的输入字符串
     * @return 解密后的文本内容
     */
    private static String decryptQrc(String hexString) {
        // 将十六进制字符串转换为字节数组
        byte[] encryptedData = CommonUtils.hexToBytes(hexString);
        
        // 三重DES解密 (D->E->D)
        byte[] step1 = desDecrypt(encryptedData, KEY1);
        byte[] step2 = desEncrypt(step1, KEY2);
        byte[] step3 = desDecrypt(step2, KEY3);

        // 使用zlib解压缩数据
        try {
            Inflater inflater = new Inflater();
            inflater.setInput(step3);
            // 当设置过小时可能导致最终解压出来的数据不完整，这里设置为4大一些
            byte[] decompressed = new byte[step3.length * 4]; // 初始缓冲区
            int decompressedLength = inflater.inflate(decompressed);
            inflater.end();
            
            // 将字节数据解码为UTF-8字符串
            return new String(decompressed, 0, decompressedLength, StandardCharsets.UTF_8);
        } catch (DataFormatException e) {
            throw new RuntimeException("解压缩失败: " + e.getMessage());
        }
    }

    /**
     * 加密QRC数据
     * @param text 待加密的文本内容
     * @return 加密后的十六进制字符串
     */
    public static String encryptQrc(String text) {
        // 将文本内容转换为字节数组
        byte[] inputData = text.getBytes(StandardCharsets.UTF_8);

        // 使用zlib压缩数据
        byte[] compressedData;
        try {
            Deflater deflater = new Deflater();
            deflater.setInput(inputData);
            deflater.finish();
            compressedData = new byte[inputData.length * 2]; // 初始缓冲区
            int compressedLength = deflater.deflate(compressedData);
            deflater.end();
            compressedData = Arrays.copyOf(compressedData, compressedLength);
        } catch (Exception e) {
            throw new RuntimeException("压缩失败: " + e.getMessage());
        }

        // 三重DES加密 (E->D->E)
        byte[] step1 = desEncrypt(compressedData, KEY3);
        byte[] step2 = desDecrypt(step1, KEY2);
        byte[] step3 = desEncrypt(step2, KEY1);

        // 将加密后的字节数组转换为十六进制字符串
        return CommonUtils.bytesToHex(step3);
    }

    /**
     * 使用QRC文件路径解密
     * @param filePath QRC文件路径
     * @return 解密后的文本内容
     */
    public static String decryptByQrcFile(String filePath){

        String hexContent = CommonUtils.bytesToHex(CommonUtils.readFileToBytes(filePath));

        return decryptByQrcHexContent(hexContent);
    }

    /**
     * 使用QRC文件内容解密
     * @param hexContent QRC文件内容的十六进制字符串
     * @return 解密后的文本内容
     */
    public static String decryptByQrcHexContent(String hexContent){

        // 跳过前11字节(22个十六进制字符)
        String processedHex = hexContent.startsWith("9825B0ACE3028368E8FC6C") ? hexContent.substring(22) : hexContent;

        // 执行异或操作
        String xoredHex = CommonUtils.xorHexStrings(processedHex, XOR_KEY);

        // 解密内容
        String decryptQrc = decryptQrc(xoredHex);

        if(decryptQrc.contains("<QrcInfos>") && !decryptQrc.contains("</QrcInfos>")){
            // 某些qrc文件解密之后缺少后缀
            decryptQrc += "\n\"/>\n</LyricInfo>\n</QrcInfos>";
        }

        // 某些qrc文件解密之后缺少后缀
        return decryptQrc;
    }

    public static void main(String[] args) {
        // 示例用法

        // 实际使用中从文件读取
        // 示例文件内容（实际使用时替换为真实文件读取）
        // 从文件读取原始字节
        String filePath = "文件路径";

        String hexContent = CommonUtils.bytesToHex(CommonUtils.readFileToBytes(filePath));

        // 跳过前11字节(22个十六进制字符)
        String processedHex = hexContent.substring(22);
        
        // 执行异或操作
        String xoredHex = CommonUtils.xorHexStrings(processedHex, XOR_KEY);
        
        // 解密内容
        String decryptText = decryptQrc(xoredHex);
        System.out.println(decryptText);
    }
}