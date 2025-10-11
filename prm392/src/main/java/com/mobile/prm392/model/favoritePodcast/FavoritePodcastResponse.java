package com.mobile.prm392.model.favoritePodcast;

import lombok.Data;

@Data
public class FavoritePodcastResponse {
    private Long id;
    private boolean active;
    private Long podcastId;
    private String podcastTitle;
    private Long userId;
    private String username;
}
