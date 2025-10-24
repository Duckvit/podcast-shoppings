package com.mobile.prm392.api;

import com.mobile.prm392.entities.Review;
import com.mobile.prm392.entities.User;
import com.mobile.prm392.model.review.ReviewRequest;
import com.mobile.prm392.services.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/review")
public class ReviewAPI {

    @Autowired
    private ReviewService reviewService;

    @PostMapping("/create")
    public ResponseEntity<Review> createReview(
            @RequestBody ReviewRequest request,
            @AuthenticationPrincipal User user) {

        Review review = reviewService.createReview(user.getId(), request);
        return ResponseEntity.ok(review);
    }

    @Operation(summary = "Get all review by productId")
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Review>> getReviewsByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getAllReviewsByProductId(productId));
    }

    @Operation(summary = "Get all review by userId")
    @GetMapping("user/{userId}")
    public ResponseEntity<List<Review>> getReviewsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(reviewService.getAllReviewsByUserId(userId));
    }
}
