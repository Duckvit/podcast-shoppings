package com.mobile.prm392.api;

import com.mobile.prm392.entities.User;
import com.mobile.prm392.model.response.Response;
import com.mobile.prm392.model.user.UserResponse;
import com.mobile.prm392.model.user.UserRoleRequest;
import com.mobile.prm392.model.user.UserUpdateRequest;
import com.mobile.prm392.services.AuthenticationService;
import com.mobile.prm392.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*") // cho phep tat ca truy cap, ket noi FE va BE vs nhau
@SecurityRequirement(name = "api") // tao controller moi nho copy qua
public class UserApi {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationService authenticationService;

    // Lấy tất cả user
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity getAllUsers(@RequestParam int page, @RequestParam int size) {
        return ResponseEntity.ok(userService.getAllUsers(page, size));
    }

    // Lấy user theo ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        try {
            UserResponse user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Lấy user theo username
    @GetMapping("/username/{username}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        try {
            UserResponse user = userService.getUserByUsername(username);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Cập nhật profile user
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
                                                   @RequestBody UserUpdateRequest request) {
        try {
            UserResponse updated = userService.updateUser(id, request.getFullName(), request.getPhoneNumber());
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Cập nhật role của user
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateRole(@PathVariable Long id,
                                                   @RequestBody UserRoleRequest request) {
        try {
            UserResponse updated = userService.updateRole(id, request.getRole());
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }


    // Deactivate user
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> deactivateUser(@PathVariable Long id) {
        try {
            UserResponse updated = userService.deactivateUser(id);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Reactivate user
    @PutMapping("/reactivate/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> reactivateUser(@PathVariable Long id) {
        try {
            UserResponse updated = userService.reactivateUser(id);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Change password", description = "Để thay đổi password khi quên password")
    @PostMapping("/change-password")
    public ResponseEntity<Response> changePasswordInUser(@RequestBody Response changeRequest){
        Response response = authenticationService.changePasswordInUser(changeRequest);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
