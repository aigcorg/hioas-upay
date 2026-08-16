package com.hioas.demo.entity;

import com.mybatisflex.annotation.Table;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Table("reconciliation_task")
public class ReconciliationTask {

    @Id(keyType = KeyType.Auto)
    private Long id;

    @Column
    private String taskNo;

    @Column
    private Long appId;

    @Column
    private String channelCode;

    @Column
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    @Column
    private Integer status;

    @Column
    private Integer totalCount;

    @Column
    private Integer successCount;

    @Column
    private Integer diffCount;

    @Column
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime finishedAt;

    @Column
    private LocalDateTime createdAt;
}
