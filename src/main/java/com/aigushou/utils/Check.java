package com.aigushou.utils;

import com.aigushou.constant.Constant;
import com.aigushou.entity.RateEntity;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;


/**
 * @author jiezhang
 */
public class Check {
    private static Logger logger = LoggerFactory.getLogger(Check.class);

    /**
     * 识别本地图片的文字，对选用使用的账号，一般情况，一个债对应一个账号，否则免费识别次数有限
     *
     * @param file 本地图片地址
     * @return 识别结果，为json格式
     * @throws URISyntaxException URI打开异常
     * @throws IOException        io流异常
     */
    public static String checkFile(File file) {
        String image = null;
        try {
            image = ImageUtil.getImageStrFromPath(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String param = "image=" + image;
        return post(param);
    }


    /**
     * 通过传递参数：url和image进行文字识别
     *
     * @param param 区分是url还是image识别
     * @return 识别结果
     * @throws URISyntaxException URI打开异常
     * @throws IOException        IO流异常
     */
    private static String post(String param) {
        try {
            //开始搭建post请求
            logger.info("进入post方法,准备发送百度查询");
            HttpClient httpClient = HttpClients.createDefault();
            HttpPost post = new HttpPost();
            URI url = null;
            String baiDuUrl = Constant.getBaiDuAccountByBlockingQueue();
            url = new URI(baiDuUrl);
            post.setURI(url);
            //设置请求头，请求头必须为application/x-www-form-urlencoded，因为是传递一个很长的字符串，不能分段发送
            post.setHeader("Content-Type", "application/x-www-form-urlencoded");
            StringEntity entity = new StringEntity(param);
            post.setEntity(entity);
            //设置请求和传输超时时间
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(2000).setConnectTimeout(2000).build();
            post.setConfig(requestConfig);

            HttpResponse response = httpClient.execute(post);
            if (response.getStatusLine().getStatusCode() == 200) {
                String str;
                //读取服务器返回过来的json字符串数据
                str = EntityUtils.toString(response.getEntity());
                JSONObject jsonObject = JSONObject.parseObject(str);
//                JSONArray array = jsonObject.getJSONArray("words_result");
//                str = array.get(0).toString();
//                str = JSONObject.parseObject(str).getString("words");
                return str;
            }
        } catch (Exception e) {
            logger.error("发送请求失败:" + e.getMessage());
            return null;
        }
        return null;
    }

    public static JSONArray analysisBaiDuResult(String param) throws Exception {
        JSONObject object = JSONObject.parseObject(param);
        JSONArray array = object.getJSONArray("words_result");
        return array;
    }


    /**
     * 只传入一行数据
     *
     * @param array
     * @return
     * @throws Exception
     */
    public static RateEntity analysisBaiDuPairedResult(JSONArray array) throws Exception {
        RateEntity entity = new RateEntity();
        String rate = array.getJSONObject(0).getString("words");
        String rateDateTime = array.getJSONObject(1).getString("words");
        try {
            Double.parseDouble(rate);
            entity.setRate(rate);
        } catch (Exception e) {
            throw new Exception("收益率爬去异常:" + rate);
        }
        try {
            //校验识别到的时间是否为时间格式 ，具体时间无所谓 "2018-12-12 "
            LocalDateTime ldt = LocalDateTime.parse("2018-12-12 " + rateDateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            entity.setDateTime(rateDateTime);
        } catch (Exception e) {
            throw new Exception("时间爬去异常:" + rateDateTime);
        }
        return entity;
    }


    /**
     * 获取utc
     *
     * @return
     */
    private static String getUTCDate() {
        String dateStr = "";
        Date date = null;
        String months = "", days = "", hours = "", sec = "", minutes = "";
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        StringBuffer UTCTimeBuffer = new StringBuffer();
        Calendar cal = Calendar.getInstance();
        int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
        int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);
        cal.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        if (month < 10) {
            months = "0" + String.valueOf(month);
        } else {
            months = String.valueOf(month);
        }
        if (minute < 10) {
            minutes = "0" + String.valueOf(minute);
        } else {
            minutes = String.valueOf(minute);
        }
        if (day < 10) {
            days = "0" + String.valueOf(day);
        } else {
            days = String.valueOf(day);
        }
        if (hour < 10) {
            hours = "0" + String.valueOf(hour);
        } else {
            hours = String.valueOf(hour);
        }
        if (second < 10) {
            sec = "0" + String.valueOf(second);
        } else {
            sec = String.valueOf(second);
        }
        UTCTimeBuffer.append(year).append("-").append(months).append("-").append(days);
        UTCTimeBuffer.append("T").append(hours).append(":").append(minutes).append(":").append(sec).append("Z");
        try {
            date = format.parse(UTCTimeBuffer.toString());
            dateStr = format.format(date);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return dateStr;
    }


}
