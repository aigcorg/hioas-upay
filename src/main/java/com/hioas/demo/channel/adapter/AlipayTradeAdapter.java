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
 * 支付宝电脑网站支付适配器
 * 参考文档: https://docs.open.alipay.com/20314/181260
 */
public class AlipayTradeAdapter extends SimulatedChannelAdapter {

    private static final Logger log = LoggerFactory.getLogger(AlipayTradeAdapter.class);
    private static final String SIGN_TYPE = "RSA2";

    @Override
    public IPaymentChannel.ChannelMeta getMeta() {
        return new IPaymentChannel.ChannelMeta(
                "alipay_trade",
                "支付宝电脑网站支付",
                "1.0.0",
                "[\"ecommerce\",\"h5\"]",
                true
        );
    }

    @Override
    public void init() {
        log.info("初始化支付宝支付适配器");
        this.successRate = 0.96;
        this.baseLatencyMs = 200;
    }

    @Override
    protected String buildPayData(String thirdOrderNo, Map<String, Object> context) {
        Map<String, String> data = new HashMap<>();
        String appId = (String) config.get("app_id");
        String merchantPrivateKey = (String) config.get("merchant_private_key");

        data.put("app_id", appId != null ? appId : "20210011000012345678");
        data.put("method", "alipay.trade.page.pay");
        data.put("format", "JSON");
        data.put("charset", "utf-8");
        data.put("sign_type", SIGN_TYPE);
        data.put("timestamp", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new java.util.Date()));
        data.put("version", "1.0");

        String bizContent = JSONObject.toJSONString(Map.of(
                "out_trade_no", thirdOrderNo,
                "product_code", "FAST_INSTANT_TRANSACTION",
                "total_amount", "0.01",
                "subject", "测试商品"
        ));
        data.put("biz_content", bizContent);

        // 支付宝使用RSA2签名
        String sign = SignUtil.signAlipay(data, merchantPrivateKey != null ? merchantPrivateKey : "");
        data.put("sign", sign);

        return JSONObject.toJSONString(data);
    }

    @Override
    public boolean verifyCallback(Map<String, String> callbackParams) {
        String notifyId = callbackParams.get("notify_id");
        String sign = callbackParams.get("sign");
        if (notifyId == null || sign == null) {
            return false;
        }
        // 实际使用支付宝公钥验签,此处简化
        return true;
    }
}
