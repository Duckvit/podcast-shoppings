package com.mobile.prm392.services;

import com.mobile.prm392.entities.Transaction;
import com.mobile.prm392.repositories.ITransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    private final ITransactionRepository transactionRepository;

    public TransactionService(ITransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    // Lấy tất cả transaction
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    // Lấy transaction theo ID
    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }

    // Lấy transaction theo PaymentId
    public List<Transaction> getTransactionsByPaymentId(Long paymentId) {
        return transactionRepository.findByPaymentId(paymentId);
    }

    // Lấy transaction theo status
    public List<Transaction> getTransactionsByStatus(String status) {
        return transactionRepository.findByStatus(status);
    }

    // Tạo hoặc cập nhật transaction
    public Transaction saveTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    // Xóa transaction
    public void deleteTransaction(Long id) {
        transactionRepository.deleteById(id);
    }
}
