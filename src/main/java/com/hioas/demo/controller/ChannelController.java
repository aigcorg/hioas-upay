package com.hioas.demo.controller;

import com.hioas.demo.dto.*;
import com.hioas.demo.entity.ChannelInstance;
import com.hioas.demo.service.ChannelService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/channel")
public class ChannelController {

    private final ChannelService channelService;

    public ChannelController(ChannelService channelService) {
        this.channelService = channelService;
    }

    @GetMapping("/available")
    public ApiResponse<AvailableChannelResponse> getAvailableChannels() {
        List<Map<String, Object>> channels = channelService.getAvailableChannels();
        return ApiResponse.success(new AvailableChannelResponse(channels));
    }

    @PostMapping("/app/{appId}/select")
    public ApiResponse<String> selectChannels(@PathVariable Long appId,
                                                @Valid @RequestBody ChannelSelectRequest request) {
        channelService.selectChannels(appId, request.getChannelCodes());
        return ApiResponse.success("通道选择成功");
    }

    @PostMapping("/app/{appId}/instance")
    public ApiResponse<ChannelInstance> createChannelInstance(@PathVariable Long appId,
                                                               @Valid @RequestBody ChannelInstanceConfig config) {
        ChannelInstance instance = channelService.createChannelInstance(appId, config);
        return ApiResponse.success(instance);
    }

    @GetMapping("/app/{appId}/instance")
    public ApiResponse<List<ChannelInstance>> getInstanceList(@PathVariable Long appId) {
        List<ChannelInstance> instances = channelService.getInstanceList(appId);
        return ApiResponse.success(instances);
    }

    @PostMapping("/app/{appId}/instance/{instanceId}/test")
    public ApiResponse<ChannelTestResponse> testChannelInstance(@PathVariable Long appId,
                                                                 @PathVariable Long instanceId) {
        ChannelTestResponse result = channelService.testChannelInstance(instanceId);
        return ApiResponse.success(result);
    }

    @DeleteMapping("/app/{appId}/instance/{instanceId}")
    public ApiResponse<String> deleteChannelInstance(@PathVariable Long appId,
                                                       @PathVariable Long instanceId) {
        channelService.deleteChannelInstance(instanceId);
        return ApiResponse.success("删除成功");
    }
}
