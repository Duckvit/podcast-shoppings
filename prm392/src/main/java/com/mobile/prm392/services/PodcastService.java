package com.mobile.prm392.services;

import com.mobile.prm392.entities.Podcast;
import com.mobile.prm392.entities.User;
import com.mobile.prm392.midleware.Duplicate;
import com.mobile.prm392.model.podcast.PodcastPageResponse;
import com.mobile.prm392.repositories.IPodcastRepository;
import com.mobile.prm392.repositories.IUserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    public Podcast uploadPodcast(String title, String description, MultipartFile file, MultipartFile file1) throws IOException {
        User user = authenticationService.getCurrentUser();

        // Upload file audio lên Cloudinary
        String audioUrl = cloudinaryService.uploadAudio(file);

        // Upload file anh lên Cloudinary
        String imgUrl = cloudinaryService.uploadImage(file1);

        // Lưu metadata xuống DB
        Podcast podcast = new Podcast();
        podcast.setTitle(title);
        podcast.setDescription(description);
        podcast.setAudioUrl(audioUrl);
        podcast.setImageUrl(imgUrl);
        podcast.setUser(user);

        return podcastRepository.save(podcast);
    }

    // 2. Lấy tất cả Podcast active
    public PodcastPageResponse getAll(int page, int size) {
        Page podcast = podcastRepository.findAllByIsActiveTrue(PageRequest.of(page - 1, size));

        PodcastPageResponse response = new PodcastPageResponse();
        response.setContent(podcast.getContent());
        response.setTotalPages(podcast.getTotalPages());
        response.setPageNumber(podcast.getNumber());
        response.setTotalElements(podcast.getTotalElements());
        return response;
    }

    // 3. Lấy theo ID
    public Podcast getById(Long id) {
        return podcastRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Podcast không tồn tại"));
    }

    // 4. Lấy theo user hiện tại
    public PodcastPageResponse getMyPodcasts(int page, int size) {
        User user = authenticationService.getCurrentUser();
        Page podcast = podcastRepository.findByUserIdAndIsActiveTrue(user.getId(), PageRequest.of(page - 1, size));

        PodcastPageResponse response = new PodcastPageResponse();
        response.setContent(podcast.getContent());
        response.setTotalPages(podcast.getTotalPages());
        response.setPageNumber(podcast.getNumber());
        response.setTotalElements(podcast.getTotalElements());
        return response;
    }

    // 5. Update Podcast
    public Podcast updatePodcast(Long id, String title, String description, MultipartFile file, MultipartFile file1) throws IOException {
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

        // Nếu có file mới thì upload lại
        if (file1 != null && !file1.isEmpty()) {
            String imgUrl = cloudinaryService.uploadImage(file1);
            podcast.setImageUrl(imgUrl);
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

    // 8. Tìm podcast qua tên category
    public List<Podcast> getPodcastsByCategoryName(String categoryName) {
        return podcastRepository.findByCategoryName(categoryName);
    }
}

