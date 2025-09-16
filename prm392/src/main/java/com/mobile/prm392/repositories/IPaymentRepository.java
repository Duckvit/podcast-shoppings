package com.mobile.prm392.repositories;

import com.mobile.prm392.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IPaymentRepository extends JpaRepository<Payment, Long> {
    boolean existsByTransactionId(String transactionId);
}
