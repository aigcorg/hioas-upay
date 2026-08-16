package com.hioas.demo.controller;

import com.hioas.demo.dto.*;
import com.hioas.demo.entity.Transaction;
import com.hioas.demo.entity.Refund;
import com.hioas.demo.entity.ChannelInstance;
import com.hioas.demo.service.PaymentService;
import com.hioas.demo.service.impl.PaymentServiceImpl.PaymentExecutionResult;
import com.hioas.demo.service.impl.PaymentServiceImpl;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/v1/pay")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentServiceImpl paymentServiceImpl;

    public PaymentController(PaymentService paymentService, PaymentServiceImpl paymentServiceImpl) {
        this.paymentService = paymentService;
        this.paymentServiceImpl = paymentServiceImpl;
    }

    @PostMapping("/order/create")
    public ApiResponse<PaymentOrderResponse> createOrder(@Valid @RequestBody PaymentOrderRequest request) {
        PaymentOrderResponse resp = paymentService.createOrder(request);
        return ApiResponse.success(resp);
    }

    @GetMapping("/order/{orderNo}")
    public ApiResponse<PaymentOrderResponse> getOrderByNo(@PathVariable String orderNo) {
        Transaction tx = paymentService.getOrderByTransactionNo(orderNo);
        if (tx == null) return ApiResponse.error("ORDER_NOT_FOUND", "订单不存在");
        PaymentOrderResponse resp = new PaymentOrderResponse();
        resp.setOrderId(tx.getId());
        resp.setOrderNo(tx.getTransactionNo());
        resp.setAmount(tx.getAmount().divide(new BigDecimal("100")).longValue());
        resp.setCurrency(tx.getCurrency());
        resp.setStatus(mapStatus(tx.getStatus()));
        resp.setPaidAt(tx.getPaidAt() != null ? tx.getPaidAt().toString() : null);
        resp.setCreatedAt(tx.getCreatedAt() != null ? tx.getCreatedAt().toString() : null);
        return ApiResponse.success(resp);
    }

    @PostMapping("/order/execute")
    public ApiResponse<PaymentExecuteResponse> executePayment(@Valid @RequestBody PaymentExecuteRequest request) {
        PaymentExecutionResult result = paymentServiceImpl.executePayment(
                request.getAppId(), request.getUserId(), request.getMerchantOrderNo(),
                request.getAmount(), request.getCurrency(), request.getScene(),
                request.getRiskLevel(), request.getRegion(), request.getDevice(),
                request.getContextMap());

        if (result.isNeedsAuth()) {
            ChannelInstance inst = result.getChannelInstance();
            return ApiResponse.success(new PaymentExecuteResponse("NEED_AUTH", "需要授权", 
                    inst.getChannelCode(), inst.getChannelCode()));
        }

        if (result.isSuccess()) {
            Transaction tx = result.getTransaction();
            return ApiResponse.success(new PaymentExecuteResponse("SUCCESS", "支付成功",
                    tx.getChannelCode(), tx.getThirdOrderNo()));
        } else {
            return ApiResponse.error("PAYMENT_FAILED", "支付失败: " + result.getTransaction().getFailureReason());
        }
    }

    @PostMapping("/order/query")
    public ApiResponse<PaymentOrderResponse> queryOrder(@Valid @RequestBody PaymentOrderQueryRequest request) {
        PaymentOrderResponse resp = paymentService.queryOrder(request.getOrderNo());
        return ApiResponse.success(resp);
    }

    @PostMapping("/refund")
    public ApiResponse<RefundResponse> refund(@Valid @RequestBody RefundRequest request) {
        RefundResponse resp = paymentService.refund(request);
        return ApiResponse.success(resp);
    }

    @GetMapping("/refund/{refundNo}")
    public ApiResponse<RefundResponse> getRefund(@PathVariable String refundNo) {
        Refund refund = paymentService.getRefundByNo(refundNo);
        if (refund == null) return ApiResponse.error("REFUND_NOT_FOUND", "退款记录不存在");
        RefundResponse resp = new RefundResponse();
        resp.setRefundId(refund.getId());
        resp.setRefundNo(refund.getRefundNo());
        Transaction tx = paymentService.getTransactionById(refund.getTransactionId());
        resp.setOrderNo(tx != null ? tx.getTransactionNo() : refund.getTransactionId().toString());
        resp.setAmount(refund.getAmount().divide(new BigDecimal("100")).longValue());
        resp.setStatus(refund.getStatus() == 2 ? "SUCCESS" : "FAILED");
        resp.setChannelCode(refund.getChannelCode());
        resp.setThirdRefundNo(refund.getThirdRefundNo());
        resp.setCreatedAt(refund.getCreatedAt() != null ? refund.getCreatedAt().toString() : null);
        return ApiResponse.success(resp);
    }

    @PostMapping("/order/{orderNo}/cancel")
    public ApiResponse<String> cancelOrder(@PathVariable String orderNo) {
        boolean result = paymentService.cancelOrder(orderNo);
        if (result) return ApiResponse.success("取消成功");
        return ApiResponse.error("CANCEL_FAILED", "取消失败");
    }

    private String mapStatus(Integer status) {
        if (status == 0) return "PENDING";
        if (status == 1) return "PROCESSING";
        if (status == 2) return "SUCCESS";
        if (status == 3) return "FAILED";
        if (status == 5) return "CLOSED";
        return "UNKNOWN";
    }
}
