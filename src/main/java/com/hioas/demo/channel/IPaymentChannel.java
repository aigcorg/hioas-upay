package com.hioas.demo.channel;

import com.hioas.demo.dto.PaymentOrderRequest;
import com.hioas.demo.dto.RefundRequest;

import java.util.Map;

/**
 * 支付通道统一接口
 * 所有第三方支付通道适配器都必须实现此接口
 */
public interface IPaymentChannel {

    /**
     * 通道元信息
     */
    ChannelMeta getMeta();

    /**
     * 通道初始化（加载配置、预热连接等）
     */
    void init();

    /**
     * 创建第三方支付订单（返回预支付编号或 third order no）
     */
    ChannelOrderResult createOrder(PaymentOrderRequest request);

    /**
     * 执行支付（对于需要跳转/唤起的渠道，返回跳转信息）
     * 对于 JSAPI/App 类型，返回预支付数据供前端调用
     */
    PayResult pay(ChannelOrderResult orderResult, Map<String, Object> context);

    /**
     * 查询订单状态
     */
    TransactionQueryResult queryOrder(String thirdOrderNo);

    /**
     * 退款
     */
    RefundResult refund(RefundRequest request);

    /**
     * 交易撤销（仅适用于部分渠道）
     */
    boolean cancel(String thirdOrderNo);

    /**
     * 回调通知验证
     */
    boolean verifyCallback(Map<String, String> callbackParams);

    /**
     * 获取渠道健康状态
     */
    ChannelHealth getHealth();

    /**
     * 获取实例配置信息
     */
    Map<String, Object> getConfig();

    /**
     * 通道元信息
     */
    @SuppressWarnings("unused")
    class ChannelMeta {
        private String code;
        private String name;
        private String version;
        private String scene;
        private boolean requiresCertificate;

        public ChannelMeta(String code, String name, String version, String scene, boolean requiresCertificate) {
            this.code = code;
            this.name = name;
            this.version = version;
            this.scene = scene;
            this.requiresCertificate = requiresCertificate;
        }

        public String getCode() { return code; }
        public String getName() { return name; }
        public String getVersion() { return version; }
        public String getScene() { return scene; }
        public boolean isRequiresCertificate() { return requiresCertificate; }
    }

    /**
     * 创建订单结果
     */
    @SuppressWarnings("unused")
    class ChannelOrderResult {
        private String thirdOrderNo;
        private String platformPrepayInfo; // JSAPI等类型的预支付信息

        public ChannelOrderResult(String thirdOrderNo, String platformPrepayInfo) {
            this.thirdOrderNo = thirdOrderNo;
            this.platformPrepayInfo = platformPrepayInfo;
        }

        public String getThirdOrderNo() { return thirdOrderNo; }
        public String getPlatformPrepayInfo() { return platformPrepayInfo; }
    }

    /**
     * 支付结果
     */
    @SuppressWarnings("unused")
    class PayResult {
        private boolean success;
        private String thirdOrderNo;
        private String errorCode;
        private String errorMessage;
        private String payData;       // 前端可用的支付数据(JSON字符串)

        public PayResult(boolean success, String thirdOrderNo) {
            this.success = success;
            this.thirdOrderNo = thirdOrderNo;
        }

        public PayResult(boolean success, String errorCode, String errorMessage) {
            this.success = success;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
        }

        public boolean isSuccess() { return success; }
        public String getThirdOrderNo() { return thirdOrderNo; }
        public String getErrorCode() { return errorCode; }
        public String getErrorMessage() { return errorMessage; }
        public String getPayData() { return payData; }
    }

    /**
     * 订单查询结果
     */
    @SuppressWarnings("unused")
    class TransactionQueryResult {
        private String thirdOrderNo;
        private String status;        // SUCCESS, FAIL, PROCESSING
        private Long amount;
        private String errorMessage;

        public TransactionQueryResult(String thirdOrderNo, String status, Long amount, String errorMessage) {
            this.thirdOrderNo = thirdOrderNo;
            this.status = status;
            this.amount = amount;
            this.errorMessage = errorMessage;
        }

        public String getThirdOrderNo() { return thirdOrderNo; }
        public String getStatus() { return status; }
        public Long getAmount() { return amount; }
        public String getErrorMessage() { return errorMessage; }
    }

    /**
     * 退款结果
     */
    @SuppressWarnings("unused")
    class RefundResult {
        private boolean success;
        private String thirdRefundNo;
        private String errorCode;
        private String errorMessage;

        public RefundResult(boolean success, String thirdRefundNo) {
            this.success = success;
            this.thirdRefundNo = thirdRefundNo;
        }

        public RefundResult(boolean success, String errorCode, String errorMessage) {
            this.success = success;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
        }

        public boolean isSuccess() { return success; }
        public String getThirdRefundNo() { return thirdRefundNo; }
        public String getErrorCode() { return errorCode; }
        public String getErrorMessage() { return errorMessage; }
    }

    /**
     * 通道健康状态
     */
    @SuppressWarnings("unused")
    class ChannelHealth {
        private boolean healthy;
        private long latencyMs;
        private String lastError;
        private long lastCheckTime;

        public ChannelHealth(boolean healthy, long latencyMs) {
            this.healthy = healthy;
            this.latencyMs = latencyMs;
            this.lastCheckTime = System.currentTimeMillis();
        }

        public boolean isHealthy() { return healthy; }
        public long getLatencyMs() { return latencyMs; }
        public String getLastError() { return lastError; }
        public long getLastCheckTime() { return lastCheckTime; }
    }
}
