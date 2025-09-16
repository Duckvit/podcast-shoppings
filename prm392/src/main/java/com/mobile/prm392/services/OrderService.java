package com.mobile.prm392.services;

import com.mobile.prm392.entities.Order;
import com.mobile.prm392.entities.OrderItem;
import com.mobile.prm392.entities.Product;
import com.mobile.prm392.entities.User;
import com.mobile.prm392.model.order.OrderRequest;
import com.mobile.prm392.model.order.OrderUpdateRequest;
import com.mobile.prm392.model.orderItem.OrderItemRequest;
import com.mobile.prm392.repositories.IOrderRepository;
import com.mobile.prm392.repositories.IProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private IOrderRepository orderRepository;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private IProductRepository productRepository;

    // Lấy tất cả order
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
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

}
