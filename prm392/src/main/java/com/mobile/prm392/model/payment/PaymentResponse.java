package com.mobile.prm392.model.payment;

import com.mobile.prm392.model.order.OrderShortResponse;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentResponse {
    private Long id;
    private String method = "payos";
    private String status = "pending";
    private Double amount;
    private LocalDateTime createdAt;

    private OrderShortResponse order; // gọn lại
}
