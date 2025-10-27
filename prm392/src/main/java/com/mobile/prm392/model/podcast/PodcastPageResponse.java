package com.mobile.prm392.model.podcast;

import com.mobile.prm392.entities.Podcast;
import lombok.Data;

import java.util.List;

@Data
public class PodcastPageResponse {
    private List<PodcastResponse> content;
    private int pageNumber;
    private long totalElements;
    private int totalPages;
}
