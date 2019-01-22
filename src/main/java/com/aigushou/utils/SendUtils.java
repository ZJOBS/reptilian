package com.aigushou.utils;

import com.aigushou.constant.Constant;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import netscape.javascript.JSObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * 发送
 *
 * @author jiezhang
 */
public class SendUtils {
    private static Logger logger = LoggerFactory.getLogger(SendUtils.class);

    /**
     * @param bondCode             债券编号
     * @param earnRate             收益率
     * @param transactionPenNumber 成交笔数
     */
    public static int[] sendRate(String bondCode, String earnRate, String transactionPenNumber) {
        int[] rst = new int[Constant.send_environment.length];
        String urlStr;
        for (int i = 0; i < Constant.send_environment.length; i++) {
            urlStr = Constant.send_environment[i];
            rst[i] = send(bondCode, earnRate, transactionPenNumber, urlStr);
        }
        return rst;
    }


    /**
     * 发送债券+收益率和时间
     *
     * @param bondCode    债券
     * @param rateAndTime 收益率和时间
     * @return
     */
    public static int[] sendRateAndTime(String bondCode, JSONArray rateAndTime) {
        int[] rst = new int[Constant.send_environment.length];
        String urlStr;
        for (int i = 0; i < Constant.send_environment.length; i++) {
            urlStr = Constant.send_environment[i];
            rst[i] = send(bondCode, rateAndTime, urlStr);
        }
        return rst;
    }


    /**
     * 发送心跳
     *
     * @param bondCode             债券编号
     * @param earnRate             收益率
     * @param transactionPenNumber 成交笔数
     */
    public static void sendHeart(String bondCode, String earnRate, String transactionPenNumber) {
        int[] rst = new int[Constant.send_heart_environment.length];
        String urlStr;
        for (int i = 0; i < Constant.send_heart_environment.length; i++) {
            urlStr = Constant.send_heart_environment[i];
            //logger.info("【{}】:发送心跳,地址：【{}】", bondCode, Constant.send_heart_environment[i]);
            System.out.println(bondCode + ":发送心跳," + "地址：" + Constant.send_heart_environment[i]);
            rst[i] = send(bondCode, earnRate, transactionPenNumber, urlStr);
        }
    }


    /**
     * 发送回撤收益率
     *
     * @param bondCode             债券编号
     * @param earnRate             收益率
     * @param transactionPenNumber 成交笔数
     */
    public static int[] sendWithdrawalRate(String bondCode, String earnRate, String transactionPenNumber) {
        int[] rst = new int[Constant.send_withdrawal_Environment.length];
        String urlStr;
        for (int i = 0; i < Constant.send_withdrawal_Environment.length; i++) {
            urlStr = Constant.send_withdrawal_Environment[i];
            rst[i] = send(bondCode, earnRate, transactionPenNumber, urlStr);
        }
        return rst;
    }


    /**
     * 发送错误信息
     *
     * @param bondCode
     * @return
     */
    public static int[] sendError(String bondCode) {
        int[] rst = new int[Constant.sendErrorEnvironment.length];
        String urlStr;
        for (int i = 0; i < Constant.sendErrorEnvironment.length; i++) {
            urlStr = Constant.sendErrorEnvironment[i];
            rst[i] = send(bondCode, urlStr);
        }
        return rst;
    }

    /**
     * 发送收益率+笔数
     *
     * @param bondCode
     * @param earnRate
     * @param transactionPenNumber
     * @param sendUrl
     * @return
     */
    private static int send(String bondCode, String earnRate, String transactionPenNumber, String sendUrl) {
        int rst = 0;
        try {
            HttpClient httpClient = HttpClients.createDefault();
            HttpPost post = new HttpPost();
            sendUrl = sendUrl + "?bondCode=" + bondCode + "&earnRate=" + earnRate + "&amoutStr=" + transactionPenNumber + "&source=" + Constant.properties.getProperty("source");
            URI url = new URI(sendUrl);
            HttpGet get = new HttpGet(url);
            //System.out.println("开始发送请求");
            logger.info("开始发送请求");
            //设置请求和传输超时时间
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(2000).setConnectTimeout(2000).build();
            post.setConfig(requestConfig);
            HttpResponse response = httpClient.execute(get);
            if (response.getStatusLine().getStatusCode() == 200) {
                String str;
                /*读取服务器返回过来的json字符串数据*/
                str = EntityUtils.toString(response.getEntity());
                JSONObject jsonObject = JSONObject.parseObject(str);
                logger.info("发送到【{}】,成功！ 返回结果:【{}】", sendUrl, jsonObject.toJSONString());
                //System.out.println("发送到[" + sendUrl + "]成功！ 返回结果:[" + jsonObject.toJSONString() + "]");
                if (jsonObject.get("code").equals("0000")) {
                    rst = 1;
                } else if (jsonObject.get("code").equals("9999")) {
                    rst = 0;
                } else {
                    rst = 9;
                }
            }
        } catch (Exception e) {
            logger.info("发送到【{}】,失败！ ", sendUrl);
            //System.out.println("发送到[" + sendUrl + "]失败！");
            e.printStackTrace();
        }
        return rst;
    }

    /**
     * 发送收益率+时间
     *
     * @param bondCode
     * @param rateAndTime
     * @param sendUrl
     * @return
     */
    private static int send(String bondCode, JSONArray rateAndTime, String sendUrl) {
        int rst = 0;
        try {
            HttpClient httpClient = HttpClients.createDefault();
            HttpPost post = new HttpPost();
            sendUrl = sendUrl + "?bondCode=" + bondCode + "&rateAndTime=" + rateAndTime.toJSONString() + "&source=" + Constant.properties.getProperty("source");
            URI url = new URI(sendUrl);
            HttpGet get = new HttpGet(url);
            logger.info("开始发送请求");
            //设置请求和传输超时时间
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(2000).setConnectTimeout(2000).build();
            post.setConfig(requestConfig);
            HttpResponse response = httpClient.execute(get);
            if (response.getStatusLine().getStatusCode() == 200) {
                String str;
                /*读取服务器返回过来的json字符串数据*/
                str = EntityUtils.toString(response.getEntity());
                JSONObject jsonObject = JSONObject.parseObject(str);
                logger.info("发送到【{}】,成功！ 返回结果:【{}】", sendUrl, jsonObject.toJSONString());
                //System.out.println("发送到[" + sendUrl + "]成功！ 返回结果:[" + jsonObject.toJSONString() + "]");
                if (jsonObject.get("code").equals("0000")) {
                    rst = 1;
                } else if (jsonObject.get("code").equals("9999")) {
                    rst = 0;
                } else {
                    rst = 9;
                }
            }
        } catch (Exception e) {
            logger.info("发送到【{}】,失败！ ", sendUrl);
            //System.out.println("发送到[" + sendUrl + "]失败！");
            e.printStackTrace();
        }
        return rst;
    }

    /**
     * 发送错误信息
     *
     * @param bondCode
     * @param sendUrl
     * @return
     */
    private static int send(String bondCode, String sendUrl) {
        int rst = 0;
        try {
            HttpClient httpClient = HttpClients.createDefault();
            HttpPost post = new HttpPost();
            sendUrl = sendUrl + "?errCode=E0003&errMsg=请检查" + bondCode + "位置是否正常";
            URI url = new URI(sendUrl);
            HttpGet get = new HttpGet(url);
            //设置编码
            get.setHeader(new BasicHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8"));
            logger.info("开始发送错误请求 ");
            //设置请求和传输超时时间
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(2000).setConnectTimeout(2000).build();
            post.setConfig(requestConfig);
            HttpResponse response = httpClient.execute(get);
            if (response.getStatusLine().getStatusCode() == 200) {
                String str;
                //读取服务器返回过来的json字符串数据
                str = EntityUtils.toString(response.getEntity());
                JSONObject jsonObject = JSONObject.parseObject(str);

                logger.info("发送到【{sendUrl}】 成功！ 返回结果【{sendUrl}】", sendUrl, jsonObject.toJSONString());
                if (jsonObject.get("code").equals("0000")) {
                    rst = 1;
                } else if (jsonObject.get("code").equals("9999")) {
                    rst = 0;
                } else {
                    rst = 9;
                }
            }
        } catch (Exception e) {
            logger.info("发送到【{sendUrl}】 失败！", sendUrl);
            e.printStackTrace();
        }
        return rst;
    }

}
