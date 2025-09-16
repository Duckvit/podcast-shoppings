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
    private String method; // VNPay, MoMo, Paypal, Stripe...

    @Column(nullable = false, length = 20)
    private String status; // pending, success, failed, refunded

    private Double amount;

    @Column(name = "transaction_id", length = 100)
    private String transactionId; // mã giao dịch trả về từ cổng thanh toán


    private LocalDateTime createdAt = LocalDateTime.now();


    private LocalDateTime updatedAt = LocalDateTime.now();

    // Một Payment có thể có nhiều Transaction
    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // tránh swagger json infinite loop
    private List<Transaction> transactions = new ArrayList<>();

    private boolean isActive = true;
}
