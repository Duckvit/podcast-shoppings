package com.mobile.prm392.model.podcastRating;

import lombok.Data;

@Data
public class PodcastRatingRequest {
    private int rating;
    private String comment;
}
