package com.mobile.prm392.model.payment;

import com.mobile.prm392.entities.Payment;
import lombok.Data;

import java.util.List;

@Data
public class PaymentPageResponse {
    private List<Payment> content;
    private int pageNumber;
    private long totalElements;
    private int totalPages;
}
