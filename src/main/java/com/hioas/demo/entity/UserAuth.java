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
@Table("user_auth")
public class UserAuth {

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
    private Integer authType;

    @Column
    private Integer authStatus;

    @Column
    private String authToken;

    @Column
    private String refreshToken;

    @Column
    private LocalDateTime authTime;

    @Column
    private LocalDateTime expiresAt;

    @Column
    private String authChannel;

    @Column
    private String memo;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;
}
