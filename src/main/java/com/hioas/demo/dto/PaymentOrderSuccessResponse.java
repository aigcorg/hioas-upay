package com.hioas.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrderSuccessResponse {

    private Long orderId;
    private String orderNo;
    private Long amount;
    private String currency;
    private String status;           // PENDING_PAY
    private PayInfo payInfo;
    private String createdAt;

    @Data
    public static class PayInfo {
        private String channelCode;
        private String channelName;
        private String payType;            // jsapi, native, app, h5, etc.
        private Map<String, Object> payData;  // 第三方具体参数
    }
}
