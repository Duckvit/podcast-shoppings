package com.mobile.prm392.model.transaction;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransactionResponse {
    private Long id;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long fromUserId;
    private String fromUsername;

    private Long toUserId;
    private String toUsername;

    private Long paymentId;
    private Double totalAmount;
    private String orderStatus;
}
