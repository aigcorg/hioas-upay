package com.hioas.demo.service.impl;

import com.hioas.demo.entity.App;
import com.hioas.demo.mapper.AppMapper;
import com.hioas.demo.service.AppService;
import com.hioas.demo.dto.CreateAppRequest;
import com.hioas.demo.dto.CreateAppResponse;
import com.hioas.demo.utils.IdGenerator;
import com.hioas.demo.utils.EncryptUtil;
import com.hioas.demo.utils.RandomUtil;
import com.mybatisflex.core.query.QueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppServiceImpl implements AppService {

    private static final Logger log = LoggerFactory.getLogger(AppServiceImpl.class);

    private final AppMapper appMapper;

    public AppServiceImpl(AppMapper appMapper) {
        this.appMapper = appMapper;
    }

    @Override
    @Transactional
    public CreateAppResponse createApp(CreateAppRequest request) {
        // 生成appid和密钥
        String appid = IdGenerator.generateAppId();
        String signSecretKey = RandomUtil.alphanumeric(32);

        App app = new App();
        app.setMerchantId(request.getMerchantId());
        app.setName(request.getName());
        app.setType(request.getType());
        app.setCallbackUrl(request.getCallbackUrl());
        app.setReturnUrl(request.getReturnUrl());
        app.setStatus(0);  // 草稿状态
        app.setAppid(appid);
        app.setSignSecretKey(EncryptUtil.encrypt(signSecretKey));
        app.setCreatedAt(LocalDateTime.now());
        app.setUpdatedAt(LocalDateTime.now());

        appMapper.insert(app);

        log.info("应用创建成功: appId={}, appid={}, merchantOrderNo={}", app.getId(), appid, request.getMerchantId());

        return new CreateAppResponse(app.getId(), appid, signSecretKey, app.getStatus());
    }

    @Override
    public App getAppById(Long appId) {
        return appMapper.selectOneById(appId);
    }

    @Override
    public List<App> getAppListByMerchantId(Long merchantId) {
        return appMapper.selectListByQuery(QueryWrapper.create().where("merchant_id = ?", merchantId));
    }

    @Override
    public App updateApp(App app) {
        app.setUpdatedAt(LocalDateTime.now());
        appMapper.update(app);
        return app;
    }

    @Override
    public boolean deleteApp(Long appId) {
        return appMapper.deleteById(appId) > 0;
    }
}
