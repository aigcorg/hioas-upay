package com.hioas.demo.channel.adapter;

import com.hioas.demo.channel.IPaymentChannel;
import com.hioas.demo.dto.PaymentOrderRequest;
import com.hioas.demo.dto.RefundRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 支付通道适配器抽象基类
 * 提供通用功能：日志、配置获取、签名辅助等
 */
public abstract class AbstractPaymentChannelAdapter implements IPaymentChannel {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    protected Map<String, Object> config;

    public void setConfig(Map<String, Object> config) {
        this.config = config;
    }

    @Override
    public Map<String, Object> getConfig() {
        return config;
    }

    /**
     * 子类可重写以添加通道特有的初始化逻辑
     */
    @Override
    public void init() {
        log.info("初始化通道适配器: {}", getMeta().getCode());
    }

    /**
     * 生成签名的通用辅助方法
     * 子类可根据各自通道的签名算法重写
     */
    protected String sign(Map<String, String> params, String secretKey) {
        // 默认实现：按键排序 + 拼接 + SHA256
        // 各具体通道可重写
        return null;
    }

    /**
     * HTTP 请求的通用辅助方法
     */
    protected String httpPost(String url, Map<String, String> params) {
        // 默认实现可使用 HttpClient 或 RestTemplate
        // 子类可重写以适配不同通道的 HTTP 客户端需求
        return null;
    }

    protected String httpGet(String url, Map<String, String> params) {
        return null;
    }
}
