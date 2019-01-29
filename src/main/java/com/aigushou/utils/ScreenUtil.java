package com.aigushou.utils;

import com.aigushou.GuiCamera;
import com.aigushou.entity.Area;
import com.aigushou.entity.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * 屏幕工具
 *
 * @author jiezhang
 */
public class ScreenUtil {

    private static Logger logger = LoggerFactory.getLogger(ScreenUtil.class);

    public static int[] getData(BufferedImage img) {
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
            return data;
        } catch (Exception exception) {
            logger.error("有文件没有找到,请检查文件是否存在或路径是否正确");
            return null;
        }
    }

    public static float compare(int[] s, int[] t) {
        try {
            float result = 0F;
            for (int i = 0; i < 256; i++) {
                int abs = Math.abs(s[i] - t[i]);
                int max = Math.max(s[i], t[i]);
                result += (1 - ((float) abs / (max == 0 ? 1 : max)));
            }
            return (result / 256) * 100;
        } catch (Exception e) {
            logger.error("对比错误，返回0。【{}】", e.getMessage());
            return 0;
        }
    }


    /**
     * 获取坐标列表（7个位置）
     *
     * @param area     第一个位置
     * @param lineHigh 行高
     * @return
     */
    public static List<Area> position(Area area, int lineHigh) {

        List<Area> areas = new LinkedList<Area>();
        //添加第一个
        areas.add(area);

        Node rateNode, timeNode;
        for (int i = 0; i < 6; i++) {
            area = new Area();
            rateNode = new Node(area.getRateNode().getX(), area.getRateNode().getY() + lineHigh, area.getRateNode().getWidth(), area.getRateNode().getHeight());
            timeNode = new Node(area.getTimeNode().getX(), area.getTimeNode().getY() + lineHigh, area.getTimeNode().getWidth(), area.getTimeNode().getHeight());
            area.setRateNode(rateNode);
            area.setTimeNode(timeNode);
            areas.add(area);
        }
        return areas;
    }


    public static File screenshot(String path, String fileName, String imageFormat, int x, int y, int width, int height) {
        GuiCamera cam = new GuiCamera(imageFormat);
        File imageFile = cam.snapshot(path, fileName, x, y, width, height);
        return imageFile;
    }


}
