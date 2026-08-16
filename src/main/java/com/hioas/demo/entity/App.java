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
@Table("app")
public class App {

    @Id(keyType = KeyType.Auto)
    private Long id;

    @Column
    private Long merchantId;

    @Column
    private String appid;

    @Column
    private String name;

    @Column
    private Integer type;

    @Column
    private String callbackUrl;

    @Column
    private String returnUrl;

    @Column
    private Integer status;

    @Column
    private String signSecretKey;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;
}
