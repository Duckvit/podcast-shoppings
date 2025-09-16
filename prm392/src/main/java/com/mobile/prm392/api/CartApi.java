package com.mobile.prm392.api;

import com.mobile.prm392.entities.Cart;
import com.mobile.prm392.entities.CartItem;
import com.mobile.prm392.services.CartService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin("*") // cho phep tat ca truy cap, ket noi FE va BE vs nhau
@SecurityRequirement(name = "api") // tao controller moi nho copy qua
public class CartApi {
    @Autowired
    private CartService cartService;

//    @PostMapping
//    public ResponseEntity<Cart> createCart(@RequestBody Cart cart) {
//        return ResponseEntity.ok(cartService.createCart(cart));
//    }
//
//    @GetMapping("/user/{userId}")
//    public ResponseEntity<List<Cart>> getCartsByUser(@PathVariable Long userId) {
//        return ResponseEntity.ok(cartService.getCartsByUser(userId));
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<Cart> getCart(@PathVariable Long id) {
//        return ResponseEntity.ok(cartService.getCart(id));
//    }
//
//    @PostMapping("/{cartId}/items")
//    public ResponseEntity<CartItem> addItem(@PathVariable Long cartId, @RequestBody CartItem item) {
//        return ResponseEntity.ok(cartService.addItemToCart(cartId, item));
//    }
//
//    @GetMapping("/{cartId}/items")
//    public ResponseEntity<List<CartItem>> getItems(@PathVariable Long cartId) {
//        return ResponseEntity.ok(cartService.getItems(cartId));
//    }
//
//    @DeleteMapping("/items/{itemId}")
//    public ResponseEntity<Void> removeItem(@PathVariable Long itemId) {
//        cartService.removeItem(itemId);
//        return ResponseEntity.noContent().build();
//    }
}

