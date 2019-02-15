package com.aigushou.utils;

import com.aigushou.constant.Constant;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 数据库工具类
 *
 * @author jiezhang
 */
public class DataBaseUtils {
    private static Logger logger = LoggerFactory.getLogger(DataBaseUtils.class);

    /**
     * 插入数据库
     *
     * @param date
     * @param rate
     * @param bondCode
     */
    public static void insert(String date, String rate, String bondCode, String transactionPenNumber, String send, String sendRst) {
        logger.info("开始存入数据库");
        Connection con = null;
        PreparedStatement ps = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(Constant.dbUrl, Constant.dbUser, Constant.dbPassword);
            //问号叫做占位符，这样可以避免SQL注入
            String sql = "insert into zhang_rate(trade_date,rate,bond_code,num,send,sendrst,reptilian_type) values (?,?,?,?,?,?,?)";
            ps = con.prepareStatement(sql);
            ps.setObject(1, date);
            ps.setObject(2, rate);
            ps.setObject(3, bondCode);
            ps.setObject(4, transactionPenNumber);
            ps.setObject(5, send);
            ps.setObject(6, sendRst);
            ps.setObject(7, Constant.properties.getProperty("reptilian_type"));
            ps.execute();
        } catch (ClassNotFoundException e) {
            logger.error("e.getMessage()");
        } catch (SQLException e) {
            logger.error("e.getMessage()");
        } finally {
            close(con, ps);
        }
    }


    /**
     * 插入数据库
     *
     * @param date
     * @param bondCode
     */
    public static void insertArea(String date, String bondCode, JSONArray jsonArray, String send, String sendRst) {
        logger.info("开始存入数据库");
        Connection con = null;
        PreparedStatement ps = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(Constant.dbUrl, Constant.dbUser, Constant.dbPassword);
            //问号叫做占位符，这样可以避免SQL注入
            String sql = "insert into +Constant.dbDatabase+(trade_date,bond_code,content,send,sendrst,reptilian_type) values (?,?,?,?,?,?)";
            ps = con.prepareStatement(sql);
            ps.setObject(1, date);
            ps.setObject(2, bondCode);
            ps.setObject(3, jsonArray.toJSONString());
            ps.setObject(4, send);
            ps.setObject(5, sendRst);
            ps.setObject(6, Constant.properties.getProperty("reptilian_type"));
            ps.execute();
        } catch (ClassNotFoundException e) {
            logger.error("e.getMessage()");
        } catch (SQLException e) {
            logger.error("e.getMessage()");
        } finally {
            close(con, ps);
        }
    }


    private static void close(Connection con, PreparedStatement ps) {
        try {
            if (con != null) {
                //后开的先关
                ps.close();
            }
        } catch (SQLException e) {
            logger.error("PreparedStatement关闭异常 【{}】", e.getMessage());
        }
        try {
            if (con != null) {
                con.close();
            }
        } catch (SQLException e) {

            logger.error("Connection关闭异常【{}】", e.getMessage());

        }
    }
}
