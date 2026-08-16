package com.hioas.demo.service.impl;

import com.hioas.demo.entity.PaymentStrategy;
import com.hioas.demo.entity.StrategyRule;
import com.hioas.demo.entity.ChannelInstance;
import com.hioas.demo.service.ChannelService;
import com.hioas.demo.service.AuthService;
import com.hioas.demo.channel.IPaymentChannel;
import com.hioas.demo.channel.config.ChannelAdapterRegistry;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 支付路由引擎
 * 根据策略规则和实时通道状态，动态选择最佳支付通道
 */
@Component
public class PaymentRouter {

    private static final Logger log = LoggerFactory.getLogger(PaymentRouter.class);

    private final ChannelAdapterRegistry registry;
    private final ChannelService channelService;
    private final AuthService authService;

    public PaymentRouter(ChannelAdapterRegistry registry, ChannelService channelService, AuthService authService) {
        this.registry = registry;
        this.channelService = channelService;
        this.authService = authService;
    }

    /**
     * 根据应用ID和支付请求条件，选择最佳通道
     * 返回选中的ChannelInstance和IPaymentChannel适配器
     */
    public RoutingResult route(Long appId, PaymentOrderRequestContext context) {
        // 获取已发布策略
        PaymentStrategy strategy = getActiveStrategy(appId);
        if (strategy == null) {
            log.error("应用[{}]未找到已发布的支付策略", appId);
            return RoutingResult.noStrategy();
        }

        // 获取所有已配置的通道实例
        List<ChannelInstance> allInstances = channelService.getInstanceList(appId);
        if (allInstances.isEmpty()) {
            log.error("应用[{}]未配置任何支付通道实例", appId);
            return RoutingResult.noChannel();
        }

        // 按策略规则匹配
        List<StrategyRule> rules = getStrategyRules(strategy);
        for (StrategyRule rule : rules) {
            if (matchesCondition(rule, context)) {
                log.info("策略[{}]规则[{}]匹配成功: priority={}, channels={}", 
                        strategy.getId(), rule.getId(), rule.getPriority(), rule.getChannels());

                // 从候选通道列表中选择最佳通道
                List<String> candidateCodes = parseChannelCodes(rule.getChannels());
                List<ChannelInstance> candidates = allInstances.stream()
                        .filter(instance -> candidateCodes.contains(instance.getChannelCode()))
                        .filter(instance -> instance.getStatus() == 3 || instance.getStatus() == 1)  // 正常或已配置
                        .collect(Collectors.toList());

                if (candidates.isEmpty()) {
                    log.warn("策略[{}]规则[{}]的候选通道均不可用", strategy.getId(), rule.getId());
                    continue;
                }

                // 按排序方式排序
                List<ChannelInstance> sorted = sortByRule(candidates, rule.getSortBy());

                // 遍历候选通道,找到第一个可用的 (健康 + 用户已授权)
                for (ChannelInstance instance : sorted) {
                    if (isChannelHealthy(instance) && authService.isAuthorized(context.getUserId(), appId, instance.getChannelCode())) {
                        IPaymentChannel adapter = registry.getChannel(instance.getChannelCode());
                        if (adapter != null) {
                            log.info("路由选择通道: instanceId={}, channelCode={}, name={}", 
                                    instance.getId(), instance.getChannelCode(), instance.getInstanceName());
                            return RoutingResult.success(instance, adapter);
                        }
                    }
                }

                // 如果有候选通道但都未授权,返回第一个候选实例作为fallback (调用方决定是否需要授权)
                ChannelInstance firstInstance = sorted.get(0);
                IPaymentChannel adapter = registry.getChannel(firstInstance.getChannelCode());
                return RoutingResult.fallback(firstInstance, adapter);
            }
        }

        log.error("应用[{}]没有匹配的策略规则", appId);
        return RoutingResult.noRuleMatch();
    }

    private PaymentStrategy getActiveStrategy(Long appId) {
        return channelService.getStrategyByAppId(appId);
    }

    private List<StrategyRule> getStrategyRules(PaymentStrategy strategy) {
        return channelService.getStrategyRules(strategy.getId());
    }

    private boolean matchesCondition(StrategyRule rule, PaymentOrderRequestContext context) {
        JSONObject condition = JSONObject.parseObject(rule.getCondition());
        
        // 金额区间校验
        if (condition.containsKey("amountRange")) {
            List<Double> range = condition.getJSONArray("amountRange").toJavaList(Double.class);
            double min = range.get(0);
            double max = range.size() > 1 && range.get(1) != null ? range.get(1) : Double.MAX_VALUE;
            double amount = context.getAmount().doubleValue();
            if (amount < min || amount > max) {
                return false;
            }
        }

        // 场景校验
        if (condition.containsKey("scene") && condition.getString("scene") != null) {
            if (!condition.getString("scene").equals(context.getScene())) {
                return false;
            }
        }

        // 地域校验 (可选)
        if (condition.containsKey("region") && condition.getString("region") != null) {
            if (!condition.getString("region").equals(context.getRegion())) {
                return false;
            }
        }

        // 设备类型校验
        if (condition.containsKey("device") && condition.getString("device") != null) {
            if (!condition.getString("device").equals(context.getDevice())) {
                return false;
            }
        }

        // 风控等级校验
        if (condition.containsKey("riskLevel")) {
            int riskLevel = condition.getInteger("riskLevel");
            if (riskLevel != context.getRiskLevel()) {
                return false;
            }
        }

        return true;
    }

    private List<ChannelInstance> sortByRule(List<ChannelInstance> candidates, String sortBy) {
        if (sortBy == null || sortBy.isEmpty()) {
            return candidates;
        }

        List<ChannelInstance> sorted = new ArrayList<>(candidates);

        switch (sortBy) {
            case "fee_rate_asc":
                sorted.sort(Comparator.comparingDouble(c -> {
                    JSONObject fees = JSONObject.parseObject(c.getFees());
                    return fees == null ? 0d : fees.getDoubleValue("rate");
                }));
                break;
            case "fee_rate_desc":
                sorted.sort((a, b) -> {
                    JSONObject fa = JSONObject.parseObject(a.getFees());
                    JSONObject fb = JSONObject.parseObject(b.getFees());
                    double ra = fa == null ? 0d : fa.getDoubleValue("rate");
                    double rb = fb == null ? 0d : fb.getDoubleValue("rate");
                    return Double.compare(rb, ra);
                });
                break;
            case "limit_first":
                sorted.sort(Comparator.comparingLong(c -> c.getAmountLimit() != null ? 0 : 1));
                break;
            case "latency_asc":
                // 根据通道健康状态排序
                sorted.sort(Comparator.comparingLong(c -> {
                    IPaymentChannel ch = registry.getChannel(c.getChannelCode());
                    return ch != null && ch.getHealth().isHealthy() ? 0 : 1;
                }));
                break;
            default:
                break;
        }

        return sorted;
    }

    private boolean isChannelHealthy(ChannelInstance instance) {
        IPaymentChannel adapter = registry.getChannel(instance.getChannelCode());
        if (adapter == null) {
            return false;
        }
        return adapter.getHealth().isHealthy();
    }

    private List<String> parseChannelCodes(String channelsJson) {
        if (channelsJson == null || channelsJson.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> codes = JSONObject.parseArray(channelsJson, String.class);
        return codes != null ? codes : Collections.emptyList();
    }

    /**
     * 获取应用的已发布策略
     */
    public PaymentStrategy getActiveStrategyForApp(Long appId) {
        return channelService.getStrategyByAppId(appId);
    }

    /**
     * 路由结果
     */
    @SuppressWarnings("unused")
    public static class RoutingResult {
        private boolean success;
        private String reason;
        private ChannelInstance selectedInstance;
        private IPaymentChannel selectedAdapter;

        private RoutingResult(boolean success, String reason) {
            this.success = success;
            this.reason = reason;
        }

        private RoutingResult(ChannelInstance instance, IPaymentChannel adapter) {
            this.success = true;
            this.selectedInstance = instance;
            this.selectedAdapter = adapter;
        }

        public static RoutingResult success(ChannelInstance instance, IPaymentChannel adapter) {
            return new RoutingResult(instance, adapter);
        }

        public static RoutingResult noStrategy() {
            return new RoutingResult(false, "NO_STRATEGY");
        }

        public static RoutingResult noChannel() {
            return new RoutingResult(false, "NO_CHANNEL");
        }

        public static RoutingResult noRuleMatch() {
            return new RoutingResult(false, "NO_RULE_MATCH");
        }

        public static RoutingResult fallback(ChannelInstance instance, IPaymentChannel adapter) {
            RoutingResult result = new RoutingResult(instance, adapter);
            result.success = true;
            result.reason = "FALLBACK";
            return result;
        }

        public boolean isSuccess() { return success; }
        public String getReason() { return reason; }
        public ChannelInstance getSelectedInstance() { return selectedInstance; }
        public IPaymentChannel getSelectedAdapter() { return selectedAdapter; }
    }

    @SuppressWarnings("unused")
    public static class PaymentOrderRequestContext {
        private Long userId;
        private Long appId;
        private String merchantOrderNo;
        private java.math.BigDecimal amount;
        private String scene;
        private Integer riskLevel;
        private String region;
        private String device;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Long getAppId() { return appId; }
        public void setAppId(Long appId) { this.appId = appId; }
        public String getMerchantOrderNo() { return merchantOrderNo; }
        public void setMerchantOrderNo(String merchantOrderNo) { this.merchantOrderNo = merchantOrderNo; }
        public java.math.BigDecimal getAmount() { return amount; }
        public void setAmount(java.math.BigDecimal amount) { this.amount = amount; }
        public String getScene() { return scene; }
        public void setScene(String scene) { this.scene = scene; }
        public Integer getRiskLevel() { return riskLevel; }
        public void setRiskLevel(Integer riskLevel) { this.riskLevel = riskLevel; }
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        public String getDevice() { return device; }
        public void setDevice(String device) { this.device = device; }
    }
}
