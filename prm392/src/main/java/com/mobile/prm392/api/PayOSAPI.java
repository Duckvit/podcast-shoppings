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
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.view.RedirectView;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.webhooks.Webhook;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Optional;

@RestController
@RequestMapping("/payos")
@CrossOrigin("*")
public class PayOSAPI {
    private final PayOS payOS;
    private final IPaymentRepository paymentRepository;
    private final IOrderRepository orderRepository;

    @Autowired
    private ObjectMapper mapper;

    public PayOSAPI(PayOS payOS, IPaymentRepository paymentRepository, IOrderRepository orderRepository) {
        this.payOS = payOS;
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    @PostMapping(path = "/create")
    public ApiResponse<CreatePaymentLinkResponse> createPaymentLink(@RequestBody CreatePaymentLinkRequestBody requestBody) {
        try {
            // Lấy order từ DB (fetch items + products to avoid lazy init)
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

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@org.springframework.web.bind.annotation.RequestBody String rawJson) {
        try {
            System.out.println("📩 RAW JSON RECEIVED: " + rawJson);

            // Parse payload
            WebhookRequest payload = mapper.readValue(rawJson, WebhookRequest.class);
            if (payload == null || payload.getData() == null || payload.getData().getOrderCode() == null) {
                return ResponseEntity.ok("fail");
            }

            String orderCodeStr = String.valueOf(payload.getData().getOrderCode());

            Payment payment = paymentRepository.findByTransactionId(orderCodeStr)
                    .orElse(null);

            if (payment == null) {
                // Unknown transactionId
                return ResponseEntity.ok("fail");
            }

            // If already success, idempotent success response
            if ("success".equalsIgnoreCase(payment.getStatus())) {
                return ResponseEntity.ok("success");
            }

            boolean isSuccess = payload.isSuccess();

            if (isSuccess) {
                payment.setStatus("success");
                payment.setUpdatedAt(LocalDateTime.now());
                paymentRepository.save(payment);

                // Also mark order as paid
                Order order = payment.getOrder();
                if (order != null) {
                    order.setStatus("paid");
                    order.setUpdatedAt(LocalDateTime.now());
                    orderRepository.save(order);
                }

                return ResponseEntity.ok("success");
            } else {
                payment.setStatus("failed");
                payment.setUpdatedAt(LocalDateTime.now());
                paymentRepository.save(payment);
                return ResponseEntity.ok("fail");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Webhook Exception: " + e.getMessage());
            return ResponseEntity.internalServerError().body("fail");
        }
    }

    @GetMapping("/test-webhook")
    public ResponseEntity<String> confirmWebhook() {
        System.out.println("✅ GET webhook confirmation called");
        return ResponseEntity.ok("OK");
    }

    @PutMapping("/cancel")
    public ResponseEntity<?> cancelOrderByTransactionId(@RequestParam String transactionId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElse(null);

        if (payment == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Payment not found");
        }

        Order order = payment.getOrder();
        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Order not found for this payment");
        }

        // Cập nhật status
        order.setStatus("cancelled");
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        // Cập nhật payment
        payment.setStatus("cancelled");
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        return ResponseEntity.ok("Order and Payment cancelled successfully");
    }


}
