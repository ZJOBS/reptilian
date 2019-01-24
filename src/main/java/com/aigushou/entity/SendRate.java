package com.aigushou.entity;

/**
 * @author jiezhang
 */
public class SendRate {

    private String bondCode;

    private String rate;

    private String dateTime;


    public String getBondCode() {
        return bondCode;
    }

    public void setBondCode(String bondCode) {
        this.bondCode = bondCode;
    }

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
        return "SendRate{" +
                "bondCode='" + bondCode + '\'' +
                ", rate='" + rate + '\'' +
                ", dateTime='" + dateTime + '\'' +
                '}';
    }
}
