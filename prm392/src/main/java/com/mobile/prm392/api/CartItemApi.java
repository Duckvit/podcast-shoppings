package com.mobile.prm392.api;

import com.mobile.prm392.entities.Cart;
import com.mobile.prm392.entities.CartItem;
import com.mobile.prm392.services.CartItemService;
import com.mobile.prm392.services.CartService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cartItem")
@CrossOrigin("*") // cho phep tat ca truy cap, ket noi FE va BE vs nhau
@SecurityRequirement(name = "api") // tao controller moi nho copy qua
public class CartItemApi {
    @Autowired
    private CartService cartService;

    @Autowired
    private CartItemService cartItemService;

    // Thêm sản phẩm vào giỏ hàng
    @PostMapping
    public ResponseEntity addItem(@RequestParam Long productId,
                                  @RequestParam int quantity) {
        Cart cart = cartService.getOrCreateCart();
        CartItem cartItem = cartItemService.addItem(cart, productId, quantity);
        return ResponseEntity.ok(cartItem);
    }

    // Cập nhật số lượng sản phẩm trong giỏ hàng
    @PutMapping("/{cartItemId}")
    public ResponseEntity<CartItem> updateItem(@PathVariable Long cartItemId,
                                               @RequestParam int quantity) {
        CartItem item = cartItemService.updateQuantity(cartItemId, quantity);
        return ResponseEntity.ok(item);
    }

    // Xóa sản phẩm khỏi giỏ hàng
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long cartItemId) {
        cartItemService.removeItem(cartItemId);
        return ResponseEntity.noContent().build();
    }
}
