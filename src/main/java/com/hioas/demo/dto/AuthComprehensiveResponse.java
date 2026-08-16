package com.hioas.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthComprehensiveResponse {

    private Map<String, String> authResult;   // channelCode -> AUTHORIZED/FAILED
    private Boolean allAuthorized;
    private List<String> failedChannels;
    private String authTime;
}
