package com.mobile.prm392.repositories;

import com.mobile.prm392.entities.FavoritePodcast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IFavoritePodcastRepository extends JpaRepository<FavoritePodcast, Long> {
    List<FavoritePodcast> findByUserId(Long userId);
}
