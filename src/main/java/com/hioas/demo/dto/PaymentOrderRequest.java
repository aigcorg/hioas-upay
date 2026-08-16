package com.hioas.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class PaymentOrderRequest {

    @NotBlank(message = "商户订单号不能为空")
    private String merchantOrderNo;

    @NotNull(message = "金额不能为空")
    private BigDecimal amount;

    private String currency = "CNY";

    private List<String> channelPref;

    @NotBlank(message = "场景不能为空")
    private String scene;

    private Integer riskLevel = 0;

    @NotBlank(message = "商品名称不能为空")
    private String productName;

    private String productDesc;

    private Map<String, Object> metadata;

    private Map<String, String> callbackParams;
}
