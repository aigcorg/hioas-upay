package com.hioas.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationResponse {

    private Long taskId;
    private String taskNo;
    private Integer status;        // QUEUED, RUNNING, COMPLETED, FAILED
    private String estimatedStart;
}
