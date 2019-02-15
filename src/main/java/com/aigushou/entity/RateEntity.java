package com.aigushou.entity;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RateEntity that = (RateEntity) o;
        return Objects.equals(rate, that.rate) &&
                Objects.equals(dateTime, that.dateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rate, dateTime);
    }
}
