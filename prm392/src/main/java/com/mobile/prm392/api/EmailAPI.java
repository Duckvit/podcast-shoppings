package com.mobile.prm392.api;

import com.mobile.prm392.model.email.EmailRequest;
import com.mobile.prm392.model.response.Response;
import com.mobile.prm392.services.EmailServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
