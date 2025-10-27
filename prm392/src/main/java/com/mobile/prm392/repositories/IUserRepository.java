package com.mobile.prm392.repositories;


import com.mobile.prm392.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IUserRepository extends JpaRepository<User, Long> {
    // Tìm user theo username
    Optional<User> findByUsername(String username);

    // Tìm user theo email
    Optional<User> findByEmail(String email);

    Optional<User> findByOtpCodeAndEmail(String otp, String email);

    Optional<User> findByEmailAndIsActive(String email, boolean isActive);

    Optional<User> findById(Long id);

    // Kiểm tra username đã tồn tại chưa
    boolean existsByUsername(String username);

    // Kiểm tra email đã tồn tại chưa
    boolean existsByEmail(String email);

    Page<User> findAll(Pageable pageable);

    User findByRole(String role);
}
