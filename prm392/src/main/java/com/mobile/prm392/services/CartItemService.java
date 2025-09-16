package com.mobile.prm392.services;

import com.mobile.prm392.entities.Cart;
import com.mobile.prm392.entities.CartItem;
import com.mobile.prm392.entities.Product;
import com.mobile.prm392.repositories.ICartItemRepository;
import com.mobile.prm392.repositories.IProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartItemService {
    @Autowired
    private ICartItemRepository cartItemRepository;

    @Autowired
    private IProductRepository productRepository;

    @Autowired
    private CartService cartService;

    // Thêm sản phẩm vào giỏ
    public CartItem addItem(Long userId, Long productId, Integer quantity) {
        Cart cart = cartService.getOrCreateCart(userId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setCart(cart);
                    newItem.setProduct(product);
                    newItem.setQuantity(0);
                    newItem.setPrice(product.getPrice());
                    return newItem;
                });

        cartItem.setQuantity(cartItem.getQuantity() + quantity);
        return cartItemRepository.save(cartItem);
    }

    // Lấy tất cả item trong giỏ
    public List<CartItem> getItems(Long cartId) {
        return cartItemRepository.findByCartId(cartId);
    }

    // Xóa item
    public void removeItem(Long cartId, Long productId) {
        CartItem item = cartItemRepository.findByCartIdAndProductId(cartId, productId)
                .orElseThrow(() -> new EntityNotFoundException("Item not found"));
        cartItemRepository.delete(item);
    }

    // Xóa hết item
    public void clearCart(Long cartId) {
        List<CartItem> items = cartItemRepository.findByCartId(cartId);
        cartItemRepository.deleteAll(items);
    }
}

