package com.mobile.prm392.model.product;

import lombok.Data;

@Data
public class ProductShortResponse {
    private Long id;
    private String name;
    private Double price;
    private String imageUrl;
}
