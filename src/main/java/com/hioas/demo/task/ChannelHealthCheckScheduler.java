package com.hioas.demo.task;

import com.hioas.demo.channel.config.ChannelAdapterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ChannelHealthCheckScheduler {

    private static final Logger log = LoggerFactory.getLogger(ChannelHealthCheckScheduler.class);
    private final ChannelAdapterRegistry registry;

    public ChannelHealthCheckScheduler(ChannelAdapterRegistry registry) {
        this.registry = registry;
    }

    /**
     * 每30秒检查一次通道健康状态
     */
    @Scheduled(fixedRate = 30000)
    public void checkChannelHealth() {
        Map<String, com.hioas.demo.channel.IPaymentChannel> channels = registry.getAllChannels();
        log.debug("开始通道健康检查，通道数: {}", channels.size());

        for (Map.Entry<String, com.hioas.demo.channel.IPaymentChannel> entry : channels.entrySet()) {
            try {
                com.hioas.demo.channel.IPaymentChannel ch = entry.getValue();
                com.hioas.demo.channel.IPaymentChannel.ChannelHealth health = ch.getHealth();
                log.debug("通道[{}]健康检查: healthy={}, latency={}ms", 
                        entry.getKey(), health.isHealthy(), health.getLatencyMs());
            } catch (Exception e) {
                log.error("通道[{}]健康检查异常", entry.getKey(), e);
            }
        }
    }
}
