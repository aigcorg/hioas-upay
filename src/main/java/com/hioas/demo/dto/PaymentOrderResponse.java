package com.hioas.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrderResponse {

    private Long orderId;
    private String orderNo;
    private Long amount;
    private String currency;
    private String status;
    private String paidAt;
    private String createdAt;
}
