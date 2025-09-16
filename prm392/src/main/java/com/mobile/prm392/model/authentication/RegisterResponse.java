package com.mobile.prm392.model.authentication;

import lombok.Data;

@Data
public class RegisterResponse {
    private String username;
    private String passwordHash;
}
