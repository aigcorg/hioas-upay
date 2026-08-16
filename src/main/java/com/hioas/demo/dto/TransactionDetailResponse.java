package com.hioas.demo.dto;

import com.hioas.demo.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDetailResponse {

    private Long id;
    private String transactionNo;
    private String merchantOrderNo;
    private Long appId;
    private Long userId;
    private String channelCode;
    private String thirdOrderNo;
    private Integer orderType;          // 1-支付、2-退款
    private BigDecimal amount;
    private String currency;
    private Integer status;
    private String scene;
    private Integer riskLevel;
    private String failureReason;
    private String paidAt;
    private String createdAt;
    private String metadata;
}
