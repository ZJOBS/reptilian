package com.aigushou.constant;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 线程池
 */
public class ThreadPoolUtil {

    /**
     *
     */
    public static final ExecutorService fixedThreadPool = Executors.newSingleThreadExecutor();


    /**
     * 心跳线程池
     */
    public final static ScheduledExecutorService heartScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();


    /**
     * 区域识别线程池
     */
    public static final ExecutorService areaThreadPool = Executors.newFixedThreadPool(32);
}
