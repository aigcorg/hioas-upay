package com.hioas.demo.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.TreeMap;

/**
 * 签名工具类
 * 提供多种签名算法: MD5签名, SHA256签名, RSA签名模拟, BF专属签名
 */
public class SignUtil {

    /**
     * MD5签名: 参数按key排序,拼接key=value&...字符串,加密
     */
    public static String signMd5(Map<String, String> params, String secretKey) {
        StringBuilder sb = new StringBuilder();
        Map<String, String> sorted = new TreeMap<>(params);
        for (Map.Entry<String, String> e : sorted.entrySet()) {
            if (e.getValue() != null && !e.getValue().isEmpty()) {
                sb.append(e.getKey()).append("=").append(e.getValue()).append("&");
            }
        }
        if (secretKey != null && !secretKey.isEmpty()) {
            sb.append("key=").append(secretKey);
        }
        return md5(sb.toString());
    }

    /**
     * SHA256签名: 同样排序拼接,使用SHA256
     */
    public static String signSha256(Map<String, String> params, String secretKey) {
        StringBuilder sb = new StringBuilder();
        Map<String, String> sorted = new TreeMap<>(params);
        for (Map.Entry<String, String> e : sorted.entrySet()) {
            if (e.getValue() != null && !e.getValue().isEmpty() && !"sign".equals(e.getKey())) {
                sb.append(e.getKey()).append("=").append(e.getValue()).append("&");
            }
        }
        if (secretKey != null && !secretKey.isEmpty()) {
            sb.append("key=").append(secretKey);
        }
        return sha256(sb.toString());
    }

    /**
     * RSA2签名模拟: 使用SHA256WithRSAPrivateKey (实际需要RSA密钥对)
     * 此处返回模拟值用于演示
     */
    public static String signAlipay(Map<String, String> params, String privateKey) {
        // 实际中: 使用私钥对排序后的参数字符串进行SHA256WithRSA签名
        // 此处模拟返回一个伪签名
        String data = sortAndJoin(params);
        return "ALIPAY_SIGN_" + sha256(data + "_" + (privateKey != null ? privateKey.substring(0, Math.min(8, privateKey.length())) : ""));
    }

    /**
     * 模拟BF支付专有签名
     */
    public static String signBf(Map<String, String> params, String secret) {
        String data = sortAndJoin(params);
        return "BF_SIGN_" + sha256(data + "_" + (secret != null ? secret.substring(0, Math.min(8, secret.length())) : ""));
    }

    /**
     * 微信JSAPI签名: SHA256
     */
    public static String sign(Map<String, String> params, String secret) {
        String data = sortAndJoin(params);
        return sha256(data);
    }

    private static String sortAndJoin(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        Map<String, String> sorted = new TreeMap<>(params);
        for (Map.Entry<String, String> e : sorted.entrySet()) {
            if (e.getValue() != null && !e.getValue().isEmpty() && !"sign".equals(e.getKey())) {
                sb.append(e.getKey()).append("=").append(e.getValue()).append("&");
            }
        }
        return sb.toString();
    }

    private static String sha256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return "SHA256_ERROR";
        }
    }

    private static String md5(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return "MD5_ERROR";
        }
    }
}
