package com.mobile.prm392.services;

import com.mobile.prm392.entities.*;
import com.mobile.prm392.model.cart.CartResponse;
import com.mobile.prm392.repositories.ICartRepository;
import com.mobile.prm392.repositories.IOrderRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CartService {

    @Autowired
    private ICartRepository cartRepository;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private CartItemService cartItemService;

    @Autowired
    private IOrderRepository orderRepository;

    @Autowired
    private ModelMapper modelMapper;

    // Lấy giỏ hàng active của user hoặc tạo mới
    public Cart getOrCreateCart() {
        User user = authenticationService.getCurrentUser();
        return cartRepository.findByUserIdAndIsActiveTrue(user.getId())
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUser(user);
                    cart.setActive(true);
                    return cartRepository.save(cart);
                });
    }

    // lay gio hang gui len api

    public CartResponse getOrCreateCartResponse() {
        User user = authenticationService.getCurrentUser();

        Cart cart = cartRepository.findByUserIdAndIsActiveTrue(user.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setActive(true);
                    return cartRepository.save(newCart);
                });

        // Dùng ModelMapper để map sang DTO
        return modelMapper.map(cart, CartResponse.class);
    }


    // Tính tổng tiền giỏ hàng
    public double calculateTotal() {
        Cart cart = getOrCreateCart();
        return cartItemService.calculateTotal(cart);
    }

    // Checkout: từ cart tạo Order

    public Order checkoutCart() {
        Cart cart = getOrCreateCart();
        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Giỏ hàng đang trống!");
        }

        Order order = new Order();
        order.setUser(cart.getUser());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setStatus("Pending");

        List<OrderItem> orderItems = cartItemService.convertToOrderItems(cart, order);
        double totalAmount = orderItems.stream()
                .mapToDouble(oi -> oi.getPrice() * oi.getQuantity())
                .sum();

        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);

        // Disable cart sau khi checkout
        cart.setActive(false);
        cartRepository.save(cart);

        return orderRepository.save(order);
    }
}


