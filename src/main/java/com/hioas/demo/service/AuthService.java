package com.hioas.demo.service;

import com.hioas.demo.dto.AuthComprehensiveRequest;
import com.hioas.demo.dto.AuthComprehensiveResponse;
import com.hioas.demo.dto.AuthStatusResponse;

import java.util.List;
import java.util.Map;

public interface AuthService {

    /**
     * 用户综合授权 (一次性授权所有指定通道)
     */
    AuthComprehensiveResponse comprehensiveAuth(AuthComprehensiveRequest request);

    /**
     * 查询用户授权状态
     */
    AuthStatusResponse getAuthStatus(Long appId, Long userId);

    /**
     * 解除用户对某通道的授权
     */
    boolean revokeAuth(Long appId, Long userId, String channelCode);

    /**
     * 检查用户是否对指定通道授权
     */
    boolean isAuthorized(Long userId, Long appId, String channelCode);
}
