package com.hioas.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChannelHealthResponse {

    private List<ChannelHealthInfo> data;

    @Data
    public static class ChannelHealthInfo {
        private String code;
        private String name;
        private String status;          // HEALTHY, DEGRADED, UNHEALTHY
        private Long latencyMs;
        private LocalDateTime lastCheck;
        private Double errorRate;
    }
}
