package com.hioas.demo.service.impl;

import com.hioas.demo.entity.Channel;
import com.hioas.demo.entity.ChannelInstance;
import com.hioas.demo.entity.PaymentStrategy;
import com.hioas.demo.entity.StrategyRule;
import com.hioas.demo.mapper.ChannelMapper;
import com.hioas.demo.mapper.ChannelInstanceMapper;
import com.hioas.demo.mapper.PaymentStrategyMapper;
import com.hioas.demo.mapper.StrategyRuleMapper;
import com.hioas.demo.service.ChannelService;
import com.hioas.demo.dto.ChannelInstanceConfig;
import com.hioas.demo.dto.ChannelTestResponse;
import com.hioas.demo.channel.IPaymentChannel;
import com.hioas.demo.channel.config.ChannelConfig;
import com.hioas.demo.channel.config.ChannelAdapterRegistry;
import com.hioas.demo.utils.EncryptUtil;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ChannelServiceImpl implements ChannelService {

    private static final Logger log = LoggerFactory.getLogger(ChannelServiceImpl.class);
    private final ChannelMapper channelMapper;
    private final ChannelInstanceMapper instanceMapper;
    private final PaymentStrategyMapper strategyMapper;
    private final StrategyRuleMapper ruleMapper;
    private final ChannelAdapterRegistry registry;

    public ChannelServiceImpl(ChannelMapper channelMapper, ChannelInstanceMapper instanceMapper,
                              PaymentStrategyMapper strategyMapper, StrategyRuleMapper ruleMapper,
                              ChannelAdapterRegistry registry) {
        this.channelMapper = channelMapper;
        this.instanceMapper = instanceMapper;
        this.strategyMapper = strategyMapper;
        this.ruleMapper = ruleMapper;
        this.registry = registry;
    }

    @Override
    public List<Map<String, Object>> getAvailableChannels() {
        List<Channel> channels = channelMapper.selectAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Channel ch : channels) {
            Map<String, Object> map = new HashMap<>();
            map.put("code", ch.getCode());
            map.put("name", ch.getName());
            map.put("scenes", JSONObject.parseArray(ch.getScenes()));
            map.put("status", ch.getStatus() == 1 ? "AVAILABLE" : "UNAVAILABLE");
            map.put("description", ch.getName() + " - 多场景支付解决方案");
            result.add(map);
        }
        return result;
    }

    @Override
    @Transactional
    public void selectChannels(Long appId, List<String> channelCodes) {
        for (String code : channelCodes) {
            Channel ch = channelMapper.selectOneById(code);
            if (ch == null || ch.getStatus() != 1) {
                throw new RuntimeException("通道 " + code + " 不可用");
            }
            ChannelInstance instance = new ChannelInstance();
            instance.setAppId(appId);
            instance.setChannelCode(code);
            instance.setInstanceName(ch.getName() + " 实例");
            instance.setConfig("{}");
            instance.setFees("{\"rate\":0.006}");
            instance.setAmountLimit("{\"singleMax\":50000,\"dailyMax\":500000}");
            instance.setStatus(0);
            instance.setCreatedAt(LocalDateTime.now());
            instance.setUpdatedAt(LocalDateTime.now());
            instanceMapper.insert(instance);
        }
        log.info("应用[{}]选择了{}个通道", appId, channelCodes.size());
    }

    @Override
    @Transactional
    public ChannelInstance createChannelInstance(Long appId, ChannelInstanceConfig config) {
        String channelCode = config.getChannelCode();
        Channel ch = channelMapper.selectOneById(channelCode);
        if (ch == null) {
            throw new RuntimeException("通道 " + channelCode + " 不存在");
        }

        List<ChannelInstance> existing = instanceMapper.selectByAppIdAndStatus(appId, null);
        long maxId = 0;
        for (ChannelInstance ei : existing) {
            if (ei.getId() > maxId) maxId = ei.getId();
        }

        Map<String, Object> configMap = config.getConfig();
        Map<String, String> encryptedConfig = new HashMap<>();
        if (configMap != null) {
            for (Map.Entry<String, Object> e : configMap.entrySet()) {
                String key = e.getKey();
                String val = String.valueOf(e.getValue());
                if (key.equals("secret") || key.equals("merchant_private_key") ||
                    key.equals("cert_path") || key.equals("key_path") || key.equals("bf_secret")) {
                    encryptedConfig.put(key, EncryptUtil.encrypt(val));
                } else {
                    encryptedConfig.put(key, val);
                }
            }
        }

        ChannelInstance instance = new ChannelInstance();
        instance.setId(maxId + 1);
        instance.setAppId(appId);
        instance.setChannelCode(channelCode);
        instance.setInstanceName(config.getInstanceName());
        instance.setConfig(JSONObject.toJSONString(encryptedConfig));
        instance.setFees(JSONObject.toJSONString(config.getFees()));
        instance.setAmountLimit(JSONObject.toJSONString(config.getAmountLimit()));
        instance.setPriority(1);
        instance.setStatus(1);
        instance.setCreatedAt(LocalDateTime.now());
        instance.setUpdatedAt(LocalDateTime.now());

        instanceMapper.insert(instance);

        // 注入配置到注册表
        try {
            ChannelConfig channelConfig = new ChannelConfig();
            channelConfig.setChannelCode(channelCode);
            channelConfig.setChannelName(ch.getName());
            channelConfig.setAdapterClass(ch.getAdapterClass());
            channelConfig.setInstanceId(instance.getId());
            channelConfig.setAppId(appId);
            channelConfig.setDecryptedConfig(configMap);
            channelConfig.setStatus(instance.getStatus());

            IPaymentChannel existingAdapter = registry.getChannel(channelCode);
            if (existingAdapter != null && existingAdapter instanceof com.hioas.demo.channel.adapter.AbstractPaymentChannelAdapter) {
                ((com.hioas.demo.channel.adapter.AbstractPaymentChannelAdapter) existingAdapter).setConfig(configMap);
            } else {
                IPaymentChannel newAdapter = registry.createChannelFromConfig(channelConfig);
                registry.registerChannel(newAdapter, channelConfig);
            }
            log.info("创建并注册通道实例: instanceId={}, channelCode={}", instance.getId(), channelCode);
        } catch (Exception e) {
            log.error("注册通道实例适配器失败", e);
        }

        return instance;
    }

    @Override
    public List<ChannelInstance> getInstanceList(Long appId) {
        return instanceMapper.selectByAppIdAndStatus(appId, null);
    }

    @Override
    public ChannelTestResponse testChannelInstance(Long instanceId) {
        ChannelInstance instance = instanceMapper.selectOneById(instanceId);
        if (instance == null) {
            throw new RuntimeException("通道实例不存在");
        }
        IPaymentChannel adapter = registry.getChannel(instance.getChannelCode());
        if (adapter == null) {
            return new ChannelTestResponse("FAILED", 0L, "适配器未找到");
        }
        try {
            long startTime = System.currentTimeMillis();
            IPaymentChannel.ChannelHealth health = adapter.getHealth();
            long latency = System.currentTimeMillis() - startTime;
            if (health.isHealthy()) {
                return new ChannelTestResponse("CONNECTED", latency, "测试成功");
            } else {
                return new ChannelTestResponse("FAILED", latency, health.getLastError());
            }
        } catch (Exception e) {
            log.error("测试通道实例失败", e);
            return new ChannelTestResponse("FAILED", 0L, e.getMessage());
        }
    }

    @Override
    public boolean deleteChannelInstance(Long instanceId) {
        ChannelInstance instance = instanceMapper.selectOneById(instanceId);
        if (instance == null) return false;
        registry.unregisterChannel(instance.getChannelCode());
        return instanceMapper.deleteById(instanceId) > 0;
    }

    @Override
    public IPaymentChannel getChannelAdapter(Long instanceId) {
        ChannelInstance instance = instanceMapper.selectOneById(instanceId);
        if (instance == null) return null;
        return registry.getChannel(instance.getChannelCode());
    }

    @Override
    public ChannelInstance getInstance(Long instanceId) {
        return instanceMapper.selectOneById(instanceId);
    }

    @Override
    public PaymentStrategy getStrategyByAppId(Long appId) {
        List<PaymentStrategy> published = strategyMapper.selectPublishedByAppId(appId);
        if (published.isEmpty()) return null;
        return published.stream()
                .max(Comparator.comparingInt(PaymentStrategy::getVersion))
                .orElse(null);
    }

    @Override
    public List<StrategyRule> getStrategyRules(Long strategyId) {
        return ruleMapper.selectByStrategyIdOrderByPriorityAsc(strategyId);
    }
}
