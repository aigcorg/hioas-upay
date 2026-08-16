package com.hioas.demo.task;

import com.hioas.demo.service.ReconciliationService;
import com.hioas.demo.dto.ReconciliationTriggerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ReconciliationTaskScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReconciliationTaskScheduler.class);
    private final ReconciliationService reconciliationService;

    @Value("${pay.reconciliation.cron:0 0 2 * * *}")
    private String reconciliationCron;

    public ReconciliationTaskScheduler(ReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    /**
     * 每日凌晨2点执行对账
     */
    @Scheduled(cron = "${pay.reconciliation.cron}")
    public void dailyReconciliation() {
        log.info("触发每日对账任务...");
        try {
            ReconciliationTriggerRequest request = new ReconciliationTriggerRequest();
            request.setStartDate(LocalDate.now().minusDays(1).toString());
            request.setEndDate(LocalDate.now().toString());
            reconciliationService.triggerReconciliation(request);
            log.info("每日对账任务执行完成");
        } catch (Exception e) {
            log.error("每日对账任务执行失败", e);
        }
    }
}
