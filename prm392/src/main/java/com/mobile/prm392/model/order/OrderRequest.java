package com.mobile.prm392.model.order;

import com.mobile.prm392.model.orderItem.OrderItemRequest;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    List<OrderItemRequest> items;
}
