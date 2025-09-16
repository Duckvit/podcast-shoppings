package com.mobile.prm392.repositories;

import com.mobile.prm392.entities.PodcastRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IPodcastRatingRepository extends JpaRepository<PodcastRating, Long> {
    List<PodcastRating> findByPodcastIdAndIsActiveTrue(Long podcastId);
    Optional<PodcastRating> findByPodcastIdAndUserIdAndIsActiveTrue(Long podcastId, Long userId);

}

