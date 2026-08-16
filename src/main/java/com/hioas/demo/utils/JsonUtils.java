package com.hioas.demo.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import java.util.List;
import java.util.Map;

/**
 * JSON工具类 (基于 FastJSON)
 */
public class JsonUtils {

    public static String toJson(Object obj) {
        return JSON.toJSONString(obj);
    }

    public static <T> T toBean(String json, Class<T> clazz) {
        return JSON.parseObject(json, clazz);
    }

    public static <T> T toBean(String json, TypeReference<T> type) {
        return JSON.parseObject(json, type);
    }

    public static JSONObject parseObject(String json) {
        return JSON.parseObject(json);
    }

    public static List<Map<String, Object>> parseArray(String json) {
        return JSON.parseObject(json, new TypeReference<List<Map<String, Object>>>() {});
    }
}
