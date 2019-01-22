package com.aigushou.thread.temporary;

import com.aigushou.GuiCamera;
import com.aigushou.constant.Constant;
import com.aigushou.thread.immediate.LongReptilianThread;
import com.aigushou.utils.Check;
import com.aigushou.utils.DataBaseUtils;
import com.aigushou.utils.SendUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 统计 笔数和收益率时间差线程的代码（无用了）
 *
 * @author jiezhang
 */
public class TimeDifference implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(LongReptilianThread.class);

    /**
     * 拍照X轴
     */
    private final int rateX;

    /**
     * 拍照Y轴
     */
    private final int rateY;
    /**
     * 拍照宽度
     */
    private final int rateWidth;

    /**
     * 拍照宽度
     */
    private final int rateHeight;

    /**
     * 路径
     */
    private final String imagePath;

    /**
     * 文件类型
     */
    private final String imageFormat;
    /**
     * 开始时间
     */
    private final String startTime;
    /**
     * 结束时间
     */
    private final String endTime;

    private final String sendTime;
    /**
     * 债券编码
     */
    private final String bondCode;
    /**
     * 账号下标（一个债的对应一个账号）
     */
    private final int index;
    /**
     * 交易笔数X轴
     */
    private final int tNX;
    /**
     * 交易笔数 Y轴
     */
    private final int tNY;
    /**
     * 交易笔数 宽
     */
    private final int tNWidth;
    /**
     * 交易笔数长
     */
    private final int tNHeight;

    public TimeDifference(int rateX, int rateY, int rateWidth, int rateHeight, int tNX, int tNY, int tNWidth, int tNHeight, String imagePath, String imageFormat, String startTime, String endTime, String sendTime, String bondCode, int index) {
        this.rateX = rateX;
        this.rateY = rateY;
        this.rateWidth = rateWidth;
        this.rateHeight = rateHeight;
        this.tNX = tNX;
        this.tNY = tNY;
        this.tNWidth = tNWidth;
        this.tNHeight = tNHeight;
        this.imagePath = imagePath;
        this.imageFormat = imageFormat;
        this.startTime = startTime;
        this.endTime = endTime;
        this.sendTime = sendTime;
        this.bondCode = bondCode;
        this.index = index;
    }

    @Override
    public void run() {

        SimpleDateFormat tf = new SimpleDateFormat("HHmmss");

        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

        SimpleDateFormat dtf = new SimpleDateFormat("yyyyMMddHHmmss");
        logger.info("");
        logger.info("");
        logger.info("");
        logger.info("");

        String bufferedImageMapKey = Constant.bufferedImageMapKey + index;
        Constant.reptilianStateMap.put(bufferedImageMapKey, false);
        int delay = Integer.parseInt(Constant.properties.getProperty("delay"));
//        Future future = ThreadPoolUtil.heartScheduledExecutorService.scheduleAtFixedRate(new HeartThread(bondCode, index, bufferedImageMapKey), 0, delay, TimeUnit.SECONDS);

        //死循环对比
        while (true) {
            try {
                //当前时间
                String rate;
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
                    BufferedImage currentImg = robot.createScreenCapture(new Rectangle(tNX, tNY, tNWidth, tNHeight));
                    if (!Constant.bufferedImageMap.containsKey(bufferedImageMapKey)) {
                        //项目启动，第一次爬到的数据
                        Constant.bufferedImageMap.put(bufferedImageMapKey, currentImg);
                        continue;
                    }
                    float sameRate = compare(getData(currentImg), getData(Constant.bufferedImageMap.get(bufferedImageMapKey)));

                    if (sameRate < 100) {
                        //像素有变化
                        logger.info("相似度：【{}】%", sameRate);

                        logger.info("开始爬数据");
                        // 路径为默认路径+年月日
                        String path = imagePath + df.format(currentDate) + "/";
                        //收益率文件名
                        String fileName = "ER_" + tf.format(currentDate);
                        //交易笔数文件名
                        String fileNameTN = "TN_" + tf.format(currentDate);
                        File file = new File(path);
                        if (!file.exists()) {
                            file.mkdir();
                        }

                        logger.info("文件路径");
                        String currentTransactionPenNumber = recognition(path, fileNameTN, tNX, tNY, tNWidth, tNHeight, 0);
                        logger.info("当前爬到笔数数【{}】", currentTransactionPenNumber);

                        String tn = Constant.tnList.get(index);

                        logger.info("当前爬到笔数数:" + currentTransactionPenNumber);
                        if (currentTransactionPenNumber.equals("") || currentTransactionPenNumber.equals("0")) {
                            //当前的笔数
                            logger.info("设置当前的笔数 0");
                            Constant.tnList.set(index, "0");
                            //当前的的图像
                            logger.info("设置当前的图像 0图像");
                            Constant.bufferedImageMap.put(bufferedImageMapKey, currentImg);
                            logger.info("没有新的成交量");

                            //笔数为0，休眠100毫秒后再爬
                            TimeUnit.MILLISECONDS.sleep(100);
                            continue;
                        } else {
                            //休眠200毫秒后，抓取收益率
                            //TimeUnit.MILLISECONDS.sleep(400);

                            robot = new Robot();
                            BufferedImage currentRateImg1 = robot.createScreenCapture(new Rectangle(rateX, rateY, rateWidth, rateHeight));
                            long t1 = System.currentTimeMillis();
                            while (true) {
                                robot = new Robot();
                                BufferedImage currentRateImg2 = robot.createScreenCapture(new Rectangle(rateX, rateY, rateWidth, rateHeight));
                                long t2 = System.currentTimeMillis();
                                //超过500毫秒没变，收益率可能保持一样
                                if (t2 - t1 > 500) {
                                    break;
                                }
                                float sameRate2 = compare(getData(currentRateImg2), getData(currentRateImg1));
                                if (sameRate2 < 100) {
                                    logger.info("收益率变化了");
                                    break;
                                }
                            }

                            //将历史数据改为,只用一个账户
                            rate = recognition(path, fileName, rateX, rateY, rateWidth, rateHeight, 0);
                            logger.info("当前抓取的收率为" + rate);

                            try {
                                Double dCheckValue = Double.parseDouble(rate);
                            } catch (NumberFormatException e) {
                                //收益率不是double
                                logger.error("债券:【{}】 收益率解析不是double类型 ， 笔数:【{}】， 截图地址:【{}】 ", bondCode, currentTransactionPenNumber, path + fileName);
                                continue;
                            }

                            Integer transactionPenInt = Integer.parseInt(currentTransactionPenNumber);
                            Integer tnInt = Integer.parseInt(tn);

                            //默认需要发送
                            boolean needSend = true;
                            if (transactionPenInt > tnInt) {
                                //当前笔数大于 之前一次爬到的笔数
                                needSend = true;
                                sendAndSave(needSend, 1, sendTime, bondCode, rate, currentTransactionPenNumber, currentDateTimeStr);
                            } else if (transactionPenInt.equals(tnInt)) {
                                //收益率被撤回或无变动，保存数据库且不发送
                                //两笔相等时
                                needSend = true;
                                sendAndSave(needSend, 1, sendTime, bondCode, rate, currentTransactionPenNumber, currentDateTimeStr);
                            } else if (transactionPenInt < tnInt && (tnInt - transactionPenInt) > 50) {
                                needSend = false;
                                //记录下不正常数据
                                sendAndSave(needSend, 1, sendTime, bondCode, rate, currentTransactionPenNumber, currentDateTimeStr);
                            } else {
                                //回撤
                                needSend = true;
                                sendAndSave(needSend, 1, sendTime, bondCode, rate, currentTransactionPenNumber, currentDateTimeStr);
                            }
                            //当前的笔数
                            logger.info("设置当前的笔数");
                            Constant.tnList.set(index, currentTransactionPenNumber);
                            //当前的的图像
                            logger.info("设置当前的图像");
                            Constant.bufferedImageMap.put(bufferedImageMapKey, currentImg);
                        }
                    } else {
                        //笔数相同，休眠100毫秒再爬，
                        TimeUnit.MILLISECONDS.sleep(100);
                    }
                } else {
                    //不在时间范围内， 休眠100毫秒再爬
                    TimeUnit.MILLISECONDS.sleep(100);
                    logger.info("不在时间范围内，不需要扒");
                }
                Constant.reptilianStateMap.put(bufferedImageMapKey, true);
            } catch (Exception e) {
                logger.info("有异常", e.getStackTrace());
                Constant.reptilianStateMap.put(bufferedImageMapKey, false);
            }
        }
    }

    /**
     * 发送和存库
     *
     * @param needSend                    是否需要发送，true 为发送到收益率接口，false时 发送到回撤接口
     * @param sendTime                    是否发送时间之内，false时，
     * @param type                        1:收益率 2:回撤
     * @param bondCode                    债券code
     * @param rate                        收益率
     * @param currentTransactionPenNumber 当前笔数
     * @param currentDateTimeStr          当前时间
     */
    private void sendAndSave(boolean needSend, int type, boolean sendTime, String bondCode, String rate, String currentTransactionPenNumber, String currentDateTimeStr) {
        int sendRst[];
        needSend = false;
        //需要发送并且在发送时间段内
        String sendTag = (needSend && sendTime) ? "1" : "0";
        if (needSend && sendTime) {
            switch (type) {
                case 1: {
                    logger.info("发送收益率 债券【{}】,收益率【{}】, 笔数【{}】", bondCode, rate, currentTransactionPenNumber);
                    sendRst = SendUtils.sendRate(bondCode, rate, currentTransactionPenNumber);
                    break;
                }
                case 2: {
                    logger.info("发送回撤 债券【" + bondCode + "】 , 收益率【" + rate + "】 笔数:【" + currentTransactionPenNumber + "】");
                    sendRst = SendUtils.sendWithdrawalRate(bondCode, rate, currentTransactionPenNumber);
                    break;
                }
                default:
                    //未发送
                    sendRst = new int[Constant.send_environment.length];
                    for (int i = 0; i < Constant.send_environment.length; i++) {
                        sendRst[i] = 0;
                    }
                    break;
            }
        } else if (sendTime) {
            sendRst = new int[Constant.send_environment.length];
            for (int i = 0; i < Constant.send_environment.length; i++) {
                sendRst[i] = 0;
            }
            //logger.info("发送回撤 债券【" + bondCode + "】 , 收益率【" + rate + "】 笔数:【" + currentTransactionPenNumber + "】");
            //SendUtils.sendWithdrawalRate(bondCode, rate, currentTransactionPenNumber);
        } else {
            sendRst = new int[Constant.send_environment.length];
            for (int i = 0; i < Constant.send_environment.length; i++) {
                sendRst[i] = 0;
            }
        }
        //存库
        logger.info("存库 债券【" + bondCode + "】 , 收益率【" + rate + "】 笔数:【" + currentTransactionPenNumber + "】");
        DataBaseUtils.insert(currentDateTimeStr, rate, bondCode, currentTransactionPenNumber, sendTag, Arrays.toString(sendRst));
    }


    /**
     * 识别文字
     *
     * @param path
     * @param fileName
     * @param x
     * @param y
     * @param width
     * @param height
     * @param baiDuNumIndex 百度账号
     * @return
     */
    private String recognition(String path, String fileName, int x, int y, int width, int height, int baiDuNumIndex) {
        GuiCamera cam = new GuiCamera(imageFormat);
        //汇率图
        File imageFile = cam.snapshot(path, fileName, x, y, width, height);
        //识别交易笔数
        logger.info("准备进入checkFile" + imageFile.getName());
        return Check.checkFile(imageFile, baiDuNumIndex);
    }


    private int[] getData(BufferedImage img) {
        try {
            BufferedImage slt = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            slt.getGraphics().drawImage(img, 0, 0, 100, 100, null);
            int[] data = new int[256];
            for (int x = 0; x < slt.getWidth(); x++) {
                for (int y = 0; y < slt.getHeight(); y++) {
                    int rgb = slt.getRGB(x, y);
                    Color myColor = new Color(rgb);
                    int r = myColor.getRed();
                    int g = myColor.getGreen();
                    int b = myColor.getBlue();
                    data[(r + g + b) / 3]++;
                }
            }
            // data 就是所谓图形学当中的直方图的概念
            return data;
        } catch (Exception exception) {
            logger.error("有文件没有找到,请检查文件是否存在或路径是否正确");
            return null;
        }
    }

    private float compare(int[] s, int[] t) {
        try {
            float result = 0F;
            for (int i = 0; i < 256; i++) {
                int abs = Math.abs(s[i] - t[i]);
                int max = Math.max(s[i], t[i]);
                result += (1 - ((float) abs / (max == 0 ? 1 : max)));
            }
            return (result / 256) * 100;
        } catch (Exception exception) {
            return 0;
        }
    }
}
