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
@Table("payment_strategy")
public class PaymentStrategy {

    @Id(keyType = KeyType.Auto)
    private Long id;

    @Column
    private Long appId;

    @Column
    private String name;

    @Column
    private String description;

    @Column
    private Integer version;

    @Column
    private Integer status;

    @Column
    private String fallbackPolicy;

    @Column
    private Long createdBy;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime publishedAt;

    @Column
    private Long publishedBy;
}
