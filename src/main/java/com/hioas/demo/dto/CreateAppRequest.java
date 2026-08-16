package com.hioas.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateAppRequest {

    @NotNull(message = "商户ID不能为空")
    private Long merchantId;

    @NotBlank(message = "应用名称不能为空")
    private String name;

    @NotNull(message = "应用类型不能为空")
    private Integer type; // 1-电商、2-服务、3-直播、4-其他

    @NotBlank(message = "回调地址不能为空")
    private String callbackUrl;

    private String returnUrl;
}
