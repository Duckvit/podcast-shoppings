package com.mobile.prm392.api;

import com.mobile.prm392.entities.Order;
import com.mobile.prm392.model.order.CompleteOrderRequest;
import com.mobile.prm392.model.order.OrderRequest;
import com.mobile.prm392.model.order.OrderUpdateRequest;
import com.mobile.prm392.repositories.IOrderRepository;
import com.mobile.prm392.services.OrderService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin("*") // cho phep tat ca truy cap, ket noi FE va BE vs nhau
@SecurityRequirement(name = "api") // tao controller moi nho copy qua
public class OrderApi {

    @Autowired
    private OrderService orderService;

    @Autowired
    private IOrderRepository orderRepository;

    // Lấy tất cả orders
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity getAllOrders(@RequestParam int page, @RequestParam int size) {
        return ResponseEntity.ok(orderService.getAllOrders(page, size));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity getOrdersByUserId(
            @PathVariable Long userId,
            @RequestParam int page,
            @RequestParam int size) {
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId, page, size));
    }

    // Lấy order theo id
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity getOrderById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(orderService.getOrderById(id));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Tạo order mới
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity createOrder(@RequestBody OrderRequest orderRequest) {
        Order order = orderService.createOrder(orderRequest);
        return ResponseEntity.ok(order);
    }

    // Cập nhật order
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Order> updateOrder(@PathVariable Long id, @RequestBody OrderUpdateRequest order) {
        try {
            return ResponseEntity.ok(orderService.updateOrder(id, order));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Xóa order
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity deleteOrder(@PathVariable Long id) {
//        try {
//            orderService.deleteOrder(id);
//            return ResponseEntity.noContent().build();
//        } catch (EntityNotFoundException ex) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//        }
        boolean result = orderService.deleteOrder(id);
        String resultString;
        if (result) {
            resultString = "Delete successfully";
        }
        return ResponseEntity.ok(result);
    }

    @PutMapping("/complete")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<String> completeOrder(@RequestBody CompleteOrderRequest request) {
        try {
            Long orderId = request.getOrderId();
            if (orderId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("orderId is null");
            }

            Order order = orderRepository.findById(orderId).orElse(null);
            if (order == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("order not found");
            }

            order.setStatus("completed");
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);

            return ResponseEntity.ok("Order completed");
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating complete order");
        }
    }

}
