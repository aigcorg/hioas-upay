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
@Table("channel")
public class Channel {

    @Id(keyType = KeyType.Auto)
    private String code;

    @Column
    private String name;

    @Column
    private String adapterClass;

    @Column
    private String version;

    @Column
    private Integer status;

    @Column
    private String scenes;

    @Column
    private String configTemplate;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;
}
