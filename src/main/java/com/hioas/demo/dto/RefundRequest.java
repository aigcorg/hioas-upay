package com.hioas.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class RefundRequest {

    @NotBlank(message = "商户退款号不能为空")
    private String merchantRefundNo;

    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    @NotNull(message = "退款金额不能为空")
    private BigDecimal amount;

    private String reason;

    private Map<String, String> callbackParams;
}
