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
@Table("user_channel_binding")
public class UserChannelBinding {

    @Id(keyType = KeyType.Auto)
    private Long id;

    @Column
    private Long userId;

    @Column
    private Long appId;

    @Column
    private String channelCode;

    @Column
    private Long channelInstanceId;

    @Column
    private LocalDateTime bindTime;

    @Column
    private LocalDateTime lastUsedAt;

    @Column
    private Integer useCount;

    @Column
    private Integer status;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;
}
