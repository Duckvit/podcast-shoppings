package com.mobile.prm392.repositories;

import com.mobile.prm392.entities.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IOrderRepository extends JpaRepository<Order, Long> {
    Page findByIsActiveTrue(Pageable pageable);
    Page<Order> findByUserIdAndIsActiveTrue(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"items", "items.product"})
    Optional<Order> findWithItemsAndProductsById(Long id);
}
