package com.hioas.demo.mapper;

import com.hioas.demo.entity.Transaction;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface TransactionMapper extends BaseMapper<Transaction> {

    default Transaction selectByTransactionNo(String transactionNo) {
        return selectOneByQuery(QueryWrapper.create().where("transaction_no = ?", transactionNo));
    }

    default Transaction selectByMerchantOrderNo(String merchantOrderNo) {
        return selectOneByQuery(QueryWrapper.create().where("merchant_order_no = ?", merchantOrderNo));
    }

    default List<Transaction> selectByAppIdAndStatus(Long appId, Integer status) {
        QueryWrapper query = QueryWrapper.create();
        if (appId != null) query.where("app_id = ?", appId);
        if (status != null) query.where("status = ?", status);
        return selectListByQuery(query);
    }

    default List<Transaction> selectByAppIdAndDateRange(Long appId, LocalDate start, LocalDate end) {
        QueryWrapper query = QueryWrapper.create();
        if (appId != null) query.where("app_id = ?", appId);
        query.where("created_at >= ?", start.atStartOfDay())
             .where("created_at < ?", end.plusDays(1).atStartOfDay());
        return selectListByQuery(query);
    }

    default int updateStatusAndThirdOrderNo(Transaction transaction) {
        return updateByQuery(transaction, QueryWrapper.create().where("id = ?", transaction.getId()));
    }
}
