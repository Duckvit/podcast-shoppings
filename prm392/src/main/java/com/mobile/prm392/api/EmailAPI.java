package com.mobile.prm392.api;

import com.mobile.prm392.entities.User;
import com.mobile.prm392.model.email.EmailRequest;
import com.mobile.prm392.model.email.ScheduleEmailRequest;
import com.mobile.prm392.model.payos.ApiResponse;
import com.mobile.prm392.model.response.Response;
import com.mobile.prm392.services.EmailServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/emails")
public class EmailAPI {

    @Autowired
    private EmailServiceImpl emailService;

    @Operation(summary = "Gửi mail tới người nhận (recipient)")
    @PostMapping("/send")
    public ResponseEntity<Response> sendEmail(@RequestBody EmailRequest emailRequest) {
        Response response = emailService.sendHtmlMail(emailRequest);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatusCode()));
    }

    @PostMapping("/schedule")
    public ApiResponse<String> scheduleEmail(@RequestBody ScheduleEmailRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal(); // đã set User vào đây trong filter
        Long userId = user.getId();
        emailService.scheduleEmail(userId, request.getTitle(), request.getDescription(), request.getSendTime());
        return ApiResponse.success("Email scheduled successfully");
    }

}
