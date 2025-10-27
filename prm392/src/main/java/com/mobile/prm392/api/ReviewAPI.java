package com.mobile.prm392.api;

import com.mobile.prm392.entities.Review;
import com.mobile.prm392.entities.User;
import com.mobile.prm392.model.review.ReviewRequest;
import com.mobile.prm392.services.ReviewService;
import com.mobile.prm392.services.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/review")
public class ReviewAPI {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private TokenService tokenService;

    @Operation(summary = "Create review for an order item (get userId from token)")
    @PostMapping("/create")
    public ResponseEntity<Review> createReview(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ReviewRequest request) {

        // Lấy userId từ token
        String token = authHeader.replace("Bearer ", "");
        User user = tokenService.getUserByToken(token);

        Review review = reviewService.createReview(user.getId(), request);
        return ResponseEntity.ok(review);
    }

    @Operation(summary = "Get all review by productId")
    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getReviewsByProductId(@PathVariable Long productId) {
        List<Review> reviews = reviewService.getAllReviewsByProductId(productId);

        if (reviews.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "message", "No reviews found for this product",
                    "data", Collections.emptyList()
            ));
        }

        return ResponseEntity.ok(Map.of(
                "message", "Successfully retrieved reviews",
                "data", reviews
        ));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getReviewsByOrderId(@PathVariable Long orderId) {
        List<Review> reviews = reviewService.getAllReviewsByOrderId(orderId);

        if (reviews.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "message", "No reviews found for this order",
                    "data", Collections.emptyList()
            ));
        }

        return ResponseEntity.ok(Map.of(
                "message", "Successfully retrieved reviews",
                "data", reviews
        ));
    }
}
