package com.mobile.prm392.services;

import com.mobile.prm392.entities.OrderItem;
import com.mobile.prm392.entities.Product;
import com.mobile.prm392.entities.Review;
import com.mobile.prm392.entities.User;
import com.mobile.prm392.model.review.ReviewRequest;
import com.mobile.prm392.repositories.IOrderItemRepository;
import com.mobile.prm392.repositories.IReviewRepository;
import com.mobile.prm392.repositories.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final IUserRepository userRepository;
    private final IReviewRepository reviewRepository;
    private final IOrderItemRepository orderItemRepository;

    public Review createReview(Long userId, ReviewRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        OrderItem orderItem = orderItemRepository.findById(request.getOrderItemId())
                .orElseThrow(() -> new RuntimeException("Order item not found"));

        Review review = new Review();
        review.setUser(user);
        review.setOrderItem(orderItem);
        review.setComment(request.getComment());
        review.setStar(request.getStar());
        review.setDateCreated(LocalDateTime.now());

        return reviewRepository.save(review);
    }

    public List<Review> getAllReviewsByOrderId(Long orderId) {
        return reviewRepository.findByOrderItem_Order_Id(orderId);
    }

    public List<Review> getAllReviewsByProductId(Long productId) {
        return reviewRepository.findByOrderItem_Product_Id(productId);
    }
}
