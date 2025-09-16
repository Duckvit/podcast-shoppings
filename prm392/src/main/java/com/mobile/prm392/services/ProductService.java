package com.mobile.prm392.services;

import com.mobile.prm392.entities.Product;
import com.mobile.prm392.model.product.ProductRequest;
import com.mobile.prm392.repositories.IProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private IProductRepository productRepository;

    // Lấy tất cả sản phẩm
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Lấy sản phẩm theo id
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
    }

    // Tạo sản phẩm mới
    public Product createProduct(ProductRequest productRequest) {

        Optional<Product> product = productRepository.findByName(productRequest.getName());
        if(product.isPresent()){
            // Nếu tồn tại -> cộng thêm số lượng kho
            Product newProduct = product.get();

//            newProduct.setName(productRequest.getName());
//            newProduct.setDescription(productRequest.getDescription());
//            newProduct.setPrice(productRequest.getPrice());
            newProduct.setStockQuantity(newProduct.getStockQuantity() + productRequest.getStockQuantity());
//            newProduct.setImageUrl(newProduct.getImageUrl());
            newProduct.setUpdatedAt(LocalDateTime.now());

            return productRepository.save(newProduct);

        }else{
            // Nếu chưa có -> tạo mới
            Product newProduct = new Product();
            newProduct.setName(productRequest.getName());
            newProduct.setDescription(productRequest.getDescription());
            newProduct.setPrice(productRequest.getPrice());
            newProduct.setStockQuantity(productRequest.getStockQuantity());
            newProduct.setImageUrl(productRequest.getImageUrl());
            newProduct.setCreatedAt(LocalDateTime.now());

            return productRepository.save(newProduct);
        }
    }

    // Cập nhật sản phẩm
    public Product updateProduct(Long id, ProductRequest productRequest) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

        if (productRequest.getName() != null) {
            existingProduct.setName(productRequest.getName());
        }

        if (productRequest.getDescription() != null) {
            existingProduct.setDescription(productRequest.getDescription());
        }

        if (productRequest.getPrice() != null) {
            existingProduct.setPrice(productRequest.getPrice());
        }

        if (productRequest.getStockQuantity() != null) {
            existingProduct.setStockQuantity(productRequest.getStockQuantity());
        }

        if (productRequest.getImageUrl() != null) {
            existingProduct.setImageUrl(productRequest.getImageUrl());
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
        productRepository.deleteById(id);
        return result;
    }
}
