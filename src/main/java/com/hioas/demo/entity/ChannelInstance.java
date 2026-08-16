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
@Table("channel_instance")
public class ChannelInstance {

    @Id(keyType = KeyType.Auto)
    private Long id;

    @Column
    private Long appId;

    @Column
    private String channelCode;

    @Column
    private String instanceName;

    @Column
    private String config;

    @Column
    private String fees;

    @Column
    private String amountLimit;

    @Column
    private Integer priority;

    @Column
    private Integer status;

    @Column
    private String testResult;

    @Column
    private String memo;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;
}
