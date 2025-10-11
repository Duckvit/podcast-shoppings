package com.mobile.prm392.model.payos;

import lombok.*;

@Data
public class WebhookRequest {
    private String code;
    private String desc;
    private boolean success;
    private String signature;
    private WebhookData data;

    @Data
    public static class WebhookData {
        private Long orderCode;
        private Long amount;
        private String description;
        private String accountNumber;
        private String reference;
        private String transactionDateTime;
        private String currency;
        private String paymentLinkId;
        private String code;
        private String desc;
        private String counterAccountBankId;
        private String counterAccountBankName;
        private String counterAccountName;
        private String counterAccountNumber;
        private String virtualAccountName;
        private String virtualAccountNumber;
    }
}