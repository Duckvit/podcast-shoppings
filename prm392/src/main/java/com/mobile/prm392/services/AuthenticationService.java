package com.mobile.prm392.services;


import com.mobile.prm392.entities.User;
import com.mobile.prm392.midleware.Duplicate;
import com.mobile.prm392.model.authentication.LoginRequest;
import com.mobile.prm392.model.authentication.LoginResponse;
import com.mobile.prm392.model.authentication.RegisterRequest;
import com.mobile.prm392.model.authentication.RegisterResponse;
import com.mobile.prm392.repositories.IUserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService implements UserDetailsService {

    @Autowired
    IUserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    TokenService tokenService;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserService userService; // or your repository

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            String userName = userDetails.getUsername();
            return userRepository.findByUsername(userName).orElse(null);
        } else {
            return null;
        }
    }


    // Đăng ký user mới
    public RegisterResponse registerUser(RegisterRequest registerRequest) {

        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new Duplicate("Username already exists!");
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new Duplicate ("Email already exists!");
        }

        try {
            User account = new User();
            account.setUsername(registerRequest.getUsername());
            account.setEmail(registerRequest.getEmail());
            account.setPassword(passwordEncoder.encode(registerRequest.getPasswordHash())); // ✅ encode đúng

            User newAccount = userRepository.save(account);

            return modelMapper.map(newAccount, RegisterResponse.class);
        } catch (Exception e) {
            e.printStackTrace(); // log cho dễ debug
            throw new RuntimeException("Register failed: " + e.getMessage());
        }
    }


    //LOGIN CUSTOMER
    public LoginResponse loginForCustomer(LoginRequest loginRequest){
        try{
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPasswordHash()
            ));

            //=> tài khoản có tồn tại
            User account = (User) authentication.getPrincipal();
//            if(account.isDeleted()){
//                throw new Duplicate("Your account is blocked!");
//            } else {
//                AccountForCustomerResponse accountResponseForCustomer = modelMapper.map(account, AccountForCustomerResponse.class);
//                accountResponseForCustomer.setToken(tokenService.generateTokenCustomer(account));
//                return accountResponseForCustomer;
//            }
            LoginResponse user = modelMapper.map(account, LoginResponse.class);
            user.setToken(tokenService.generateTokenUser(account));
            return user;
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Username or password invalid!");
        }

    }



}
