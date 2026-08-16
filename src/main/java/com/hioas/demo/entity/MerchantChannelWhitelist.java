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
@Table("merchant_channel_white_list")
public class MerchantChannelWhitelist {

    @Id(keyType = KeyType.Auto)
    private Long id;

    @Column
    private Long merchantId;

    @Column
    private String channelCode;

    @Column
    private Boolean enabled;

    @Column
    private String memo;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;
}
