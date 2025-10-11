package com.mobile.prm392.repositories;

import com.mobile.prm392.entities.Podcast;
import com.mobile.prm392.entities.PodcastRating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IPodcastRatingRepository extends JpaRepository<PodcastRating, Long> {
    List<PodcastRating> findByPodcastIdAndIsActiveTrue(Long podcastId);
    Optional<PodcastRating> findByPodcastIdAndUserIdAndIsActiveTrue(Long podcastId, Long userId);
    Page findByPodcastIdAndIsActiveTrue(Long podcastId, Pageable pageable);
}

