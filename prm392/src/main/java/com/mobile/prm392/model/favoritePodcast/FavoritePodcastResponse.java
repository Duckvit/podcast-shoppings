package com.mobile.prm392.model.favoritePodcast;

import lombok.Data;

@Data
public class FavoritePodcastResponse {
    private Long id;
    private Long podcastId;
    private String podcastTitle; // có thể lấy tên podcast
}
