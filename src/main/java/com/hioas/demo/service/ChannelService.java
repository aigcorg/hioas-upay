package com.hioas.demo.service;

import com.hioas.demo.entity.StrategyRule;

import java.util.List;

public interface ChannelService {

    /**
     * 获取可用通道列表 (元信息)
     */
    java.util.List<java.util.Map<String, Object>> getAvailableChannels();

    /**
     * 选择通道 (关联到应用)
     */
    void selectChannels(Long appId, java.util.List<String> channelCodes);

    /**
     * 创建渠道实例配置
     */
    com.hioas.demo.entity.ChannelInstance createChannelInstance(Long appId, com.hioas.demo.dto.ChannelInstanceConfig config);

    /**
     * 获取渠道实例列表
     */
    java.util.List<com.hioas.demo.entity.ChannelInstance> getInstanceList(Long appId);

    /**
     * 测试渠道实例连通性
     */
    com.hioas.demo.dto.ChannelTestResponse testChannelInstance(Long instanceId);

    /**
     * 删除渠道实例
     */
    boolean deleteChannelInstance(Long instanceId);

    /**
     * 获取通道适配器
     */
    com.hioas.demo.channel.IPaymentChannel getChannelAdapter(Long instanceId);

    /**
     * 获取通道实例
     */
    com.hioas.demo.entity.ChannelInstance getInstance(Long instanceId);

    /**
     * 获取应用的已发布策略 (用于路由)
     */
    com.hioas.demo.entity.PaymentStrategy getStrategyByAppId(Long appId);

    /**
     * 获取策略规则
     */
    List<StrategyRule> getStrategyRules(Long strategyId);
}
