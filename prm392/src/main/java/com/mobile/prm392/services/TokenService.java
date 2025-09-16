package com.mobile.prm392.services;

import com.mobile.prm392.entities.User;
import com.mobile.prm392.repositories.IUserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Service
public class TokenService {
    @Autowired
    UserService userService;

//    @Autowired
//    UserInfoService userInfoService;

    @Autowired
    IUserRepository userRepository;

    public final String SECRET_KEY = "4bb6d1dfbafb64a681139d1586b6f1160d18159afd57c8c79136d7490630407P";

    private SecretKey getSigninKey(){
        byte[] ketBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(ketBytes);
    }

    //tạo ra token cho customer
    public String generateTokenUser(User user){
        String token = Jwts.builder()
                .subject(user.getId()+ "")
                .issuedAt(new Date(System.currentTimeMillis())) // 10:30
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(getSigninKey())
                .compact();
        return token;
    }


    //verify token cho user
    public User getUserByToken(String token){
        Claims claims = Jwts.parser()
                .verifyWith(getSigninKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String userId = claims.getSubject();

        return userRepository.findById(Long.parseLong(userId)).orElse(null);
    }


}
