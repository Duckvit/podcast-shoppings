package com.mobile.prm392.services;

import com.mobile.prm392.entities.*;
import com.mobile.prm392.repositories.ICartItemRepository;
import com.mobile.prm392.repositories.ICartRepository;
import com.mobile.prm392.repositories.IProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartItemService {

    @Autowired
    private ICartItemRepository cartItemRepository;

    @Autowired
    private IProductRepository productRepository;

    @Autowired
    private ICartRepository cartRepository;

    // Thêm sản phẩm vào giỏ hàng
    @Transactional
    public CartItem addItem(Cart cart, Long productId, int quantity) {
        // Tải lại cart từ DB để chắc chắn nó được quản lý bởi Hibernate
        Cart managedCart = cartRepository.findById(cart.getId())
                .orElseThrow(() -> new EntityNotFoundException("Cart not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        // Truy cập an toàn (Hibernate Session vẫn mở)
        CartItem existingItem = managedCart.getItems().stream()
                .filter(ci -> ci.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            return cartItemRepository.save(existingItem);
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setCart(managedCart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setPrice(product.getPrice() * quantity);
            product.setStockQuantity(product.getStockQuantity() - quantity);
            productRepository.save(product);
            cartItemRepository.save(cartItem);
            managedCart.getItems().add(cartItem);
            return cartItem;
        }
    }



    // Xóa item khỏi giỏ hàng
    public void removeItem(Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("Cart item not found"));
        cartItemRepository.delete(item);
    }

    // Tính tổng tiền giỏ hàng
    public double calculateTotal(Cart cart) {
        return cart.getItems().stream()
                .mapToDouble(ci -> ci.getProduct().getPrice() * ci.getQuantity())
                .sum();
    }

    // Chuyển cart item sang order item
    public List<OrderItem> convertToOrderItems(Cart cart, Order order) {
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem ci : cart.getItems()) {
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(ci.getProduct());
            oi.setQuantity(ci.getQuantity());
            oi.setPrice(ci.getProduct().getPrice());
            orderItems.add(oi);
        }
        return orderItems;
    }

    public CartItem updateQuantity(Long cartItemId, int quantity) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("Cart item not found"));
        item.setQuantity(quantity);
        return cartItemRepository.save(item);
    }
}


