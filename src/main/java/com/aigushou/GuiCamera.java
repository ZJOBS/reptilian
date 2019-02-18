package com.aigushou;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * @author jiezhang
 */
public class GuiCamera {
    private static Logger logger = LoggerFactory.getLogger(GuiCamera.class);
    /**
     * 图像文件的格式
     */
    private String imageFormat;
//    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

    public GuiCamera(String format) {
        imageFormat = format;
    }


    /**
     * @param path     硬盘位置
     * @param fileName 保存文件名称
     * @param x        拍照X轴
     * @param y        拍照Y轴
     * @param width    拍照宽度
     * @param height   拍照高度
     * @return
     */
    public File snapshot(String path, String fileName, int x, int y, int width, int height) {
        logger.info("开始截图----");
        BufferedImage screenshot = null;
        File f = null;
        try {
            //拷贝屏幕到一个BufferedImage对象screenshot
            screenshot = (new Robot()).createScreenCapture(new Rectangle(x, y, width, height));
            //根据文件前缀变量和文件格式变量，自动生成文件名
            String name = path + fileName + "." + imageFormat;
            f = new File(name);
            //将screenshot对象写入图像文件
            ImageIO.write(screenshot, imageFormat, f);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        logger.info("保存图片完成---");
        return f;
    }


}
