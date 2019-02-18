package com.aigushou.thread.area;

import com.aigushou.entity.Node;
import com.aigushou.entity.RateEntity;
import com.aigushou.utils.Check;
import com.aigushou.utils.ImageUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 一行行识别
 *
 * @author jiezhang
 */
public class CountDownRowReptilian implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(CountDownReptilian.class);
    /**
     *
     */
    private final CountDownLatch count;
    /**
     * 图片
     */
    private final BufferedImage image;


    /**
     * 返回结果集
     */
    private final List rstList;

    /**
     * 结果集下标
     */
    private final int index;

    /**
     * 图片路径
     */
    private final String path;

    /**
     * 图片格式
     */
    private final String imageFormat;

    public CountDownRowReptilian(CountDownLatch count, BufferedImage image, List rstList, int index, String path, String imageFormat) {
        this.count = count;
        this.image = image;
        this.rstList = rstList;
        this.index = index;
        this.path = path;
        this.imageFormat = imageFormat;
    }

    @Override
    public void run() {
        LocalDateTime localDateTime = LocalDateTime.now();
        String dateTimeStr = localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        try {

            String filePath = path + "/saved_" + index + "_" + "rate_time.png";
            File rateFile = new File(filePath);
            ImageIO.write(image, "png", rateFile);

            //发送 百度识别
            String result = Check.checkFile(rateFile);
            JSONArray array = Check.analysisBaiDuResult(result);
            if (array.size() == 2) {
                RateEntity rateEntity = Check.analysisBaiDuPairedResult(array);
                rstList.set(index, rateEntity);
            } else {
                //使用高精度版再识别一次
                array = Check.accurate(filePath);
                if (array.size() == 2) {
                    RateEntity rateEntity = Check.analysisBaiDuPairedResult(array);
                    rstList.set(index, rateEntity);
                } else {
                    //普通识别和高精度识别都有问题，识别失败
                    logger.error("识别到的数据为{}", array);
                    throw new Exception("识别数据未成对");
                }
            }
        } catch (Exception e) {
            //防止list长度不一致，若异常，则填入null
            rstList.set(index, null);
            logger.error(e.getMessage());
        } finally {
            count.countDown();
        }
    }
}
