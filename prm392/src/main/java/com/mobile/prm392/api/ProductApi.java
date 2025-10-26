package com.mobile.prm392.api;

import com.mobile.prm392.entities.Product;
import com.mobile.prm392.model.product.ProductRequest;
import com.mobile.prm392.services.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin("*") // cho phep tat ca truy cap, ket noi FE va BE vs nhau
@SecurityRequirement(name = "api") // tao controller moi nho copy qua
public class ProductApi {

    @Autowired
    private ProductService productService;

    // Lấy tất cả sản phẩm
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity getAllProducts(@RequestParam int page, @RequestParam int size) {
        return ResponseEntity.ok(productService.getAllProducts(page, size));
    }

    // lay theo isActive true
    @GetMapping("/active")
    public ResponseEntity getActiveProducts(
            @RequestParam int page,
            @RequestParam int size) {
        return ResponseEntity.ok(productService.getAllActiveProducts(page, size));
    }

    // Lấy sản phẩm theo id
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(productService.getProductById(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Tạo sản phẩm mới", description = "Upload ảnh sản phẩm lên Cloudinary")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tạo thành công"),
            @ApiResponse(responseCode = "400", description = "Sai request"),
    })
    @PostMapping(consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> createProduct(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam Double price,
            @RequestParam Integer stockQuantity,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        return ResponseEntity.ok(productService.createProduct(name, description, price, stockQuantity, file));
    }

    @Operation(summary = "Cập nhật sản phẩm", description = "Cập nhật thông tin sản phẩm và có thể thay ảnh")
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Double price,
            @RequestParam(required = false) Integer stockQuantity,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        return ResponseEntity.ok(productService.updateProduct(id, name, description, price, stockQuantity, file));
    }

    // Xóa sản phẩm
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity deleteProduct(@PathVariable Long id) {
        boolean result = productService.deleteProduct(id);
        String resultString = "";
        if(result){
            resultString = "Delete successfully";
        }
        return ResponseEntity.ok(resultString);
    }
}
