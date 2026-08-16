package com.hioas.demo.dto;

import lombok.Data;

@Data
public class ReconciliationTriggerRequest {

    private Long appId;
    private String channelCode;
    private String startDate;   // YYYY-MM-DD
    private String endDate;     // YYYY-MM-DD
}
