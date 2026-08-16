package com.hioas.demo.service;

import com.hioas.demo.entity.Merchant;
import com.hioas.demo.dto.MerchantRegisterRequest;
import com.hioas.demo.dto.MerchantCertificationRequest;

import java.util.List;

public interface MerchantService {

    /**
     * 商户注册
     */
    Merchant register(MerchantRegisterRequest request);

    /**
     * 提交认证材料
     */
    Merchant submitCertification(Long merchantId, MerchantCertificationRequest request);

    /**
     * 获取商户详情
     */
    Merchant getMerchantById(Long merchantId);

    /**
     * 获取商户列表 (平台管理员用)
     */
    List<Merchant> getMerchantList(Integer status, Integer page, Integer size);
}
