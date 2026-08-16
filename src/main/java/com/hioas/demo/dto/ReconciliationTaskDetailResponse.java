package com.hioas.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationTaskDetailResponse {

    private Long taskId;
    private String taskNo;
    private Long appId;
    private String channelCode;
    private String startDate;
    private String endDate;
    private String status;
    private Integer totalCount;
    private Integer successCount;
    private Integer diffCount;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
}
