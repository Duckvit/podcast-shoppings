package com.mobile.prm392.repositories;

import com.mobile.prm392.entities.Podcast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IPodcastRepository extends JpaRepository<Podcast, Long> {
    List<Podcast> findByUserId(Long creatorId);
}

