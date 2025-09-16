package com.mobile.prm392.api;

import com.mobile.prm392.entities.Podcast;
import com.mobile.prm392.services.PodcastService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/podcasts")
@CrossOrigin("*") // cho phep tat ca truy cap, ket noi FE va BE vs nhau
@SecurityRequirement(name = "api") // tao controller moi nho copy qua
public class PodcastApi {
    @Autowired
    private PodcastService podcastService;

    @Operation(summary = "Upload podcast audio", description = "Upload file audio lên Cloudinary")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upload thành công"),
            @ApiResponse(responseCode = "400", description = "Sai request"),
    })
    @PostMapping(
            value = "/upload",
            consumes = {"multipart/form-data"}
    )
    public ResponseEntity<Podcast> uploadPodcast(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestPart("file") MultipartFile file) throws IOException {

        Podcast podcast = podcastService.uploadPodcast(title, description, file);
        return ResponseEntity.ok(podcast);
    }

    @GetMapping
    public ResponseEntity<List<Podcast>> getAll() {
        return ResponseEntity.ok(podcastService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Podcast> getById(@PathVariable Long id) {
        return podcastService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/my")
    public ResponseEntity<List<Podcast>> getMyPodcasts() {
        return ResponseEntity.ok(podcastService.getMyPodcasts());
    }

    @PutMapping(
            value = "/{id}",
            consumes = {"multipart/form-data"}
    )
    public ResponseEntity<Podcast> updatePodcast(
            @PathVariable Long id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        return ResponseEntity.ok(podcastService.updatePodcast(id, title, description, file));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePodcast(@PathVariable Long id) {
        podcastService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/restore")
    public ResponseEntity<Podcast> restorePodcast(@PathVariable Long id) {
        return ResponseEntity.ok(podcastService.restore(id));
    }

}
