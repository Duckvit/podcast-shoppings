package com.mobile.prm392.model.comment;

import lombok.Data;

@Data
public class CommentRequest {
    private Long podcastId;
    private String commentUser;
    private String content;
}

