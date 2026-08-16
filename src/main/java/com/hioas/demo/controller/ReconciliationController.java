package com.hioas.demo.controller;

import com.hioas.demo.dto.*;
import com.hioas.demo.entity.ReconciliationTask;
import com.hioas.demo.service.ReconciliationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/reconciliation")
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    public ReconciliationController(ReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @PostMapping("/trigger")
    public ApiResponse<ReconciliationResponse> trigger(@Valid @RequestBody ReconciliationTriggerRequest request) {
        ReconciliationResponse resp = reconciliationService.triggerReconciliation(request);
        return ApiResponse.success(resp);
    }

    @GetMapping("/task")
    public ApiResponse<List<ReconciliationTask>> getTaskList(@RequestParam(required = false) Long appId,
                                                               @RequestParam(required = false) Integer status,
                                                               @RequestParam(required = false) Integer page,
                                                               @RequestParam(required = false) Integer size) {
        List<ReconciliationTask> tasks = reconciliationService.getTaskList(appId, status, page, size);
        return ApiResponse.success(tasks);
    }

    @GetMapping("/task/{taskId}")
    public ApiResponse<ReconciliationTaskDetailResponse> getTaskDetail(@PathVariable Long taskId) {
        ReconciliationTaskDetailResponse resp = reconciliationService.getTaskDetail(taskId);
        return ApiResponse.success(resp);
    }

    @GetMapping("/task/{taskId}/diff")
    public ApiResponse<List<ReconciliationDiffItem>> getDiffItems(@PathVariable Long taskId) {
        List<ReconciliationDiffItem> diffs = reconciliationService.getDiffItems(taskId);
        return ApiResponse.success(diffs);
    }

    @GetMapping("/task/{taskId}/report")
    public ApiResponse<String> getReport(@PathVariable Long taskId) {
        String csv = reconciliationService.getReportCsv(taskId);
        return ApiResponse.success(csv);
    }
}
