package com.mobile.prm392.model.order;

import lombok.Data;

@Data
public class UpdateAddressRequest {
    private Long orderId;
    private String address;
}

