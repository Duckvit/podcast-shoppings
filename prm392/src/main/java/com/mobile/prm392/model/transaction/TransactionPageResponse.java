package com.mobile.prm392.model.transaction;

import com.mobile.prm392.entities.Transaction;
import lombok.Data;

import java.util.List;

@Data
public class TransactionPageResponse {
    private List<Transaction> content;
    private int pageNumber;
    private long totalElements;
    private int totalPages;
}
