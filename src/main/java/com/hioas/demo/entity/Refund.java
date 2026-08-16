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
@Table("refund")
public class Refund {

    @Id(keyType = KeyType.Auto)
    private Long id;

    @Column
    private String refundNo;

    @Column
    private Long transactionId;

    @Column
    private Long appId;

    @Column
    private Long userId;

    @Column
    private String channelCode;

    @Column
    private Long channelInstanceId;

    @Column
    private String thirdRefundNo;

    @Column
    private BigDecimal amount;

    @Column
    private String reason;

    @Column
    private Integer status;

    @Column
    private String failureReason;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime refundedAt;
}
