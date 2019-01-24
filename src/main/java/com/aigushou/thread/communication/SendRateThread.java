package com.aigushou.thread.communication;

import com.aigushou.entity.SendRate;
import com.aigushou.utils.SendUtils;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * 发送收益率
 *
 * @author jiezhang
 */
public class SendRateThread implements Runnable {
    /**
     * 发送队列
     */
    public static final ArrayBlockingQueue<SendRate> queue = new ArrayBlockingQueue<SendRate>(100);

    /**
     * 消费速度(越小越快)
     */
    private static final int consumeSpeed = 1;

    @Override
    public void run() {
        while (true) {
            try {
                SendRate sendRate = queue.take();
                SendUtils.sendRateAndTime(sendRate.getBondCode(), sendRate.getRate(), sendRate.getDateTime());
                Thread.sleep(consumeSpeed);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
