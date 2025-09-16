package com.mobile.prm392.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "`order`") // order la tu khoa trong sql nen can dat trong dau ``
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private Double totalAmount;

    @Column(length = 20)
    private String status = "pending"; // pending, paid, shipped

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL) // Casecade ALL de khi xoa order thi xoa het order item luon ( chuc nang cho phep luu cac bang reference toi no )
    private List<OrderItem> items;

    private boolean isActive = true;
}
