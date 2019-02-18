package com.aigushou.utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 时间工具
 *
 * @author jiezhang
 */
public class DateTimeUtils {

    /**
     * 线程安全的
     */
    DateTimeFormatter df_yyyy_MM_dd = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 计算两个时间持续时间
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return
     */
    public static long duration(LocalDateTime startTime, LocalDateTime endTime) {
        Duration duration = Duration.between(startTime, endTime);
        return duration.getSeconds();
    }


//    public static void main(String[] args) {
//        LocalDateTime ldt = LocalDateTime.now();
//
//        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//        LocalDateTime time = LocalDateTime.now();
//        String localTime = df.format(time);
//
//        System.out.println("年月日" + localTime);
//
//        LocalDateTime now = LocalDateTime.parse("2018-12-12 12:21:22", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//        LocalDateTime ldt2 = LocalDateTime.parse("2018-12-12 12:22:22", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//
//        Duration duration = Duration.between(now, ldt2);
//
//        System.out.println(duration.getSeconds());
//    }

}
