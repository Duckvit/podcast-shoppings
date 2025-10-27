package com.mobile.prm392.services;

import com.mobile.prm392.entities.Category;
import com.mobile.prm392.entities.Podcast;
import com.mobile.prm392.entities.User;
import com.mobile.prm392.midleware.Duplicate;
import com.mobile.prm392.model.podcast.PodcastPageResponse;
import com.mobile.prm392.model.podcast.PodcastResponse;
import com.mobile.prm392.repositories.ICategoryRepository;
import com.mobile.prm392.repositories.IPodcastRepository;
import com.mobile.prm392.repositories.IUserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private ICategoryRepository categoryRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private AuthenticationService authenticationService;

    public Podcast uploadPodcast(String title, String description, MultipartFile file, MultipartFile file1, List<Long> categoryIds) throws IOException {
        User user = authenticationService.getCurrentUser();

        // Upload file audio lên Cloudinary
        String audioUrl = cloudinaryService.uploadAudio(file);

        // Upload file anh lên Cloudinary
        String imgUrl = cloudinaryService.uploadImage(file1);

        List<Category> categories = categoryRepository.findAllById(categoryIds);

        // Lưu metadata xuống DB
        Podcast podcast = new Podcast();
        podcast.setTitle(title);
        podcast.setDescription(description);
        podcast.setCategories(categories);
        podcast.setAudioUrl(audioUrl);
        podcast.setImageUrl(imgUrl);
        podcast.setUser(user);

        return podcastRepository.save(podcast);
    }

    // 2. Lấy tất cả Podcast active
    @Transactional(readOnly = true)
    public PodcastPageResponse getAll(int page, int size) {
        Page<Podcast> podcastPage = podcastRepository.findAllByIsActiveTrue(PageRequest.of(page - 1, size));

        List<PodcastResponse> content = podcastPage.getContent().stream()
                .map(podcast -> {
                    PodcastResponse dto = new PodcastResponse();
                    dto.setId(podcast.getId());
                    dto.setTitle(podcast.getTitle());
                    dto.setDescription(podcast.getDescription());
                    dto.setAudioUrl(podcast.getAudioUrl());
                    dto.setImageUrl(podcast.getImageUrl());
                    dto.setActive(podcast.isActive());
                    dto.setCreatedAt(podcast.getCreatedAt());
                    dto.setUpdatedAt(podcast.getUpdatedAt());
                    dto.setCategories(podcast.getCategories()
                            .stream()
                            .map(Category::getName) // lấy tên category thôi
                            .toList());
                    return dto;
                })
                .toList();

        PodcastPageResponse response = new PodcastPageResponse();
        response.setContent(content);
        response.setTotalPages(podcastPage.getTotalPages());
        response.setPageNumber(podcastPage.getNumber());
        response.setTotalElements(podcastPage.getTotalElements());
        return response;
    }


    // 3. Lấy theo ID
    @Transactional(readOnly = true)
    public PodcastResponse getById(Long id) {
        Podcast podcast = podcastRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Podcast không tồn tại"));

        PodcastResponse dto = new PodcastResponse();
        dto.setId(podcast.getId());
        dto.setTitle(podcast.getTitle());
        dto.setDescription(podcast.getDescription());
        dto.setAudioUrl(podcast.getAudioUrl());
        dto.setImageUrl(podcast.getImageUrl());

        dto.setCategories(podcast.getCategories()
                .stream()
                .map(Category::getName) // chỉ lấy tên
                .toList());

        return dto;
    }


    // 4. Lấy theo user hiện tại
    @Transactional(readOnly = true)
    public PodcastPageResponse getMyPodcasts(int page, int size) {
        User user = authenticationService.getCurrentUser();
        Page<Podcast> podcastPage = podcastRepository.findByUserIdAndIsActiveTrue(
                user.getId(), PageRequest.of(page - 1, size)
        );

        List<PodcastResponse> content = podcastPage.getContent().stream()
                .map(p -> {
                    PodcastResponse dto = new PodcastResponse();
                    dto.setId(p.getId());
                    dto.setTitle(p.getTitle());
                    dto.setDescription(p.getDescription());
                    dto.setAudioUrl(p.getAudioUrl());
                    dto.setImageUrl(p.getImageUrl());
                    dto.setActive(p.isActive());
                    dto.setCreatedAt(p.getCreatedAt());
                    dto.setUpdatedAt(p.getUpdatedAt());
                    dto.setCategories(p.getCategories()
                            .stream()
                            .map(Category::getName)
                            .toList());
                    return dto;
                })
                .toList();

        PodcastPageResponse response = new PodcastPageResponse();
        response.setContent(content);
        response.setTotalPages(podcastPage.getTotalPages());
        response.setPageNumber(podcastPage.getNumber());
        response.setTotalElements(podcastPage.getTotalElements());
        return response;
    }


    // 5. Update Podcast
    public Podcast updatePodcast(Long id, String title, String description, MultipartFile file, MultipartFile file1, List<Long> categoryIds) throws IOException {
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

        if (categoryIds != null && !categoryIds.isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(categoryIds);
            podcast.setCategories(categories);
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
    @Transactional(readOnly = true)
    public List<PodcastResponse> getPodcastsByCategoryName(String categoryName) {
        List<Podcast> podcasts = podcastRepository.findByCategoryName(categoryName);

        return podcasts.stream().map(this::mapToResponse).toList();
    }

    private PodcastResponse mapToResponse(Podcast podcast) {
        PodcastResponse response = new PodcastResponse();
        response.setId(podcast.getId());
        response.setTitle(podcast.getTitle());
        response.setDescription(podcast.getDescription());
        response.setAudioUrl(podcast.getAudioUrl());
        response.setImageUrl(podcast.getImageUrl());
        response.setActive(podcast.isActive());
        response.setCreatedAt(podcast.getCreatedAt());
        response.setUpdatedAt(podcast.getUpdatedAt());

        if (podcast.getCategories() != null) {
            response.setCategories(
                    podcast.getCategories()
                            .stream()
                            .map(category -> category.getName())
                            .toList()
            );
        }

        return response;
    }


}

