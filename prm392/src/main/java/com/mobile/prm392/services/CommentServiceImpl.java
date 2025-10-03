package com.mobile.prm392.services;

import com.mobile.prm392.entities.Comment;
import com.mobile.prm392.entities.Podcast;
import com.mobile.prm392.repositories.ICommentRepository;
import com.mobile.prm392.repositories.IPodcastRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private ICommentRepository commentRepository;

    @Autowired
    private IPodcastRepository podcastRepository;

    @Override
    public Comment createComment(Long podcastId, String commentUser, String content){
        // Lấy podcast theo id
        Podcast podcast = podcastRepository.findById(podcastId)
                .orElseThrow(() -> new EntityNotFoundException("Podcast with id " + podcastId + " not found"));

        // Tạo comment mới
        Comment comment = new Comment();
        comment.setPodcast(podcast);
        comment.setContent(content);
        comment.setCommentUser(commentUser);
        comment.setCreatedAt(LocalDateTime.now());

        // Lưu vào DB và return
        return commentRepository.save(comment);
    }

    @Override
    public List<Comment> getAllCommentsByPodcastId(Long podcastId) {
        // Kiểm tra podcast tồn tại
        Podcast podcast = podcastRepository.findById(podcastId)
                .orElseThrow(() -> new EntityNotFoundException("Podcast with id " + podcastId + " not found"));

        // Lấy danh sách comment
        return commentRepository.findAllByPodcastIdOrderByCreatedAtDesc(podcast.getId());
    }

}
