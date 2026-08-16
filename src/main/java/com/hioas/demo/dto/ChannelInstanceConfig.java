package com.hioas.demo.dto;

import lombok.Data;
import java.util.Map;

@Data
public class ChannelInstanceConfig {

    private String channelCode;
    private String instanceName;
    private Map<String, Object> config;  // 密钥、appid、mchid等
    private FeeConfig fees;
    private AmountLimit amountLimit;

    @Data
    public static class FeeConfig {
        private Double rate;
        private Map<String, Double> caps; // min, max
    }

    @Data
    public static class AmountLimit {
        private Double singleMax;
        private Double dailyMax;
    }
}
