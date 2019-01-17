package com.aigushou.thread.common;

import com.aigushou.constant.Constant;
import com.aigushou.utils.SendUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 心跳
 *
 * @author jiezhang
 */
public class HeartThread implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(HeartThread.class);
    /**
     * 债券编码
     */
    private final String bondCode;
    /**
     * 账号下标（一个债的对应一个账号）
     */
    private final int index;

    private final String bufferedImageMapKey;

    public HeartThread(String bondCode, int index, String bufferedImageMapKey) {
        this.bondCode = bondCode;
        this.index = index;
        this.bufferedImageMapKey = bufferedImageMapKey;
    }

    @Override
    public void run() {
        String[] strs = Constant.send_heart_environment;
        try {
            logger.info("【{}】状态 【{}】",bondCode,Constant.reptilianStateMap.get(bufferedImageMapKey));
            //System.out.println(bondCode + "状态:" + Constant.reptilianStateMap.get(bufferedImageMapKey));
            if (Constant.reptilianStateMap.containsKey(bufferedImageMapKey) &&
                    Constant.reptilianStateMap.get(bufferedImageMapKey)) {
                SendUtils.sendHeart(bondCode, "0", "0");
            }
        } catch (Exception e) {
            logger.error("发送异常");
            //System.out.println("发送异常");
        }

    }
}
