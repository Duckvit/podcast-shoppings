package com.mobile.prm392.model.cartItem;

import lombok.Data;

@Data
public class CartItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Double price;
    private Integer quantity;
}