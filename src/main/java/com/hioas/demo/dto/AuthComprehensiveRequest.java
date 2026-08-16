package com.hioas.demo.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class AuthComprehensiveRequest {

    private Long appId;
    private Long userId;
    private String merchantOrderNo;

    private List<String> channels;

    private AgreementDraft agreement;

    @Data
    public static class AgreementDraft {
        private String ip;
        private String ua;
        private Long timestamp;
    }
}
