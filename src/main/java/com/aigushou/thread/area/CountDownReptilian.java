package com.aigushou.thread.area;

import com.aigushou.entity.Node;
import com.aigushou.entity.RateEntity;
import com.aigushou.utils.Check;
import com.aigushou.utils.ImageUtil;
import com.aigushou.utils.ScreenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 多爬虫功能
 *
 * @author jiezhang
 */
public class CountDownReptilian implements Runnable {

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
     * 收益率坐标点
     */
    private final Node rateNode;

    /**
     * 时间坐标点
     */
    private final Node timeNode;

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


    public CountDownReptilian(CountDownLatch count, BufferedImage image, Node rateNode, Node timeNode, List rstList, int index, String path, String imageFormat) {
        this.count = count;
        this.image = image;
        this.rateNode = rateNode;
        this.timeNode = timeNode;
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
            //切割为 收益率和时间

            List<Node> nodes = new ArrayList<Node>();
            nodes.add(rateNode);
            nodes.add(timeNode);

            //写文件
            List<BufferedImage> images = ImageUtil.cutImage(image, nodes);

            BufferedImage b0 = images.get(0);
            File rateFile = new File(path + "/saved_" + index + "_" + "rate.png");
            ImageIO.write(b0, "png", rateFile);

            BufferedImage b1 = images.get(1);
            File rateTimeFile = new File(path + "/saved_" + index + "_" + "time.png");
            ImageIO.write(b1, "png", rateTimeFile);

            //收益率
            // File rateFile = ScreenUtil.screenshot(path, "rate" + dateTimeStr, imageFormat, nodes.get(0).getX(), nodes.get(0).getY(), nodes.get(0).getWidth(), nodes.get(0).getHeight());


            //收益率对应时间
            //  File rateTimeFile = ScreenUtil.screenshot(path, "rate" + dateTimeStr, imageFormat, nodes.get(1).getX(), nodes.get(1).getY(), nodes.get(1).getWidth(), nodes.get(1).getHeight());


            //发送 百度识别
            String rateStr = Check.checkFile(rateFile);
            String rateTimeStr = Check.checkFile(rateTimeFile);

            //判断是否为时间格式 和 收益率是否为Double
            try {
                Double dCheckValue = Double.parseDouble(rateStr);
                LocalDateTime ldt = LocalDateTime.parse("2018-12-12 " + rateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (Exception e) {
                logger.error("时间格式、收益率识别失败。时间为【{}】,收益率为【{}】", rateTimeStr, rateStr);
                rstList.add(index, null);
                return;
            }
            rstList.add(index, new RateEntity(rateStr, rateTimeStr));
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            count.countDown();
        }
    }
}
