package com.aigushou.constant;

import com.aigushou.App;
import com.aigushou.utils.AuthServiceUtil;
import com.alibaba.fastjson.JSONObject;
import com.baidu.aip.ocr.AipOcr;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeoutException;

/**
 * 常量
 *
 * @author jiezhang
 */
public class Constant {

    private static Logger logger = LoggerFactory.getLogger(Constant.class);

    /**
     * 数据库URL
     */
    public static String dbUrl;

    public static String dbDatabase;

    /**
     * 数据库用户名
     */
    public static String dbUser;
    /**
     * 数据库密码
     */
    public static String dbPassword;

    /**
     * 初始化6个
     */
    public static List<String> tnList = Arrays.asList("0", "0", "0", "0", "0", "0");

    /**
     * 临时截图
     */
    public static Map<String, BufferedImage> bufferedImageMap = new HashMap<String, BufferedImage>();


    /**
     * 是否异常，false 异常，true非异常
     */
    public static Map<String, Boolean> reptilianStateMap = new HashMap<String, Boolean>();

    /**
     * 坐标异常数
     */
    public static Map<String, Integer> positionStateMap = new HashMap<String, Integer>();

    /**
     * bufferedImageMap 中 key的前缀
     */
    public static final String bufferedImageMapKey = "BIMK";

    /**
     * 坐标位置key
     */
    public static final String positionStateMapKey = "PSMK";

    /**
     * 需要发送的地址
     */
    public static final String[] send_environment;

    /**
     * 需要发送的地址
     */
    public static final String[] send_withdrawal_Environment;

    /**
     * 心跳发送地址
     */
    public static final String[] send_heart_environment;


    /**
     * 错误发送地址
     */
    public static final String[] sendErrorEnvironment;

    /**
     * 配置文件
     */
    public static final Properties properties;


    public static final Map<String, String> baiduAccountURLMap;

    /**
     * baiduAccountURLMap 中 key的前缀
     */
    public static final String baiduAccountURLMapKey = "BAI_DU_";


    /**
     * 类别 收益率
     */
    public static final int rate_type = 1;
    /**
     * 类别 回撤
     */
    public static final int Withdrawal = 2;


    /**
     * 百度配置文件
     */
    public static final Properties baiDuProperties;


    /**
     * 百度账号文件
     */
    private static BlockingQueue<String> baiDuAccountQueue;


    /**
     * 百度图像识别对象
     */
    private static BlockingQueue<AipOcr> baiDuAipOcrQueue;


    private static final Connection rabbitMQConnection;


    static {
        properties = getProperties();
        send_environment = getSendEnvironment();
        send_withdrawal_Environment = getSendWithdrawalEnvironment();
        baiDuProperties = getBaiDuProperties();
        baiduAccountURLMap = getBaiDuAccountURLMap();
        send_heart_environment = getSendHeartEnvironment();
        sendErrorEnvironment = getSendErrorEnvironment();
        baiDuAccountQueue = getBaiDuAccountQueue();
        baiDuAipOcrQueue = getBaiDuAipOcrQueue();
        setDB();
        rabbitMQConnection = getRabbitMQConnection();
    }

    /**
     * 获取定位配置信息
     *
     * @return
     */
    private static Properties getProperties() {
        Properties properties = new Properties();
        try {
            logger.info("加载配置文件，路径为" + "d:/jiezhang/reptilian_jar/region.properties");
//            FileInputStream in = new FileInputStream("d:/jiezhang/reptilian_jar/region.properties");
//            properties.load(in);
//            in.close();
            properties.load(App.class.getResourceAsStream("/region.properties"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    /**
     * 获取百度账号
     *
     * @return
     */
    private static Properties getBaiDuProperties() {
        Properties properties = new Properties();
        try {
            logger.info("加载百度配置文件，路径为" + "d:/jiezhang/reptilian_jar/region.properties");
//            FileInputStream in = new FileInputStream("d:/jiezhang/reptilian_jar/baidu.properties");
//            properties.load(in);
//            in.close();
            properties.load(App.class.getResourceAsStream("/baidu.properties"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    /**
     * 获取rabbit配置
     *
     * @return
     */
    private static Properties getRabbitMQProperties() {
        Properties properties = new Properties();
        try {
            logger.info("加载rabbitMQ配置文件，路径为" + "d:/jiezhang/reptilian_jar/region.properties");
//            FileInputStream in = new FileInputStream("d:/jiezhang/reptilian_jar/rabbit.properties");
//            properties.load(in);
//            in.close();
            properties.load(App.class.getResourceAsStream("/rabbit.properties"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }


    /**
     * 获取发送的地址
     *
     * @return
     */
    private static String[] getSendEnvironment() {
        String sendEnvironments = properties.getProperty("sendEnvironment");
        logger.info("加载收益率发送地址【{}】", sendEnvironments);
        String[] sendEnvironmentArray = sendEnvironments.split(",");
        return sendEnvironmentArray;
    }

    /**
     * 获取发送回撤的地址
     *
     * @return
     */
    private static String[] getSendWithdrawalEnvironment() {
        String sendWithdrawalEnvironments = properties.getProperty("sendWithdrawalEnvironment");
        logger.info("加载回撤发送地址【{}】", sendWithdrawalEnvironments);
        String[] sendWithdrawalEnvironmentArry = sendWithdrawalEnvironments.split(",");
        return sendWithdrawalEnvironmentArry;
    }


    private static List<BufferedImage> getBufferedImage() {
        int reptilian_num = Integer.parseInt(properties.getProperty("reptilian_num"));
        logger.info("需要爬债券数量【{}】", reptilian_num);
        List<BufferedImage> list = new LinkedList<BufferedImage>();

        for (int i = 0; i < reptilian_num; i++) {
            BufferedImage bi = new BufferedImage(10, 10, BufferedImage.TYPE_BYTE_GRAY);
            list.add(bi);
        }
        return list;
    }

    /**
     * 一个爬虫一个账号
     *
     * @return
     */
    private static Map<String, String> getBaiDuAccountURLMap() {
        logger.info("加载百度账号");
        Map<String, String> map = new HashMap<String, String>();
        //爬虫数量,
        int baiDuAccountNum = Integer.parseInt(baiDuProperties.getProperty("baiDuAccountNum"));
        for (int i = 0; i < baiDuAccountNum; i++) {
            String baiDuURL = "https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic?access_token=" + AuthServiceUtil.getAuthParameterByIndex(i);
            map.put(baiduAccountURLMapKey + i, baiDuURL);
        }
        logger.info("加载百度账号完成");
        return map;
    }


    /**
     * 百度账号队列
     *
     * @return
     */
    private static BlockingQueue<String> getBaiDuAccountQueue() {
        logger.info("百度账号入队");
        BlockingQueue<String> baiDuQueue = new LinkedBlockingDeque<String>();
        int baiDuAccountNum = Integer.parseInt(baiDuProperties.getProperty("baiDuAccountNum"));
        List<String> accountList = new ArrayList<String>();
        for (int i = 0; i < baiDuAccountNum; i++) {
            String baiDuURL = "https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic?access_token=" + AuthServiceUtil.getAuthParameterByIndex(i);
            accountList.add(baiDuURL);
        }
        for (int i = 0; i < 120; i++) {
            int current = i % baiDuAccountNum;
            baiDuQueue.offer(accountList.get(current));
        }
        logger.info("百度账号入队完成");
        return baiDuQueue;
    }


    /**
     * 百度OCR对象队列
     *
     * @return
     */
    private static BlockingQueue<AipOcr> getBaiDuAipOcrQueue() {
        logger.info("百度ApiOcr入队");
        BlockingQueue<AipOcr> baiDuQueue = new LinkedBlockingDeque<AipOcr>();
        int baiDuAccountNum = Integer.parseInt(baiDuProperties.getProperty("baiDuAccountNum"));
        List<AipOcr> accountList = new ArrayList<AipOcr>();
        for (int i = 0; i < baiDuAccountNum; i++) {
            AipOcr aipOcr = AuthServiceUtil.getOcrParameterByIndex(i);
            accountList.add(aipOcr);
        }
        for (int i = 0; i < 120; i++) {
            int current = i % baiDuAccountNum;
            baiDuQueue.offer(accountList.get(current));
        }
        logger.info("百度ApiOcr入队完成");
        return baiDuQueue;
    }

    /**
     * 心跳地址
     *
     * @return
     */
    private static String[] getSendHeartEnvironment() {
        String sendHeartEnvironments = properties.getProperty("sendHeartEnvironment");
        logger.info("加载发送心跳地址：【{}】", sendHeartEnvironments);
        String[] sendHeartEnvironmentArray = sendHeartEnvironments.split(",");
        return sendHeartEnvironmentArray;
    }

    /**
     * 错误消息地址
     *
     * @return
     */
    private static String[] getSendErrorEnvironment() {
        String sendErrorEnvironments = properties.getProperty("sendErrorEnvironment");
        logger.info("加载发送错误消息地址：【{}】", sendErrorEnvironments);
        String[] sendErrorEnvironmentArray = sendErrorEnvironments.split(",");
        return sendErrorEnvironmentArray;
    }


    private static void setDB() {
        dbUrl = properties.getProperty("DBurl");
        dbUser = properties.getProperty("DBuser");
        dbPassword = properties.getProperty("DBpassword");
        dbDatabase = properties.getProperty("DBdatabase");
    }


    /**
     * 获取百度账号
     *
     * @return
     */
    public synchronized static String getBaiDuAccountByBlockingQueue() {
        //从队列中获取百度账号并放入到队尾
        String account = baiDuAccountQueue.poll();
        baiDuAccountQueue.offer(account);
        return account;
    }


    /**
     * 获取百度ocr
     *
     * @return
     */
    public synchronized static AipOcr getBaiDuOcrByBlockingQueue() {
        //从队列中获取百度账号并放入到队尾
        AipOcr aipOcr = baiDuAipOcrQueue.poll();
        baiDuAipOcrQueue.offer(aipOcr);
        return aipOcr;
    }


    /**
     * 获取rabbitMQ连接
     *
     * @return
     */
    private static Connection getRabbitMQConnection() {
        Properties properties = getRabbitMQProperties();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(properties.getProperty("rabbit_user_name"));
        factory.setPassword(properties.getProperty("rabbit_password"));
        factory.setHost(properties.getProperty("rabbit_host"));
        factory.setPort(Integer.parseInt(properties.getProperty("rabbit_port")));
        try {
            return factory.newConnection();
        } catch (Exception e) {
            throw new RuntimeException("创建rabbit client 连接失败" + e);
        }
    }


    public static void rabbitMQPublish(String queueName, JSONObject message) throws IOException, TimeoutException {
        Channel channel = null;
        try {
            channel = rabbitMQConnection.createChannel();
            channel.queueDeclare(queueName, true, false, false, null);
            channel.txSelect(); // 声明事务 // 发送消息
            channel.basicPublish("", queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, message.toString().getBytes("UTF-8"));
            channel.txCommit(); // 提交事务
        } catch (IOException e) {
            channel.txRollback();
        } finally {
            channel.close();
        }

    }


    public static void rabbitMQConsumer(String queueName) {

        try {
            boolean autoAck = false;
            Channel channel = rabbitMQConnection.createChannel();
            channel.basicConsume(queueName, true, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    System.out.println("消息序号:" + envelope.getDeliveryTag());
                    System.out.println("交换器:" + envelope.getExchange());
                    System.out.println("路由键:" + envelope.getRoutingKey());
                    System.out.println("消息内容:" + new String(body, StandardCharsets.UTF_8));
                }
            });
        } catch (IOException e) {

        }
    }
}
