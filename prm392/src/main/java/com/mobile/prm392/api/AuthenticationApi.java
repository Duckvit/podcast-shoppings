package com.mobile.prm392.api;


import com.mobile.prm392.model.authentication.LoginRequest;
import com.mobile.prm392.model.authentication.LoginResponse;
import com.mobile.prm392.model.authentication.RegisterRequest;
import com.mobile.prm392.model.authentication.RegisterResponse;
import com.mobile.prm392.model.response.Response;
import com.mobile.prm392.services.AuthenticationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*") // cho phep tat ca truy cap, ket noi FE va BE vs nhau
@SecurityRequirement(name = "api") // tao controller moi nho copy qua
public class AuthenticationApi {

    @Autowired
    AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity LoginForCustomer(@Valid @RequestBody LoginRequest loginRequest){
        LoginResponse loginResponse = authenticationService.loginForCustomer(loginRequest);
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/register")
    public ResponseEntity register(@Valid @RequestBody RegisterRequest account){
        //nho thang nay tao dum acc
        RegisterResponse newAccount = authenticationService.registerUser(account);
        return ResponseEntity.ok(newAccount);
    }

    @PostMapping("/email-existed")
    public ResponseEntity<Response> isEmailExisted(@RequestBody Response responseEmail) {
        Response response = authenticationService.findByGmailChangePassword(responseEmail);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/otp-existed")
    public ResponseEntity<Response> isCorrectOTP(@RequestBody Response responseEmail) {
        Response response = authenticationService.findByOTPChangePassword(responseEmail);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
