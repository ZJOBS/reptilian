package com.aigushou.entity;

/**
 * 收益对象
 *
 * @author jiezhang
 */
public class RateEntity {

    public RateEntity() {
    }

    public RateEntity(String rate, String dateTime) {
        this.rate = rate;
        this.dateTime = dateTime;
    }

    /**
     * 收益率
     */
    private String rate;

    /**
     * 时间
     */
    private String dateTime;

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public String toString() {
        return "RateEntity{" +
                "rate='" + rate + '\'' +
                ", dateTime='" + dateTime + '\'' +
                '}';
    }
}
