package com.mobile.prm392.services;

import com.mobile.prm392.entities.Product;
import com.mobile.prm392.entities.Review;
import com.mobile.prm392.entities.User;
import com.mobile.prm392.model.review.ReviewRequest;
import com.mobile.prm392.repositories.IProductRepository;
import com.mobile.prm392.repositories.IReviewRepository;
import com.mobile.prm392.repositories.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final IReviewRepository reviewRepository;
    private final IProductRepository productRepository;
    private final IUserRepository userRepository;

    public Review createReview(Long userId, ReviewRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Product product = productRepository.findById(request.getProductId()).orElseThrow(() -> new RuntimeException("Product not found"));

        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setComment(request.getComment());
        review.setStar(request.getStar());
        review.setDateCreated(LocalDateTime.now());
        return reviewRepository.save(review);
    }

    public List<Review> getAllReviewsByProductId(Long productId) {
        return reviewRepository.findByProductId(productId);
    }

    public List<Review> getAllReviewsByUserId(Long userId) {
        return reviewRepository.findByUserId(userId);
    }
}
