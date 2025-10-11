package com.mobile.prm392.model.podcastRating;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PodcastRatingResponse {
    private Long id;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
    private Long podcastId;
    private String podcastTitle;
    private Long userId;
    private String username;
}

