package com.mobile.prm392.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mobile.prm392.model.user.UserResponse;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@Entity
@Getter
@Setter
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(length = 100)
    private String fullName;

    @Column(length = 20)
    private String phoneNumber;

    @Column(length = 20)
    private String role = "user"; // customer/admin/artist

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Quan hệ
    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<Order> orders;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Cart> carts;

    // User có thể tạo nhiều podcast
    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<Podcast> podcasts;

    // User có thể favorite nhiều podcast
    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<FavoritePodcast> favoritePodcasts;

    // User có thể rating nhiều podcast
    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<PodcastRating> podcastRatings;

    @OneToMany(mappedBy = "from")
    @JsonIgnore
    private List<Transaction> transactionsFrom; // User có thể tham gia nhiều Transaction

    @OneToMany(mappedBy = "to")
    @JsonIgnore
    private List<Transaction> transactionsTo; // User có thể tham gia nhiều Transaction



    private boolean isActive = true;

    private String otpCode;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.role.toUpperCase()));
        return authorities;

    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
