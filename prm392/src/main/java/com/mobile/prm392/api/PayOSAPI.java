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
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
            // Lấy order từ DB
            Order order = orderRepository.findById(requestBody.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            // Kiểm tra đã có payment chưa
            boolean exists = paymentRepository.existsByOrder(order);
            if (exists) {
                throw new RuntimeException("Order had been processed");
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
//    @PostMapping("/webhook")
//    public ResponseEntity<String> handleWebhook(@RequestBody WebhookRequest payload) {
//        System.out.println("📩 Webhook received: " + payload);
//
//        try {
//            String orderCode = payload.getData().getOrderCode();
//            String status = payload.getData().getStatus();
//
//            Payment payment = paymentRepository.findByTransactionId(orderCode)
//                    .orElseThrow(() -> new RuntimeException("Payment not found with transactionId: " + orderCode));
//
//            if ("success".equalsIgnoreCase(payment.getStatus())) {
//                return ResponseEntity.ok("Payment already processed");
//            }
//
//            if ("PAID".equalsIgnoreCase(status)) {
//                payment.setStatus("success");
//            } else if ("CANCELLED".equalsIgnoreCase(status)) {
//                payment.setStatus("cancel");
//            } else {
//                payment.setStatus("failed");
//            }
//
//            payment.setUpdatedAt(LocalDateTime.now());
//            paymentRepository.save(payment);
//
//            System.out.println("✅ Payment updated via webhook: " + payment.getStatus());
//            return ResponseEntity.ok("Webhook processed successfully");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body("Error processing webhook: " + e.getMessage());
//        }
//    }
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
            return ResponseEntity.ok("fail");
        }
    }


}
