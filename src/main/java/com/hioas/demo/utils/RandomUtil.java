package com.hioas.demo.utils;

import java.util.Random;

/**
 * 随机工具类
 */
public class RandomUtil {

    private static final Random RANDOM = new Random();

    public static String alphanumeric(int length) {
        StringBuilder sb = new StringBuilder();
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public static String numeric(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    public static long randomLong(long min, long max) {
        return min + RANDOM.nextInt((int)(max - min));
    }
}
