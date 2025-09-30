package com.mobile.prm392.services;

import com.mobile.prm392.entities.User;
import com.mobile.prm392.model.user.UserPageResponse;
import com.mobile.prm392.model.user.UserResponse;
import com.mobile.prm392.repositories.IUserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    // 1. Lấy user theo ID
    public UserResponse getUserById(Long id) {
        User user = getUserEntity(id);
        return modelMapper.map(user, UserResponse.class);
    }

    // 2. Lấy user theo username
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        return modelMapper.map(user, UserResponse.class);
    }

    // 3. Lấy tất cả user
    public UserPageResponse getAllUsers(int page, int size) {
        Page user = userRepository.findAll(PageRequest.of(page - 1, size));
        UserPageResponse response = new UserPageResponse();
        response.setTotalPages(user.getTotalPages());
        response.setContent(user.getContent());
        response.setPageNumber(user.getNumber());
        response.setTotalElements(user.getTotalElements());
        return response;
    }

    // 4. Update profile
    public UserResponse updateUser(Long id, String fullName, String phoneNumber) {
        User user = getUserEntity(id);
        if (fullName != null && !fullName.isBlank()) {
            user.setFullName(fullName);
        }
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            user.setPhoneNumber(phoneNumber);
        }
        user.setUpdatedAt(java.time.LocalDateTime.now());
        User updated = userRepository.save(user);
        return modelMapper.map(updated, UserResponse.class);
    }

    // 5. Update role
    public UserResponse updateRole(Long id, String role) {
        User user = getUserEntity(id);
        user.setRole(role);
        user.setUpdatedAt(java.time.LocalDateTime.now());
        User updated = userRepository.save(user);
        return modelMapper.map(updated, UserResponse.class);
    }

    // 6. Deactivate user
    public UserResponse deactivateUser(Long id) {
        User user = getUserEntity(id);
        user.setActive(false);
        user.setUpdatedAt(java.time.LocalDateTime.now());
        User updated = userRepository.save(user);
        return modelMapper.map(updated, UserResponse.class);
    }

    // 7. Reactivate user
    public UserResponse reactivateUser(Long id) {
        User user = getUserEntity(id);
        user.setActive(true);
        user.setUpdatedAt(java.time.LocalDateTime.now());
        User updated = userRepository.save(user);
        return modelMapper.map(updated, UserResponse.class);
    }


    // Private helper
    private User getUserEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
    }
}
