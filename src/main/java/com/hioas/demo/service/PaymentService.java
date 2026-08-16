package com.hioas.demo.service;

import com.hioas.demo.entity.Transaction;
import com.hioas.demo.dto.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface PaymentService {

    /**
     * 创建支付订单 (不执行支付,仅创建订单记录)
     */
    PaymentOrderResponse createOrder(PaymentOrderRequest request);

    /**
     * 获取订单状态
     */
    Transaction getOrderByTransactionNo(String orderNo);

    /**
     * 查询订单(幂等)
     */
    PaymentOrderResponse queryOrder(String orderNo);

    /**
     * 获取交易记录列表 (供商户查看)
     */
    PageResult<Transaction> getTransactionList(Long appId, Integer status, Integer page, Integer size);

    /**
     * 按交易ID获取交易记录
     */
    Transaction getTransactionById(Long id);

    /**
     * 退款
     */
    RefundResponse refund(RefundRequest request);

    /**
     * 查询退款状态
     */
    com.hioas.demo.entity.Refund getRefundByNo(String refundNo);

    /**
     * 取消订单 (仅部分通道支持)
     */
    boolean cancelOrder(String orderNo);
}
