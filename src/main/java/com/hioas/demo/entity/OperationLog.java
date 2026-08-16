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
@Table("operation_log")
public class OperationLog {

    @Id(keyType = KeyType.Auto)
    private Long id;

    @Column
    private Integer operatorType;

    @Column
    private Long operatorId;

    @Column
    private String operation;

    @Column
    private String targetType;

    @Column
    private String targetId;

    @Column
    private Integer result;

    @Column
    private String detail;

    @Column
    private String ip;

    @Column
    private LocalDateTime createdAt;
}
