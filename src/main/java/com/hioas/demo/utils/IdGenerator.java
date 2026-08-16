package com.hioas.demo.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ID生成器工具类
 */
public class IdGenerator {

    /**
     * 生成交易流水号: pay_YYYYMMDDHHMMSS_xxxxxx
     */
    public static String generateTradeNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = RandomUtil.alphanumeric(6).toUpperCase();
        return "pay_" + timestamp + "_" + random;
    }

    /**
     * 生成退款流水号: ref_YYYYMMDDHHMMSS_xxxxxx
     */
    public static String generateRefundNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = RandomUtil.alphanumeric(6).toUpperCase();
        return "ref_" + timestamp + "_" + random;
    }

    /**
     * 生成应用ID: app_YYYYMMDD_xxxx
     */
    public static String generateAppId() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = RandomUtil.numeric(4);
        return "app_" + date + "_" + random;
    }

    /**
     * 生成任务编号: recon_YYYYMMDD_xxxxx
     */
    public static String generateTaskNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = RandomUtil.alphanumeric(8);
        return "recon_" + date + "_" + random;
    }

    /**
     * 生成数据库自增ID模拟 (实际使用数据库自增)
     */
    public static long generateId() {
        return System.currentTimeMillis() % 1000000 + (long)(Math.random() * 1000000);
    }
}
