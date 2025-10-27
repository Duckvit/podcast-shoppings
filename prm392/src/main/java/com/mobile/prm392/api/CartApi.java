package com.mobile.prm392.api;

import com.mobile.prm392.entities.Cart;
import com.mobile.prm392.entities.CartItem;
import com.mobile.prm392.entities.Order;
import com.mobile.prm392.model.cart.CartResponse;
import com.mobile.prm392.model.order.OrderAddress;
import com.mobile.prm392.services.CartService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin("*") // cho phep tat ca truy cap, ket noi FE va BE vs nhau
@SecurityRequirement(name = "api") // tao controller moi nho copy qua
public class CartApi {
    @Autowired
    private CartService cartService;

    // Lấy giỏ hàng active của user
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity getCart() {
        CartResponse cart = cartService.getOrCreateCartResponse();
        return ResponseEntity.ok(cart);
    }

    // Tính tổng tiền giỏ hàng
    @GetMapping("/total")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Double> getTotal() {
        double total = cartService.calculateTotal();
        return ResponseEntity.ok(total);
    }

    // Checkout giỏ hàng → tạo Order
    @PostMapping("/checkout")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity checkoutCart(@RequestBody OrderAddress orderAddress) {
        Order order = cartService.checkoutCart(orderAddress);
        return ResponseEntity.ok(order);
    }
}

