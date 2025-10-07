package com.mobile.prm392.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "payment")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false, length = 50)
    private String method; // VNPay, MoMo, Paypal, Stripe, PayOS...

    @Column(nullable = false, length = 20)
    private String status = "pending"; // pending, success, failed, cancel

    private Double amount;

    @Column(name = "transaction_id", length = 100, unique = true)
    private String transactionId; // mã giao dịch trả về từ cổng thanh toán

    // Link checkout từ PayOS (redirect cho user)
    @Column(name = "checkout_url", columnDefinition = "TEXT")
    private String checkoutUrl;


    private LocalDateTime createdAt = LocalDateTime.now();


    private LocalDateTime updatedAt = LocalDateTime.now();

    // Một Payment có thể có nhiều Transaction
    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // tránh swagger json infinite loop
    private List<Transaction> transactions = new ArrayList<>();

    private boolean isActive = true;
}
