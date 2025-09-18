package com.mobile.prm392.model.favoritePodcast;

import lombok.Data;

import java.util.List;

@Data
public class FavoritePodcastPageResonse {
    private List<FavoritePodcastResponse> content;
    private int pageNumber;
    private long totalElements;
    private int totalPages;
}
