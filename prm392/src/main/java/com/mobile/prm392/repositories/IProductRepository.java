package com.mobile.prm392.repositories;

import com.mobile.prm392.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IProductRepository extends JpaRepository<Product, Long> {
    boolean existsByName(String name);

    Optional<Product> findByName(String name);
}
