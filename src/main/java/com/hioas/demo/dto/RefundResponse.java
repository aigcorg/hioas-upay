package com.hioas.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundResponse {

    private Long refundId;
    private String refundNo;
    private String orderNo;
    private Long amount;
    private String status;              // PROCESSING, SUCCESS, FAILED
    private String channelCode;
    private String thirdRefundNo;
    private String createdAt;
}
