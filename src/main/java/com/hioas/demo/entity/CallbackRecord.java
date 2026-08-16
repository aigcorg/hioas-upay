package com.hioas.demo.entity;

import com.mybatisflex.annotation.Table;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Table("callback_record")
public class CallbackRecord {

    @Id(keyType = KeyType.Auto)
    private Long id;

    @Column
    private Long transactionId;

    @Column
    private String callbackUrl;

    @Column
    private String callbackMethod;

    @Column
    private String requestParams;

    @Column
    private Integer callbackStatus;

    @Column
    private Integer retryCount;

    @Column
    private LocalDateTime lastCallbackTime;

    @Column
    private LocalDateTime nextRetryTime;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;
}
