package com.mobile.prm392.services;

import com.mobile.prm392.entities.OrderItem;
import com.mobile.prm392.repositories.IOrderItemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderItemService {

    @Autowired
    private IOrderItemRepository orderItemRepository;

    // Lấy tất cả order items
    public List<OrderItem> getAllOrderItems() {
        return orderItemRepository.findAll();
    }

    // Lấy order item theo id
    public OrderItem getOrderItemById(Long id) {
        return orderItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("OrderItem not found with id: " + id));
    }

    // Tạo order item mới
    public OrderItem createOrderItem(OrderItem orderItem) {
        return orderItemRepository.save(orderItem);
    }

    // Cập nhật order item
    public OrderItem updateOrderItem(Long id, OrderItem orderItem) {
        OrderItem existingOrderItem = orderItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("OrderItem not found with id: " + id));

        existingOrderItem.setOrder(orderItem.getOrder());
        existingOrderItem.setProduct(orderItem.getProduct());
        existingOrderItem.setQuantity(orderItem.getQuantity());
        existingOrderItem.setPrice(orderItem.getPrice());

        return orderItemRepository.save(existingOrderItem);
    }

    // Xóa order item
    public void deleteOrderItem(Long id) {
        if (!orderItemRepository.existsById(id)) {
            throw new EntityNotFoundException("OrderItem not found with id: " + id);
        }
        orderItemRepository.deleteById(id);
    }
}
