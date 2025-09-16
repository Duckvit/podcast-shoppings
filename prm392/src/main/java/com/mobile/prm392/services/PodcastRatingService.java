package com.mobile.prm392.services;

import com.mobile.prm392.entities.PodcastRating;
import com.mobile.prm392.repositories.IPodcastRatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PodcastRatingService {
    @Autowired
    private IPodcastRatingRepository podcastRatingRepository;

    public List<PodcastRating> getByPodcast(Long podcastId) {
        return podcastRatingRepository.findByPodcastId(podcastId);
    }

    public PodcastRating save(PodcastRating rating) {
        return podcastRatingRepository.save(rating);
    }
}

