package com.hioas.demo.dto;

import lombok.Data;

@Data
public class MerchantCertificationRequest {

    private String businessLicenseFront;
    private String businessLicenseBack;
    private String legalIdCardFront;
    private String legalIdCardBack;
    private String authorizationLetter;
}
