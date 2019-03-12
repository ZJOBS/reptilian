package com.aigushou.utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;

/**
 * 时间工具
 *
 * @author jiezhang
 */
public class DateTimeUtils {

    /**
     * 线程安全的
     */
    public static final DateTimeFormatter DTF_YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    /**
     * 线程安全的
     */
    public static final DateTimeFormatter DTF_HH_MM_SS = DateTimeFormatter.ofPattern("HH:mm:ss");
    /**
     * 线程安全的
     */
    public static final DateTimeFormatter DTF_YYYY_MM_DD_HH_MM_SS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


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


    /**
     * 判断时间是否在区间之内
     *
     * @param dateTime
     * @param startDateTime
     * @param endDateTime
     * @return
     */
    public static boolean isWithin(LocalDateTime dateTime, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        boolean isWithin = false;
        if (dateTime.isAfter(startDateTime) && dateTime.isBefore(endDateTime)) {
            isWithin = true;
        }
        return isWithin;
    }


    public static void main(String[] args) {

        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime time = LocalDateTime.now();
        String localTime = df.format(time);

        System.out.println("年月日" + localTime);

        LocalDateTime ldt1 = LocalDateTime.parse("2018-12-12 12:21:22", DTF_YYYY_MM_DD_HH_MM_SS);
        LocalDateTime ldt2 = LocalDateTime.parse("2018-12-12 12:22:22", DTF_YYYY_MM_DD_HH_MM_SS);
        LocalDateTime ldt3 = LocalDateTime.parse("2018-12-12 12:23:22", DTF_YYYY_MM_DD_HH_MM_SS);

        Duration duration = Duration.between(ldt1, ldt2);
        System.out.println(duration.getSeconds());

        System.out.println(isWithin(ldt2, ldt1, ldt3));

    }

}
