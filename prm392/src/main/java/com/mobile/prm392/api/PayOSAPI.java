package com.mobile.prm392.api;

import com.mobile.prm392.entities.Order;
import com.mobile.prm392.entities.Payment;
import com.mobile.prm392.exception.OurException;
import com.mobile.prm392.model.payos.ApiResponse;
import com.mobile.prm392.model.payos.CreatePaymentLinkRequestBody;
import com.mobile.prm392.model.payos.UpdatePaymentStatusRequest;
import com.mobile.prm392.repositories.IOrderRepository;
import com.mobile.prm392.repositories.IPaymentRepository;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/payos")
@CrossOrigin("*")
public class PayOSAPI {
    private final PayOS payOS;
    private final IPaymentRepository paymentRepository;
    private final IOrderRepository orderRepository;

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


}
