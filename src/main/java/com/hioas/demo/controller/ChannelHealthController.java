package com.hioas.demo.controller;

import com.hioas.demo.channel.config.ChannelAdapterRegistry;
import com.hioas.demo.dto.ChannelHealthResponse;
import com.hioas.demo.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/admin/channel")
public class ChannelHealthController {

    private final ChannelAdapterRegistry registry;

    public ChannelHealthController(ChannelAdapterRegistry registry) {
        this.registry = registry;
    }

    @GetMapping("/health")
    public ApiResponse<ChannelHealthResponse> getChannelHealth() {
        Map<String, com.hioas.demo.channel.IPaymentChannel> channels = registry.getAllChannels();
        List<ChannelHealthResponse.ChannelHealthInfo> data = new ArrayList<>();

        for (Map.Entry<String, com.hioas.demo.channel.IPaymentChannel> entry : channels.entrySet()) {
            String code = entry.getKey();
            com.hioas.demo.channel.IPaymentChannel ch = entry.getValue();
            com.hioas.demo.channel.IPaymentChannel.ChannelHealth health = ch.getHealth();

            ChannelHealthResponse.ChannelHealthInfo info = new ChannelHealthResponse.ChannelHealthInfo();
            info.setCode(code);
            info.setName(ch.getMeta().getName());
            info.setStatus(health.isHealthy() ? "HEALTHY" : "DEGRADED");
            info.setLatencyMs(health.getLatencyMs());
            info.setLastCheck(LocalDateTime.now());
            info.setErrorRate(0.0);

            data.add(info);
        }

        return ApiResponse.success(new ChannelHealthResponse(data));
    }
}
