package com.mobile.prm392.repositories;

import com.mobile.prm392.entities.Podcast;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IPodcastRepository extends JpaRepository<Podcast, Long> {
    List<Podcast> findByUserId(Long creatorId);
    Page findAll(Pageable pageable);
    Page findAllByIsActiveTrue(Pageable pageable);
    Page findByUserIdAndIsActiveTrue(Long creatorId, Pageable pageable);

    @Query("SELECT p FROM Podcast p JOIN p.categories c WHERE c.name = :categoryName AND p.isActive = true")
    List<Podcast> findByCategoryName(@Param("categoryName") String categoryName);
}

