package com.mobile.prm392.services;

import com.mobile.prm392.entities.Payment;
import com.mobile.prm392.model.payment.PaymentPageResponse;
import com.mobile.prm392.repositories.IPaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {

    @Autowired
    private IPaymentRepository paymentRepository;

    // Lấy tất cả Payment
    public PaymentPageResponse getAllPayments(int page, int size) {
        Page payment =  paymentRepository.findAll(PageRequest.of(page - 1, size));

        PaymentPageResponse response = new PaymentPageResponse();
        response.setContent(payment.getContent());
        response.setPageNumber(payment.getNumber());
        response.setTotalElements(payment.getTotalElements());
        response.setTotalPages(payment.getTotalPages());
        return response;
    }

    // Lấy Payment theo id
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found with id: " + id));
    }

    // Tạo Payment mới
//    public Payment createPayment(Payment payment) {
//        if (payment.getTransactionId() != null &&
//                paymentRepository.existsByTransactionId(payment.getTransactionId())) {
//            throw new IllegalStateException("Transaction ID already exists!");
//        }
//        return paymentRepository.save(payment);
//    }

    // Cập nhật Payment
    public Payment updatePayment(Long id, Payment payment) {
        Payment existingPayment = paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found with id: " + id));

        existingPayment.setOrder(payment.getOrder());
        existingPayment.setMethod(payment.getMethod());
        existingPayment.setStatus(payment.getStatus());
        existingPayment.setAmount(payment.getAmount());
//        existingPayment.setTransactionId(payment.getTransactionId());
        existingPayment.setUpdatedAt(payment.getUpdatedAt());

        return paymentRepository.save(existingPayment);
    }

    // Xóa Payment
    public void deletePayment(Long id) {
        if (!paymentRepository.existsById(id)) {
            throw new EntityNotFoundException("Payment not found with id: " + id);
        }
        paymentRepository.deleteById(id);
    }
}
