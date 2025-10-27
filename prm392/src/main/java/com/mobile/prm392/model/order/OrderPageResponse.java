package com.mobile.prm392.model.order;

import com.mobile.prm392.entities.Order;
import lombok.Data;

import java.util.List;

@Data
public class OrderPageResponse {
    private List<OrderResponse> content;
    private int pageNumber;
    private long totalElements;
    private int totalPages;
}
