package com.mobile.prm392.api;

import com.mobile.prm392.entities.Transaction;
import com.mobile.prm392.services.TransactionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin("*") // cho phep tat ca truy cap, ket noi FE va BE vs nhau
@SecurityRequirement(name = "api") // tao controller moi nho copy qua
public class TransactionApi {

    @Autowired
    private TransactionService transactionService;



    // Lấy tất cả transaction
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity getAllTransactions(@RequestParam int page, @RequestParam int size) {
        return ResponseEntity.ok(transactionService.getAllTransactions(page, size));
    }

    // Lấy transaction theo ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable Long id) {
        Optional<Transaction> transaction = transactionService.getTransactionById(id);
        return transaction.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Lấy transaction theo PaymentId
    @GetMapping("/payment/{paymentId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity getTransactionsByPaymentId(@PathVariable Long paymentId, @RequestParam int page, @RequestParam int size) {
        return ResponseEntity.ok(transactionService.getTransactionsByPaymentId(paymentId, page, size));
    }

    // Lấy transaction theo status
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity getTransactionsByStatus(@PathVariable String status, @RequestParam int page, @RequestParam int size) {
        return ResponseEntity.ok(transactionService.getTransactionsByStatus(status, page, size));
    }

    // Tạo transaction thanh toán thành công
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity saveTransaction(@RequestParam Long orderId) {
        transactionService.createTransactionSuccess(orderId);
        return ResponseEntity.ok("Thanh toán thành công cho order " + orderId);
    }

    // Xóa transaction
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
//        transactionService.deleteTransaction(id);
//        return ResponseEntity.noContent().build();
//    }
}
