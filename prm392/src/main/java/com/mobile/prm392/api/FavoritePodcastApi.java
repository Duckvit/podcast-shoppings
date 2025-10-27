package com.mobile.prm392.api;

import com.mobile.prm392.entities.FavoritePodcast;
import com.mobile.prm392.model.favoritePodcast.FavoritePodcastPageResonse;
import com.mobile.prm392.model.favoritePodcast.FavoritePodcastResponse;
import com.mobile.prm392.services.FavoritePodcastService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorite-podcasts")
@CrossOrigin("*") // cho phep tat ca truy cap, ket noi FE va BE vs nhau
@SecurityRequirement(name = "api") // tao controller moi nho copy qua
public class FavoritePodcastApi {
    @Autowired
    private FavoritePodcastService favoritePodcastService;

    // 1. Lấy danh sách favorite của user hiện tại
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<FavoritePodcastPageResonse> getFavorites(@RequestParam int page, @RequestParam int size) {
        FavoritePodcastPageResonse favorites = favoritePodcastService.getFavoritesOfCurrentUser(page, size);
        return ResponseEntity.ok(favorites);
    }

    // 2. Thêm hoặc đánh dấu favorite
    @PostMapping("/{podcastId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity addFavorite(@PathVariable Long podcastId) {
        FavoritePodcastResponse favorite = favoritePodcastService.toggleFavorite(podcastId);
        return ResponseEntity.ok(favorite);
    }

    // 3. Xóa mềm favorite
    @DeleteMapping("/{podcastId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<String> removeFavorite(@PathVariable Long podcastId) {
        favoritePodcastService.removeFavorite(podcastId);
        return ResponseEntity.ok("Favorite đã được xóa mềm thành công");
    }
}

