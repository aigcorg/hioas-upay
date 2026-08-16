package com.hioas.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentExecuteResponse {
    private String code;
    private String message;
    private String channelCode;
    private String thirdOrderNo;
}
