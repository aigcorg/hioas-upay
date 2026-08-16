package com.hioas.demo.controller;

import com.hioas.demo.dto.*;
import com.hioas.demo.entity.PaymentStrategy;
import com.hioas.demo.entity.StrategyRule;
import com.hioas.demo.service.PaymentStrategyService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/strategy")
public class StrategyController {

    private final PaymentStrategyService strategyService;

    public StrategyController(PaymentStrategyService strategyService) {
        this.strategyService = strategyService;
    }

    @PostMapping("/app/{appId}/draft")
    public ApiResponse<StrategyDraftResponse> createDraft(@PathVariable Long appId,
                                                           @Valid @RequestBody StrategyDraftRequest request) {
        StrategyDraftResponse resp = strategyService.createStrategyDraft(appId, request);
        return ApiResponse.success(resp);
    }

    @GetMapping("/app/{appId}/draft")
    public ApiResponse<List<PaymentStrategy>> getDraftList(@PathVariable Long appId) {
        List<PaymentStrategy> drafts = strategyService.getDraftList(appId);
        return ApiResponse.success(drafts);
    }

    @GetMapping("/draft/{strategyId}")
    public ApiResponse<PaymentStrategy> getDraftDetail(@PathVariable Long strategyId) {
        PaymentStrategy strategy = strategyService.getDraftDetail(strategyId);
        return ApiResponse.success(strategy);
    }

    @PutMapping("/draft/{strategyId}")
    public ApiResponse<PaymentStrategy> updateDraft(@PathVariable Long strategyId,
                                                      @RequestBody PaymentStrategy strategy) {
        strategy.setId(strategyId);
        PaymentStrategy updated = strategyService.updateStrategyDraft(strategy);
        return ApiResponse.success(updated);
    }

    @DeleteMapping("/draft/{strategyId}")
    public ApiResponse<String> deleteDraft(@PathVariable Long strategyId) {
        strategyService.deleteStrategyDraft(strategyId);
        return ApiResponse.success("删除成功");
    }

    @PostMapping("/draft/{strategyId}/publish")
    public ApiResponse<StrategyDraftResponse> publish(@PathVariable Long strategyId) {
        StrategyDraftResponse resp = strategyService.publishStrategy(strategyId);
        return ApiResponse.success(resp);
    }

    @GetMapping("/app/{appId}/published")
    public ApiResponse<List<PaymentStrategy>> getPublishedList(@PathVariable Long appId) {
        List<PaymentStrategy> published = strategyService.getPublishedList(appId);
        return ApiResponse.success(published);
    }

    @PostMapping("/published/{strategyId}/disable")
    public ApiResponse<String> disable(@PathVariable Long strategyId) {
        strategyService.disableStrategy(strategyId);
        return ApiResponse.success("已停用");
    }
}
