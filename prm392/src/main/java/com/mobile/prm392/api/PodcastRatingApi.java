package com.mobile.prm392.api;

import com.mobile.prm392.entities.PodcastRating;
import com.mobile.prm392.model.podcastRating.PodcastRatingPageResponse;
import com.mobile.prm392.model.podcastRating.PodcastRatingRequest;
import com.mobile.prm392.services.PodcastRatingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/podcast-ratings")
@CrossOrigin("*") // cho phep tat ca truy cap, ket noi FE va BE vs nhau
@SecurityRequirement(name = "api") // tao controller moi nho copy qua
public class PodcastRatingApi {
    @Autowired
    private PodcastRatingService podcastRatingService;

    // 1. Rate podcast (tạo mới hoặc update)
    @PostMapping("/ratings/{podcastId}")
    public ResponseEntity<PodcastRating> ratePodcast(
            @PathVariable Long podcastId,
            @RequestBody PodcastRatingRequest request) {

        PodcastRating savedRating = podcastRatingService.ratePodcast(podcastId, request.getRating(), request.getComment());
        return ResponseEntity.ok(savedRating);
    }

    // 2. Lấy danh sách rating theo podcast
    @GetMapping("/ratings/{podcastId}")
    public ResponseEntity getRatingsByPodcast(@PathVariable Long podcastId, @RequestParam int page, @RequestParam int size) {
        PodcastRatingPageResponse ratings = podcastRatingService.getByPodcast(podcastId, page, size);
        return ResponseEntity.ok(ratings);
    }

    // 3. Lấy trung bình rating
    @GetMapping("/ratings/average/{podcastId}")
    public ResponseEntity<Double> getAverageRating(@PathVariable Long podcastId) {
        double average = podcastRatingService.getAverageRating(podcastId);
        return ResponseEntity.ok(average);
    }

    // 4. Xóa mềm rating của user
    @DeleteMapping("/ratings/{podcastId}")
    public ResponseEntity<String> deleteRating(@PathVariable Long podcastId) {
        podcastRatingService.deleteRating(podcastId);
        return ResponseEntity.ok("Rating đã được xóa mềm thành công");
    }
}

