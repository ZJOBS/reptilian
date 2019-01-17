package com.aigushou.thread.common;


import com.aigushou.GuiCamera;
import com.aigushou.constant.Constant;
import com.aigushou.utils.Check;
import com.aigushou.utils.SendUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 位置校验（未完成）
 * @author jiezhang
 */
public class PositionThread implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(PositionThread.class);
    /**
     * 路径
     */
    private final String imagePath;

    /**
     * 文件类型
     */
    private final String imageFormat;

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

    public PositionThread(int tNX, int tNY, int tNWidth, int tNHeight, String imagePath, String imageFormat, String bondCode, int index) {
        this.tNX = tNX;
        this.tNY = tNY;
        this.tNWidth = tNWidth;
        this.tNHeight = tNHeight;
        this.imagePath = imagePath;
        this.imageFormat = imageFormat;
        this.bondCode = bondCode;
        this.index = index;
    }

    @Override
    public void run() {

        String positionStateMapKey = Constant.positionStateMapKey + index;

        //默认有错误
        Constant.positionStateMap.put(positionStateMapKey, 2);

        SimpleDateFormat tf = new SimpleDateFormat("HHmmss");

        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

        Date currentDate = new Date();

        String path = imagePath + df.format(currentDate) + "/";
        //收益率文件名
        String fileName = "ER_" + tf.format(currentDate);
        //交易笔数文件名
        String fileNameTN = "TN_" + tf.format(currentDate);
        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }
        String currentTransactionPenNumber = recognition(path, fileNameTN, tNX, tNY, tNWidth, tNHeight, 0);
        int frequency = Constant.positionStateMap.get(positionStateMapKey);
        try {
            Double.parseDouble(currentTransactionPenNumber);
            if (frequency > 0) {
                logger.info("【{}】位置已修正", bondCode);
                Constant.positionStateMap.put(positionStateMapKey, 0);
            }
        } catch (Exception e) {
            frequency = frequency + 1;
            if (frequency <= 1) {
                //只发1次
                logger.info("【{}】位置有问题", bondCode);
                SendUtils.sendError(bondCode);
            }
        }
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
        return Check.checkFile(imageFile, baiDuNumIndex);
    }
}
