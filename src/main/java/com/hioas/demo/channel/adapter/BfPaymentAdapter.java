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
 * 宝付支付适配器
 * 专注于直播、社交场景的支付方案
 */
public class BfPaymentAdapter extends SimulatedChannelAdapter {

    private static final Logger log = LoggerFactory.getLogger(BfPaymentAdapter.class);

    @Override
    public IPaymentChannel.ChannelMeta getMeta() {
        return new IPaymentChannel.ChannelMeta(
                "bf_payment",
                "宝付支付",
                "1.0.0",
                "[\"ecommerce\",\"live\"]",
                false
        );
    }

    @Override
    public void init() {
        log.info("初始化宝付支付适配器");
        this.successRate = 0.95;
        this.baseLatencyMs = 120;
    }

    @Override
    protected String buildPayData(String thirdOrderNo, Map<String, Object> context) {
        Map<String, String> data = new HashMap<>();
        String bfMerchantId = (String) config.get("bf_merchant_id");

        data.put("bf_merchant_id", bfMerchantId != null ? bfMerchantId : "bf_001");
        data.put("order_no", thirdOrderNo);
        data.put("scene", (String) config.get("scene"));
        data.put("timestamp", String.valueOf(System.currentTimeMillis()));

        String sign = SignUtil.signBf(data, (String) config.get("bf_secret"));
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
        String bfMerchantId = callbackParams.get("bf_merchant_id");
        String orderNo = callbackParams.get("order_no");

        Map<String, String> params = new HashMap<>();
        params.put("bf_merchant_id", bfMerchantId);
        params.put("order_no", orderNo);
        params.put("timestamp", timestamp);

        String expectedSign = SignUtil.signBf(params, (String) config.get("bf_secret"));
        return expectedSign.equals(sign);
    }
}
