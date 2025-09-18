package com.mobile.prm392.model.user;

import lombok.Data;

@Data
public class UserShortResponse {
    private Long id;
    private String username;
    private String fullName;
    private String email;
}
