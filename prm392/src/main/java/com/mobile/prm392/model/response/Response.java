package com.mobile.prm392.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {
    private int statusCode;
    private String message;
    private String email;
    private String otpCode;
    private String password;
    private String newPassword;
    private String title;
    private String description;
    private LocalDateTime sendTime;
}
