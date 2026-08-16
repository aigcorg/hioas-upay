package com.hioas.demo.service;

import com.hioas.demo.entity.PaymentStrategy;
import com.hioas.demo.entity.StrategyRule;
import com.hioas.demo.dto.StrategyDraftRequest;
import com.hioas.demo.dto.StrategyDraftResponse;

import java.util.List;

public interface PaymentStrategyService {

    /**
     * 创建策略草稿
     */
    StrategyDraftResponse createStrategyDraft(Long appId, StrategyDraftRequest request);

    /**
     * 获取策略草稿列表
     */
    List<PaymentStrategy> getDraftList(Long appId);

    /**
     * 获取策略草稿详情
     */
    PaymentStrategy getDraftDetail(Long strategyId);

    /**
     * 更新策略草稿
     */
    PaymentStrategy updateStrategyDraft(PaymentStrategy strategy);

    /**
     * 删除策略草稿
     */
    boolean deleteStrategyDraft(Long strategyId);

    /**
     * 发布策略
     */
    StrategyDraftResponse publishStrategy(Long strategyId);

    /**
     * 获取已发布策略
     */
    List<PaymentStrategy> getPublishedList(Long appId);

    /**
     * 停用策略
     */
    boolean disableStrategy(Long strategyId);

    /**
     * 获取指定应用的已发布策略 (用于路由)
     */
    PaymentStrategy getActiveStrategy(Long appId);
}
