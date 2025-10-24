package com.mobile.prm392.model.review;

import lombok.Data;

@Data
public class ReviewRequest {
    private Long productId;
    private String comment;
    private float star;
}
