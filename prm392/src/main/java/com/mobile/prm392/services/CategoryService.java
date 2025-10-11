package com.mobile.prm392.services;

import com.mobile.prm392.entities.Category;
import com.mobile.prm392.repositories.ICategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final ICategoryRepository iCategoryRepository;

    public List<Category> getAllCategories() {
        return iCategoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        return iCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }

    public Category createCategory(Category category) {
        return iCategoryRepository.save(category);
    }

    public Category updateCategory(Long id, Category category) {
        Category existing = getCategoryById(id);
        existing.setName(category.getName());
        return iCategoryRepository.save(existing);
    }

    public void deleteCategory(Long id) {
        iCategoryRepository.deleteById(id);
    }
}
