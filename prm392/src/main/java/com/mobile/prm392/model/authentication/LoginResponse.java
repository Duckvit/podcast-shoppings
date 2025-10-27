package com.mobile.prm392.model.authentication;

import lombok.Data;

@Data
public class LoginResponse {
    private String username;
    private String passwordHash;
    private String token;
}
