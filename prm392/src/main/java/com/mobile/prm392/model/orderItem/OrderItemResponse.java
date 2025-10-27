package com.mobile.prm392.model.orderItem;

import com.mobile.prm392.model.product.ProductShortResponse;
import lombok.Data;

@Data
public class OrderItemResponse {
    private Long id;
    private ProductShortResponse product;
    private Integer quantity;
    private Double price;
}