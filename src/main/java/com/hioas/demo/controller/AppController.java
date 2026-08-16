package com.hioas.demo.controller;

import com.hioas.demo.dto.*;
import com.hioas.demo.entity.App;
import com.hioas.demo.service.AppService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/app")
public class AppController {

    private final AppService appService;

    public AppController(AppService appService) {
        this.appService = appService;
    }

    @PostMapping
    public ApiResponse<CreateAppResponse> createApp(@Valid @RequestBody CreateAppRequest request) {
        CreateAppResponse resp = appService.createApp(request);
        return ApiResponse.success(resp);
    }

    @GetMapping("/{appId}")
    public ApiResponse<App> getApp(@PathVariable Long appId) {
        App app = appService.getAppById(appId);
        return ApiResponse.success(app);
    }

    @GetMapping
    public ApiResponse<AppListResponse> getAppList(@RequestParam Long merchantId,
                                                     @RequestParam(required = false) Integer page,
                                                     @RequestParam(required = false) Integer size) {
        List<App> apps = appService.getAppListByMerchantId(merchantId);
        long total = apps.size();
        int pageSize = size != null ? size : 20;
        int pageNum = page != null ? page : 1;
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, apps.size());
        List<App> list = fromIndex < toIndex ? apps.subList(fromIndex, toIndex) : List.of();
        return ApiResponse.success(new AppListResponse(list, total, pageNum, pageSize, (int)Math.ceil(total / (double)pageSize)));
    }

    @PutMapping("/{appId}")
    public ApiResponse<App> updateApp(@PathVariable Long appId, @RequestBody App app) {
        app.setId(appId);
        App updated = appService.updateApp(app);
        return ApiResponse.success(updated);
    }

    @DeleteMapping("/{appId}")
    public ApiResponse<String> deleteApp(@PathVariable Long appId) {
        appService.deleteApp(appId);
        return ApiResponse.success("删除成功");
    }
}
