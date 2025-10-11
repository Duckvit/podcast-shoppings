package com.mobile.prm392.services;

import com.mobile.prm392.entities.*;
import com.mobile.prm392.model.cart.CartResponse;
import com.mobile.prm392.model.order.OrderAddress;
import com.mobile.prm392.repositories.ICartRepository;
import com.mobile.prm392.repositories.IOrderRepository;
import com.mobile.prm392.repositories.IProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private IProductRepository productRepository;

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
    public Order checkoutCart(OrderAddress orderAddress) {
        Cart cart = getOrCreateCart();
        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Giỏ hàng đang trống!");
        }

        Order order = new Order();
        order.setUser(cart.getUser());
        order.setAddress(orderAddress.getAddress());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setStatus("Pending");

        List<OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0.0;

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            // Kiểm tra tồn kho
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new IllegalArgumentException("Không đủ tồn kho cho sản phẩm: " + product.getName());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(product.getPrice());

            orderItems.add(orderItem);

            totalAmount += orderItem.getPrice() * orderItem.getQuantity();

            // Trừ tồn kho
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }

        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);

        // Disable cart sau khi checkout
        cart.setActive(false);
        cartRepository.save(cart);

        return orderRepository.save(order);
    }

}


