package com.mobile.prm392.services;

import com.mobile.prm392.entities.Podcast;
import com.mobile.prm392.entities.User;
import com.mobile.prm392.midleware.Duplicate;
import com.mobile.prm392.repositories.IPodcastRepository;
import com.mobile.prm392.repositories.IUserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.HttpRequestMethodNotSupportedException;
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

    // 2. Lấy tất cả Podcast active
    public List<Podcast> getAll() {
        return podcastRepository.findAll()
                .stream()
                .filter(Podcast::isActive)
                .toList();
    }

    // 3. Lấy theo ID
    public Optional<Podcast> getById(Long id) {
        return podcastRepository.findById(id)
                .filter(Podcast::isActive);
    }

    // 4. Lấy theo user hiện tại
    public List<Podcast> getMyPodcasts() {
        User user = authenticationService.getCurrentUser();
        return podcastRepository.findByUserId(user.getId());
    }

    // 5. Update Podcast
    public Podcast updatePodcast(Long id, String title, String description, MultipartFile file) throws IOException {
        User user = authenticationService.getCurrentUser();
        Podcast podcast = podcastRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Podcast không tồn tại"));

        if (!podcast.getUser().getId().equals(user.getId())) {
            throw new Duplicate("Bạn không có quyền sửa podcast này");
        }

        if (title != null) podcast.setTitle(title);
        if (description != null) podcast.setDescription(description);

        // Nếu có file mới thì upload lại
        if (file != null && !file.isEmpty()) {
            String audioUrl = cloudinaryService.uploadAudio(file);
            podcast.setAudioUrl(audioUrl);
        }

        podcast.setUpdatedAt(java.time.LocalDateTime.now());
        return podcastRepository.save(podcast);
    }

    // 6. Xóa (Soft Delete)
    public void delete(Long id) {
        User user = authenticationService.getCurrentUser();
        Podcast podcast = podcastRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Podcast không tồn tại"));

        if (!podcast.getUser().getId().equals(user.getId())) {
            throw new Duplicate("Bạn không có quyền xóa podcast này");
        }

        podcast.setActive(false);
        podcastRepository.save(podcast);
    }

    // 7. Restore
    public Podcast restore(Long id) {
        User user = authenticationService.getCurrentUser();
        Podcast podcast = podcastRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Podcast không tồn tại"));

        if (!podcast.getUser().getId().equals(user.getId())) {
            throw new Duplicate("Bạn không có quyền khôi phục podcast này");
        }

        podcast.setActive(true);
        return podcastRepository.save(podcast);
    }
}

