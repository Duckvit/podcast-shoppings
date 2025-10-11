package com.mobile.prm392.model.podcast;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PodcastResponse {
    private Long id;
    private String title;
    private String description;
    private String audioUrl;
    private String imageUrl;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> categories; // hoặc List<CategoryResponse> nếu muốn chi tiết
}
