package com.aigushou;

import com.aigushou.constant.Constant;
import com.aigushou.thread.area.AreaReptilianThread;
import com.aigushou.thread.area.CountDownReptilian;
import com.aigushou.thread.immediate.LongReptilianThread;
import com.aigushou.thread.time.ReptilianThread;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;


/**
 * Hello world!
 *
 * @author jiezhang
 */
public class App {
    private static Logger logger = LoggerFactory.getLogger(App.class);
    /**
     * 启动一个线程池
     */
    private final static ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();


    public static void main(String[] args) throws Exception {
        // 记录info级别的信息
        logger.info("开始执行爬虫程序");
        //爬虫 爬债券的数量
        int reptilianNum = Integer.parseInt(Constant.properties.getProperty("reptilian_num"));
        //每个多少秒一次
        int delay = Integer.parseInt(Constant.properties.getProperty("delay"));
        //开始时间
        String startTime = Constant.properties.getProperty("startTime");
        //结束时间
        String endTime = Constant.properties.getProperty("endTime");

        String sendTime = Constant.properties.getProperty("sendTime");
        //1:每隔一秒爬，2:无限对比
        String reptilianType = Constant.properties.getProperty("reptilian_type");

        //保存图片的格式
        String imageFormat = Constant.properties.getProperty("image_format");
        if ("1".equals(reptilianType)) {
            //图片格式
            for (int i = 0; i < reptilianNum; i++) {
                int x = Integer.parseInt(Constant.properties.getProperty("rateX" + i));
                int y = Integer.parseInt(Constant.properties.getProperty("rateY" + i));
                int width = Integer.parseInt(Constant.properties.getProperty("rateWidth" + i));
                int height = Integer.parseInt(Constant.properties.getProperty("rateHeight" + i));
                int tNX = Integer.parseInt(Constant.properties.getProperty("tNX" + i));
                int tNY = Integer.parseInt(Constant.properties.getProperty("tNY" + i));
                int tNWidth = Integer.parseInt(Constant.properties.getProperty("tNWidth" + i));
                int tNHeight = Integer.parseInt(Constant.properties.getProperty("tNHeight" + i));
                String imagePath = Constant.properties.getProperty("path" + i);
                String bondCode = Constant.properties.getProperty("bandCode" + i);
                Future future = ses.scheduleAtFixedRate(new ReptilianThread(x, y, width, height, tNX, tNY, tNWidth, tNHeight, imagePath, imageFormat, startTime, endTime, bondCode, i), 0, delay, TimeUnit.SECONDS);
            }
        }
        if ("2".equals(reptilianType)) {
            for (int i = 0; i < reptilianNum; i++) {
                int x = Integer.parseInt(Constant.properties.getProperty("rateX" + i));
                int y = Integer.parseInt(Constant.properties.getProperty("rateY" + i));
                int width = Integer.parseInt(Constant.properties.getProperty("rateWidth" + i));
                int height = Integer.parseInt(Constant.properties.getProperty("rateHeight" + i));
                int tNX = Integer.parseInt(Constant.properties.getProperty("tNX" + i));
                int tNY = Integer.parseInt(Constant.properties.getProperty("tNY" + i));
                int tNWidth = Integer.parseInt(Constant.properties.getProperty("tNWidth" + i));
                int tNHeight = Integer.parseInt(Constant.properties.getProperty("tNHeight" + i));
                String imagePath = Constant.properties.getProperty("path" + i);
                String bondCode = Constant.properties.getProperty("bandCode" + i);
                Thread thread = new Thread(new LongReptilianThread(x, y, width, height, tNX, tNY, tNWidth, tNHeight, imagePath, imageFormat, startTime, endTime, sendTime, bondCode, i));
                thread.start();
            }
        }
        if ("3".equals(reptilianType)) {
            for (int i = 0; i < reptilianNum; i++) {
                int aX = Integer.parseInt(Constant.properties.getProperty("aX" + i));
                int aY = Integer.parseInt(Constant.properties.getProperty("aY" + i));
                int aWidth = Integer.parseInt(Constant.properties.getProperty("aWidth" + i));
                int aHeight = Integer.parseInt(Constant.properties.getProperty("aHeight" + i));
                String imagePath = Constant.properties.getProperty("path" + i);
                String bondCode = Constant.properties.getProperty("bandCode" + i);
                Thread thread = new Thread(new AreaReptilianThread(bondCode, i, aX, aY, aWidth, aHeight, startTime, endTime, sendTime, imagePath, imageFormat));
                thread.start();
            }
        }

        logger.info("爬虫全部启用");
    }

    /**
     * 测试代码
     * @param args
     */
    public static void main1(String[] args) {

        for (int i = 0; i < 100; i++) {
            try {
                JSONObject object = new JSONObject();
                object.put("p1" + i, "参数1" + i);
                Constant.rabbitMQPublish("ZJOBSQUEUE", object);
            } catch (Exception e) {

            }
        }

        Constant.rabbitMQConsumer("ZJOBSQUEUE");

    }
}
