package com.hioas.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChannelTestResponse {

    private String status;       // CONNECTED, FAILED
    private Long latencyMs;
    private String details;
}
