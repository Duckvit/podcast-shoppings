package com.mobile.prm392.model.order;

import com.mobile.prm392.model.orderItem.OrderItemResponse;
import com.mobile.prm392.model.user.UserShortResponse;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private Long id;
    private UserShortResponse user;
    private Double totalAmount;
    private String status;
    private String address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemResponse> items;
}
