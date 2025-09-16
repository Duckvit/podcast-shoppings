package com.mobile.prm392.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mobile.prm392.entities.User;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "podcast")
public class Podcast {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    private boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Chỉ lưu URL trả về từ Cloudinary
    @Column(nullable = false)
    private String audioUrl;


    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;
}
