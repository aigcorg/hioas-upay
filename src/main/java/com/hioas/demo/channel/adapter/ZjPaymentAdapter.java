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
 * 中金支付适配器
 * 综合支付解决方案提供方
 */
public class ZjPaymentAdapter extends SimulatedChannelAdapter {

    private static final Logger log = LoggerFactory.getLogger(ZjPaymentAdapter.class);

    @Override
    public IPaymentChannel.ChannelMeta getMeta() {
        return new IPaymentChannel.ChannelMeta(
                "zj_payment",
                "中金支付",
                "1.0.0",
                "[\"ecommerce\",\"app\"]",
                false
        );
    }

    @Override
    public void init() {
        log.info("初始化中金支付适配器");
        this.successRate = 0.94;
        this.baseLatencyMs = 180;
    }

    @Override
    protected String buildPayData(String thirdOrderNo, Map<String, Object> context) {
        Map<String, String> data = new HashMap<>();
        String merchantNo = (String) config.get("merchant_no");

        data.put("merchant_no", merchantNo != null ? merchantNo : "zj_mch_001");
        data.put("order_no", thirdOrderNo);
        data.put("amount", "0.01");
        String callbackUrl = (String) config.get("callback_url");
        data.put("callback_url", callbackUrl != null ? callbackUrl : "");
        data.put("timestamp", String.valueOf(System.currentTimeMillis()));

        String sign = SignUtil.sign(data, (String) config.get("api_secret"));
        data.put("sign", sign);

        return JSONObject.toJSONString(data);
    }

    @Override
    public boolean verifyCallback(Map<String, String> callbackParams) {
        String sign = callbackParams.get("sign");
        if (sign == null) {
            return false;
        }
        String timestamp = callbackParams.get("timestamp");
        String merchantNo = callbackParams.get("merchant_no");
        String orderNo = callbackParams.get("order_no");

        Map<String, String> params = new HashMap<>();
        params.put("merchant_no", merchantNo);
        params.put("order_no", orderNo);
        params.put("timestamp", timestamp);

        String expectedSign = SignUtil.sign(params, (String) config.get("api_secret"));
        return expectedSign.equals(sign);
    }
}
