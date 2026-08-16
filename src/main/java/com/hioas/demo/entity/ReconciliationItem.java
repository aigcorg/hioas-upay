package com.hioas.demo.entity;

import com.mybatisflex.annotation.Table;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import lombok.Data;
import lombok.experimental.Accessors;
import java.math.BigDecimal;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Table("reconciliation_item")
public class ReconciliationItem {

    @Id(keyType = KeyType.Auto)
    private Long id;

    @Column
    private Long taskId;

    @Column
    private Long transactionId;

    @Column
    private String channelCode;

    @Column
    private String thirdOrderNo;

    @Column
    private BigDecimal thirdAmount;

    @Column
    private String thirdStatus;

    @Column
    private BigDecimal platformAmount;

    @Column
    private String platformStatus;

    @Column
    private Integer matchResult;

    @Column
    private String diffDetail;

    @Column
    private LocalDateTime createdAt;
}
