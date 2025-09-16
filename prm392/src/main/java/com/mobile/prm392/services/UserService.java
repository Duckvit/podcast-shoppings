package com.mobile.prm392.services;


import com.mobile.prm392.entities.User;
import com.mobile.prm392.repositories.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    IUserRepository userRepository;




    // Lấy user theo ID
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // Lấy user theo username
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Lấy tất cả user
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Cập nhật user
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    // Xóa user
//    public void deleteUser(UUID id) {
//        userRepository.deleteById(id);
//    }
}
