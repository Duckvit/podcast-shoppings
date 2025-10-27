package com.mobile.prm392.model.user;

import lombok.Data;

import java.util.List;

@Data
public class UserPageResponse {
    private List<UserResponse> content;
    private int pageNumber;
    private long totalElements;
    private int totalPages;
}
