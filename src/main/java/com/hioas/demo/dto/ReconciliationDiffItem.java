package com.hioas.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationDiffItem {

    private Long transactionId;
    private String orderNo;
    private String thirdOrderNo;
    private BigDecimal platformAmount;
    private BigDecimal thirdAmount;
    private String platformStatus;
    private String thirdStatus;
    private String matchResult;        // AMOUNT_MISMATCH, STATUS_MISMATCH, NOT_FOUND
    private String diffDetail;
}
