package com.mobile.prm392.model.order;

import com.mobile.prm392.model.orderItem.OrderItemRequest;
import lombok.Data;

import java.util.List;

@Data
public class OrderUpdateRequest {
    private String status; // FE chỉ được phép đổi trạng thái
    private List<OrderItemRequest> items; // Cho phép đổi danh sách item
}
