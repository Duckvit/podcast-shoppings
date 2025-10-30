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
import java.time.ZoneId;
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
            String htmlContent =
                    "<!DOCTYPE html>" +
                            "<html lang='vi'>" +
                            "<head>" +
                            "    <meta charset='UTF-8'>" +
                            "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                            "    <title>Thông báo từ Healing Podcast</title>" +
                            "</head>" +
                            "<body style='margin: 0; padding: 20px; background-color: #f5ebe0; font-family: Segoe UI, Tahoma, Helvetica Neue, Arial, sans-serif;'>" +

                            "    <div style='max-width: 650px; margin: 0 auto; background-color: #fffcf7; box-shadow: 0 4px 12px rgba(139, 90, 43, 0.15); border-radius: 8px; overflow: hidden; border: 1px solid #e8d5c4;'>" +

                            "        <div style='background: linear-gradient(135deg, #d4a574 0%, #c9956e 100%); padding: 30px 40px; position: relative; border-bottom: 3px solid #b8865f;'>" +
                            "            <div style='position: absolute; top: 10px; right: 20px; width: 60px; height: 60px; background-color: rgba(255,255,255,0.25); border-radius: 50%; border: 3px dashed #fff;'></div>" +
                            "            <h1 style='margin: 0; color: #ffffff; font-size: 28px; font-weight: 600; text-shadow: 2px 2px 4px rgba(0,0,0,0.15); letter-spacing: 0.5px;'>🎧 Healing Podcast System</h1>" +
                            "            <p style='margin: 8px 0 0 0; color: #fff; font-size: 14px; opacity: 0.95; font-weight: 400;'>Thông báo quan trọng</p>" +
                            "        </div>" +

                            "        <div style='padding: 40px 45px; background-color: #fffcf7;'>" +
                            "            <p style='margin: 0 0 20px 0; font-size: 16px; color: #5a4a3a; line-height: 1.7; font-weight: 500;'>Kính gửi Quý khách,</p>" +

                            "            <div style='margin: 25px 0; padding: 22px; background-color: #fff9f0; border-left: 4px solid #c9956e; border-radius: 4px; box-shadow: 0 2px 4px rgba(139, 90, 43, 0.08);'>" +
                            "                <p style='margin: 0; font-size: 16px; color: #5a4a3a; line-height: 1.8; font-weight: 400;'>" +
                            emailRequest.getMsgBody() +
                            "                </p>" +
                            "            </div>" +

                            "            <p style='margin: 25px 0 10px 0; font-size: 16px; color: #5a4a3a; line-height: 1.7; font-weight: 400;'>Trân trọng,</p>" +
                            "            <p style='margin: 0; font-size: 16px; color: #b8865f; font-weight: 600;'>Đội ngũ Healing Podcast System</p>" +
                            "        </div>" +

                            "        <div style='background-color: #4a3f35; padding: 25px 40px; text-align: center; border-top: 3px solid #c9956e;'>" +
                            "            <p style='margin: 0 0 8px 0; font-size: 14px; color: #f5ebe0; font-weight: 600;'>Healing Podcast System</p>" +
                            "            <p style='margin: 0; font-size: 11px; color: #a89888;'>&copy; 2025 Healing Podcast System. All rights reserved.</p>" +
                            "        </div>" +

                            "    </div>" +

                            "</body>" +
                            "</html>";

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

            String htmlContent =
                    "<!DOCTYPE html>" +
                            "<html lang='vi'>" +
                            "<head>" +
                            "    <meta charset='UTF-8'>" +
                            "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                            "    <title>Thông báo từ Healing Podcast</title>" +
                            "</head>" +
                            "<body style='margin: 0; padding: 20px; background-color: #f5ebe0; font-family: Segoe UI, Tahoma, Helvetica Neue, Arial, sans-serif;'>" +

                            "    <div style='max-width: 650px; margin: 0 auto; background-color: #fffcf7; box-shadow: 0 4px 12px rgba(139, 90, 43, 0.15); border-radius: 8px; overflow: hidden; border: 1px solid #e8d5c4;'>" +

                            "        <div style='background: linear-gradient(135deg, #d4a574 0%, #c9956e 100%); padding: 30px 40px; position: relative; border-bottom: 3px solid #b8865f;'>" +
                            "            <div style='position: absolute; top: 10px; right: 20px; width: 60px; height: 60px; background-color: rgba(255,255,255,0.25); border-radius: 50%; border: 3px dashed #fff;'></div>" +
                            "            <h1 style='margin: 0; color: #ffffff; font-size: 28px; font-weight: 600; text-shadow: 2px 2px 4px rgba(0,0,0,0.15); letter-spacing: 0.5px;'>🎧 Healing Podcast System</h1>" +
                            "            <p style='margin: 8px 0 0 0; color: #fff; font-size: 14px; opacity: 0.95; font-weight: 400;'>Thông báo quan trọng</p>" +
                            "        </div>" +

                            "        <div style='padding: 40px 45px; background-color: #fffcf7;'>" +
                            "            <p style='margin: 0 0 20px 0; font-size: 16px; color: #5a4a3a; line-height: 1.7; font-weight: 500;'>Kính gửi Quý khách,</p>" +

                            "            <div style='margin: 25px 0; padding: 22px; background-color: #fff9f0; border-left: 4px solid #c9956e; border-radius: 4px; box-shadow: 0 2px 4px rgba(139, 90, 43, 0.08);'>" +
                            "                <p style='margin: 0; font-size: 16px; color: #5a4a3a; line-height: 1.8; font-weight: 400;'>" +
                            emailRequest.getMsgBody() +
                            "                </p>" +
                            "            </div>" +

                            "            <p style='margin: 25px 0 10px 0; font-size: 16px; color: #5a4a3a; line-height: 1.7; font-weight: 400;'>Trân trọng,</p>" +
                            "            <p style='margin: 0; font-size: 16px; color: #b8865f; font-weight: 600;'>Đội ngũ Healing Podcast System</p>" +
                            "        </div>" +

                            "        <div style='background-color: #4a3f35; padding: 25px 40px; text-align: center; border-top: 3px solid #c9956e;'>" +
                            "            <p style='margin: 0 0 8px 0; font-size: 14px; color: #f5ebe0; font-weight: 600;'>Healing Podcast System</p>" +
                            "            <p style='margin: 0; font-size: 11px; color: #a89888;'>&copy; 2025 Healing Podcast System. All rights reserved.</p>" +
                            "        </div>" +

                            "    </div>" +

                            "</body>" +
                            "</html>";

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
        ZoneId vietnamZone = ZoneId.of("Asia/Ho_Chi_Minh");
        LocalDateTime nowInVietnam = LocalDateTime.now(vietnamZone);

        List<ScheduledEmail> pending = scheduledEmailRepository
                .findByStatusAndSendTimeBefore("PENDING", nowInVietnam);
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