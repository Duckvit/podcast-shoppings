package com.mobile.prm392.services;


import com.mobile.prm392.entities.User;
import com.mobile.prm392.exception.OurException;
import com.mobile.prm392.midleware.Duplicate;
import com.mobile.prm392.model.authentication.LoginRequest;
import com.mobile.prm392.model.authentication.LoginResponse;
import com.mobile.prm392.model.authentication.RegisterRequest;
import com.mobile.prm392.model.authentication.RegisterResponse;
import com.mobile.prm392.model.email.EmailRequest;
import com.mobile.prm392.model.response.Response;
import com.mobile.prm392.model.user.UserResponse;
import com.mobile.prm392.repositories.IUserRepository;
import com.mobile.prm392.util.Utils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
    @Lazy
    IUserRepository userRepository;

    @Autowired
    @Lazy
    PasswordEncoder passwordEncoder;

    @Autowired
    @Lazy
    ModelMapper modelMapper;

    @Autowired
    @Lazy
    TokenService tokenService;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserService userService; // or your repository

    @Autowired
    private EmailServiceImpl emailService;

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
            account.setFullName(registerRequest.getFullName());
            account.setPhoneNumber(registerRequest.getPhoneNumber());
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

    /**
     *  Phương thức đổi password
     */
    public Response changePassword(Response changeResponse) {
        Response response = new Response();
        try {
            // Tìm người dùng dựa trên username
            User user = userRepository.findByOtpCodeAndEmail(changeResponse.getOtpCode(), changeResponse.getEmail())
                    .orElseThrow(() -> new OurException("OTP not correct"));

            // Mã hóa và lưu mật khẩu mới
            user.setPassword(passwordEncoder.encode(changeResponse.getNewPassword()));
            user.setOtpCode(null);
            userRepository.save(user);

            // Trả về phản hồi thành công
            response.setStatusCode(200);
            response.setMessage("Password changed successfully");

        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred during password change: " + e.getMessage());
        }

        return response;
    }

    /**
     *  Phương thức đổi password khi đăng nhập
     */
    public Response changePasswordInUser(Response changeResponse) {
        Response response = new Response();
        try {
            // Tìm người dùng dựa trên username
            User user = userRepository.findByEmailAndIsActive(changeResponse.getEmail(), true)
                    .orElseThrow(() -> new OurException("Email not correct"));

            // Kiểm tra xem mật khẩu hiện tại có đúng không
            if (!passwordEncoder.matches(changeResponse.getPassword(), user.getPassword())) {
                throw new OurException("Current password is incorrect");
            }
            // Kiểm tra mật khẩu mới không được trùng với mật khẩu hiện tại
            if (passwordEncoder.matches(changeResponse.getNewPassword(), user.getPassword())) {
                throw new OurException("New password cannot be the same as the current password");
            }

            // Mã hóa và lưu mật khẩu mới
            user.setPassword(passwordEncoder.encode(changeResponse.getNewPassword()));
            user.setOtpCode(null);
            userRepository.save(user);

            // Trả về phản hồi thành công
            response.setStatusCode(200);
            response.setMessage("Password changed successfully");

        } catch (OurException e) {
            response.setStatusCode(400);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred during password change: " + e.getMessage());
        }

        return response;
    }

    /**
     *  Phương thức nhập gmail để đổi password
     */
    public Response findByGmailChangePassword(Response responseEmail){
        Response response = new Response();
        try {
            User users = userRepository.findByEmailAndIsActive(
                    responseEmail.getEmail(),
                    true
            ).orElseThrow(() -> new OurException("Email is not existed " + responseEmail.getEmail()));

            String otp = Utils.generateOTP();
            users.setOtpCode(otp);
            userRepository.save(users);

            // tạo mail
            EmailRequest emailRequest = new EmailRequest();
            emailRequest.setRecipient(users.getEmail());
            emailRequest.setMsgBody(otp);
            emailRequest.setSubject("OTP");
            emailService.sendOTP(emailRequest);

            response.setStatusCode(200);
            response.setMessage("OTP generated successfully");
        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred during password change: " + e.getMessage());
        }
        return response;
    }

    public Response findByOTPChangePassword(Response responseEmail){
        Response response = new Response();
        try {
            User users = userRepository.findByOtpCodeAndEmail(
                    responseEmail.getOtpCode(),
                    responseEmail.getEmail()
            ).orElseThrow(() -> new OurException("OTP not correct"));

            response.setStatusCode(200);
            response.setMessage("Successfully");
        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred during password change: " + e.getMessage());
        }
        return response;
    }

}
