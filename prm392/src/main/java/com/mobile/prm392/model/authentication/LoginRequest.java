package com.mobile.prm392.model.authentication;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String passwordHash;
}
