package com.mobile.prm392.services;

import com.mobile.prm392.entities.Cart;
import com.mobile.prm392.entities.CartItem;
import com.mobile.prm392.entities.User;
import com.mobile.prm392.repositories.ICartItemRepository;
import com.mobile.prm392.repositories.ICartRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {
    @Autowired
    private ICartRepository cartRepository;

    @Autowired
    private AuthenticationService authenticationService;

    // Lấy giỏ hàng active của user, nếu chưa có thì tạo
    public Cart getOrCreateCart(Long userId) {

        User user = authenticationService.getCurrentUser();

        return cartRepository.findByUserIdAndIsActiveTrue(userId)
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUser(user);
                    return cartRepository.save(cart);
                });
    }

    public Cart getCartById(Long cartId) {
        return cartRepository.findById(cartId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found"));
    }

    public List<Cart> getCartsByUser(Long userId) {
        return cartRepository.findAll()
                .stream()
                .filter(c -> c.getUser().getId().equals(userId))
                .toList();
    }

    public Cart saveCart(Cart cart) {
        return cartRepository.save(cart);
    }
}

