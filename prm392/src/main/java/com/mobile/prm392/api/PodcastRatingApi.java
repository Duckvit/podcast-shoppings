package com.mobile.prm392.api;

import com.mobile.prm392.entities.PodcastRating;
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

    @GetMapping("/podcast/{podcastId}")
    public ResponseEntity<List<PodcastRating>> getByPodcast(@PathVariable Long podcastId) {
        return ResponseEntity.ok(podcastRatingService.getByPodcast(podcastId));
    }

    @PostMapping
    public ResponseEntity<PodcastRating> create(@RequestBody PodcastRating rating) {
        return ResponseEntity.ok(podcastRatingService.save(rating));
    }
}

