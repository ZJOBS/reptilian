package com.aigushou.utils;

import com.aigushou.entity.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 图片工具
 *
 * @author jiezhang
 */
public class ImageUtil {
    private static Logger logger = LoggerFactory.getLogger(ImageUtil.class);

    /**
     * 将一张本地图片转化成Base64字符串
     *
     * @param file 本地图片地址
     * @return 图片转化base64后再UrlEncode结果
     */
    public static String getImageStrFromPath(File file) throws Exception {
        InputStream in;
        byte[] data = null;
        // 读取图片字节数组
        try {
            in = new FileInputStream(file);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 对字节数组Base64编码
        BASE64Encoder encoder = new BASE64Encoder();
        // 返回Base64编码过再URLEncode的字节数组字符串
        return URLEncoder.encode(encoder.encode(data));
    }


    /**
     * 从上直下按顺序切割图片
     *
     * @param fileUrl
     * @param rows
     * @param cols
     * @param nums
     * @return
     */
    public static LinkedList<BufferedImage> cutRowImage(String fileUrl, int cols, int rows, int nums) {
        LinkedList<BufferedImage> list = new LinkedList<BufferedImage>();
        try {
            BufferedImage img = ImageIO.read(new File(fileUrl));
            int lw = img.getWidth() / cols;
            int lh = img.getHeight() / rows;
            for (int i = 0; i < nums; i++) {
                BufferedImage buffImg = img.getSubimage(i % cols * lw, i / cols * lh + 1, lw, lh + 1);
                list.add(buffImg);
            }
            return list;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }


    /**
     * 按Nodes 给出的位置 位置切割
     *
     * @param img
     * @param nodes
     * @return
     */
    public static LinkedList<BufferedImage> cutImage(BufferedImage img, List<Node> nodes) {
        LinkedList<BufferedImage> list = new LinkedList<BufferedImage>();
        for (int i = 0; i < nodes.size(); i++) {
            BufferedImage buffImg = img.getSubimage(nodes.get(i).getX(), nodes.get(i).getY(), nodes.get(i).getWidth(), nodes.get(i).getHeight());
            list.add(buffImg);
        }


        return list;
    }


    /**
     * 测试,截图位置有偏差，需要通过百度识别去测试
     *
     * @param args
     */
    public static void main(String[] args) {
//        String url = "/Users/jiezhang/Downloads/cutImage/jietu.png";
//        List<BufferedImage> list = cutRowImage(url, 1, 7, 7);
//        for (int i = 0; i < list.size(); i++) {
//            try {
//                BufferedImage bi = list.get(i);
//                File outputfile = new File("/Users/jiezhang/Downloads/cutImage/saved" + i + ".png");
//                ImageIO.write(bi, "png", outputfile);
//            } catch (Exception e) {
//
//            }
//
//        }


//        List<Node> nodes = new LinkedList<Node>();
//
//        nodes.add(new Node(180, 0, 80, 25));
//        nodes.add(new Node(437, 0, 70, 25));
//        try {
//            String url = "/Users/jiezhang/Downloads/cutImage/saved0+.png";
//            BufferedImage img = ImageIO.read(new File(url));
//            List<BufferedImage> list = cutImage(img, nodes);
//            for (int i = 0; i < list.size(); i++) {
//                BufferedImage bi = list.get(i);
//                File outputfile = new File("/Users/jiezhang/Downloads/cutImage/jianqie" + i + ".png");
//                ImageIO.write(bi, "png", outputfile);
//            }
//        } catch (Exception e) {
//
//        }


        List<Node> nodes = new LinkedList<Node>();
        nodes.add(new Node(180, 0, 80, 25));
        nodes.add(new Node(437, 0, 70, 25));

        String url = "/Users/jiezhang/Downloads/cutImage/jietu.png";
        List<BufferedImage> list = cutRowImage(url, 1, 7, 7);
        for (int i = 0; i < list.size(); i++) {
            try {
                BufferedImage bi = list.get(i);
                List<BufferedImage> rateTime = cutImage(bi, nodes);
                for (int j = 0; j < rateTime.size(); j++) {
                    BufferedImage bi2 = rateTime.get(j);
                    File outputfile = new File("/Users/jiezhang/Downloads/cutImage/jianqie_" + i + "_" + j + ".png");
                    ImageIO.write(bi2, "png", outputfile);
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
            }

        }

    }


}
