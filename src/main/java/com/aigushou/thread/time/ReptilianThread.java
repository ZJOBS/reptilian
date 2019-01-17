package com.aigushou.thread.time;

import com.aigushou.constant.Constant;
import com.aigushou.GuiCamera;
import com.aigushou.utils.Check;
import com.aigushou.utils.SendUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 每隔一段时间爬虫线程
 *
 * @author jiezhang
 */
public class ReptilianThread implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(ReptilianThread.class);

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

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

    public ReptilianThread(int rateX, int rateY, int rateWidth, int rateHeight, int tNX, int tNY, int tNWidth, int tNHeight, String imagePath, String imageFormat, String startTime, String endTime, String bondCode, int index) {
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
        this.bondCode = bondCode;
        this.index = index;
    }

    @Override
    public void run() {


        logger.info("");
        logger.info("");
        logger.info("");
        logger.info("");


        SimpleDateFormat tf = new SimpleDateFormat("HHmmss");

        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

        SimpleDateFormat dtf = new SimpleDateFormat("yyyyMMddHHmmss");

        //当前时间
        Date currentDate = new Date();
        //计算休市时间
        Date currentDateTime = new Date();
        //当前日期
        String currentDateStr = df.format(currentDate);
        //当前时间
        String currentDateTimeStr = dtf.format(currentDate);
        String rate;
        try {
            //开始日期
            Date startDateTime = dtf.parse(currentDateStr + startTime);
            Date endDateTime = dtf.parse(currentDateStr + endTime);
            System.out.println("当前时间：" + currentDateTime);
            System.out.println("开始时间：" + startDateTime);
            System.out.println("结束时间：" + endDateTime);
            if (currentDateTime.before(endDateTime) && currentDateTime.after(startDateTime)) {
                System.out.println("开始爬数据");
                // 路径为默认路径+年月日
                String path = imagePath + df.format(currentDate) + "/";
                // 文件名为时分秒
                //收益率文件名
                String fileName = "ER_" + tf.format(currentDate);

                //交易笔数文件名
                String fileNameTN = "TN_" + tf.format(currentDate);
                File file = new File(path);
                if (!file.exists()) {
                    file.mkdir();
                }

                logger.info("文件路径【{]】", path);
                //System.out.println("文件路径" + path);
                String transactionPenNumber = recognition(path, fileNameTN, tNX, tNY, tNWidth, tNHeight);
                String tn = Constant.tnList.get(index);

                if (transactionPenNumber.equals("") || transactionPenNumber.equals("0")) {
                    logger.info("没有新的成交量");
                    //System.out.println("没有新的成交量");
                    return;
                } else {
                    //将历史数据改为

                    //休眠500毫秒再爬，
                    TimeUnit.MILLISECONDS.sleep(500);

                    Constant.tnList.set(index, transactionPenNumber);

                    rate = recognition(path, fileName, rateX, rateY, rateWidth, rateHeight);

                    Integer transactionPenInt = Integer.parseInt(transactionPenNumber);
                    Integer tnInt = Integer.parseInt(tn);

                    if (transactionPenInt >= tnInt) {
                        //发送交易笔数
                        int sendRst[] = SendUtils.sendRate(bondCode, rate, transactionPenNumber);
                        //存库
                        insert(currentDateTimeStr, rate, bondCode, transactionPenNumber, "1", Arrays.toString(sendRst));
                    } else {
                        int[] sendRst = new int[Constant.send_environment.length];
                        for (int i = 0; i < Constant.send_environment.length; i++) {
                            sendRst[i] = 0;
                        }
                        //小于的时候，缓存中刷为小的transactionPenNumber
                        Constant.tnList.set(index, transactionPenNumber);
                        //存库
                        insert(currentDateTimeStr, rate, bondCode, transactionPenNumber, "0", Arrays.toString(sendRst));
                    }
                    //发送到远端
                    logger.info("截取到的收益率【{}】",rate);
                }
            } else {
                System.out.println("不在时间范围内，不需要扒");
            }
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
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
     * @return
     */
    private String recognition(String path, String fileName, int x, int y, int width, int height) {
        GuiCamera cam = new GuiCamera(imageFormat);
        //汇率图
        File imageFile = cam.snapshot(path, fileName, x, y, width, height);
        //识别交易笔数
        System.out.println("准备进入checkFile" + imageFile.getName());
        return Check.checkFile(imageFile, index);
    }

    /**
     * 插入数据库
     *
     * @param date
     * @param rate
     * @param bondCode
     */
    private void insert(String date, String rate, String bondCode, String transactionPenNumber, String send, String sendRst) {
        System.out.println("开始存入数据库");
        Connection con = null;
        PreparedStatement ps = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://47.98.218.102:3306/cqAI?allowMultiQueries=true&useUnicode=true&characterEncoding=UTF-8", "cq_ai", "cq201805091919");
            //问号叫做占位符，这样可以避免SQL注入
            String sql = "insert into zhang_rate(trade_date,rate,bond_code,num,send,sendrst) values (?,?,?,?,?,?)";
            ps = con.prepareStatement(sql);
            ps.setObject(1, date);
            ps.setObject(2, rate);
            ps.setObject(3, bondCode);
            ps.setObject(4, transactionPenNumber);
            ps.setObject(5, send);
            ps.setObject(6, sendRst);
            ps.execute();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        } finally {
            try {
                if (con != null) {
                    //后开的先关
                    ps.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
