package com.hioas.demo.channel.adapter;

import com.hioas.demo.channel.IPaymentChannel;
import com.hioas.demo.dto.PaymentOrderRequest;
import com.hioas.demo.dto.RefundRequest;
import com.hioas.demo.utils.RandomUtil;
import com.hioas.demo.utils.SignUtil;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信支付 JSAPI 适配器
 * 实现微信支付 JSAPI 模式的支付流程
 * 参考文档: https://pay.weixin.qq.com/wiki/doc/api/jsapi.php
 */
public class WeChatJSAPIAdapter extends SimulatedChannelAdapter {

    private static final Logger log = LoggerFactory.getLogger(WeChatJSAPIAdapter.class);

    @Override
    public IPaymentChannel.ChannelMeta getMeta() {
        return new IPaymentChannel.ChannelMeta(
                "wx_jsapi",
                "微信支付 JSAPI",
                "1.0.0",
                "[\"ecommerce\",\"app\",\"h5\"]",
                true
        );
    }

    @Override
    public void init() {
        log.info("初始化微信支付 JSAPI 适配器");
        this.successRate = 0.97;
        this.baseLatencyMs = 150;
    }

    @Override
    protected String buildPayData(String thirdOrderNo, Map<String, Object> context) {
        Map<String, String> data = new HashMap<>();
        String appId = (String) config.get("appid");
        String mchId = (String) config.get("mch_id");
        String secret = (String) config.get("secret");

        data.put("appid", appId != null ? appId : "wx1234567890");
        data.put("partnerid", mchId != null ? mchId : "1234567890");
        data.put("prepayid", thirdOrderNo);
        data.put("nonce_str", RandomUtil.alphanumeric(32));
        data.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        data.put("package", "Sign=WXPay");

        String sign = SignUtil.sign(data, secret != null ? secret : "");
        data.put("sign", sign);

        return JSONObject.toJSONString(data);
    }

    @Override
    public boolean verifyCallback(Map<String, String> callbackParams) {
        String sign = callbackParams.get("sign");
        if (sign == null || sign.isEmpty()) {
            return false;
        }
        String timestamp = callbackParams.get("timestamp");
        String nonceStr = callbackParams.get("nonce_str");
        String appId = callbackParams.get("appid");

        Map<String, String> params = new HashMap<>();
        params.put("appid", appId);
        params.put("timestamp", timestamp);
        params.put("nonce_str", nonceStr);
        params.put("need_total", callbackParams.get("need_total"));
        params.put("prepay_id", callbackParams.get("prepay_id"));
        params.put("package", callbackParams.get("package"));

        String expectedSign = SignUtil.sign(params, (String) config.get("secret"));
        return expectedSign.equals(sign);
    }
}
