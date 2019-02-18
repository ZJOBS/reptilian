package com.aigushou.thread.area;

import com.aigushou.constant.Constant;
import com.aigushou.constant.ThreadPoolUtil;
import com.aigushou.entity.RateEntity;
import com.aigushou.utils.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 区域爬虫
 *
 * @author jiezhang
 */
public class AreaReptilianThread implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(AreaReptilianThread.class);

    /**
     * 债券编码
     */
    private final String bondCode;

    /**
     * 账号下标（一个债的对应一个账号）
     */
    private final int index;

    /**
     * 区域X点坐标
     */
    private final int aX;

    /**
     * 拍照Y点坐标
     */
    private final int aY;

    /**
     * 区域宽度
     */
    private final int aWidth;

    /**
     * 区域高度
     */
    private final int aHeight;

    /**
     * 开始时间
     */
    private final String startTime;
    /**
     * 结束时间
     */
    private final String endTime;

    /**
     * 发送时间
     */
    private final String sendTime;

    /**
     * 路径
     */
    private final String imagePath;

    /**
     * 文件类型
     */
    private final String imageFormat;


    public AreaReptilianThread(String bondCode, int index, int aX, int aY, int awidth, int aHeight, String startTime, String endTime, String sendTime, String imagePath, String imageFormat) {
        this.bondCode = bondCode;
        this.index = index;
        this.aX = aX;
        this.aY = aY;
        aWidth = awidth;
        this.aHeight = aHeight;
        this.startTime = startTime;
        this.endTime = endTime;
        this.sendTime = sendTime;
        this.imagePath = imagePath;
        this.imageFormat = imageFormat;
    }

    @Override
    public void run() {
        SimpleDateFormat tf = new SimpleDateFormat("HHmmss");

        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

        SimpleDateFormat dtf = new SimpleDateFormat("yyyyMMddHHmmss");
        logger.info("");
        logger.info("");

        String bufferedImageMapKey = Constant.bufferedImageMapKey + index;
        Constant.reptilianStateMap.put(bufferedImageMapKey, false);
        int delay = Integer.parseInt(Constant.properties.getProperty("delay"));
        //Future future = ThreadPoolUtil.heartScheduledExecutorService.scheduleAtFixedRate(new HeartThread(bondCode, index, bufferedImageMapKey), 0, delay, TimeUnit.SECONDS);


        //当日所有日收益率
        Set<RateEntity> dayRates = new HashSet<RateEntity>(3000);

        //死循环对比
        while (true) {
            try {
                //当前时间
                Date currentDate = new Date();
                //计算休市时间
                Date currentDateTime = new Date();
                //当前日期
                String currentDateStr = df.format(currentDate);
                //当前时间
                String currentDateTimeStr = dtf.format(currentDate);
                Date startDateTime = dtf.parse(currentDateStr + startTime);
                Date endDateTime = dtf.parse(currentDateStr + endTime);
                Date sendDateTime = dtf.parse(currentDateStr + sendTime);


                boolean sendTime = false;
                if (currentDateTime.before(endDateTime) && currentDateTime.after(sendDateTime)) {
                    sendTime = true;
                } else {
                    sendTime = false;
                }
                if (currentDateTime.before(endDateTime) && currentDateTime.after(startDateTime)) {
                    //在交易时间内
                    Robot robot = new Robot();
                    BufferedImage currentImg = robot.createScreenCapture(new Rectangle(aX, aY, aWidth, aHeight));
                    if (!Constant.bufferedImageMap.containsKey(bufferedImageMapKey)) {
                        //项目启动，第一次爬到的数据
                        Constant.bufferedImageMap.put(bufferedImageMapKey, currentImg);
                        continue;
                    }
                    float sameArea = ScreenUtil.compare(ScreenUtil.getData(currentImg), ScreenUtil.getData(Constant.bufferedImageMap.get(bufferedImageMapKey)));

                    if (sameArea < 100) {
                        //大区域发生变化
                        // 路径为默认路径+年月日
                        String path = imagePath + df.format(currentDate) + "/";

                        String areaDateTime = tf.format(currentDate);
                        //区域文件名
                        String fileName = "AREA_" + tf.format(currentDate);
                        File file = new File(path);
                        if (!file.exists()) {
                            file.mkdir();
                        }

                        //截取区域图
                        ScreenUtil.screenshot(path, fileName, imageFormat, aX, aY, aWidth, aHeight);

                        //切割为7套数据 收益率&时间
                        List<BufferedImage> rate_TimeImages = ImageUtil.cutRowImage(path + fileName + "." + imageFormat, 1, 7, 7);
                        //
//                        for (int i = 0; i <rate_TimeImages.size() ; i++) {
//                            BufferedImage bi = rate_TimeImages.get(i);
//                            File outputfile = new File(path+"/saved" + i + ".png");
//                            ImageIO.write(bi, "png", outputfile);
//                        }

                        //多线程识别
                        int size = rate_TimeImages.size();
                        CountDownLatch countDownLatch = new CountDownLatch(size);

                        List<RateEntity> rateEntities = new LinkedList<RateEntity>();
                        for (int i = 0; i < size; i++) {
                            rateEntities.add(new RateEntity());
                        }

                        for (int i = 0; i < size; i++) {
                            ThreadPoolUtil.areaThreadPool.execute(new CountDownRowReptilian(countDownLatch, rate_TimeImages.get(i), rateEntities, i, path, imageFormat, areaDateTime));
                        }
                        countDownLatch.await();

                        JSONArray resultArray = new JSONArray();
                        for (int i = 0; i < rateEntities.size(); i++) {
                            if (rateEntities.get(i) == null) {
                                //爬虫异常或界面上不足rateEntities.size()个
                                continue;
                            }
                            JSONObject object = new JSONObject();
                            object.put("rate", rateEntities.get(i).getRate());
                            object.put("time", rateEntities.get(i).getDateTime());
                            object.put("bondCode", bondCode);
                            resultArray.add(object);
                        }

                        //先设置图片，再发送，防止发送失败大致一直解析把百度账号弄挂
                        logger.info("设置当前的图像");
                        Constant.bufferedImageMap.put(bufferedImageMapKey, currentImg);

                        //发送
                        int[] rst = SendUtils.sendArea(bondCode, resultArray);


                        List<RateEntity> newRates = new ArrayList<RateEntity>();
                        for (RateEntity rateEntity : rateEntities) {
                            if (!dayRates.contains(rateEntity)) {
                                newRates.add(rateEntity);
                                dayRates.add(rateEntity);
                            }
                        }
                        JSONArray newArray = JSONArray.parseArray(JSON.toJSONString(newRates));

                        //记录数据库
                        DataBaseUtils.insertArea(currentDateTimeStr, bondCode, resultArray, newArray, "1", Arrays.toString(rst));
                    } else {
                        //笔数相同，休眠100毫秒再爬，
                        TimeUnit.MILLISECONDS.sleep(100);
                    }
                } else {
                    //不在时间范围内， 休眠100毫秒再爬
                    TimeUnit.MILLISECONDS.sleep(100);
                    //清楚当日爬去的收益率
                    dayRates.clear();
                    logger.info("不在时间范围内，不需要扒");

                }
                Constant.reptilianStateMap.put(bufferedImageMapKey, true);
            } catch (Exception e) {
                logger.info("有异常", e.getMessage());
                Constant.reptilianStateMap.put(bufferedImageMapKey, false);
            }
        }
    }
}
