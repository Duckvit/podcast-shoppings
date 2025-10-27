package com.mobile.prm392.model.order;

import lombok.Data;

@Data
public class OrderShortResponse {
    private Long id;
    private Double totalAmount;
    private String status;
}
