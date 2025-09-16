package com.mobile.prm392.repositories;

import com.mobile.prm392.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ITransactionRepository extends JpaRepository<Transaction, Long> {
    // Tìm theo paymentId
    List<Transaction> findByPaymentId(Long paymentId);

    // Tìm theo status
    List<Transaction> findByStatus(String status);
}
