package com.hioas.demo.service;

import com.hioas.demo.entity.ReconciliationTask;
import com.hioas.demo.dto.*;
import com.hioas.demo.entity.ReconciliationItem;

import java.util.List;

public interface ReconciliationService {

    /**
     * 触发对账
     */
    ReconciliationResponse triggerReconciliation(ReconciliationTriggerRequest request);

    /**
     * 获取对账任务列表
     */
    List<ReconciliationTask> getTaskList(Long appId, Integer status, Integer page, Integer size);

    /**
     * 获取对账任务详情
     */
    ReconciliationTaskDetailResponse getTaskDetail(Long taskId);

    /**
     * 获取对账差异明细
     */
    List<ReconciliationDiffItem> getDiffItems(Long taskId);

    /**
     * 获取对账报告 (CSV/Excel,此处返回CSV字符串)
     */
    String getReportCsv(Long taskId);
}
