package com.hioas.demo.service.impl;

import com.hioas.demo.entity.UserAuth;
import com.hioas.demo.mapper.UserAuthMapper;
import com.hioas.demo.mapper.UserChannelBindingMapper;
import com.hioas.demo.service.AuthService;
import com.hioas.demo.dto.AuthComprehensiveRequest;
import com.hioas.demo.dto.AuthComprehensiveResponse;
import com.hioas.demo.dto.AuthStatusResponse;
import com.hioas.demo.channel.IPaymentChannel;
import com.hioas.demo.channel.config.ChannelAdapterRegistry;
import com.hioas.demo.utils.IdGenerator;
import com.hioas.demo.utils.EncryptUtil;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final int AUTH_EXPIRE_DAYS = 720; // 2年有效期

    private final UserAuthMapper userAuthMapper;
    private final UserChannelBindingMapper userChannelBindingMapper;
    private final ChannelAdapterRegistry registry;

    public AuthServiceImpl(UserAuthMapper userAuthMapper, UserChannelBindingMapper userChannelBindingMapper,
                           ChannelAdapterRegistry registry) {
        this.userAuthMapper = userAuthMapper;
        this.userChannelBindingMapper = userChannelBindingMapper;
        this.registry = registry;
    }

    @Override
    @Transactional
    public AuthComprehensiveResponse comprehensiveAuth(AuthComprehensiveRequest request) {
        Long userId = request.getUserId();
        Long appId = request.getAppId();
        List<String> channelCodes = request.getChannels();

        log.info("用户[{}]对应用[{}]进行综合授权,通道数:{}", userId, appId, channelCodes.size());

        Map<String, String> authResult = new HashMap<>();
        List<String> failedChannels = new ArrayList<>();
        boolean allAuthorized = true;

        for (String channelCode : channelCodes) {
            try {
                // 获取通道适配器
                IPaymentChannel adapter = registry.getChannel(channelCode);
                if (adapter == null) {
                    log.warn("通道[{}]适配器未找到", channelCode);
                    authResult.put(channelCode, "FAILED");
                    failedChannels.add(channelCode);
                    allAuthorized = false;
                    continue;
                }

                // 模拟向第三方请求授权 (实际需调用第三方API)
                String authToken = simulateThirdPartyAuth(channelCode, userId, appId);

                if (authToken != null) {
                    // 保存授权记录到数据库
                    saveUserAuth(userId, appId, channelCode, adapter.getMeta().getCode(), authToken, 2);

                    // 更新为已授权
                    authResult.put(channelCode, "AUTHORIZED");
                    log.info("用户[{}]对通道[{}]综合授权成功", userId, channelCode);
                } else {
                    authResult.put(channelCode, "FAILED");
                    failedChannels.add(channelCode);
                    allAuthorized = false;
                    log.warn("用户[{}]对通道[{}]授权失败", userId, channelCode);
                }
            } catch (Exception e) {
                log.error("用户[{}]对通道[{}]授权异常", userId, channelCode, e);
                authResult.put(channelCode, "FAILED");
                failedChannels.add(channelCode);
                allAuthorized = false;
            }
        }

        // 如果全部授权成功,创建用户-通道绑定记录 (便于后续路由查找)
        if (allAuthorized) {
            for (String channelCode : channelCodes) {
                // 创建绑定关系
                createBindingIfNotExists(userId, appId, channelCode);
            }
        }

        String authTime = LocalDateTime.now().toString();

        return new AuthComprehensiveResponse(authResult, allAuthorized, failedChannels, authTime);
    }

    @Override
    public AuthStatusResponse getAuthStatus(Long appId, Long userId) {
        List<UserAuth> authList = userAuthMapper.selectByUserAndApp(userId, appId);
        Map<String, String> statusMap = new HashMap<>();

        for (UserAuth auth : authList) {
            String channelCode = auth.getChannelCode();
            String status;
            switch (auth.getAuthStatus()) {
                case 1:
                    status = "AUTHORIZED";
                    break;
                case 0:
                    status = "NOT_AUTHORIZED";
                    break;
                case 2:
                    status = "FAILED";
                    break;
                case 3:
                    status = "REJECTED";
                    break;
                case 4:
                    status = "EXPIRED";
                    break;
                default:
                    status = "UNKNOWN";
            }
            statusMap.put(channelCode, status);
        }

        // 补充未授权通道 (从数据库中找出所有通道实例并补充)
        // 此处简化,假设所有通道均已在authList中
        return new AuthStatusResponse(statusMap);
    }

    @Override
    @Transactional
    public boolean revokeAuth(Long appId, Long userId, String channelCode) {
        // 查找授权记录
        List<UserAuth> authList = userAuthMapper.selectByUserAndAppAndChannelCodes(userId, appId, 
                JSONObject.toJSONString(Collections.singletonList(channelCode)));
        
        for (UserAuth auth : authList) {
            if (channelCode.equals(auth.getChannelCode())) {
                auth.setAuthStatus(0);  // 设置为未授权
                auth.setAuthToken(null);
                auth.setRefreshToken(null);
                userAuthMapper.update(auth);
                
                // 移除绑定关系
                userChannelBindingMapper.deleteByUserAndAppAndChannel(userId, appId, channelCode);
                
                log.info("用户[{}]撤销了对通道[{}]的授权", userId, channelCode);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isAuthorized(Long userId, Long appId, String channelCode) {
        List<UserAuth> authList = userAuthMapper.selectByUserAndAppAndChannelCodes(userId, appId,
                JSONObject.toJSONString(Collections.singletonList(channelCode)));
        
        for (UserAuth auth : authList) {
            if (channelCode.equals(auth.getChannelCode()) && auth.getAuthStatus() == 1) {
                // 检查是否过期
                if (auth.getExpiresAt() != null && auth.getExpiresAt().isBefore(LocalDateTime.now())) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 保存用户授权记录到数据库
     */
    private void saveUserAuth(Long userId, Long appId, String channelCode, String channelCodeFromMeta,
                              String authToken, Integer authType) {
        // 获取通道实例ID (从配置缓存中获取)
        Long channelInstanceId = getChannelInstanceId(channelCode, appId);

        UserAuth auth = new UserAuth();
        auth.setId(IdGenerator.generateId());
        auth.setUserId(userId);
        auth.setAppId(appId);
        auth.setChannelCode(channelCode);
        auth.setChannelInstanceId(channelInstanceId);
        auth.setAuthType(authType);  // 2 = 综合授权
        auth.setAuthStatus(1);  // 已授权
        auth.setAuthToken(authToken != null ? EncryptUtil.encrypt(authToken) : null);
        auth.setAuthTime(LocalDateTime.now());
        auth.setExpiresAt(LocalDateTime.now().plusDays(AUTH_EXPIRE_DAYS));
        auth.setCreatedAt(LocalDateTime.now());
        auth.setUpdatedAt(LocalDateTime.now());

        userAuthMapper.insert(auth);
    }

    /**
     * 创建用户-通道绑定关系
     */
    private void createBindingIfNotExists(Long userId, Long appId, String channelCode) {
        // 检查是否已存在绑定
        List<com.hioas.demo.entity.UserChannelBinding> existing = 
                userChannelBindingMapper.selectByUserAndApp(userId, appId);
        
        boolean exists = false;
        for (com.hioas.demo.entity.UserChannelBinding b : existing) {
            if (channelCode.equals(b.getChannelCode())) {
                exists = true;
                break;
            }
        }

        if (!exists) {
            Long instanceId = getChannelInstanceId(channelCode, appId);
            com.hioas.demo.entity.UserChannelBinding binding = new com.hioas.demo.entity.UserChannelBinding();
            binding.setId(IdGenerator.generateId());
            binding.setUserId(userId);
            binding.setAppId(appId);
            binding.setChannelCode(channelCode);
            binding.setChannelInstanceId(instanceId);
            binding.setBindTime(LocalDateTime.now());
            binding.setLastUsedAt(LocalDateTime.now());
            binding.setUseCount(0);
            binding.setStatus(1);
            binding.setCreatedAt(LocalDateTime.now());
            binding.setUpdatedAt(LocalDateTime.now());

            userChannelBindingMapper.insert(binding);
        }
    }

    /**
     * 获取通道实例ID (从ChannelInstanceMapper获取,此处简化用模拟值)
     */
    private Long getChannelInstanceId(String channelCode, Long appId) {
        // 实际应从ChannelInstanceMapper查询此appId下的对应channelCode实例
        // 演示中返回模拟值
        return 1000L + (int)(Math.random() * 10000);
    }

    /**
     * 模拟向第三方请求授权 (实际需实现具体API调用)
     */
    private String simulateThirdPartyAuth(String channelCode, Long userId, Long appId) {
        // 模拟: 根据不同通道模拟返回不同的授权凭证
        // 实际业务中应调用第三方授权API,此处简化为生成模拟Token
        log.info("模拟向第三方[{}]请求授权: userId={}, appId={}", channelCode, userId, appId);
        return "AUTH_TOKEN_" + channelCode + "_" + userId + "_" + System.currentTimeMillis();
    }
}
