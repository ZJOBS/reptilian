package com.aigushou.utils;

import com.aigushou.constant.Constant;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import netscape.javascript.JSObject;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            urlStr = urlStr + "?bondCode=" + bondCode + "&earnRate=" + earnRate + "&amoutStr=" + transactionPenNumber + "&source=" + Constant.properties.getProperty("source");
            JSONObject jsonObject = sendGet(urlStr);
            rst[i] = analysis(jsonObject);
        }
        return rst;
    }

    /**
     * @param bondCode 债券编号
     * @param earnRate 收益率
     * @param dateTime 时间
     */
    public static int[] sendRateAndTime(String bondCode, String earnRate, String dateTime) {
        int[] rst = new int[Constant.send_environment.length];
        String urlStr;
        for (int i = 0; i < Constant.send_environment.length; i++) {
            urlStr = Constant.send_environment[i];
            urlStr = urlStr + "?bondCode=" + bondCode + "&earnRate=" + earnRate + "&dateTime=" + dateTime + "&source=" + Constant.properties.getProperty("source");
            JSONObject jsonObject = sendGet(urlStr);
            rst[i] = analysis(jsonObject);
        }
        return rst;
    }

    /**
     * 发送债券+收益率和时间
     *
     * @param bondCode        债券
     * @param rateAndTimeList 收益率和时间
     * @return
     */
    public static int[] sendArea(String bondCode, JSONArray rateAndTimeList) {
        int[] rst = new int[Constant.send_environment.length];
        String urlStr;
        for (int i = 0; i < Constant.send_environment.length; i++) {
            urlStr = Constant.send_environment[i];
            urlStr = urlStr + "?bondCode=" + bondCode + "&rateAndTime=" + rateAndTimeList.toJSONString() + "&source=" + Constant.properties.getProperty("source");

            Map<String, String> pmp = new HashMap<String, String>();
            pmp.put("bondCode", bondCode);
            pmp.put("rateAndTimeList", rateAndTimeList.toJSONString());
            JSONObject jsonObject = sendPost(urlStr, pmp);
            rst[i] = analysis(jsonObject);
        }
        return rst;
    }


    /**
     * 发送心跳
     *
     * @param bondCode 债券编号
     * @param earnRate 收益率
     * @param pmp      成交笔数
     */
    public static void sendHeart(String bondCode, String earnRate, String pmp) {
        int[] rst = new int[Constant.send_heart_environment.length];
        String urlStr;
        for (int i = 0; i < Constant.send_heart_environment.length; i++) {
            urlStr = Constant.send_heart_environment[i];
            logger.info("【{}】:发送心跳,地址：【{}】", bondCode, Constant.send_heart_environment[i]);
            urlStr = urlStr + "?bondCode=" + bondCode + "&earnRate=" + earnRate + "&amoutStr=" + pmp + "&source=" + Constant.properties.getProperty("source");
            JSONObject jsonObject = sendGet(urlStr);
            rst[i] = analysis(jsonObject);
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
            urlStr = urlStr + "?bondCode=" + bondCode + "&earnRate=" + earnRate + "&amoutStr=" + transactionPenNumber + "&source=" + Constant.properties.getProperty("source");
            JSONObject jsonObject = sendGet(urlStr);
            rst[i] = analysis(jsonObject);
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
            urlStr = urlStr + "?errCode=E0003&errMsg=请检查" + bondCode + "位置是否正常";
            logger.info("开始发送错误请求 ");
            JSONObject jsonObject = sendGet(urlStr);
            rst[i] = analysis(jsonObject);
        }
        return rst;
    }


    /**
     * 发送get请求信息
     *
     * @param sendUrl
     * @return
     */
    private static JSONObject sendGet(String sendUrl) {
        int rst = 0;
        JSONObject jsonObject = new JSONObject();
        try {
            HttpClient httpClient = HttpClients.createDefault();
            HttpPost post = new HttpPost();
            URI url = new URI(sendUrl);
            HttpGet get = new HttpGet(url);
            logger.info("开始发送请求");
            //设置请求和传输超时时间
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(2000).setConnectTimeout(2000).build();
            post.setConfig(requestConfig);
            HttpResponse response = httpClient.execute(get);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String str;
                /*读取服务器返回过来的json字符串数据*/
                str = EntityUtils.toString(response.getEntity());
                jsonObject = JSONObject.parseObject(str);
                logger.info("发送到【{}】,成功！ 返回结果:【{}】", sendUrl, jsonObject.toJSONString());
            }
        } catch (Exception e) {
            logger.info("发送到【{}】,失败！ ", sendUrl);
            e.printStackTrace();
        }
        return jsonObject;
    }


    /**
     * 发送post请求信息
     *
     * @param sendUrl
     * @return
     */
    private static JSONObject sendPost(String sendUrl, Map<String, String> params) {
        JSONObject jsonObject = new JSONObject();
        try {
            HttpClient client = HttpClients.createDefault();
            List<NameValuePair> list = new ArrayList<NameValuePair>();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String key = entry.getKey().toString();
                String value = entry.getValue().toString();
                System.out.println("key=" + key + " value=" + value);
                NameValuePair pair = new BasicNameValuePair(key, value);
                list.add(pair);
            }
            UrlEncodedFormEntity entity = null;
            //设置编码
            entity = new UrlEncodedFormEntity(list, "UTF-8");
            //新建一个post请求
            HttpPost post = new HttpPost(sendUrl);
            post.setEntity(entity);
            HttpResponse response = null;
            //客服端向服务器发送请求
            response = client.execute(post);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String str;
                //读取服务器返回过来的json字符串数据
                str = EntityUtils.toString(response.getEntity());
                jsonObject = JSONObject.parseObject(str);
                logger.info("发送到【{}】,成功！ 返回结果:【{}】", sendUrl, jsonObject.toJSONString());
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }


    /**
     * 解析长琴返回值
     *
     * @param json
     * @return
     */
    private static int analysis(JSONObject json) {
        int rst = 0;
        if (json.get("code").equals("0000")) {
            //返回成功
            rst = 1;
        } else if (json.get("code").equals("9999")) {
            //返回错误
            rst = 0;
        } else {
            //未知返回值
            rst = 9;
        }
        return rst;
    }

}
