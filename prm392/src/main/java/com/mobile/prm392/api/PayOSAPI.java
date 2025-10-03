package com.mobile.prm392.api;

import com.mobile.prm392.entities.Order;
import com.mobile.prm392.entities.Payment;
import com.mobile.prm392.exception.OurException;
import com.mobile.prm392.model.payos.ApiResponse;
import com.mobile.prm392.model.payos.CreatePaymentLinkRequestBody;
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

            // Kiểm tra xem đã có payment cho order này chưa
            boolean exists = paymentRepository.existsByOrder(order);
            if (exists) {
                throw new RuntimeException("Order had been processed"); // <-- ném exception nếu đã có payment
            }

            long orderCode = System.currentTimeMillis() / 1000;

            PaymentLinkItem item = PaymentLinkItem.builder()
                    .name(requestBody.getName())
                    .quantity(1)
                    .price(requestBody.getPrice())
                    .build();

            CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
                    .orderCode(orderCode)
                    .description(requestBody.getDescription())
                    .amount(requestBody.getPrice())
                    .item(item)
                    .returnUrl(requestBody.getReturnUrl())
                    .cancelUrl(requestBody.getCancelUrl())
                    .build();

            // Gọi PayOS SDK tạo link
            CreatePaymentLinkResponse data = payOS.paymentRequests().create(paymentData);

            // Lưu Payment vào DB
            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setMethod("payos");
            payment.setStatus("pending");
            payment.setAmount((double) requestBody.getPrice());
            payment.setTransactionId(String.valueOf(orderCode));
            payment.setCheckoutUrl(data.getCheckoutUrl());
            paymentRepository.save(payment);

            return ApiResponse.success(data);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage()); // trả về message cụ thể
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("fail");
        }
    }

}
