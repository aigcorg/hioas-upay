package com.hioas.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StrategyDraftResponse {

    private Long strategyId;
    private Integer status; // DRAFT, PUBLISHED
}
