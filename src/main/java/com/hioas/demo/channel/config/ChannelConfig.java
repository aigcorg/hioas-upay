package com.hioas.demo.channel.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 通道实例配置对象
 * 与 ChannelInstance 实体对应，用于运行时加载
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChannelConfig {

    /**
     * channel_code
     */
    private String channelCode;

    /**
     * 通道名称
     */
    private String channelName;

    /**
     * 适配器类的全限定名
     */
    private String adapterClass;

    /**
     * 通道实例ID
     */
    private Long instanceId;

    /**
     * 所属app_id
     */
    private Long appId;

    /**
     * 配置内容 (密钥, appid, mchid 等, 加密存储, 运行时解密后加载)
     */
    private Map<String, Object> decryptedConfig;

    /**
     * 费率配置
     */
    private ChannelFeeConfig fees;

    /**
     * 额度配置
     */
    private ChannelAmountLimit amountLimit;

    /**
     * 实例优先级
     */
    private Integer priority;

    /**
     * 状态
     */
    private Integer status;

    @Data
    public static class ChannelFeeConfig {
        private Double rate;
        private Double minAmount;
        private Double maxAmount;
    }

    @Data
    public static class ChannelAmountLimit {
        private Double singleMax;
        private Double dailyMax;
    }
}
