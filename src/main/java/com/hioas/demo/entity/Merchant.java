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
@Table("merchant")
public class Merchant {

    @Id(keyType = KeyType.Auto)
    private Long id;

    @Column
    private String externalId;

    @Column
    private String name;

    @Column
    private String companyName;

    @Column
    private String unifiedCode;

    @Column
    private String legalPerson;

    @Column
    private String legalIdCard;

    @Column
    private String contactName;

    @Column
    private String contactPhone;

    @Column
    private String contactEmail;

    @Column
    private Integer status;

    @Column
    private Integer certStatus;

    @Column
    private String certReviewer;

    @Column
    private LocalDateTime certReviewTime;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;
}
