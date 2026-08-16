package com.hioas.demo.service.impl;

import com.hioas.demo.entity.Merchant;
import com.hioas.demo.mapper.MerchantMapper;
import com.hioas.demo.service.MerchantService;
import com.hioas.demo.dto.MerchantRegisterRequest;
import com.hioas.demo.dto.MerchantCertificationRequest;
import com.hioas.demo.utils.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MerchantServiceImpl implements MerchantService {

    private static final Logger log = LoggerFactory.getLogger(MerchantServiceImpl.class);
    private final MerchantMapper merchantMapper;

    public MerchantServiceImpl(MerchantMapper merchantMapper) {
        this.merchantMapper = merchantMapper;
    }

    @Override
    @Transactional
    public Merchant register(MerchantRegisterRequest request) {
        Merchant merchant = new Merchant();
        merchant.setId(IdGenerator.generateId());
        merchant.setName(request.getName());
        merchant.setCompanyName(request.getCompanyName());
        merchant.setUnifiedCode(request.getUnifiedCode());
        merchant.setLegalPerson(request.getLegalPerson());
        merchant.setLegalIdCard(request.getLegalIdCard());
        merchant.setContactName(request.getContactName());
        merchant.setContactPhone(request.getContactPhone());
        merchant.setContactEmail(request.getContactEmail());
        merchant.setStatus(0);
        merchant.setCertStatus(0);
        merchant.setCreatedAt(LocalDateTime.now());
        merchant.setUpdatedAt(LocalDateTime.now());

        merchantMapper.insert(merchant);
        log.info("商户注册成功: merchantId={}", merchant.getId());
        return merchant;
    }

    @Override
    @Transactional
    public Merchant submitCertification(Long merchantId, MerchantCertificationRequest request) {
        Merchant merchant = merchantMapper.selectOneById(merchantId);
        if (merchant == null) throw new RuntimeException("商户不存在");

        merchant.setCertStatus(1);
        merchant.setUpdatedAt(LocalDateTime.now());
        merchantMapper.update(merchant);
        log.info("商户提交认证材料: merchantId={}", merchantId);
        return merchant;
    }

    @Override
    public Merchant getMerchantById(Long merchantId) {
        return merchantMapper.selectOneById(merchantId);
    }

    @Override
    public List<Merchant> getMerchantList(Integer status, Integer page, Integer size) {
        List<Merchant> all = merchantMapper.selectAll();
        if (status != null) {
            all = all.stream().filter(m -> m.getStatus().equals(status)).collect(Collectors.toList());
        }
        long total = all.size();
        int pageSize = size != null ? size : 20;
        int pageNum = page != null ? page : 1;
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, all.size());
        List<Merchant> list = fromIndex < toIndex ? all.subList(fromIndex, toIndex) : Collections.emptyList();
        return list;
    }
}
