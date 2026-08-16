package com.hioas.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class MerchantRegisterRequest {

    @NotBlank(message = "商户名称不能为空")
    private String name;

    @NotBlank(message = "公司名称不能为空")
    private String companyName;

    @NotBlank(message = "统一社会信用代码不能为空")
    @Pattern(regexp = "^[A-Z0-9]{10,18}$", message = "统一社会信用代码格式不正确")
    private String unifiedCode;

    @NotBlank(message = "法人姓名不能为空")
    private String legalPerson;

    @NotBlank(message = "法人身份证号不能为空")
    @Pattern(regexp = "^[0-9]{17}[0-9Xx]$", message = "身份证号格式不正确")
    private String legalIdCard;

    @NotBlank(message = "联系人姓名不能为空")
    private String contactName;

    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^[0-9]{11}$", message = "电话号码格式不正确")
    private String contactPhone;

    private String contactEmail;
}
