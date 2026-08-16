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
@Table("system_config")
public class SystemConfig {

    @Id(keyType = KeyType.Auto)
    private Long id;

    @Column
    private String key;

    @Column
    private String value;

    @Column
    private String description;

    @Column
    private LocalDateTime updatedAt;
}
