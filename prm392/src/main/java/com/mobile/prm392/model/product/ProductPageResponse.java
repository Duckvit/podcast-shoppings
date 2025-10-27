package com.mobile.prm392.model.product;

import com.mobile.prm392.entities.Product;
import lombok.Data;

import java.util.List;

@Data
public class ProductPageResponse {
    private List<Product> content;
    private int pageNumber;
    private long totalElements;
    private int totalPages;
}
