package com.hioas.demo.service.impl;

import com.hioas.demo.entity.Transaction;
import com.hioas.demo.entity.Refund;
import com.hioas.demo.entity.ChannelInstance;
import com.hioas.demo.entity.StrategyRule;
import com.hioas.demo.entity.PaymentStrategy;
import com.mybatisflex.core.query.QueryWrapper;
import com.hioas.demo.mapper.TransactionMapper;
import com.hioas.demo.mapper.RefundMapper;
import com.hioas.demo.service.PaymentService;
import com.hioas.demo.dto.*;
import com.hioas.demo.channel.IPaymentChannel;
import com.hioas.demo.channel.config.ChannelAdapterRegistry;
import com.hioas.demo.service.ChannelService;
import com.hioas.demo.service.AuthService;
import com.hioas.demo.utils.IdGenerator;
import com.hioas.demo.utils.JsonUtils;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final TransactionMapper transactionMapper;
    private final RefundMapper refundMapper;
    private final ChannelAdapterRegistry registry;
    private final ChannelService channelService;
    private final AuthService authService;
    private final PaymentRouter router;

    public PaymentServiceImpl(TransactionMapper transactionMapper, RefundMapper refundMapper,
                              ChannelAdapterRegistry registry, ChannelService channelService,
                              AuthService authService, PaymentRouter router) {
        this.transactionMapper = transactionMapper;
        this.refundMapper = refundMapper;
        this.registry = registry;
        this.channelService = channelService;
        this.authService = authService;
        this.router = router;
    }

    @Override
    @Transactional
    public PaymentOrderResponse createOrder(PaymentOrderRequest request) {
        Transaction existing = transactionMapper.selectByMerchantOrderNo(request.getMerchantOrderNo());
        if (existing != null) {
            log.info("检测到重复订单请求: merchantOrderNo={}", request.getMerchantOrderNo());
            return buildOrderResponse(existing);
        }

        Transaction tx = new Transaction();
        tx.setId(IdGenerator.generateId());
        tx.setTransactionNo(IdGenerator.generateTradeNo());
        tx.setMerchantOrderNo(request.getMerchantOrderNo());
        tx.setAmount(request.getAmount().multiply(new BigDecimal("100")));
        tx.setCurrency(request.getCurrency() != null ? request.getCurrency() : "CNY");
        tx.setOrderType(1);
        tx.setStatus(0);
        tx.setScene(request.getScene());
        tx.setRiskLevel(request.getRiskLevel() != null ? request.getRiskLevel() : 0);
        tx.setMetadata(JsonUtils.toJson(request.getMetadata()));
        tx.setCreatedAt(LocalDateTime.now());
        tx.setUpdatedAt(LocalDateTime.now());

        transactionMapper.insert(tx);
        log.info("支付订单创建: transactionNo={}, merchantOrderNo={}, amount={}", 
                tx.getTransactionNo(), request.getMerchantOrderNo(), tx.getAmount());

        return buildOrderResponse(tx);
    }

    @Override
    public Transaction getOrderByTransactionNo(String orderNo) {
        return transactionMapper.selectByTransactionNo(orderNo);
    }

    @Override
    public PaymentOrderResponse queryOrder(String orderNo) {
        Transaction tx = transactionMapper.selectByTransactionNo(orderNo);
        if (tx == null) {
            throw new RuntimeException("订单不存在");
        }
        return buildOrderResponse(tx);
    }

    @Override
    public PageResult<Transaction> getTransactionList(Long appId, Integer status, Integer page, Integer size) {
        List<Transaction> all = transactionMapper.selectByAppIdAndStatus(appId, status);
        long total = all.size();
        int pageSize = size != null ? size : 20;
        int pageNum = page != null ? page : 1;
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, all.size());
        List<Transaction> list = fromIndex < toIndex ? all.subList(fromIndex, toIndex) : Collections.emptyList();
        return new PageResult<>(list, total, pageNum, pageSize, (int)Math.ceil(total / (double)pageSize));
    }

    /**
     * 执行支付核心流程
     */
    @Transactional
    public PaymentExecutionResult executePayment(Long appId, Long userId, String merchantOrderNo,
                                                  BigDecimal amount, String currency, String scene,
                                                  Integer riskLevel, String region, String device,
                                                  Map<String, Object> contextMap) {
        log.info("执行支付: appId={}, userId={}, merchantOrderNo={}", appId, userId, merchantOrderNo);

        Transaction tx = transactionMapper.selectByMerchantOrderNo(merchantOrderNo);
        if (tx == null) {
            throw new RuntimeException("订单不存在");
        }

        if (tx.getStatus() == 2) {
            return new PaymentExecutionResult(tx, true, null, null, false);
        }
        if (tx.getStatus() == 3) {
            throw new RuntimeException("订单已失败");
        }

        // 更新为处理中
        tx.setStatus(1);
        tx.setAppId(appId);
        tx.setUserId(userId);
        tx.setUpdatedAt(LocalDateTime.now());
        transactionMapper.update(tx);

        // 路由选择
        PaymentRouter.PaymentOrderRequestContext routingCtx = new PaymentRouter.PaymentOrderRequestContext();
        routingCtx.setUserId(userId);
        routingCtx.setAppId(appId);
        routingCtx.setMerchantOrderNo(merchantOrderNo);
        routingCtx.setAmount(amount);
        routingCtx.setScene(scene);
        routingCtx.setRiskLevel(riskLevel);
        routingCtx.setRegion(region);
        routingCtx.setDevice(device);

        PaymentRouter.RoutingResult routingResult = router.route(appId, routingCtx);

        if (!routingResult.isSuccess()) {
            tx.setStatus(3);
            tx.setFailureReason("路由失败: " + routingResult.getReason());
            tx.setUpdatedAt(LocalDateTime.now());
            transactionMapper.update(tx);
            return new PaymentExecutionResult(tx, false, null, null, false);
        }

        // 检查授权
        if ("FALLBACK".equals(routingResult.getReason())) {
            ChannelInstance inst = routingResult.getSelectedInstance();
            if (!authService.isAuthorized(userId, appId, inst.getChannelCode())) {
                return new PaymentExecutionResult(tx, true, inst, routingResult.getSelectedAdapter(), true);
            }
        }

        // 执行支付
        return executeChannelPayment(tx, routingResult.getSelectedInstance(),
                routingResult.getSelectedAdapter(), contextMap, userId, appId, merchantOrderNo,
                amount, currency, scene, riskLevel, region, device);
    }

    private PaymentExecutionResult executeChannelPayment(Transaction tx, ChannelInstance instance,
                                                          IPaymentChannel adapter, Map<String, Object> contextMap,
                                                          Long userId, Long appId, String merchantOrderNo,
                                                          BigDecimal amount, String currency, String scene,
                                                          Integer riskLevel, String region, String device) {
        PaymentOrderRequest payReq = new PaymentOrderRequest();
        payReq.setMerchantOrderNo(merchantOrderNo);
        payReq.setAmount(amount);
        payReq.setCurrency(currency != null ? currency : "CNY");
        payReq.setScene(scene);

        IPaymentChannel.ChannelOrderResult orderResult = adapter.createOrder(payReq);
        IPaymentChannel.PayResult payResult = adapter.pay(orderResult, contextMap);

        if (payResult.isSuccess()) {
            tx.setStatus(2);
            tx.setChannelCode(instance.getChannelCode());
            tx.setChannelInstanceId(instance.getId());
            tx.setThirdOrderNo(orderResult.getThirdOrderNo());
            tx.setPaidAt(LocalDateTime.now());
            tx.setUpdatedAt(LocalDateTime.now());
            transactionMapper.update(tx);
            updateUserChannelBinding(userId, appId, instance.getChannelCode());
            log.info("支付成功: txNo={}, channel={}, thirdNo={}", 
                    tx.getTransactionNo(), instance.getChannelCode(), orderResult.getThirdOrderNo());
            return new PaymentExecutionResult(tx, true, instance, adapter, false);
        } else {
            tx.setStatus(3);
            tx.setFailureReason(payResult.getErrorCode() + ": " + payResult.getErrorMessage());
            tx.setUpdatedAt(LocalDateTime.now());
            transactionMapper.update(tx);
            log.error("支付失败: txNo={}, errorCode={}", tx.getTransactionNo(), payResult.getErrorCode());

            if (canFallback(appId, userId, amount, currency, scene, riskLevel, region, device, contextMap)) {
                return tryFallback(tx, userId, appId, amount, currency, scene, riskLevel, region, device, contextMap);
            }
            return new PaymentExecutionResult(tx, false, null, null, false);
        }
    }

    private boolean canFallback(Long appId, Long userId, BigDecimal amount, String currency, 
                                 String scene, Integer riskLevel, String region, String device,
                                 Map<String, Object> contextMap) {
        return true;
    }

    private PaymentExecutionResult tryFallback(Transaction failedTx, Long userId, Long appId,
                                                BigDecimal amount, String currency, String scene,
                                                Integer riskLevel, String region, String device,
                                                Map<String, Object> contextMap) {
        PaymentStrategy strategy = router.getActiveStrategyForApp(appId);
        if (strategy == null) {
            return new PaymentExecutionResult(failedTx, false, null, null, false);
        }

        List<StrategyRule> rules = channelService.getStrategyRules(strategy.getId());
        if (rules == null || rules.isEmpty()) {
            return new PaymentExecutionResult(failedTx, false, null, null, false);
        }

        int maxAttempts = 3;
        for (StrategyRule rule : rules) {
            if (!matchesCondition(rule, null, amount, scene, riskLevel, region, device)) continue;

            List<String> candidateCodes = parseChannelCodes(rule.getChannels());
            List<ChannelInstance> allInstances = channelService.getInstanceList(appId);
            List<ChannelInstance> candidates = allInstances.stream()
                    .filter(inst -> candidateCodes.contains(inst.getChannelCode()))
                    .filter(inst -> inst.getStatus() == 3 || inst.getStatus() == 1)
                    .filter(inst -> !inst.getChannelCode().equals(failedTx.getChannelCode()))
                    .collect(Collectors.toList());

            for (ChannelInstance inst : candidates) {
                if (maxAttempts <= 0) break;
                maxAttempts--;

                if (!authService.isAuthorized(userId, appId, inst.getChannelCode())) {
                    continue;
                }

                IPaymentChannel chAdapter = registry.getChannel(inst.getChannelCode());
                if (chAdapter == null || !chAdapter.getHealth().isHealthy()) continue;

                log.info("尝试备选通道: channelCode={}", inst.getChannelCode());
                try {
                    PaymentOrderRequest payReq = new PaymentOrderRequest();
                    payReq.setMerchantOrderNo(failedTx.getMerchantOrderNo());
                    payReq.setAmount(amount);
                    payReq.setCurrency(currency);
                    payReq.setScene(scene);

                    IPaymentChannel.ChannelOrderResult orderResult = chAdapter.createOrder(payReq);
                    IPaymentChannel.PayResult payResult = chAdapter.pay(orderResult, contextMap);

                    if (payResult.isSuccess()) {
                        failedTx.setStatus(2);
                        failedTx.setChannelCode(inst.getChannelCode());
                        failedTx.setChannelInstanceId(inst.getId());
                        failedTx.setThirdOrderNo(orderResult.getThirdOrderNo());
                        failedTx.setPaidAt(LocalDateTime.now());
                        failedTx.setUpdatedAt(LocalDateTime.now());
                        transactionMapper.update(failedTx);
                        updateUserChannelBinding(userId, appId, inst.getChannelCode());
                        log.info("备选通道支付成功: channelCode={}, thirdNo={}", 
                                inst.getChannelCode(), orderResult.getThirdOrderNo());
                        return new PaymentExecutionResult(failedTx, true, inst, chAdapter, false);
                    }
                } catch (Exception e) {
                    log.error("备选通道支付异常", e);
                }
            }
        }

        log.error("所有备选通道支付失败");
        return new PaymentExecutionResult(failedTx, false, null, null, false);
    }

    private boolean matchesCondition(StrategyRule rule, PaymentRouter.PaymentOrderRequestContext ctx,
                                      BigDecimal amount, String scene, Integer riskLevel, String region, String device) {
        JSONObject condition = JSONObject.parseObject(rule.getCondition());

        if (condition.containsKey("amountRange")) {
            List<Double> range = condition.getJSONArray("amountRange").toJavaList(Double.class);
            if (range != null && !range.isEmpty()) {
                double min = range.get(0);
                double max = range.size() > 1 && range.get(1) != null ? range.get(1) : Double.MAX_VALUE;
                double amt = amount.doubleValue();
                if (amt < min || amt > max) return false;
            }
        }

        if (condition.containsKey("scene") && condition.getString("scene") != null) {
            if (!condition.getString("scene").equals(scene)) return false;
        }

        if (condition.containsKey("region") && condition.getString("region") != null) {
            if (!condition.getString("region").equals(region)) return false;
        }

        if (condition.containsKey("device") && condition.getString("device") != null) {
            if (!condition.getString("device").equals(device)) return false;
        }

        if (condition.containsKey("riskLevel")) {
            int rl = condition.getInteger("riskLevel");
            if (rl != (riskLevel != null ? riskLevel : 0)) return false;
        }

        return true;
    }

    private List<String> parseChannelCodes(String channelsJson) {
        if (channelsJson == null || channelsJson.isEmpty()) return Collections.emptyList();
        List<String> codes = JSONObject.parseArray(channelsJson, String.class);
        return codes != null ? codes : Collections.emptyList();
    }

    private void updateUserChannelBinding(Long userId, Long appId, String channelCode) {
        log.info("更新用户通道绑定: userId={}, appId={}, channelCode={}", userId, appId, channelCode);
    }

    @Override
    public boolean cancelOrder(String orderNo) {
        Transaction tx = transactionMapper.selectByTransactionNo(orderNo);
        if (tx == null) return false;
        // 未路由通道的订单(如 PENDING 未执行)无法在通道侧撤销
        if (tx.getChannelCode() == null) return false;
        IPaymentChannel adapter = registry.getChannel(tx.getChannelCode());
        if (adapter == null) return false;
        return adapter.cancel(tx.getThirdOrderNo());
    }

    @Override
    @Transactional
    public RefundResponse refund(RefundRequest request) {
        Transaction originalTx = transactionMapper.selectByTransactionNo(request.getOrderNo());
        if (originalTx == null) {
            throw new RuntimeException("原交易不存在");
        }
        if (originalTx.getStatus() != 2) {
            throw new RuntimeException("原交易未支付成功");
        }

        Refund refund = new Refund();
        refund.setId(IdGenerator.generateId());
        refund.setRefundNo(IdGenerator.generateRefundNo());
        refund.setTransactionId(originalTx.getId());
        refund.setAppId(originalTx.getAppId());
        refund.setUserId(originalTx.getUserId());
        refund.setAmount(request.getAmount().multiply(new BigDecimal("100")));
        refund.setReason(request.getReason());
        refund.setStatus(0);
        refund.setCreatedAt(LocalDateTime.now());
        refund.setUpdatedAt(LocalDateTime.now());
        refundMapper.insert(refund);

        String refundChannelCode = originalTx.getChannelCode();
        IPaymentChannel adapter = registry.getChannel(refundChannelCode);
        if (adapter == null) {
            adapter = findAlternativeRefundChannel(originalTx, request);
        }

        if (adapter == null) {
            refund.setStatus(3);
            refund.setFailureReason("无可用退款通道");
            refund.setUpdatedAt(LocalDateTime.now());
            refundMapper.update(refund);
            return buildRefundResponse(refund, null);
        }

        try {
            IPaymentChannel.RefundResult result = adapter.refund(request);
            if (result.isSuccess()) {
                refund.setStatus(2);
                refund.setChannelCode(refundChannelCode);
                refund.setThirdRefundNo(result.getThirdRefundNo());
                refund.setRefundedAt(LocalDateTime.now());
                refund.setUpdatedAt(LocalDateTime.now());
                refundMapper.update(refund);
                log.info("退款成功: refundNo={}, channel={}, thirdRefundNo={}", 
                        refund.getRefundNo(), refundChannelCode, result.getThirdRefundNo());
                return buildRefundResponse(refund, result.getThirdRefundNo());
            } else {
                refund.setStatus(3);
                refund.setFailureReason(result.getErrorCode() + ": " + result.getErrorMessage());
                refund.setUpdatedAt(LocalDateTime.now());
                refundMapper.update(refund);
                return buildRefundResponse(refund, null);
            }
        } catch (Exception e) {
            log.error("退款异常", e);
            refund.setStatus(3);
            refund.setFailureReason(e.getMessage());
            refund.setUpdatedAt(LocalDateTime.now());
            refundMapper.update(refund);
            return buildRefundResponse(refund, null);
        }
    }

    @Override
    public Refund getRefundByNo(String refundNo) {
        return refundMapper.selectOneByQuery(QueryWrapper.create().where("refund_no = ?", refundNo));
    }

    @Override
    public Transaction getTransactionById(Long id) {
        return transactionMapper.selectOneById(id);
    }

    private IPaymentChannel findAlternativeRefundChannel(Transaction originalTx, RefundRequest request) {
        Long userId = originalTx.getUserId();
        Long appId = originalTx.getAppId();
        List<ChannelInstance> instances = channelService.getInstanceList(appId);
        for (ChannelInstance inst : instances) {
            if (!inst.getChannelCode().equals(originalTx.getChannelCode())) {
                if (authService.isAuthorized(userId, appId, inst.getChannelCode())) {
                    IPaymentChannel adapter = registry.getChannel(inst.getChannelCode());
                    if (adapter != null) {
                        log.info("找到替代退款通道: channelCode={}", inst.getChannelCode());
                        return adapter;
                    }
                }
            }
        }
        return null;
    }

    private RefundResponse buildRefundResponse(Refund refund, String thirdRefundNo) {
        RefundResponse resp = new RefundResponse();
        resp.setRefundId(refund.getId());
        resp.setRefundNo(refund.getRefundNo());
        Transaction tx = transactionMapper.selectOneById(refund.getTransactionId());
        resp.setOrderNo(tx != null ? tx.getTransactionNo() : refund.getTransactionId().toString());
        resp.setAmount(refund.getAmount().divide(new BigDecimal("100")).longValue());
        resp.setStatus(refund.getStatus() == 2 ? "SUCCESS" : "FAILED");
        resp.setChannelCode(refund.getChannelCode());
        resp.setThirdRefundNo(thirdRefundNo);
        resp.setCreatedAt(refund.getCreatedAt() != null ? refund.getCreatedAt().toString() : null);
        return resp;
    }

    private PaymentOrderResponse buildOrderResponse(Transaction tx) {
        PaymentOrderResponse resp = new PaymentOrderResponse();
        resp.setOrderId(tx.getId());
        resp.setOrderNo(tx.getTransactionNo());
        resp.setAmount(tx.getAmount().divide(new BigDecimal("100")).longValue());
        resp.setCurrency(tx.getCurrency());
        resp.setStatus(mapStatus(tx.getStatus()));
        resp.setPaidAt(tx.getPaidAt() != null ? tx.getPaidAt().toString() : null);
        resp.setCreatedAt(tx.getCreatedAt() != null ? tx.getCreatedAt().toString() : null);
        return resp;
    }

    private String mapStatus(Integer status) {
        if (status == 0) return "PENDING";
        if (status == 1) return "PROCESSING";
        if (status == 2) return "SUCCESS";
        if (status == 3) return "FAILED";
        if (status == 5) return "CLOSED";
        return "UNKNOWN";
    }

    public static class PaymentExecutionResult {
        private Transaction transaction;
        private boolean success;
        private ChannelInstance channelInstance;
        private IPaymentChannel adapter;
        private boolean needsAuth;

        public PaymentExecutionResult(Transaction tx, boolean success, ChannelInstance inst, 
                                       IPaymentChannel adapter, boolean needsAuth) {
            this.transaction = tx;
            this.success = success;
            this.channelInstance = inst;
            this.adapter = adapter;
            this.needsAuth = needsAuth;
        }

        public Transaction getTransaction() { return transaction; }
        public boolean isSuccess() { return success; }
        public ChannelInstance getChannelInstance() { return channelInstance; }
        public IPaymentChannel getAdapter() { return adapter; }
        public boolean isNeedsAuth() { return needsAuth; }
    }
}
