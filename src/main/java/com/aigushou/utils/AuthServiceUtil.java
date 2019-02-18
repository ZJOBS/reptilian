package com.aigushou.utils;

import com.aigushou.constant.Constant;
import com.alibaba.fastjson.JSONObject;
import com.baidu.aip.ocr.AipOcr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * 获取token类
 *
 * @author jiezhang
 */
public class AuthServiceUtil {
    private static Logger logger = LoggerFactory.getLogger(AuthServiceUtil.class);

    /**
     * 通过下标获取百度权限
     *
     * @param index
     * @return
     */
    public static String getAuthParameterByIndex(int index) {
        // 官网获取的 API Key 更新为你注册的
        String clientId = Constant.baiDuProperties.getProperty("clientId" + index);
        // 官网获取的 Secret Key 更新为你注册的
        String clientSecret = Constant.baiDuProperties.getProperty("clientSecret" + index);
        return getAuth(clientId, clientSecret);
    }

    /**
     * 通过下标获取百度Ocr
     *
     * @param index
     * @return
     */
    public static AipOcr getOcrParameterByIndex(int index) {
        // 官网获取的 API Key 更新为你注册的
        String API_KEY = Constant.baiDuProperties.getProperty("clientId" + index);
        // 官网获取的 Secret Key 更新为你注册的
        String SECRET_KEY = Constant.baiDuProperties.getProperty("clientSecret" + index);
        String APP_ID = Constant.baiDuProperties.getProperty("appId" + index);
        return getBaiDuOcr(APP_ID, API_KEY, SECRET_KEY);
    }


    /**
     * 获取API访问token
     * 该token有一定的有效期，需要自行管理，当失效时需重新获取.
     *
     * @param ak - 百度云官网获取的 API Key
     * @param sk - 百度云官网获取的 Securet Key
     * @return assess_token 示例：
     * "24.460da4889caad24cccdb1fea17221975.2592000.1491995545.282335-1234567"
     */
    private static String getAuth(String ak, String sk) {
        // 获取token地址
        String authHost = "https://aip.baidubce.com/oauth/2.0/token?";
        String getAccessTokenUrl = authHost
                // 1. grant_type为固定参数
                + "grant_type=client_credentials"
                // 2. 官网获取的 API Key
                + "&client_id=" + ak
                // 3. 官网获取的 Secret Key
                + "&client_secret=" + sk;
        try {
            URL realUrl = new URL(getAccessTokenUrl);
            HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            Map<String, List<String>> map = connection.getHeaderFields();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
            JSONObject jsonObject = JSONObject.parseObject(result.toString());
            return jsonObject.getString("access_token");
        } catch (Exception e) {
            logger.error("获取token失败！【{}】", e.getMessage());
        }
        return null;
    }


    /**
     * @param APP_ID     百度云官网获取的 APP_ID
     * @param API_KEY    百度云官网获取的 API_KEY
     * @param SECRET_KEY 百度云官网获取的 SECRET_KEY
     * @return
     */
    private static AipOcr getBaiDuOcr(String APP_ID, String API_KEY, String SECRET_KEY) {
        AipOcr client = new AipOcr(APP_ID, API_KEY, SECRET_KEY);
        //设置连接超时时间
        client.setConnectionTimeoutInMillis(2000);
        //设置通讯超时时间
        client.setSocketTimeoutInMillis(60000);
        return client;
    }

}
