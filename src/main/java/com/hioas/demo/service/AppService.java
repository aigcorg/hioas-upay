package com.hioas.demo.service;

import com.hioas.demo.entity.App;
import com.hioas.demo.dto.CreateAppRequest;
import com.hioas.demo.dto.CreateAppResponse;

import java.util.List;

public interface AppService {

    /**
     * 创建应用
     */
    CreateAppResponse createApp(CreateAppRequest request);

    /**
     * 获取应用详情
     */
    App getAppById(Long appId);

    /**
     * 获取应用列表 (按商户)
     */
    List<App> getAppListByMerchantId(Long merchantId);

    /**
     * 更新应用
     */
    App updateApp(App app);

    /**
     * 删除应用
     */
    boolean deleteApp(Long appId);
}
