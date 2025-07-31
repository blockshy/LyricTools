package entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;

// 通用字幕模型
public class LyricEntity implements Comparator<LyricEntity> {

    /**
     * 字幕序号
     */
    private Integer number;

    /**
     * 字幕开始时间（毫秒）
     */
    private Long startTimeMs;

    /**
     * 字幕结束时间（毫秒）
     */
    private Long endTimeMs;

    /**
     * 字幕文本
     */
    private String text;

    public LyricEntity(Integer number, Long startTimeMs, Long endTimeMs, String text) {
        this.number = number;
        this.startTimeMs = startTimeMs;
        this.endTimeMs = endTimeMs;
        this.text = text;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Long getStartTimeMs() {
        return startTimeMs;
    }

    public Double getStartTimeSeconds() {
        return BigDecimal.valueOf(startTimeMs).divide(new BigDecimal(1000), 2, RoundingMode.HALF_UP).doubleValue();
    }

    public void setStartTimeMs(Long startTimeMs) {
        this.startTimeMs = startTimeMs;
    }

    public Long getEndTimeMs() {
        return endTimeMs;
    }

    public Double getEndTimeSeconds() {
        return BigDecimal.valueOf(endTimeMs).divide(new BigDecimal(1000), 2, RoundingMode.HALF_UP).doubleValue();
    }

    public void setEndTimeMs(Long endTimeMs) {
        this.endTimeMs = endTimeMs;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }


    @Override
    public int compare(LyricEntity o1, LyricEntity o2) {
        return o1.getStartTimeMs().compareTo(o2.getStartTimeMs());
    }
}

