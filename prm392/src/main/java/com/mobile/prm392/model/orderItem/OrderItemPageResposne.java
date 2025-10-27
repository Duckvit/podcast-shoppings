package com.mobile.prm392.model.orderItem;

import com.mobile.prm392.entities.OrderItem;
import lombok.Data;

import java.util.List;

@Data
public class OrderItemPageResposne {
    private List<OrderItem> content;
    private int pageNumber;
    private long totalElements;
    private int totalPages;
}
