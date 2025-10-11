package com.mobile.prm392.services;

import com.mobile.prm392.entities.FavoritePodcast;
import com.mobile.prm392.entities.User;
import com.mobile.prm392.model.favoritePodcast.FavoritePodcastPageResonse;
import com.mobile.prm392.model.favoritePodcast.FavoritePodcastResponse;
import com.mobile.prm392.repositories.IFavoritePodcastRepository;
import com.mobile.prm392.repositories.IPodcastRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FavoritePodcastService {
    @Autowired
    private IFavoritePodcastRepository favoritePodcastRepository;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private IPodcastRepository podcastRepository;

    // 1. Lấy danh sách favorite của user
    @Transactional(readOnly = true)
    public FavoritePodcastPageResonse getFavoritesOfCurrentUser(int page, int size) {
        Long userId = authenticationService.getCurrentUser().getId();
        Page<FavoritePodcast> favoritePage = favoritePodcastRepository
                .findByUserIdAndIsActiveTrue(userId, PageRequest.of(page - 1, size));

        // Map sang DTO để tránh lỗi Lazy
        List<FavoritePodcastResponse> dtoList = favoritePage.getContent().stream().map(fav -> {
            FavoritePodcastResponse dto = new FavoritePodcastResponse();
            dto.setId(fav.getId());
            dto.setActive(fav.isActive());
            if (fav.getPodcast() != null) {
                dto.setPodcastId(fav.getPodcast().getId());
                dto.setPodcastTitle(fav.getPodcast().getTitle());
            }
            if (fav.getUser() != null) {
                dto.setUserId(fav.getUser().getId());
                dto.setUsername(fav.getUser().getUsername());
            }
            return dto;
        }).toList();

        FavoritePodcastPageResonse response = new FavoritePodcastPageResonse();
        response.setContent(dtoList);
        response.setPageNumber(favoritePage.getNumber());
        response.setTotalElements(favoritePage.getTotalElements());
        response.setTotalPages(favoritePage.getTotalPages());
        return response;
    }


    // 2. Thêm hoặc đánh dấu favorite
    @Transactional
    public FavoritePodcastResponse toggleFavorite(Long podcastId) {
        var user = authenticationService.getCurrentUser();
        var podcast = podcastRepository.findById(podcastId)
                .orElseThrow(() -> new EntityNotFoundException("Podcast không tồn tại"));

        FavoritePodcast favorite = favoritePodcastRepository
                .findByUserIdAndPodcastId(user.getId(), podcastId)
                .orElse(new FavoritePodcast());

        favorite.setUser(user);
        favorite.setPodcast(podcast);
        favorite.setActive(true);

        FavoritePodcast saved = favoritePodcastRepository.save(favorite);

        // Convert sang DTO (map dữ liệu cần thiết)
        FavoritePodcastResponse dto = new FavoritePodcastResponse();
        dto.setId(saved.getId());
        dto.setActive(saved.isActive());
        dto.setPodcastId(podcast.getId());
        dto.setPodcastTitle(podcast.getTitle());
        dto.setUserId(user.getId());
        dto.setUsername(user.getUsername());
        return dto;
    }


    // 3. Xóa mềm favorite
    @Transactional
    public boolean removeFavorite(Long podcastId) {
        User user = authenticationService.getCurrentUser();

        FavoritePodcast favorite = favoritePodcastRepository
                .findByUserIdAndPodcastId(user.getId(), podcastId)
                .orElseThrow(() -> new EntityNotFoundException("Favorite không tồn tại"));

        favorite.setActive(false);
        favoritePodcastRepository.save(favorite);
        return true;
    }

}

