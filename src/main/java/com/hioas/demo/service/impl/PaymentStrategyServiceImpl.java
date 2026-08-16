package com.hioas.demo.service.impl;

import com.hioas.demo.entity.PaymentStrategy;
import com.hioas.demo.entity.StrategyRule;
import com.hioas.demo.mapper.PaymentStrategyMapper;
import com.hioas.demo.mapper.StrategyRuleMapper;
import com.hioas.demo.service.PaymentStrategyService;
import com.hioas.demo.dto.StrategyDraftRequest;
import com.hioas.demo.dto.StrategyDraftResponse;
import com.hioas.demo.utils.IdGenerator;
import com.alibaba.fastjson.JSONObject;
import com.mybatisflex.core.query.QueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PaymentStrategyServiceImpl implements PaymentStrategyService {

    private static final Logger log = LoggerFactory.getLogger(PaymentStrategyServiceImpl.class);
    private final PaymentStrategyMapper strategyMapper;
    private final StrategyRuleMapper ruleMapper;

    public PaymentStrategyServiceImpl(PaymentStrategyMapper strategyMapper, StrategyRuleMapper ruleMapper) {
        this.strategyMapper = strategyMapper;
        this.ruleMapper = ruleMapper;
    }

    @Override
    @Transactional
    public StrategyDraftResponse createStrategyDraft(Long appId, StrategyDraftRequest request) {
        PaymentStrategy strategy = new PaymentStrategy();
        strategy.setId(IdGenerator.generateId());
        strategy.setAppId(appId);
        strategy.setName(request.getName());
        strategy.setDescription(request.getDescription());
        strategy.setVersion(1);
        strategy.setStatus(0);
        strategy.setFallbackPolicy(JSONObject.toJSONString(request.getFallbackPolicy()));
        strategy.setCreatedBy(1L);
        strategy.setCreatedAt(LocalDateTime.now());
        strategy.setUpdatedAt(LocalDateTime.now());

        strategyMapper.insert(strategy);

        if (request.getRules() != null) {
            for (StrategyDraftRequest.StrategyRuleDraft ruleDraft : request.getRules()) {
                StrategyRule rule = new StrategyRule();
                rule.setId(IdGenerator.generateId());
                rule.setStrategyId(strategy.getId());
                rule.setPriority(ruleDraft.getPriority());
                rule.setName(ruleDraft.getName());
                rule.setDescription(ruleDraft.getDescription());
                rule.setCondition(JSONObject.toJSONString(ruleDraft.getCondition()));
                rule.setChannels(JSONObject.toJSONString(ruleDraft.getChannels()));
                rule.setSortBy(ruleDraft.getSortBy());
                rule.setCreatedAt(LocalDateTime.now());
                rule.setUpdatedAt(LocalDateTime.now());
                ruleMapper.insert(rule);
            }
        }

        log.info("策略草稿创建: strategyId={}, appId={}, rules={}", strategy.getId(), appId, request.getRules().size());
        return new StrategyDraftResponse(strategy.getId(), strategy.getStatus());
    }

    @Override
    public List<PaymentStrategy> getDraftList(Long appId) {
        List<PaymentStrategy> all = strategyMapper.selectListByQuery(QueryWrapper.create().where("app_id = ?", appId));
        return all.stream().filter(s -> s.getStatus() == 0).collect(Collectors.toList());
    }

    @Override
    public PaymentStrategy getDraftDetail(Long strategyId) {
        return strategyMapper.selectOneById(strategyId);
    }

    @Override
    @Transactional
    public PaymentStrategy updateStrategyDraft(PaymentStrategy strategy) {
        strategy.setUpdatedAt(LocalDateTime.now());
        PaymentStrategy existing = strategyMapper.selectOneById(strategy.getId());
        if (existing != null) {
            strategy.setVersion(existing.getVersion() + 1);
        }
        strategyMapper.update(strategy);
        return strategy;
    }

    @Override
    @Transactional
    public boolean deleteStrategyDraft(Long strategyId) {
        ruleMapper.deleteByQuery(QueryWrapper.create().where("strategy_id = ?", strategyId));
        return strategyMapper.deleteById(strategyId) > 0;
    }

    @Override
    @Transactional
    public StrategyDraftResponse publishStrategy(Long strategyId) {
        PaymentStrategy strategy = strategyMapper.selectOneById(strategyId);
        if (strategy == null) throw new RuntimeException("策略不存在");

        strategy.setStatus(1);
        strategy.setPublishedAt(LocalDateTime.now());
        strategy.setPublishedBy(strategy.getCreatedBy());
        strategy.setUpdatedAt(LocalDateTime.now());

        List<PaymentStrategy> oldPublished = strategyMapper.selectPublishedByAppId(strategy.getAppId());
        for (PaymentStrategy old : oldPublished) {
            if (old.getId() != strategyId) {
                old.setStatus(2);
                old.setUpdatedAt(LocalDateTime.now());
                strategyMapper.update(old);
            }
        }

        strategyMapper.update(strategy);
        log.info("策略发布: strategyId={}, appId={}", strategyId, strategy.getAppId());
        return new StrategyDraftResponse(strategy.getId(), strategy.getStatus());
    }

    @Override
    public List<PaymentStrategy> getPublishedList(Long appId) {
        return strategyMapper.selectPublishedByAppId(appId);
    }

    @Override
    @Transactional
    public boolean disableStrategy(Long strategyId) {
        PaymentStrategy strategy = strategyMapper.selectOneById(strategyId);
        if (strategy == null) return false;
        strategy.setStatus(2);
        strategy.setUpdatedAt(LocalDateTime.now());
        strategyMapper.update(strategy);
        return true;
    }

    @Override
    public PaymentStrategy getActiveStrategy(Long appId) {
        List<PaymentStrategy> published = getPublishedList(appId);
        if (published.isEmpty()) return null;
        return published.stream()
                .max(Comparator.comparingInt(PaymentStrategy::getVersion))
                .orElse(null);
    }
}
