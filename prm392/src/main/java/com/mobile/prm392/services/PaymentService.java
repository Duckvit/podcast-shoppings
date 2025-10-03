package com.mobile.prm392.services;

import com.mobile.prm392.entities.Order;
import com.mobile.prm392.entities.Payment;
import com.mobile.prm392.model.order.OrderRequest;
import com.mobile.prm392.model.payment.PaymentPageResponse;
import com.mobile.prm392.model.payment.PaymentResponse;
import com.mobile.prm392.repositories.IOrderRepository;
import com.mobile.prm392.repositories.IPaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class PaymentService {

    @Autowired
    private IPaymentRepository paymentRepository;

    @Autowired
    private IOrderRepository orderRepository;

    @Autowired
    private ModelMapper modelMapper;

    // Lấy tất cả Payment
    public PaymentPageResponse getAllPayments(int page, int size) {
        Page<Payment> paymentPage = paymentRepository.findAll(PageRequest.of(page - 1, size));

        // Dùng ModelMapper để map sang DTO
        List<PaymentResponse> paymentResponses = paymentPage.getContent().stream()
                .map(payment -> modelMapper.map(payment, PaymentResponse.class))
                .toList();

        PaymentPageResponse response = new PaymentPageResponse();
        response.setContent(paymentResponses);
        response.setPageNumber(paymentPage.getNumber());
        response.setTotalElements(paymentPage.getTotalElements());
        response.setTotalPages(paymentPage.getTotalPages());

        return response;
    }


    // Lấy Payment theo id
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found with id: " + id));

        return modelMapper.map(payment, PaymentResponse.class);
    }


    // Cập nhật Payment
    public Payment updatePayment(Long id, Payment payment) {
        Payment existingPayment = paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found with id: " + id));

        existingPayment.setOrder(payment.getOrder());
        existingPayment.setMethod(payment.getMethod());
        existingPayment.setStatus(payment.getStatus());
        existingPayment.setAmount(payment.getAmount());
        existingPayment.setTransactionId(payment.getTransactionId());
        existingPayment.setCheckoutUrl(payment.getCheckoutUrl());
        existingPayment.setUpdatedAt(payment.getUpdatedAt());

        return paymentRepository.save(existingPayment);
    }

    // Xóa Payment
    public void deletePayment(Long id) {
        if (!paymentRepository.existsById(id)) {
            throw new EntityNotFoundException("Payment not found with id: " + id);
        }
        paymentRepository.deleteById(id);
    }

    // thanh toan order
    public String createUrl(Long orderId) throws  Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime createDate = LocalDateTime.now();
        String formattedCreateDate = createDate.format(formatter);

        // code cua minh
//        Appointment appointment = appointmentService.completeAppointmentById(orderRequest);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));
        double money = order.getTotalAmount() * 100;
        String amount = String.valueOf((int) money);

        String tmnCode = "OAXJYXKZ";
        String secretKey = "4MKK3NOKE1SOCD9YNKN9BOKKV3BQJBFU";
        String vnpUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
        String returnUrl = "http://localhost:5173/staff_page/paymentSuccessful?orderID=" + order.getId(); // trang thanh toan thanh cong cua fe
        String currCode = "VND";

        Map<String, String> vnpParams = new TreeMap<>();

        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", tmnCode);
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_CurrCode", currCode);
        vnpParams.put("vnp_TxnRef", order.getId() + "");
        vnpParams.put("vnp_OrderInfo", "Thanh toan cho ma GD: " + order.getId());
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Amount",amount);

        vnpParams.put("vnp_ReturnUrl", returnUrl);
        vnpParams.put("vnp_CreateDate", formattedCreateDate);
        vnpParams.put("vnp_IpAddr", "128.199.178.23");

        StringBuilder signDataBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
            signDataBuilder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString()));
            signDataBuilder.append("=");
            signDataBuilder.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString()));
            signDataBuilder.append("&");
        }
        signDataBuilder.deleteCharAt(signDataBuilder.length() - 1); // Remove last '&'

        String signData = signDataBuilder.toString();
        String signed = generateHMAC(secretKey, signData);

        vnpParams.put("vnp_SecureHash", signed);

        StringBuilder urlBuilder = new StringBuilder(vnpUrl);
        urlBuilder.append("?");
        for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
            urlBuilder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString()));
            urlBuilder.append("=");
            urlBuilder.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString()));
            urlBuilder.append("&");
        }
        urlBuilder.deleteCharAt(urlBuilder.length() - 1); // Remove last '&'

        return urlBuilder.toString();
    }

    private String generateHMAC(String secretKey, String signData) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmacSha512 = Mac.getInstance("HmacSHA512");
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        hmacSha512.init(keySpec);
        byte[] hmacBytes = hmacSha512.doFinal(signData.getBytes(StandardCharsets.UTF_8));

        StringBuilder result = new StringBuilder();
        for (byte b : hmacBytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
