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
        String filePath = path + "/saved_" + index + "_" + "rate_time.png";
        JSONArray array;
        try {
            File rateFile = new File(filePath);
            ImageIO.write(image, "png", rateFile);

            //发送 百度识别
            String result = Check.checkFile(rateFile);
            array = Check.analysisBaiDuResult(result);
            RateEntity rateEntity = Check.analysisBaiDuPairedResult(array);

            if (rateEntity == null) {
                //使用高精度版再识别一次
                array = Check.accurate(filePath);
                rateEntity = Check.analysisBaiDuPairedResult(array);
            }
            rstList.set(index, rateEntity);
        } catch (Exception e) {
            rstList.set(index, null);
            logger.error(e.getMessage());
        } finally {
            count.countDown();
        }
    }
}
