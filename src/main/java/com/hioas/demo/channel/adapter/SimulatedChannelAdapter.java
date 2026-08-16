package com.hioas.demo.channel.adapter;

import com.hioas.demo.channel.IPaymentChannel;
import com.hioas.demo.dto.PaymentOrderRequest;
import com.hioas.demo.dto.RefundRequest;
import com.hioas.demo.utils.RandomUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模拟支付通道适配器基类
 * 用于演示和测试，不依赖真实第三方 API
 * 所有模拟通道继承此类，提供统一的模拟行为
 */
public abstract class SimulatedChannelAdapter extends AbstractPaymentChannelAdapter {

    // 模拟成功率
    protected double successRate = 0.95;

    // 模拟延迟 (毫秒)
    protected long baseLatencyMs = 100;

    // 存储模拟的第三方订单号
    protected final Map<String, String> thirdOrderNos = new ConcurrentHashMap<>();

    /**
     * 模拟创建订单
     */
    @Override
    public IPaymentChannel.ChannelOrderResult createOrder(PaymentOrderRequest request) {
        log.info("[{}] 创建模拟订单: merchantOrderNo={}, amount={}", 
                getMeta().getCode(), request.getMerchantOrderNo(), request.getAmount());

        String thirdOrderNo = getMeta().getCode() + "_" + RandomUtil.alphanumeric(16);
        thirdOrderNos.put(request.getMerchantOrderNo(), thirdOrderNo);

        return new IPaymentChannel.ChannelOrderResult(thirdOrderNo, null);
    }

    /**
     * 模拟支付执行
     */
    @Override
    public IPaymentChannel.PayResult pay(IPaymentChannel.ChannelOrderResult orderResult, Map<String, Object> context) {
        String thirdOrderNo = orderResult.getThirdOrderNo();
        log.info("[{}] 模拟支付请求: thirdOrderNo={}", getMeta().getCode(), thirdOrderNo);

        // 模拟延迟
        try {
            long latency = baseLatencyMs + (long)(Math.random() * 100);
            Thread.sleep(latency);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 模拟成功/失败
        if (Math.random() < successRate) {
            String platformPrepayInfo = buildPayData(thirdOrderNo, context);
            log.info("[{}] 模拟支付成功: thirdOrderNo={}", getMeta().getCode(), thirdOrderNo);
            return new IPaymentChannel.PayResult(true, thirdOrderNo);
        } else {
            log.warn("[{}] 模拟支付失败: thirdOrderNo={}, reason=模拟失败", getMeta().getCode(), thirdOrderNo);
            return new IPaymentChannel.PayResult(false, "SIMULATED_FAIL", "模拟支付失败");
        }
    }

    /**
     * 模拟查询订单
     */
    @Override
    public IPaymentChannel.TransactionQueryResult queryOrder(String thirdOrderNo) {
        log.info("[{}] 模拟查询订单: thirdOrderNo={}", getMeta().getCode(), thirdOrderNo);
        return new IPaymentChannel.TransactionQueryResult(thirdOrderNo, "SUCCESS", null, null);
    }

    /**
     * 模拟退款
     */
    @Override
    public IPaymentChannel.RefundResult refund(RefundRequest request) {
        log.info("[{}] 模拟退款: merchantRefundNo={}, orderNo={}, amount={}", 
                getMeta().getCode(), request.getMerchantRefundNo(), request.getOrderNo(), request.getAmount());

        try {
            Thread.sleep(baseLatencyMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (Math.random() < successRate) {
            String thirdRefundNo = getMeta().getCode() + "_REFUND_" + RandomUtil.alphanumeric(12);
            return new IPaymentChannel.RefundResult(true, thirdRefundNo);
        } else {
            return new IPaymentChannel.RefundResult(false, "REFUND_FAIL", "模拟退款失败");
        }
    }

    @Override
    public boolean cancel(String thirdOrderNo) {
        log.info("[{}] 模拟撤销订单: thirdOrderNo={}", getMeta().getCode(), thirdOrderNo);
        return true;
    }

    @Override
    public boolean verifyCallback(Map<String, String> callbackParams) {
        // 模拟回调验证：只要有sign参数就认为验证通过
        return callbackParams.containsKey("sign") && callbackParams.containsKey("third_order_no");
    }

    @Override
    public IPaymentChannel.ChannelHealth getHealth() {
        long latency = baseLatencyMs + (long)(Math.random() * 200);
        return new IPaymentChannel.ChannelHealth(true, latency);
    }

    /**
     * 子类重写以提供不同的 payData 格式
     */
    protected abstract String buildPayData(String thirdOrderNo, Map<String, Object> context);
}
