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
@Table("transaction")
public class Transaction {

    @Id(keyType = KeyType.Auto)
    private Long id;

    @Column
    private String transactionNo;

    @Column
    private Long appId;

    @Column
    private Long userId;

    @Column
    private String channelCode;

    @Column
    private Long channelInstanceId;

    @Column
    private String thirdOrderNo;

    @Column
    private Integer orderType;

    @Column
    private BigDecimal amount;

    @Column
    private String currency;

    @Column
    private Integer status;

    @Column
    private String scene;

    @Column
    private Integer riskLevel;

    @Column
    private String riskNote;

    @Column
    private String metadata;

    @Column
    private String failureReason;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime paidAt;

    @Column
    private LocalDateTime closedAt;

    @Column
    private String merchantOrderNo;
}
