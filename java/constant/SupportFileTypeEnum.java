package constant;

public enum SupportFileTypeEnum {

    LRC("lrc", false),
    SRT("srt", false),
    QRC("qrc", true),
    QRC_ROMA("qrcRoma", true),
    QRC_TS("qrcTs", true);

    /**
     * 支持的文件类型枚举
     */
    private final String type;

    /**
     * 是否需要解密
     */
    private final boolean needDecrypt;

    SupportFileTypeEnum(String type, boolean needDecrypt) {
        this.type = type;
        this.needDecrypt = needDecrypt;
    }

    public String getType() {
        return type;
    }

    public boolean isNeedDecrypt() {
        return needDecrypt;
    }

    @Override
    public String toString() {
        return type;
    }
}
