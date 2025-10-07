package com.mobile.prm392.services;

import com.mobile.prm392.entities.Product;
import com.mobile.prm392.model.product.ProductPageResponse;
import com.mobile.prm392.model.product.ProductRequest;
import com.mobile.prm392.repositories.IProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private IProductRepository productRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    // Lấy tất cả sản phẩm
    public ProductPageResponse getAllProducts(int page, int size) {
        Page product = productRepository.findAll(PageRequest.of(page - 1, size));

        ProductPageResponse response = new ProductPageResponse();
        response.setTotalPages(product.getTotalPages());
        response.setContent(product.getContent());
        response.setPageNumber(product.getNumber());
        response.setTotalElements(product.getTotalElements());
        return response;
    }

    // Lấy tất cả sản phẩm có isActive = true
    public ProductPageResponse getAllActiveProducts(int page, int size) {
        Page<Product> productPage = productRepository.findByIsActiveTrue(PageRequest.of(page - 1, size));

        ProductPageResponse response = new ProductPageResponse();
        response.setTotalPages(productPage.getTotalPages());
        response.setContent(productPage.getContent());
        response.setPageNumber(productPage.getNumber());
        response.setTotalElements(productPage.getTotalElements());
        return response;
    }


    // Lấy sản phẩm theo id
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
    }

    // Tạo sản phẩm mới
    public Product createProduct(String name, String description, Double price, Integer stockQuantity, MultipartFile file) throws IOException {
        // Upload ảnh lên Cloudinary
        String imageUrl = cloudinaryService.uploadImage(file);

        // Nếu sản phẩm đã tồn tại -> cộng thêm số lượng
        Optional<Product> productOpt = productRepository.findByName(name);
        if (productOpt.isPresent()) {
            Product existing = productOpt.get();
            existing.setStockQuantity(existing.getStockQuantity() + stockQuantity);
            existing.setUpdatedAt(LocalDateTime.now());
            return productRepository.save(existing);
        }

        // Nếu chưa có -> tạo mới
        Product newProduct = new Product();
        newProduct.setName(name);
        newProduct.setDescription(description);
        newProduct.setPrice(price);
        newProduct.setStockQuantity(stockQuantity);
        newProduct.setImageUrl(imageUrl);
        newProduct.setCreatedAt(LocalDateTime.now());

        return productRepository.save(newProduct);
    }

    // Cập nhật sản phẩm
    public Product updateProduct(Long id, String name, String description, Double price, Integer stockQuantity, MultipartFile file) throws IOException {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

        if (name != null) existingProduct.setName(name);
        if (description != null) existingProduct.setDescription(description);
        if (price != null) existingProduct.setPrice(price);
        if (stockQuantity != null) existingProduct.setStockQuantity(stockQuantity);

        // Nếu có file ảnh mới thì upload lên Cloudinary
        if (file != null && !file.isEmpty()) {
            String imageUrl = cloudinaryService.uploadImage(file);
            existingProduct.setImageUrl(imageUrl);
        }

        existingProduct.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(existingProduct);
    }


    // Xóa sản phẩm
    public boolean deleteProduct(Long id) {
        boolean result;
        Product product = productRepository.getById(id);
        if (product == null) {
            throw new EntityNotFoundException("Product not found with id: " + id);
        }
        product.setActive(false);
        result = true;
        productRepository.save(product);
        return result;
    }
}
