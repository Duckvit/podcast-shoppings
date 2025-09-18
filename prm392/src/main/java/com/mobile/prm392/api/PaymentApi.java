package com.mobile.prm392.api;

import com.mobile.prm392.entities.Order;
import com.mobile.prm392.entities.Payment;
import com.mobile.prm392.model.order.OrderRequest;
import com.mobile.prm392.services.PaymentService;
import com.mobile.prm392.services.TransactionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin("*") // cho phep tat ca truy cap, ket noi FE va BE vs nhau
@SecurityRequirement(name = "api") // tao controller moi nho copy qua
public class PaymentApi {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private TransactionService transactionService;

    // Lấy tất cả payments
    @GetMapping
    public ResponseEntity getAllPayments(@RequestParam int page, @RequestParam int size) {
        return ResponseEntity.ok(paymentService.getAllPayments(page, size));
    }

    // Lấy payment theo id
    @GetMapping("/{id}")
    public ResponseEntity getPaymentById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(paymentService.getPaymentById(id));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Tạo mới payment
//    @PostMapping
//    public ResponseEntity<Payment> createPayment(@RequestParam Long orderId) {
//        try {
//            Payment created = paymentService.createPayment(payment);
//            return ResponseEntity.status(HttpStatus.CREATED).body(created);
//        } catch (IllegalStateException ex) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
//        }
//    }

    //thanh toan
    @PostMapping
    public ResponseEntity payOrder(@RequestParam Long orderId) throws Exception {
        String vnpayUrl = paymentService.createUrl(orderId);
        return ResponseEntity.ok(vnpayUrl);
    }

    // Cập nhật payment
    @PutMapping("/{id}")
    public ResponseEntity<Payment> updatePayment(@PathVariable Long id, @RequestBody Payment payment) {
        try {
            return ResponseEntity.ok(paymentService.updatePayment(id, payment));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Xóa payment
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        try {
            paymentService.deletePayment(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
