package com.mobile.prm392.services;

import com.mobile.prm392.entities.Comment;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CommentService {

    Comment createComment(Long podcastId, String commentUser, String content);

    List<Comment> getAllCommentsByPodcastId(Long podcastId);

    Comment updateComment(Long id, String newContent);

    void deleteComment(Long id);
}
