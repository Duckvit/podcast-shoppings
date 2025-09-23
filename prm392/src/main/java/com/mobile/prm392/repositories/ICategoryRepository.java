package com.mobile.prm392.repositories;

import com.mobile.prm392.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ICategoryRepository extends JpaRepository<Category, Long> {
}
