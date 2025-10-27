package com.mobile.prm392.services;

import com.mobile.prm392.entities.Order;
import com.mobile.prm392.entities.OrderItem;
import com.mobile.prm392.entities.Product;
import com.mobile.prm392.entities.User;
import com.mobile.prm392.model.order.OrderPageResponse;
import com.mobile.prm392.model.order.OrderRequest;
import com.mobile.prm392.model.order.OrderResponse;
import com.mobile.prm392.model.order.OrderUpdateRequest;
import com.mobile.prm392.model.orderItem.OrderItemRequest;
import com.mobile.prm392.model.orderItem.OrderItemResponse;
import com.mobile.prm392.repositories.IOrderRepository;
import com.mobile.prm392.repositories.IProductRepository;
import com.mobile.prm392.repositories.IUserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

//    // Lấy tất cả order
//    public OrderPageResponse getAllOrders(int page, int size) {
//        Page<Order> orderPage = orderRepository.findByIsActiveTrue(PageRequest.of(page - 1, size));
//

    /// /        List<OrderResponse> content = orderPage.getContent().stream()
    /// /                .map(order -> modelMapper.map(order, OrderResponse.class))
    /// /                .toList();
//        List<OrderResponse> content = orderPage.getContent().stream()
//                .peek(order -> order.getItems().size()) // Ép Hibernate load danh sách items
//                .map(order -> modelMapper.map(order, OrderResponse.class))
//                .toList();
//
//        OrderPageResponse response = new OrderPageResponse();
//        response.setContent(content);
//        response.setPageNumber(orderPage.getNumber());
//        response.setTotalElements(orderPage.getTotalElements());
//        response.setTotalPages(orderPage.getTotalPages());
//        return response;
//    }
    @Transactional(readOnly = true)
    public OrderPageResponse getAllOrders(int page, int size) {
        Page<Order> orderPage = orderRepository.findByIsActiveTrue(PageRequest.of(page - 1, size));

        List<OrderResponse> content = orderPage.getContent().stream()
                .peek(order -> order.getItems().size()) // ép load
                .map(order -> modelMapper.map(order, OrderResponse.class))
                .toList();

        OrderPageResponse response = new OrderPageResponse();
        response.setContent(content);
        response.setPageNumber(orderPage.getNumber());
        response.setTotalElements(orderPage.getTotalElements());
        response.setTotalPages(orderPage.getTotalPages());
        return response;
    }

    // Lấy tất cả order theo userId
//    public OrderPageResponse getOrdersByUserId(Long userId, int page, int size) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new EntityNotFoundException("user not found: " + userId));
//        Page<Order> orderPage = orderRepository.findByUserIdAndIsActiveTrue(userId, PageRequest.of(page - 1, size));
//
//        List<OrderResponse> content = orderPage.getContent().stream()
//                .map(order -> modelMapper.map(order, OrderResponse.class))
//                .toList();
//
//        OrderPageResponse response = new OrderPageResponse();
//        response.setContent(content);
//        response.setPageNumber(orderPage.getNumber());
//        response.setTotalElements(orderPage.getTotalElements());
//        response.setTotalPages(orderPage.getTotalPages());
//        return response;
//    }

    @Transactional(readOnly = true)
    public OrderPageResponse getOrdersByUserId(Long userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("user not found: " + userId));

        Page<Order> orderPage = orderRepository.findByUserIdAndIsActiveTrue(
                userId, PageRequest.of(page - 1, size)
        );

        List<OrderResponse> content = orderPage.getContent().stream()
                .peek(order -> order.getItems().size()) // ép Hibernate load items
                .map(order -> {
                    OrderResponse response = modelMapper.map(order, OrderResponse.class);
                    // Ép kiểu List<OrderItemResponse> từ PersistentBag -> List
                    response.setItems(order.getItems().stream()
                            .map(item -> modelMapper.map(item, OrderItemResponse.class))
                            .toList());
                    return response;
                })
                .toList();

        OrderPageResponse response = new OrderPageResponse();
        response.setContent(content);
        response.setPageNumber(orderPage.getNumber());
        response.setTotalElements(orderPage.getTotalElements());
        response.setTotalPages(orderPage.getTotalPages());
        return response;
    }


    // Lấy order theo id
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
        return modelMapper.map(order, OrderResponse.class);
    }

    // Tạo mới order
    public Order createOrder(OrderRequest orderRequest) {

        User user = authenticationService.getCurrentUser();
        Order order = new Order();
        List<OrderItem> orderItems = new ArrayList<>();
        Double totalAmount = 0.0;

        order.setUser(user);
        order.setAddress(order.getAddress());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        for (OrderItemRequest orderItemRequest : orderRequest.getItems()) {

            Product product = productRepository.findById(orderItemRequest.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found: " + orderItemRequest.getProductId()));

            // Kiểm tra số lượng tồn kho
            if (product.getStockQuantity() < orderItemRequest.getQuantity()) {
                throw new IllegalArgumentException("Not enough stock for product: " + product.getName());
            }

            OrderItem orderItem = new OrderItem();

            orderItem.setQuantity(orderItemRequest.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItem.setOrder(order);
            orderItem.setProduct(product);


            orderItems.add(orderItem);

            totalAmount += orderItem.getPrice() * orderItem.getQuantity();
            // Trừ số lượng tồn kho mới
            product.setStockQuantity(product.getStockQuantity() - orderItemRequest.getQuantity());
            productRepository.save(product);
        }
        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);
        order.setStatus("pending");
        order.setAddress(orderRequest.getAddress());

        return orderRepository.save(order);
    }


    // Cập nhật order
    public Order updateOrder(Long id, OrderUpdateRequest request) {
        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));

        // Nếu có items mới thì xử lý cập nhật lại
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            // Hoàn lại số lượng kho từ các order item cũ
            for (OrderItem oldItem : existingOrder.getItems()) {
                Product product = oldItem.getProduct();
                product.setStockQuantity(product.getStockQuantity() + oldItem.getQuantity());
                productRepository.save(product);
            }

            List<OrderItem> newOrderItems = new ArrayList<>();
            double totalAmount = 0.0;

            for (OrderItemRequest orderItemRequest : request.getItems()) {
                Product product = productRepository.findById(orderItemRequest.getProductId())
                        .orElseThrow(() -> new EntityNotFoundException("Product not found"));

                // Kiểm tra số lượng tồn kho
                if (product.getStockQuantity() < orderItemRequest.getQuantity()) {
                    throw new IllegalArgumentException("Not enough stock for product: " + product.getName());
                }

                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(existingOrder);
                orderItem.setProduct(product);
                orderItem.setQuantity(orderItemRequest.getQuantity());
                orderItem.setPrice(product.getPrice());

                newOrderItems.add(orderItem);
                totalAmount += orderItem.getPrice() * orderItem.getQuantity();

                // Trừ số lượng tồn kho mới
                product.setStockQuantity(product.getStockQuantity() - orderItemRequest.getQuantity());
                productRepository.save(product);
            }

            existingOrder.setItems(newOrderItems);
            existingOrder.setTotalAmount(totalAmount);
        }

        // Cập nhật status nếu có
        if (request.getStatus() != null) {
            existingOrder.setStatus(request.getStatus());
        }

        existingOrder.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(existingOrder);
    }


    // Xóa order (soft delete + hoàn lại số lượng product)
    public boolean deleteOrder(Long id) {
        boolean result;
        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));

        // Hoàn lại số lượng sản phẩm trong kho
        for (OrderItem item : existingOrder.getItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        // Soft delete
        existingOrder.setActive(false);
        result = true;
        existingOrder.setUpdatedAt(LocalDateTime.now());

        orderRepository.save(existingOrder);
        return result;
    }

    public boolean markAsComplete(long orderId) {
        Optional<Order> existingOrder = orderRepository.findById(orderId);
        if (existingOrder.isEmpty()) {
            return false;
        }
        Order order = existingOrder.get();
        order.setStatus("completed");
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        return true;
    }

    public Order updateAddress(Long orderId, String newAddress) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));

        order.setAddress(newAddress);
        return orderRepository.save(order);
    }
}
