package com.hioas.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthStatusResponse {

    private Map<String, String> authStatus;  // channelCode -> AUTHORIZED/NOT_AUTHORIZED/FAILED
}
