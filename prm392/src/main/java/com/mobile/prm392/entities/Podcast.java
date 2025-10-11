package com.mobile.prm392.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mobile.prm392.entities.User;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "podcast")
public class Podcast {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany // Một podcast có thể có nhiều category và ngược lại
    @JoinTable(
            name = "podcast_category",
            joinColumns = @JoinColumn(name = "podcast_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> categories;

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

    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;
}
