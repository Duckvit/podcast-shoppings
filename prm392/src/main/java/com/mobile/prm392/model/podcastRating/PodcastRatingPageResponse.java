package com.mobile.prm392.model.podcastRating;

import com.mobile.prm392.entities.PodcastRating;
import lombok.Data;

import java.util.List;

@Data
public class PodcastRatingPageResponse {
    private List<PodcastRatingResponse> content;
    private int pageNumber;
    private long totalElements;
    private int totalPages;
}
