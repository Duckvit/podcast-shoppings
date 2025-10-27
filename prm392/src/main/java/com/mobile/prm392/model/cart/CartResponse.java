package com.mobile.prm392.model.cart;

import com.mobile.prm392.model.cartItem.CartItemResponse;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CartResponse {
    private Long id;
    private Double totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CartItemResponse> items;
}