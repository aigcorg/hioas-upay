package com.hioas.demo.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class PaymentExecuteRequest {
    private Long appId;
    private Long userId;
    private String merchantOrderNo;
    private BigDecimal amount;
    private String currency;
    private String scene;
    private Integer riskLevel;
    private String region;
    private String device;
    private Map<String, Object> contextMap;
}
