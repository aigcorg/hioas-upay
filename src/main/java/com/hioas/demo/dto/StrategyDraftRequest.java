package com.hioas.demo.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class StrategyDraftRequest {

    private String name;
    private String description;

    @NotEmpty(message = "策略规则不能为空")
    private List<StrategyRuleDraft> rules;

    private FallbackPolicyDraft fallbackPolicy;

    @Data
    public static class StrategyRuleDraft {
        private Integer priority;
        private String name;
        private String description;
        private RuleConditionDraft condition;
        private List<String> channels;
        private String sortBy; // fee_rate_asc, fee_rate_desc, limit_first, latency_asc
    }

    @Data
    public static class RuleConditionDraft {
        private Double[] amountRange; // [min, max], max=null表示无穷
        private String scene;
        private String region;
        private String device;
        private Integer riskLevel;
    }

    @Data
    public static class FallbackPolicyDraft {
        private Boolean enabled;
        private Integer maxAttempts;
        private Boolean requireReAuthorization;
    }
}
