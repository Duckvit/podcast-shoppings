package com.mobile.prm392.model.product;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class ProductRequest {

    private String name;

    private String description;

    private Double price;

    private Integer stockQuantity;

    private String imageUrl;
}
