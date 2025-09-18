package com.mobile.prm392.services;

import com.mobile.prm392.entities.Order;
import com.mobile.prm392.entities.OrderItem;
import com.mobile.prm392.entities.Product;
import com.mobile.prm392.entities.User;
import com.mobile.prm392.model.order.OrderPageResponse;
import com.mobile.prm392.model.order.OrderRequest;
import com.mobile.prm392.model.order.OrderUpdateRequest;
import com.mobile.prm392.model.orderItem.OrderItemRequest;
import com.mobile.prm392.repositories.IOrderRepository;
import com.mobile.prm392.repositories.IProductRepository;
import jakarta.persistence.EntityNotFoundException;
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
import java.util.*;

@Service
public class OrderService {

    @Autowired
    private IOrderRepository orderRepository;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private IProductRepository productRepository;

    // Lấy tất cả order
    public OrderPageResponse getAllOrders(int page, int size) {
        Page order = orderRepository.findAll(PageRequest.of(page - 1, size));

        OrderPageResponse response = new OrderPageResponse();
        response.setContent(order.getContent());
        response.setPageNumber(order.getNumber());
        response.setTotalElements(order.getTotalElements());
        response.setTotalPages(order.getTotalPages());
        return response;
    }

    // Lấy order theo id
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
    }

    // Tạo mới order
    public Order createOrder(OrderRequest orderRequest) {

        User user = authenticationService.getCurrentUser();
        Order order = new Order();
        List<OrderItem> orderItems = new ArrayList<>();
        Double totalAmount = 0.0;

        order.setUser(user);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        for(OrderItemRequest orderItemRequest : orderRequest.getItems()){

            Product product = productRepository.getById(orderItemRequest.getProductId());

            OrderItem orderItem = new OrderItem();

            orderItem.setQuantity(orderItemRequest.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItem.setOrder(order);
            orderItem.setProduct(product);


            orderItems.add(orderItem);

            totalAmount += orderItem.getPrice() * orderItem.getQuantity();
        }
        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);
        order.setStatus("Pending");

        return orderRepository.save(order);
    }

    // Cập nhật order
    public Order updateOrder(Long id, OrderUpdateRequest request) {
        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));

        // cập nhật status nếu có
        if (request.getStatus() != null) {
            existingOrder.setStatus(request.getStatus());
        }

        // nếu có items mới thì cập nhật lại
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            List<OrderItem> newOrderItems = new ArrayList<>();
            double totalAmount = 0.0;

            for (OrderItemRequest orderItemRequest : request.getItems()) {
                Product product = productRepository.findById(orderItemRequest.getProductId())
                        .orElseThrow(() -> new EntityNotFoundException("Product not found"));

                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(existingOrder);
                orderItem.setProduct(product);
                orderItem.setQuantity(orderItemRequest.getQuantity());
                orderItem.setPrice(product.getPrice());

                newOrderItems.add(orderItem);
                totalAmount += orderItem.getPrice() * orderItem.getQuantity();
            }

            existingOrder.setItems(newOrderItems);
            existingOrder.setTotalAmount(totalAmount);
        }

        existingOrder.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(existingOrder);
    }


    // Xóa order
    public void deleteOrder(Long id) {
        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));

        existingOrder.setActive(false);
        existingOrder.setUpdatedAt(LocalDateTime.now());

        orderRepository.save(existingOrder);
    }

    // thanh toan order
    public String createUrl(OrderRequest orderRequest) throws  Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime createDate = LocalDateTime.now();
        String formattedCreateDate = createDate.format(formatter);

        // code cua minh
//        Appointment appointment = appointmentService.completeAppointmentById(orderRequest);
        Order order = createOrder(orderRequest);
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
