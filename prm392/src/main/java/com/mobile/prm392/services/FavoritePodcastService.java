package com.mobile.prm392.services;

import com.mobile.prm392.entities.FavoritePodcast;
import com.mobile.prm392.repositories.IFavoritePodcastRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoritePodcastService {
    @Autowired
    private IFavoritePodcastRepository favoritePodcastRepository;

    public List<FavoritePodcast> getByUser(Long userId) {
        return favoritePodcastRepository.findByUserId(userId);
    }

    public FavoritePodcast save(FavoritePodcast favorite) {
        return favoritePodcastRepository.save(favorite);
    }

    public void delete(Long id) {
        favoritePodcastRepository.deleteById(id);
    }
}

