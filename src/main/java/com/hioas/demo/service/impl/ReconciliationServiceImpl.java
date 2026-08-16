package com.hioas.demo.service.impl;

import com.hioas.demo.entity.ReconciliationTask;
import com.hioas.demo.entity.ReconciliationItem;
import com.hioas.demo.entity.Transaction;
import com.hioas.demo.dto.ReconciliationDiffItem;
import com.hioas.demo.dto.ReconciliationResponse;
import com.hioas.demo.dto.ReconciliationTaskDetailResponse;
import com.hioas.demo.mapper.ReconciliationItemMapper;
import com.hioas.demo.mapper.ReconciliationTaskMapper;
import com.hioas.demo.mapper.TransactionMapper;
import com.hioas.demo.service.ReconciliationService;
import com.hioas.demo.dto.ReconciliationTriggerRequest;
import com.hioas.demo.channel.IPaymentChannel;
import com.hioas.demo.channel.config.ChannelAdapterRegistry;
import com.hioas.demo.utils.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReconciliationServiceImpl implements ReconciliationService {

    private static final Logger log = LoggerFactory.getLogger(ReconciliationServiceImpl.class);

    private final ReconciliationTaskMapper taskMapper;
    private final ReconciliationItemMapper itemMapper;
    private final TransactionMapper transactionMapper;
    private final ChannelAdapterRegistry registry;

    public ReconciliationServiceImpl(ReconciliationTaskMapper taskMapper,
                                     ReconciliationItemMapper itemMapper,
                                     TransactionMapper transactionMapper,
                                     ChannelAdapterRegistry registry) {
        this.taskMapper = taskMapper;
        this.itemMapper = itemMapper;
        this.transactionMapper = transactionMapper;
        this.registry = registry;
    }

    @Override
    @Transactional
    public ReconciliationResponse triggerReconciliation(ReconciliationTriggerRequest request) {
        ReconciliationTask task = new ReconciliationTask();
        task.setId(IdGenerator.generateId());
        task.setTaskNo(IdGenerator.generateTaskNo());
        task.setAppId(request.getAppId());
        task.setChannelCode(request.getChannelCode());
        task.setStartDate(LocalDate.parse(request.getStartDate()));
        task.setEndDate(LocalDate.parse(request.getEndDate()));
        task.setStatus(1);
        task.setCreatedAt(LocalDateTime.now());
        task.setStartedAt(LocalDateTime.now());

        taskMapper.insert(task);
        log.info("对账任务创建: taskId={}, taskNo={}", task.getId(), task.getTaskNo());

        try {
            executeReconciliation(task);
            task.setStatus(2);
            task.setFinishedAt(LocalDateTime.now());
        } catch (Exception e) {
            log.error("对账任务执行失败: taskId={}", task.getId(), e);
            task.setStatus(4);
            task.setFinishedAt(LocalDateTime.now());
        }
        taskMapper.update(task);

        return new ReconciliationResponse(task.getId(), task.getTaskNo(), task.getStatus(), null);
    }

    private void executeReconciliation(ReconciliationTask task) {
        List<Transaction> transactions = transactionMapper.selectByAppIdAndDateRange(
                task.getAppId(), task.getStartDate(), task.getEndDate());

        task.setTotalCount(transactions.size());
        int successCount = 0;
        int diffCount = 0;

        for (Transaction tx : transactions) {
            IPaymentChannel adapter = registry.getChannel(tx.getChannelCode());
            if (adapter == null) {
                diffCount++;
                ReconciliationItem item = new ReconciliationItem();
                item.setId(IdGenerator.generateId());
                item.setTaskId(task.getId());
                item.setTransactionId(tx.getId());
                item.setChannelCode(tx.getChannelCode());
                item.setThirdOrderNo(tx.getThirdOrderNo());
                item.setPlatformAmount(tx.getAmount());
                item.setPlatformStatus(String.valueOf(tx.getStatus()));
                item.setMatchResult(3);
                item.setDiffDetail("通道适配器未找到");
                item.setCreatedAt(LocalDateTime.now());
                itemMapper.insert(item);
                continue;
            }

            try {
                IPaymentChannel.TransactionQueryResult queryResult = adapter.queryOrder(tx.getThirdOrderNo());
                if (queryResult == null) {
                    diffCount++;
                    insertDiffItem(task, tx, 3, "第三方查询结果为空");
                    continue;
                }

                int matchResult = checkMatch(tx, queryResult);
                if (matchResult == 0) {
                    successCount++;
                } else {
                    diffCount++;
                }

                ReconciliationItem item = new ReconciliationItem();
                item.setId(IdGenerator.generateId());
                item.setTaskId(task.getId());
                item.setTransactionId(tx.getId());
                item.setChannelCode(tx.getChannelCode());
                item.setThirdOrderNo(queryResult.getThirdOrderNo());
                item.setThirdAmount(queryResult.getAmount() != null ? java.math.BigDecimal.valueOf(queryResult.getAmount()).divide(new java.math.BigDecimal("100")) : null);
                item.setThirdStatus(queryResult.getStatus());
                item.setPlatformAmount(tx.getAmount());
                item.setPlatformStatus(String.valueOf(tx.getStatus()));
                item.setMatchResult(matchResult);
                item.setDiffDetail(matchResult == 0 ? "一致" : buildDiffMessage(matchResult, tx, queryResult));
                item.setCreatedAt(LocalDateTime.now());
                itemMapper.insert(item);

            } catch (Exception e) {
                log.error("对账单笔处理异常: transactionId={}", tx.getId(), e);
                diffCount++;
                insertDiffItem(task, tx, 3, "查询异常: " + e.getMessage());
            }
        }

        task.setSuccessCount(successCount);
        task.setDiffCount(diffCount);
        log.info("对账任务完成: taskId={}, 总数={}, 成功={}, 差异={}", task.getId(), task.getTotalCount(), successCount, diffCount);
    }

    private int checkMatch(Transaction tx, IPaymentChannel.TransactionQueryResult queryResult) {
        String platformStatus = mapStatus(tx.getStatus());
        String thirdStatus = queryResult.getStatus();

        if (queryResult.getAmount() != null && !java.math.BigDecimal.valueOf(queryResult.getAmount()).equals(tx.getAmount().divide(new java.math.BigDecimal("100")))) {
            return 1;
        }

        if (!platformStatus.equals(thirdStatus)) {
            return 2;
        }

        return 0;
    }

    private String buildDiffMessage(int matchResult, Transaction tx, IPaymentChannel.TransactionQueryResult queryResult) {
        if (matchResult == 1) {
            return String.format("金额不一致: 平台=%.2f, 第三方=%.2f", tx.getAmount().divide(new java.math.BigDecimal("100")).doubleValue(), queryResult.getAmount().doubleValue());
        }
        if (matchResult == 2) {
            return String.format("状态不一致: 平台=%s, 第三方=%s", mapStatus(tx.getStatus()), queryResult.getStatus());
        }
        return "订单在第三方不存在";
    }

    private void insertDiffItem(ReconciliationTask task, Transaction tx, int matchResult, String detail) {
        ReconciliationItem item = new ReconciliationItem();
        item.setId(IdGenerator.generateId());
        item.setTaskId(task.getId());
        item.setTransactionId(tx.getId());
        item.setChannelCode(tx.getChannelCode());
        item.setThirdOrderNo(tx.getThirdOrderNo());
        item.setPlatformAmount(tx.getAmount());
        item.setPlatformStatus(String.valueOf(tx.getStatus()));
        item.setMatchResult(matchResult);
        item.setDiffDetail(detail);
        item.setCreatedAt(LocalDateTime.now());
        itemMapper.insert(item);
    }

    private String mapStatus(Integer status) {
        if (status == 0) return "PENDING";
        if (status == 1) return "PROCESSING";
        if (status == 2) return "SUCCESS";
        if (status == 3) return "FAILED";
        if (status == 5) return "CLOSED";
        return "UNKNOWN";
    }

    @Override
    public List<ReconciliationTask> getTaskList(Long appId, Integer status, Integer page, Integer size) {
        List<ReconciliationTask> all = taskMapper.selectAll();
        if (appId != null) {
            all = all.stream().filter(t -> t.getAppId().equals(appId)).collect(Collectors.toList());
        }
        if (status != null) {
            all = all.stream().filter(t -> t.getStatus().equals(status)).collect(Collectors.toList());
        }
        int pageSize = size != null ? size : 20;
        int pageNum = page != null ? page : 1;
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, all.size());
        if (fromIndex >= all.size()) return new ArrayList<>();
        return all.subList(fromIndex, toIndex);
    }

    @Override
    public ReconciliationTaskDetailResponse getTaskDetail(Long taskId) {
        ReconciliationTask task = taskMapper.selectOneById(taskId);
        if (task == null) return null;

        ReconciliationTaskDetailResponse resp = new ReconciliationTaskDetailResponse();
        resp.setTaskId(task.getId());
        resp.setTaskNo(task.getTaskNo());
        resp.setAppId(task.getAppId());
        resp.setChannelCode(task.getChannelCode());
        resp.setStartDate(task.getStartDate().toString());
        resp.setEndDate(task.getEndDate().toString());
        resp.setStatus(mapTaskStatus(task.getStatus()));
        resp.setTotalCount(task.getTotalCount());
        resp.setSuccessCount(task.getSuccessCount());
        resp.setDiffCount(task.getDiffCount());
        resp.setStartedAt(task.getStartedAt());
        resp.setFinishedAt(task.getFinishedAt());
        return resp;
    }

    @Override
    public List<ReconciliationDiffItem> getDiffItems(Long taskId) {
        List<ReconciliationItem> items = itemMapper.selectByTaskIdAndDiff(taskId);
        List<ReconciliationDiffItem> result = new ArrayList<>();
        for (ReconciliationItem item : items) {
            ReconciliationDiffItem diff = new ReconciliationDiffItem();
            diff.setTransactionId(item.getTransactionId());
            diff.setOrderNo(item.getThirdOrderNo());
            diff.setThirdOrderNo(item.getThirdOrderNo());
            diff.setPlatformAmount(item.getPlatformAmount());
            diff.setThirdAmount(item.getThirdAmount());
            diff.setPlatformStatus(item.getPlatformStatus());
            diff.setThirdStatus(item.getThirdStatus());
            diff.setMatchResult(mapMatchResult(item.getMatchResult()));
            diff.setDiffDetail(item.getDiffDetail());
            result.add(diff);
        }
        return result;
    }

    @Override
    public String getReportCsv(Long taskId) {
        List<ReconciliationItem> items = itemMapper.selectByTaskId(taskId);
        StringBuilder sb = new StringBuilder();
        sb.append("任务编号,交易ID,第三方订单号,平台金额,第三方金额,平台状态,第三方状态,匹配结果,详情\n");
        for (ReconciliationItem item : items) {
            sb.append(String.format("%s,%d,%s,%.2f,%s,%s,%s,%s,%s\n",
                    taskId,
                    item.getTransactionId(),
                    item.getThirdOrderNo(),
                    item.getPlatformAmount() != null ? item.getPlatformAmount().doubleValue() : 0.0,
                    item.getThirdAmount() != null ? item.getThirdAmount().doubleValue() : 0.0,
                    item.getPlatformStatus(),
                    item.getThirdStatus(),
                    mapMatchResult(item.getMatchResult()),
                    item.getDiffDetail()));
        }
        return sb.toString();
    }

    private String mapTaskStatus(Integer status) {
        if (status == 0) return "PENDING";
        if (status == 1) return "RUNNING";
        if (status == 2) return "COMPLETED";
        if (status == 3) return "PARTIAL";
        if (status == 4) return "FAILED";
        return "UNKNOWN";
    }

    private String mapMatchResult(Integer matchResult) {
        if (matchResult == 0) return "CONSISTENT";
        if (matchResult == 1) return "AMOUNT_MISMATCH";
        if (matchResult == 2) return "STATUS_MISMATCH";
        if (matchResult == 3) return "NOT_FOUND";
        return "UNKNOWN";
    }
}
