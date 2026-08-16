package com.hioas.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrderUnauthorizedResponse {

    private Long orderId;
    private String orderNo;
    private Long amount;
    private String currency;
    private String status;                // PENDING_AUTH
    private AuthNeededInfo authNeeded;
    private String createdAt;

    @Data
    public static class AuthNeededInfo {
        private Long appId;
        private Long userId;
        private List<ChannelInfo> channels;
    }

    @Data
    public static class ChannelInfo {
        private String code;
        private String name;
    }
}
