package com.mobile.prm392.api;

import com.mobile.prm392.entities.Category;
import com.mobile.prm392.services.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin("*") // cho phep tat ca truy cap, ket noi FE va BE vs nhau
@SecurityRequirement(name = "api") // tao controller moi nho copy qua
public class CategoryAPI {
    private final CategoryService categoryService;

    @Operation(summary = "Lấy tất cả category", description = "Trả về danh sách tất cả category trong hệ thống")
    @GetMapping
    public List<Category> getAll() {
        return categoryService.getAllCategories();
    }

    @Operation(summary = "Lấy category theo ID", description = "Trả về thông tin chi tiết của một category")
    @GetMapping("/{id}")
    public Category getById(@PathVariable Long id) {
        return categoryService.getCategoryById(id);
    }

    @Operation(summary = "Tạo mới category", description = "Thêm một category mới vào hệ thống")
    @PostMapping("/admin/create")
    public ResponseEntity<Category> createCategory(@RequestBody Category dto) {
        Category category = categoryService.createCategory(dto); // service tạo Category
        return ResponseEntity.ok(category); // trả về đối tượng Category
    }


    @Operation(summary = "Cập nhật category", description = "Cập nhật tên category theo ID")
    @PutMapping("/{id}")
    public Category update(@PathVariable Long id, @RequestBody Category category) {
        return categoryService.updateCategory(id, category);
    }

    @Operation(summary = "Xóa category", description = "Xóa category theo ID")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
    }
}
