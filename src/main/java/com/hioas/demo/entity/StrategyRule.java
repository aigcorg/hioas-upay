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
@Table("strategy_rule")
public class StrategyRule {

    @Id(keyType = KeyType.Auto)
    private Long id;

    @Column
    private Long strategyId;

    @Column
    private Integer priority;

    @Column
    private String name;

    @Column
    private String description;

    @Column
    private String condition;

    @Column
    private String channels;

    @Column
    private String sortBy;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;
}
