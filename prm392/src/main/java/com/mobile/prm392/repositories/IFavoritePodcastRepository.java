package com.mobile.prm392.repositories;

import com.mobile.prm392.entities.FavoritePodcast;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IFavoritePodcastRepository extends JpaRepository<FavoritePodcast, Long> {
    Page<FavoritePodcast> findByUserIdAndIsActiveTrue(Long userId, Pageable pageable);
    Optional<FavoritePodcast> findByUserIdAndPodcastId(Long userId, Long podcastId);
}
