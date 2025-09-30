package com.mobile.prm392.services;

import com.mobile.prm392.entities.Order;
import com.mobile.prm392.entities.Payment;
import com.mobile.prm392.entities.Transaction;
import com.mobile.prm392.entities.User;
import com.mobile.prm392.model.transaction.TransactionPageResponse;
import com.mobile.prm392.repositories.IOrderRepository;
import com.mobile.prm392.repositories.IPaymentRepository;
import com.mobile.prm392.repositories.ITransactionRepository;
import com.mobile.prm392.repositories.IUserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    @Autowired
    private ITransactionRepository transactionRepository;

    @Autowired
    private IOrderRepository orderRepository;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IPaymentRepository paymentRepository;

    // Lấy tất cả transaction
    public TransactionPageResponse getAllTransactions(int page, int size) {
         Page transactions = transactionRepository.findAll(PageRequest.of(page - 1, size));

        TransactionPageResponse response = new TransactionPageResponse();
        response.setContent(transactions.getContent());
        response.setPageNumber(transactions.getNumber());
        response.setTotalElements(transactions.getTotalElements());
        response.setTotalPages(transactions.getTotalPages());
        return response;
    }

    // Lấy transaction theo ID
    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }

    // Lấy transaction theo PaymentId
    public TransactionPageResponse getTransactionsByPaymentId(Long paymentId, int page, int size) {
        Page transactions = transactionRepository.findByPaymentId(paymentId, PageRequest.of(page - 1, size));

        TransactionPageResponse response = new TransactionPageResponse();
        response.setContent(transactions.getContent());
        response.setPageNumber(transactions.getNumber());
        response.setTotalElements(transactions.getTotalElements());
        response.setTotalPages(transactions.getTotalPages());
        return response;
    }

    // Lấy transaction theo status
    public TransactionPageResponse getTransactionsByStatus(String status, int page, int size) {
        Page transactions = transactionRepository.findByStatus(status, PageRequest.of(page - 1, size));

        TransactionPageResponse response = new TransactionPageResponse();
        response.setContent(transactions.getContent());
        response.setPageNumber(transactions.getNumber());
        response.setTotalElements(transactions.getTotalElements());
        response.setTotalPages(transactions.getTotalPages());
        return response;
    }

    // Tạo hoặc cập nhật transaction
//    public Transaction saveTransaction(Transaction transaction) {
//        return transactionRepository.save(transaction);
//    }

    // Xóa transaction
//    public void deleteTransaction(Long id) {
//        transactionRepository.deleteById(id);
//    }

    // tao transaction vs payment
    public void createTransactionSuccess(long orderId) {
        // Tìm appointment
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found!"));

        // Tạo payment
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setMethod("Banking");
        payment.setStatus("Success");
        payment.setAmount(order.getTotalAmount());

        List<Transaction> transactions = new ArrayList<>();
        // tao transaction 1
        Transaction transaction1 = new Transaction();

        //tu vnpay to customer ( customer nap tien vo vnpay)
        User customer = authenticationService.getCurrentUser();
        transaction1.setFrom(null);
        transaction1.setTo(customer);
        transaction1.setPayment(payment);
        transaction1.setStatus("Success");

        transactions.add(transaction1);

        // Tạo giao dịch cho admin ( vnpay to he thong)
        Transaction transaction2 = new Transaction();
        User employee = userRepository.findByRole("admin"); // tim toi role nhan vien
        transaction2.setFrom(customer);
        transaction2.setTo(employee);
        transaction2.setPayment(payment);
        transaction2.setStatus("Success");
        transactions.add(transaction2);

        // an tien giao dich neu co


        // Thiết lập giao dịch trong payment
        payment.setTransactions(transactions);

        // Update order
        Order order1 = payment.getOrder();
        order.setStatus("paid");
        // Lưu payment trước
        paymentRepository.save(payment);

        // Không cần lưu giao dịch riêng biệt nếu đã sử dụng CascadeType.ALL
        // transactionRepository.saveAll(transactions); // Không cần thiết nếu CascadeType.ALL đã được sử dụng
    }
}
