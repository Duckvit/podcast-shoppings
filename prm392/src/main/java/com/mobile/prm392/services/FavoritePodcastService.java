package com.mobile.prm392.services;

import com.mobile.prm392.entities.FavoritePodcast;
import com.mobile.prm392.entities.User;
import com.mobile.prm392.model.favoritePodcast.FavoritePodcastResponse;
import com.mobile.prm392.repositories.IFavoritePodcastRepository;
import com.mobile.prm392.repositories.IPodcastRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public List<FavoritePodcastResponse> getFavoritesOfCurrentUser() {
        Long userId = authenticationService.getCurrentUser().getId();
        return favoritePodcastRepository.findByUserIdAndIsActiveTrue(userId)
                .stream()
                .map(fav -> {
                    FavoritePodcastResponse res = new FavoritePodcastResponse();
                    res.setId(fav.getId());
                    res.setPodcastId(fav.getPodcast().getId());
                    res.setPodcastTitle(fav.getPodcast().getTitle());
                    return res;
                })
                .toList();
    }

    // 2. Thêm hoặc đánh dấu favorite
    public FavoritePodcast toggleFavorite(Long podcastId) {
        var user = authenticationService.getCurrentUser();
        var podcast = podcastRepository.findById(podcastId)
                .orElseThrow(() -> new EntityNotFoundException("Podcast không tồn tại"));

        FavoritePodcast favorite = favoritePodcastRepository
                .findByUserIdAndPodcastId(user.getId(), podcastId)
                .orElse(new FavoritePodcast());

        favorite.setUser(user);
        favorite.setPodcast(podcast);
        favorite.setActive(true); // luôn đánh dấu active khi toggle
        return favoritePodcastRepository.save(favorite);
    }

    // 3. Xóa mềm favorite
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

