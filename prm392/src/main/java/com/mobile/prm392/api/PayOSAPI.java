package com.mobile.prm392.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobile.prm392.entities.Order;
import com.mobile.prm392.entities.Payment;
import com.mobile.prm392.exception.OurException;
import com.mobile.prm392.model.payos.ApiResponse;
import com.mobile.prm392.model.payos.CreatePaymentLinkRequestBody;
import com.mobile.prm392.model.payos.UpdatePaymentStatusRequest;
import com.mobile.prm392.model.payos.WebhookRequest;
import com.mobile.prm392.repositories.IOrderRepository;
import com.mobile.prm392.repositories.IPaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.Comparator;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import org.apache.commons.codec.digest.HmacUtils;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payos")
@CrossOrigin("*")
public class PayOSAPI {
    private final PayOS payOS;
    private final IPaymentRepository paymentRepository;
    private final IOrderRepository orderRepository;

    @Autowired
    private ObjectMapper mapper;

    @Value("${payos.checksum-key}")
    private String checksumKey;

    public PayOSAPI(PayOS payOS, IPaymentRepository paymentRepository, IOrderRepository orderRepository) {
        this.payOS = payOS;
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    @PostMapping(path = "/create")
    public ApiResponse<CreatePaymentLinkResponse> createPaymentLink(@RequestBody CreatePaymentLinkRequestBody requestBody) {
        try {
            if (requestBody == null || requestBody.getOrderId() == null) {
                return ApiResponse.error("orderId is required");
            }
            // Lấy order từ DB (fetch items đil + products to avoid lazy init)
            Order order = orderRepository.findWithItemsAndProductsById(requestBody.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            // Kiểm tra đã có payment chưa
            boolean exists = paymentRepository.existsByOrder(order);
            if (exists) {
                throw new RuntimeException("Order had been processed");
            } else {
                System.out.println("Order find with Id" + exists);
            }

            // Lấy danh sách sản phẩm từ OrderItem
            List<PaymentLinkItem> items = order.getItems().stream()
                    .map(oi -> PaymentLinkItem.builder()
                            .name(oi.getProduct().getName())
                            .quantity(oi.getQuantity())
                            .price(oi.getPrice().longValue()) // PayOS yêu cầu kiểu long
                            .build())
                    .toList();

            // Tính tổng tiền (nếu chưa có)
            long totalAmount = order.getTotalAmount() != null
                    ? order.getTotalAmount().longValue()
                    : items.stream().mapToLong(i -> i.getPrice() * i.getQuantity()).sum();

            // Tạo dữ liệu thanh toán
            long orderCode = System.currentTimeMillis() / 1000;
            String description = "Thanh toán đơn hàng #" + order.getId();
            String returnUrl = "http://localhost:5173/payment-success"; // 🔧 chỉnh theo FE
            String cancelUrl = "http://localhost:5173/payment-cancel";

            CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
                    .orderCode(orderCode)
                    .description(description)
                    .amount(totalAmount)
                    .items(items)
                    .returnUrl(returnUrl)
                    .cancelUrl(cancelUrl)
                    .build();

            // Gọi PayOS tạo link
            CreatePaymentLinkResponse data = payOS.paymentRequests().create(paymentData);

            // Lưu Payment vào DB
            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setMethod("payos");
            payment.setStatus("pending");
            payment.setAmount((double) totalAmount);
            payment.setTransactionId(String.valueOf(orderCode));
            payment.setCheckoutUrl(data.getCheckoutUrl());
            paymentRepository.save(payment);

            return ApiResponse.success(data);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("fail");
        }
    }

    @PostMapping("/update-status")
    public ApiResponse<String> updateStatus(@RequestBody UpdatePaymentStatusRequest request) {
        try {
            // Tìm payment theo transactionId
            Payment payment = paymentRepository.findByTransactionId(request.getOrderCode())
                    .orElseThrow(() -> new RuntimeException("Payment not found with transactionId: " + request.getOrderCode()));

            // Nếu đã ở trạng thái success thì không cho cập nhật nữa
            if ("success".equalsIgnoreCase(payment.getStatus())) {
                throw new RuntimeException("Payment had been processed");
            }

            // Cập nhật trạng thái mới
            if ("success".equalsIgnoreCase(request.getStatus()) || "PAID".equalsIgnoreCase(request.getStatus())) {
                payment.setStatus("success");
            } else if ("cancel".equalsIgnoreCase(request.getStatus()) || "CANCELLED".equalsIgnoreCase(request.getStatus())) {
                payment.setStatus("cancel");
            } else {
                payment.setStatus("failed");
            }

            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            return ApiResponse.success("Payment status updated to " + payment.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("Error updating payment: " + e.getMessage());
        }
    }

    /**
     * Webhook endpoint for PayOS to notify payment status updates.
     * <p>
     * To use this endpoint as your webhookUrl in PayOS, set:
     * https://podcast-shoppings-1.onrender.com/api/payos/webhook
     * <p>
     * Example usage in PayOS API:
     * webhookUrl: "https://podcast-shoppings-1.onrender.com/api/payos/webhook"
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String rawBody) {
        try {
            System.out.println("📩 RAW JSON RECEIVED: " + rawBody);

            JSONObject json = new JSONObject(rawBody);
            String signature = json.optString("signature", null);
            JSONObject data = json.optJSONObject("data");

            if (signature == null || data == null) {
                return ResponseEntity.ok("fail");
            }

            // ✅ B1: Tạo chuỗi sắp xếp alphabet key=value
            Iterator<String> sortedKeys = sortedIterator(data.keys(), String::compareTo);
            StringBuilder dataString = new StringBuilder();
            while (sortedKeys.hasNext()) {
                String key = sortedKeys.next();
                Object value = data.get(key);
                dataString.append(key).append('=').append(value);
                if (sortedKeys.hasNext()) dataString.append('&');
            }

            System.out.println("🧮 Data string: " + dataString);

            // ✅ B2: Tạo chữ ký
            String expectedSignature = new HmacUtils("HmacSHA256", checksumKey)
                    .hmacHex(dataString.toString());

            System.out.println("🔏 Expected signature: " + expectedSignature);
            System.out.println("📬 Received signature: " + signature);

            if (!expectedSignature.equals(signature)) {
                return ResponseEntity.ok("fail");
            }

            // ✅ B3: Xử lý logic đơn hàng và thanh toán
            String orderCode = data.optString("orderCode");
            String statusCode = data.optString("code"); // "00" = success
            String description = data.optString("desc");

            System.out.println("✅ Verified webhook for order " + orderCode + " with status " + statusCode);

            // 🔎 Tìm Payment theo transactionId
            Payment payment = paymentRepository.findByTransactionId(orderCode).orElse(null);
            if (payment == null) {
                System.out.println("⚠️ No payment found for transactionId " + orderCode);
                return ResponseEntity.ok("fail");
            }

            // ✅ Cập nhật trạng thái payment & order
            if ("00".equals(statusCode)) {
                payment.setStatus("success");
                payment.getOrder().setStatus("paid");
            } else {
                payment.setStatus("failed");
                payment.getOrder().setStatus("pending");
            }

            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            System.out.println("💾 Updated payment status: " + payment.getStatus());
            System.out.println("💾 Updated order status: " + payment.getOrder().getStatus());

            return ResponseEntity.ok("success");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok("fail");
        }
    }

    @GetMapping("/webhook")
    public ResponseEntity<String> confirmWebhook() {
        return ResponseEntity.ok("success");
    }

    private static Iterator<String> sortedIterator(Iterator<?> it, Comparator<String> comparator) {
        List<String> list = new ArrayList<>();
        while (it.hasNext()) list.add((String) it.next());
        list.sort(comparator);
        return list.iterator();
    }
}
