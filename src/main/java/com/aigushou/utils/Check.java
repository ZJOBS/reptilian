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
import java.util.HashMap;


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
     * 传入一行数据，解析 时间和收益率
     *
     * @param array
     * @return
     * @throws Exception
     */
    public static RateEntity analysisBaiDuPairedResult(JSONArray array) throws Exception {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        RateEntity entity = null;
        String rate = "";
        String rateDateTime = "";
        try {
            rate = array.getJSONObject(0).getString("words");
            rateDateTime = array.getJSONObject(1).getString("words");
            LocalDateTime ldt = LocalDateTime.parse(df.format(now) + rateDateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            //判断获取时间大于当前时间时，为异常数据，抛弃
            long duration = DateTimeUtils.duration(now, ldt);
            if (duration > 120) {
                //获取时间超过当前时间两分钟为异常数据，正常情况下，时间已经小于当前时间，考虑到机器间的时间差，故设定为两分钟，如出现问题，再往小调
                logger.error("解析到的数据 大于当前时间");
            } else {
                Double.parseDouble(rate);
                entity = new RateEntity(rate, rateDateTime);
            }
        } catch (Exception e) {
            logger.error("收益率【{}】和时间【{}】解析异常", rate, rateDateTime);
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


//    /**
//     * 百度Ocr高精度版解析
//     *
//     * @return
//     */
//    public static JSONObject accurate(byte[] file) {
//        HashMap<String, String> options = new HashMap<String, String>();
//        options.put("detect_direction", "false");
//        options.put("probability", "false");
//        JSONObject rst = JSONObject.parseObject(Constant.getBaiDuOcrByBlockingQueue().basicAccurateGeneral(file, options).toString(2));
//        return rst;
//    }

    /**
     * 百度Ocr高精度版解析
     *
     * @return
     */
    public static JSONArray accurate(String path) {
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("detect_direction", "false");
        options.put("probability", "false");
        JSONObject obj = JSONObject.parseObject(Constant.getBaiDuOcrByBlockingQueue().basicAccurateGeneral(path, options).toString(2));
        JSONArray array = obj.getJSONArray("words_result");
        return array;
    }


}
