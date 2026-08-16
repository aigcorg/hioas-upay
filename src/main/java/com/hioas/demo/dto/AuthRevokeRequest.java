package com.hioas.demo.dto;

import lombok.Data;

@Data
public class AuthRevokeRequest {
    private Long appId;
    private Long userId;
    private String channelCode;
}
