package com.hioas.demo.channel.config;

import com.hioas.demo.channel.IPaymentChannel;
import com.hioas.demo.channel.adapter.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通道适配器注册表
 * 管理所有已加载的通道适配器实例
 * 用于通道配置化加载: 运行时根据配置动态创建适配器实例
 */
@Component
public class ChannelAdapterRegistry {

    private static final Logger log = LoggerFactory.getLogger(ChannelAdapterRegistry.class);

    /**
     * 单例缓存: channelCode -> IPaymentChannel实例
     */
    private final Map<String, IPaymentChannel> adapterCache = new ConcurrentHashMap<>();

    /**
     * 配置信息缓存: channelCode -> ChannelConfig
     */
    private final Map<String, ChannelConfig> configCache = new ConcurrentHashMap<>();

    /**
     * 获取指定通道的适配器实例
     */
    public IPaymentChannel getChannel(String channelCode) {
        return adapterCache.get(channelCode);
    }

    /**
     * 获取所有已加载的适配器
     */
    public Map<String, IPaymentChannel> getAllChannels() {
        return new HashMap<>(adapterCache);
    }

    /**
     * 注册通道适配器实例
     */
    public void registerChannel(IPaymentChannel channel, ChannelConfig config) {
        adapterCache.put(channel.getMeta().getCode(), channel);
        configCache.put(channel.getMeta().getCode(), config);
        log.info("注册通道适配器: code={}, name={}", channel.getMeta().getCode(), channel.getMeta().getName());
    }

    /**
     * 移除通道适配器
     */
    public void unregisterChannel(String channelCode) {
        adapterCache.remove(channelCode);
        configCache.remove(channelCode);
        log.info("移除通道适配器: code={}", channelCode);
    }

    /**
     * 根据通道配置创建适配器实例
     * 配置化加载的核心方法: 根据 adapterClass 创建实例并注入配置
     */
    public IPaymentChannel createChannelFromConfig(ChannelConfig config) throws Exception {
        String adapterClass = config.getAdapterClass();
        IPaymentChannel channel = (IPaymentChannel) Class.forName(adapterClass).getDeclaredConstructor().newInstance();
        if (channel instanceof AbstractPaymentChannelAdapter) {
            ((AbstractPaymentChannelAdapter) channel).setConfig(config.getDecryptedConfig());
        }
        channel.init();
        log.info("从配置创建通道适配器: code={}, class={}", config.getChannelCode(), adapterClass);
        return channel;
    }

    /**
     * 获取通道配置
     */
    public ChannelConfig getConfig(String channelCode) {
        return configCache.get(channelCode);
    }

    /**
     * 初始化默认通道 (应用启动时加载内置通道)
     */
    @PostConstruct
    public void initDefaultChannels() {
        log.info("初始化默认支付通道...");
        try {
            registerDefaultChannel("wx_jsapi", "com.hioas.demo.channel.adapter.WeChatJSAPIAdapter",
                    "微信支付 JSAPI");
            registerDefaultChannel("alipay_trade", "com.hioas.demo.channel.adapter.AlipayTradeAdapter",
                    "支付宝电脑网站支付");
            registerDefaultChannel("zj_payment", "com.hioas.demo.channel.adapter.ZjPaymentAdapter",
                    "中金支付");
            registerDefaultChannel("bf_payment", "com.hioas.demo.channel.adapter.BfPaymentAdapter",
                    "宝付支付");
            log.info("默认支付通道初始化完成: {}", adapterCache.keySet());
        } catch (Exception e) {
            log.error("初始化默认通道失败", e);
        }
    }

    private void registerDefaultChannel(String code, String adapterClass, String name) throws Exception {
        ChannelConfig config = new ChannelConfig();
        config.setChannelCode(code);
        config.setChannelName(name);
        config.setAdapterClass(adapterClass);
        config.setDecryptedConfig(new HashMap<>());
        IPaymentChannel channel = createChannelFromConfig(config);
        registerChannel(channel, config);
    }
}
