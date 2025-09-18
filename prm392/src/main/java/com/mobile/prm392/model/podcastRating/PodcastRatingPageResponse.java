package com.mobile.prm392.model.podcastRating;

import com.mobile.prm392.entities.PodcastRating;
import lombok.Data;

import java.util.List;

@Data
public class PodcastRatingPageResponse {
    private List<PodcastRating> content;
    private int pageNumber;
    private long totalElements;
    private int totalPages;
}
