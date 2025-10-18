package com.mobile.prm392.repositories;

import com.mobile.prm392.entities.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ICommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByPodcastIdOrderByCreatedAtDesc(Long podcastId);

    List<Comment> findByPodcastId(Long podcastId);
}
