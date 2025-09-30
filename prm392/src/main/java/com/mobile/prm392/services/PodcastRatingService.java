package com.mobile.prm392.services;

import com.mobile.prm392.entities.Podcast;
import com.mobile.prm392.entities.PodcastRating;
import com.mobile.prm392.entities.User;
import com.mobile.prm392.model.podcastRating.PodcastRatingPageResponse;
import com.mobile.prm392.repositories.IPodcastRatingRepository;
import com.mobile.prm392.repositories.IPodcastRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.DoubleSummaryStatistics;
import java.util.List;

@Service
public class PodcastRatingService {
    @Autowired
    private IPodcastRatingRepository podcastRatingRepository;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private IPodcastRepository podcastRepository;

    // 1. Tạo mới hoặc update rating
    public PodcastRating ratePodcast(Long podcastId, int rating, String comment) {
        User user = authenticationService.getCurrentUser();
        Podcast podcast = podcastRepository.findById(podcastId) // dùng podcastRepository thay vì podcastRatingRepository
                .orElseThrow(() -> new EntityNotFoundException("Podcast không tồn tại"));

        // kiểm tra user đã rating chưa
        PodcastRating podcastRating = podcastRatingRepository.findByPodcastIdAndUserIdAndIsActiveTrue(podcastId, user.getId())
                .orElse(new PodcastRating());

        podcastRating.setPodcast(podcast);
        podcastRating.setUser(user);
        podcastRating.setRating(rating);
        podcastRating.setComment(comment);

        return podcastRatingRepository.save(podcastRating);
    }

    // 2. Lấy danh sách rating theo podcast
    public PodcastRatingPageResponse getByPodcast(Long podcastId, int page, int size) {
        Page podcastRating = podcastRatingRepository.findByPodcastIdAndIsActiveTrue(podcastId, PageRequest.of(page - 1, size));

        PodcastRatingPageResponse response = new PodcastRatingPageResponse();
        response.setContent(podcastRating.getContent());
        response.setTotalPages(podcastRating.getTotalPages());
        response.setPageNumber(podcastRating.getNumber());
        response.setTotalElements(podcastRating.getTotalElements());
        return response;
    }

    // 3. Tính trung bình rating
    public double getAverageRating(Long podcastId) {
        List<PodcastRating> ratings = podcastRatingRepository.findByPodcastIdAndIsActiveTrue(podcastId);
        if (ratings.isEmpty()) return 0.0;

        DoubleSummaryStatistics stats = ratings.stream()
                .mapToDouble(PodcastRating::getRating)
                .summaryStatistics();

        return stats.getAverage();
    }

    // 4. Xóa mềm rating của user
    public boolean deleteRating(Long podcastId) {
        User user = authenticationService.getCurrentUser();

        PodcastRating podcastRating = podcastRatingRepository
                .findByPodcastIdAndUserIdAndIsActiveTrue(podcastId, user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Rating không tồn tại"));

        podcastRating.setActive(false);
        podcastRatingRepository.save(podcastRating);
        return true;
    }

}

