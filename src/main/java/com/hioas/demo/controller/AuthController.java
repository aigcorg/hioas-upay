package com.hioas.demo.controller;

import com.hioas.demo.dto.*;
import com.hioas.demo.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/comprehensive")
    public ApiResponse<AuthComprehensiveResponse> comprehensiveAuth(@Valid @RequestBody AuthComprehensiveRequest request) {
        AuthComprehensiveResponse resp = authService.comprehensiveAuth(request);
        return ApiResponse.success(resp);
    }

    @GetMapping("/status")
    public ApiResponse<AuthStatusResponse> getAuthStatus(@RequestParam Long appId,
                                                           @RequestParam Long userId) {
        AuthStatusResponse resp = authService.getAuthStatus(appId, userId);
        return ApiResponse.success(resp);
    }

    @PostMapping("/revoke")
    public ApiResponse<String> revokeAuth(@RequestBody AuthRevokeRequest request) {
        authService.revokeAuth(request.getAppId(), request.getUserId(), request.getChannelCode());
        return ApiResponse.success("授权已解除");
    }
}
