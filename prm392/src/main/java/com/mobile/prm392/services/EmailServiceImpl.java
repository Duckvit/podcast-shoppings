package com.mobile.prm392.services;

import com.mobile.prm392.entities.ScheduledEmail;
import com.mobile.prm392.entities.User;
import com.mobile.prm392.exception.OurException;
import com.mobile.prm392.model.email.EmailRequest;
import com.mobile.prm392.model.response.Response;
import com.mobile.prm392.repositories.IScheduledEmailRepository;
import com.mobile.prm392.repositories.IUserRepository;
import com.mobile.prm392.util.JWTUtil;
import com.mobile.prm392.util.Utils;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Validated
public class EmailServiceImpl {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private JWTUtil jwtUtils;

    private AuthenticationService authService;

    @Autowired
    private IUserRepository usersRepository;

    @Autowired
    private IScheduledEmailRepository scheduledEmailRepository;

    private ScheduledEmail scheduledEmail;

    @Value("${spring.mail.username}")
    private String sender;

    /**
     * Phương thức gửi email dạng HTML
     */
    public Response sendHtmlMail(EmailRequest emailRequest) {
        Response response = new Response();
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            // Thiết lập thông tin người gửi, người nhận và tiêu đề email
            helper.setFrom(sender);
            helper.setTo(emailRequest.getRecipient());
            helper.setSubject(emailRequest.getSubject());

            // Nội dung HTML của email
            String htmlContent = "<div style='text-align: center; font-family: Arial, sans-serif; padding: 20px; background-color: #f9f9f9; border-radius: 5px;'>" +
                    "<h1 style='color: #FFBF00;'>Thông báo mới</h1>" +
                    "<p style='font-size: 16px; color: #333;'>" + emailRequest.getMsgBody() + "</p>" +
//                    "<p style='font-size: 14px; color: #888;'>Healing</p>" +
                    "<footer style='margin-top: 30px; font-size: 12px; color: #aaa;'>" +
                    "<p>&copy; " + 2025 + " Healing Podcast System </p>" +
                    "</footer>" +
                    "</div>";

            helper.setText(htmlContent.replace("\n", "<br>"), true); // true để gửi dưới dạng HTML

            // Gửi email
            javaMailSender.send(mimeMessage);

            response.setStatusCode(200);
            response.setMessage("Gửi email tới: " + emailRequest.getRecipient() + " thành công");

        } catch (OurException e) {
            response.setStatusCode(400);
            response.setMessage(e.getMessage());
        } catch (MessagingException e) {
            response.setStatusCode(500);
            response.setMessage("Đã xảy ra lỗi khi gửi email: " + e.getMessage());
        }
        return response;
    }

    /**
     * Phương thức gửi email OTP dạng HTML
     */
    public void sendOTP(EmailRequest emailRequest) {
        Response response = new Response();
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom(sender);
            helper.setTo(emailRequest.getRecipient());
            helper.setSubject(emailRequest.getSubject());

            String htmlContent = "<div style='text-align: center; font-family: Arial, sans-serif; padding: 20px; background-color: #f9f9f9; border-radius: 5px;'>" +
                    "<h1 style='color: #FFBF00;'>Mã OTP Của Bạn</h1>" +
                    "<p style='font-size: 16px; color: #333;'>" + emailRequest.getMsgBody() + "</p>" +
//                    "<p style='font-size: 14px; color: #888;'>Mentor Booking</p>" +
                    "<footer style='margin-top: 30px; font-size: 12px; color: #aaa;'>" +
                    "<p>&copy; " + 2025 + " Healing Podcast System </p>" +
                    "</footer>" +
                    "</div>";

            helper.setText(htmlContent, true); // true để gửi dưới dạng HTML

            // Gửi email
            javaMailSender.send(mimeMessage);

            response.setStatusCode(200);
            response.setMessage("Gửi email tới: " + emailRequest.getRecipient() + " thành công");

        } catch (OurException e) {
            response.setStatusCode(400);
            response.setMessage(e.getMessage());
        } catch (MessagingException e) {
            response.setStatusCode(500);
            response.setMessage("Đã xảy ra lỗi khi gửi email: " + e.getMessage());
        }
    }

    /**
     * Tạo lịch gửi email
     *
     * @param 'Id   của người dùng'
     * @param 'tiêu đề email'
     * @param 'nội  dung email'
     * @param 'thời gian gửi gmail'
     * @return
     */
    public ScheduledEmail scheduleEmail(Long userId, String title, String description, LocalDateTime sendTime) {
        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ScheduledEmail email = new ScheduledEmail();
        email.setUser(user);
        email.setTitle(title);
        email.setDescription(description);
        email.setSendTime(sendTime);
        email.setStatus("PENDING");
        return scheduledEmailRepository.save(email);
    }

    // Job chạy mỗi phút để gửi mail đến hạn
    @Scheduled(fixedRate = 60000)
    public void processPendingEmails() {
        Response response = new Response();
        List<ScheduledEmail> pending = scheduledEmailRepository.findByStatusAndSendTimeBefore("PENDING", LocalDateTime.now());
        for (ScheduledEmail e : pending) {
            try {
                // Tạo request gửi email
                EmailRequest emailRequest = new EmailRequest();
                emailRequest.setRecipient(e.getUser().getEmail());
                emailRequest.setSubject(e.getTitle());
                emailRequest.setMsgBody(e.getDescription());

                sendHtmlMail(emailRequest);

                e.setStatus("SENT");
            } catch (Exception ex) {
                e.setStatus("FAILED");
            }
            e.setUpdatedAt(LocalDateTime.now());
            scheduledEmailRepository.save(e);
        }
    }

    public String sendPasswordCreateUser(String email, String username) {
        String password = Utils.generateRandomString();
        // tạo mail

        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setRecipient(email);
        emailRequest.setMsgBody(username + " " + password);
        emailRequest.setSubject("PASSWORD");
        sendHtmlMail(emailRequest);

        return password;
    }
}
