package com.mobile.prm392.api;

import com.mobile.prm392.entities.FavoritePodcast;
import com.mobile.prm392.services.FavoritePodcastService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorite-podcasts")
@CrossOrigin("*") // cho phep tat ca truy cap, ket noi FE va BE vs nhau
@SecurityRequirement(name = "api") // tao controller moi nho copy qua
public class FavoritePodcastApi {
    @Autowired
    private FavoritePodcastService favoritePodcastService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FavoritePodcast>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(favoritePodcastService.getByUser(userId));
    }

    @PostMapping
    public ResponseEntity<FavoritePodcast> create(@RequestBody FavoritePodcast favorite) {
        return ResponseEntity.ok(favoritePodcastService.save(favorite));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        favoritePodcastService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

