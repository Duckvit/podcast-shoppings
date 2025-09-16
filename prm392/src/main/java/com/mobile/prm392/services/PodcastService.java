package com.mobile.prm392.services;

import com.mobile.prm392.entities.Podcast;
import com.mobile.prm392.entities.User;
import com.mobile.prm392.repositories.IPodcastRepository;
import com.mobile.prm392.repositories.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class PodcastService {
    @Autowired
    private IPodcastRepository podcastRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private AuthenticationService authenticationService;

    public Podcast uploadPodcast(String title, String description, MultipartFile file) throws IOException {
        User user = authenticationService.getCurrentUser();

        // Upload file audio lên Cloudinary
        String audioUrl = cloudinaryService.uploadAudio(file);

        // Lưu metadata xuống DB
        Podcast podcast = new Podcast();
        podcast.setTitle(title);
        podcast.setDescription(description);
        podcast.setAudioUrl(audioUrl);
        podcast.setUser(user);

        return podcastRepository.save(podcast);
    }

    public List<Podcast> getAll() {
        return podcastRepository.findAll();
    }

    public Optional<Podcast> getById(Long id) {
        return podcastRepository.findById(id);
    }

    public List<Podcast> getByCreator(Long creatorId) {
        return podcastRepository.findByUserId(creatorId);
    }

    public Podcast save(Podcast podcast) {
        return podcastRepository.save(podcast);
    }

    public void delete(Long id) {
        podcastRepository.deleteById(id);
    }
}

